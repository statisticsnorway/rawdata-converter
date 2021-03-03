package no.ssb.rawdata.converter.core.pseudo;

import io.micronaut.context.annotation.Property;
import no.ssb.dlp.pseudo.core.FieldPseudonymizer;
import no.ssb.dlp.pseudo.core.PseudoSecret;
import no.ssb.rawdata.converter.core.job.ConverterJobConfig;

import javax.inject.Singleton;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * <p>Factory that constructs FieldPseudonymizers that performs (de/)pseudonymization using
 * pseudo secrets.</p>
 *
 * <p>Pseudo secrets are combined from multiple configuration sources:
 * <ul>
 *     <li>either in `application.yml` or as environment variables on the format: `PSEUDO_SECRETS_[NAME]_CONTENT`</li>
 *     <li>as GCP Secret Manager keys, named on the format `PSEUDO_SECRETS_[NAME]_CONTENT`.</li>
 * </ul>
 * In case of key name collisions, then config defined in `pseudo.secrets` will take precedence.
 * </p>
 *
 * <p>Pseudo secrets managed by GCP Secret Manager must be defined in `bootstrap.yml` in order to be preloaded upon
 * application startup</p>
 *
 * <p>Note that the "magic" behind how PseudoSecrets are constructed from property values can be seen here
 * {@link no.ssb.dlp.pseudo.core.typeconverter.PseudoSecretTypeConverter}.</p>
 */
@Singleton
public class FieldPseudonymizerFactory {

    private final Set<PseudoSecret> pseudoSecrets;

    /**
     * Initialize the FieldPseudonymizerFactory
     *
     * @param configuredPseudoSecrets pseudo secrets coming from the config or environment
     * @param secretManagerPseudoSecrets pseudo secrets managed by Secret Manager
     */
    public FieldPseudonymizerFactory(@Property(name="pseudo.secrets") Map<String, PseudoSecret> configuredPseudoSecrets,
                                     @Property(name="sm.pseudo.secret") Map<String, PseudoSecret> secretManagerPseudoSecrets) {

        Map<String, PseudoSecret> combinedPseudoSecretsMap = Stream.concat(secretManagerPseudoSecrets.entrySet().stream(), configuredPseudoSecrets.entrySet().stream())
          .collect(Collectors.toMap(
            Map.Entry::getKey,
            Map.Entry::getValue,
            (v1, v2) -> v2)); // merge function (keep the first)

        pseudoSecrets = combinedPseudoSecretsMap.entrySet().stream()
          .map(e -> {
              PseudoSecret secret = e.getValue();
              secret.setId(e.getKey());
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

}
