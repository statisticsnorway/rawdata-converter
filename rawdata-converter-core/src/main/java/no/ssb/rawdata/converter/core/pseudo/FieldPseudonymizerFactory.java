package no.ssb.rawdata.converter.core.pseudo;

import com.google.common.annotations.VisibleForTesting;
import io.micronaut.context.annotation.Property;
import lombok.extern.slf4j.Slf4j;
import no.ssb.dlp.pseudo.core.FieldPseudonymizer;
import no.ssb.dlp.pseudo.core.PseudoSecret;
import no.ssb.rawdata.converter.core.job.ConverterJobConfig;
import no.ssb.rawdata.converter.service.secret.SecretService;

import javax.inject.Singleton;
import java.util.Map;
import java.util.Set;
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
    private final Set<PseudoSecret> pseudoSecrets;

    /**
     * Initialize the FieldPseudonymizerFactory
     *
     * @param configuredPseudoSecrets pseudo secrets coming from the config or environment
     */
    public FieldPseudonymizerFactory(SecretService secretService, @Property(name="pseudo.secrets") Map<String, PseudoSecret> configuredPseudoSecrets) {
        this.secretService = secretService;
        this.pseudoSecrets = configuredPseudoSecrets.entrySet().stream()
          .map(e -> {
              PseudoSecret secret = e.getValue();

              // Resolve secret content if and only if ID is specified AND content is not specified
              if (secret.getId() != null && secret.getContent() == null) {
                  secret.setContent(secretService.getCacheableSecret(secret.getId()));
              }

              if (secret.getContent() == null) {
                  log.warn("Invalid pseudo secret '{}': Unable to resolve content", e.getKey());
              }

              secret.setId(e.getKey()); // Pseudo secrets use the mapping name as id

              if (secret.getType() == null) {
                  secret.setType(DEFAULT_PSEUDO_SECRET_TYPE);
              }

              return secret;
          })
          .collect(Collectors.toSet());
    }

    public FieldPseudonymizer newFieldPseudonymizer(ConverterJobConfig jobConfig) {
        return new FieldPseudonymizer.Builder()
          .rules(jobConfig.getPseudoRules())
          .secrets(pseudoSecrets)
          .build();
    }

    @VisibleForTesting
    Set<PseudoSecret> getPseudoSecrets() {
        return pseudoSecrets;
    }

}
