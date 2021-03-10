package no.ssb.rawdata.converter.service.secret;

import io.micronaut.context.annotation.Property;
import io.micronaut.test.annotation.MockBean;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import no.ssb.rawdata.converter.core.convert.RawdataConverterFactory;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

@Disabled
@MicronautTest
@Property(name = "services.secrets.impl", value = "GCP")
@Property(name = "micronaut.config-client.enabled", value = "true")
@Property(name = "gcp.project-id", value = "dev-sirius")
@Property(name = "gcp.credentials.location", value = "../../localenv/private/gcp/sa-keys/dev-sirius-sa-key.json")
class GcpSecretServiceITest {

    // This secret is managed by GCP Secret Manager in the dev-sirius project
    private final static String SECRET_ID = "testsecret";

    @Inject SecretServiceConfig config;

    @Inject SecretService secretService;

    @MockBean(RawdataConverterFactory.class)
    RawdataConverterFactory rawdataConverterFactory() {
        return jobConfig -> null;
    }

    @Test
    void givenConfiguredImplPropertyIsGcp_whenSecretServiceIsInjected_thenShouldBeInstanceOfLocalSecretService() {
        assertThat(secretService instanceof GcpSecretService).isTrue();
    }

    @Test
    void givenSecretExists_whenGetSecret_thenShouldReturnSecretValue() {
        byte[] secret = secretService.getSecret(SECRET_ID);
        assertThat(secret).isNotEmpty();
    }

    @Test
    void givenOverriddenSecretExists_whenGetSecret_thenShouldReturnOverriddenSecretValue() {
        byte[] secretValue = "some-overridden-secret-value".getBytes(StandardCharsets.UTF_8);
        config.getOverrides().put(SECRET_ID, secretValue);
        byte[] secret = secretService.getSecret(SECRET_ID);
        assertThat(secret).isEqualTo(secretValue);
    }

}