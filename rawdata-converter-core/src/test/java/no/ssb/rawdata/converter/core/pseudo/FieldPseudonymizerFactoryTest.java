package no.ssb.rawdata.converter.core.pseudo;

import io.micronaut.context.annotation.Property;
import io.micronaut.test.annotation.MockBean;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import no.ssb.dlp.pseudo.core.PseudoSecret;
import no.ssb.rawdata.converter.core.convert.RawdataConverterFactory;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@MicronautTest(environments = "test-pseudo")
class FieldPseudonymizerFactoryTest {

    @Inject FieldPseudonymizerFactory fieldPseudonymizerFactory;

    @MockBean(RawdataConverterFactory.class)
    RawdataConverterFactory rawdataConverterFactory() {
        return jobConfig -> null;
    }

    @Property(name="pseudo.secrets")
    Map<String, PseudoSecret> configuredPseudoSecrets;

    @Test
    void givenEmptyConfiguredPseudoSecrets_whenResolvePseudoSecrets_thenShouldProduceEmptyListOfPseudoSecrets() {
        assertThat(fieldPseudonymizerFactory.resolvePseudoSecrets(Map.of())).isEqualTo(Collections.EMPTY_LIST);
        assertThat(fieldPseudonymizerFactory.resolvePseudoSecrets(null)).isEqualTo(Collections.EMPTY_LIST);
    }

    /**
     * References pseudo secrets configured in src/test/resources/application-test-pseudo.yml)
     *
     * pseudo.secrets:
     *   testsecret1:
     *     content: dGhlX3NlY3JldF9wYXNzd29yZF9pc19rZW5zZW50bWU=
     *     type: AES256
     *   testsecret2:
     *     id: pseudo-secret-testsecret2
     *     content: dGhlX3NlY3JldF9wYXNzd29yZF9pc19rZW5zZW50bWU=
     *   testsecret3:
     *     id: pseudo-secret-testsecret3
     *     type: AES256
     *   pseudo-secret-testsecret4:
     *     id: pseudo-secret-testsecret4
     *     version: 2
     *
     * services:
     *   secrets:
     *     impl: LOCAL
     *     overrides:
     *       pseudo-secret-testsecret3: supersecret3lAwuVx6NuAsMWLusOSA/ldia40ZugDI=
     *       pseudo-secret-testsecret4: supersecret4lAwuVx6NuAsMWLusOSA/ldia40ZugDI=
     *
     */
    @Test
    void givenConfiguredPseudoSecrets_whenResolvePseudoSecrets_thenShouldProduceListOfValidPseudoSecrets() {
        List<PseudoSecret> pseudoSecrets = fieldPseudonymizerFactory.resolvePseudoSecrets(configuredPseudoSecrets);

        PseudoSecret secret = pseudoSecrets.get(0);
        assertThat(secret.getName()).isEqualTo("testsecret1");
        assertThat(secret.getId()).isNull();
        assertThat(secret.getVersion()).isNull();
        assertThat(secret.getBase64EncodedContent()).isEqualTo("dGhlX3NlY3JldF9wYXNzd29yZF9pc19rZW5zZW50bWU=");
        assertThat(secret.getType()).isEqualTo("AES256");

        secret = pseudoSecrets.get(1);
        assertThat(secret.getName()).isEqualTo("testsecret2");
        assertThat(secret.getId()).isEqualTo("pseudo-secret-testsecret2");
        assertThat(secret.getVersion()).isNull();
        assertThat(secret.getBase64EncodedContent()).isEqualTo("dGhlX3NlY3JldF9wYXNzd29yZF9pc19rZW5zZW50bWU=");
        assertThat(secret.getType()).isEqualTo("AES256");

        secret = pseudoSecrets.get(2);
        assertThat(secret.getName()).isEqualTo("testsecret3");
        assertThat(secret.getId()).isEqualTo("pseudo-secret-testsecret3");
        assertThat(secret.getVersion()).isNull();
        assertThat(secret.getBase64EncodedContent()).isEqualTo("supersecret3lAwuVx6NuAsMWLusOSA/ldia40ZugDI=");
        assertThat(secret.getType()).isEqualTo("AES256");

        secret = pseudoSecrets.get(3);
        assertThat(secret.getName()).isEqualTo("pseudo-secret-testsecret4");
        assertThat(secret.getId()).isEqualTo("pseudo-secret-testsecret4");
        assertThat(secret.getVersion()).isEqualTo("2");
        assertThat(secret.getBase64EncodedContent()).isEqualTo("supersecret4lAwuVx6NuAsMWLusOSA/ldia40ZugDI=");
        assertThat(secret.getType()).isEqualTo("AES256");
    }

}
