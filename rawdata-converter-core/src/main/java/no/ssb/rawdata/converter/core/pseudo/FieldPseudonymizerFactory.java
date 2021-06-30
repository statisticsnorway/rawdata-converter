package no.ssb.rawdata.converter.core.pseudo;

import io.micronaut.context.annotation.Property;
import lombok.extern.slf4j.Slf4j;
import no.ssb.dlp.pseudo.core.FieldPseudonymizer;
import no.ssb.dlp.pseudo.core.PseudoFuncRule;
import no.ssb.dlp.pseudo.core.PseudoSecret;
import no.ssb.rawdata.converter.core.exception.RawdataConverterException;
import no.ssb.rawdata.converter.core.job.ConverterJobConfig;
import no.ssb.rawdata.converter.service.secret.SecretService;

import javax.inject.Singleton;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * <p>Factory that constructs FieldPseudonymizers that performs (de/)pseudonymization using
 * pseudo secrets.</p>
 */
@Singleton
@Slf4j
public class FieldPseudonymizerFactory {

    private final static String DEFAULT_PSEUDO_SECRET_TYPE = "AES256";
    private final SecretService secretService;
    private final Map<String, PseudoSecret> configuredPseudoSecrets;

    /**
     * Initialize the FieldPseudonymizerFactory
     *
     * @param configuredPseudoSecrets pseudo secrets coming from the config or environment
     */
    public FieldPseudonymizerFactory(SecretService secretService, @Property(name="pseudo.secrets") Map<String, PseudoSecret> configuredPseudoSecrets) {
        this.secretService = secretService;
        this.configuredPseudoSecrets = Optional.ofNullable(configuredPseudoSecrets).orElse(Map.of());
    }

    public FieldPseudonymizer newFieldPseudonymizer(ConverterJobConfig jobConfig) {
        return newFieldPseudonymizer(jobConfig.getPseudoRules());
    }

    public FieldPseudonymizer newFieldPseudonymizer(List<PseudoFuncRule> pseudoRules) {
        return new FieldPseudonymizer.Builder()
          .rules(pseudoRules)
          .secrets(resolvePseudoSecrets((configuredPseudoSecrets)))
          .build();
    }


    /**
     * <p>Clean up configured pseudo secrets and resolve pseudo secret contents.</p>
     *
     * <p>If content is specified, then this is used. If not, attempt to resolve pseudo secret from SecretService using
     * the PseudoSecret::id and PseudoSecret::version properties.</p>
     *
     * @param configuredPseudoSecrets a Map named pseudo secrets
     * @return a List of cleaned up, resolved pseudo secrets
     */
    List<PseudoSecret> resolvePseudoSecrets(Map<String, PseudoSecret> configuredPseudoSecrets) {
        if (configuredPseudoSecrets == null) {
            return List.of();
        }

        return configuredPseudoSecrets.entrySet().stream()
          .map(e -> {
              PseudoSecret secret = e.getValue();

              secret.setName(e.getKey());

              // Resolve secret content if and only if 'id' is specified AND 'content' is not specified
              if (secret.getId() != null && secret.getContent() == null) {
                  secret.setBase64EncodedContent(secretService.getCacheableSecret(secret.getId(), secret.getVersion()));
              }

              if (secret.getContent() == null) {
                  throw new InvalidPseudoSecretException("Invalid pseudo secret '" + e.getKey() + "': Unable to resolve content");
              }

              if (secret.getType() == null) {
                  secret.setType(DEFAULT_PSEUDO_SECRET_TYPE);
              }

              return secret;
          })
          .collect(Collectors.toList());
    }

    class InvalidPseudoSecretException extends RawdataConverterException {
        public InvalidPseudoSecretException(String message) {
            super(message);
        }
    }

}
