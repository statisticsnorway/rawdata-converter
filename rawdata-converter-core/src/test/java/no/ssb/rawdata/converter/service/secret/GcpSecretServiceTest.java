package no.ssb.rawdata.converter.service.secret;

import io.micronaut.context.annotation.Property;
import io.micronaut.gcp.secretmanager.client.SecretManagerClient;
import io.micronaut.test.annotation.MockBean;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import no.ssb.rawdata.converter.core.convert.RawdataConverterFactory;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import javax.inject.Inject;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

@MicronautTest
@Property(name = "services.secrets.impl", value = "GCP")
class GcpSecretServiceTest {

    @Inject SecretServiceConfig config;

    @Inject SecretService secretService;

    @MockBean(RawdataConverterFactory.class)
    RawdataConverterFactory rawdataConverterFactory() {
        return jobConfig -> null;
    }

    @MockBean(SecretManagerClient.class)
    SecretManagerClient secretManagerClient() {
        SecretManagerClient secretManagerClient = Mockito.mock(SecretManagerClient.class);
        return secretManagerClient;
    }

    @Test
    void givenConfiguredImplPropertyIsGcp_whenSecretServiceIsInjected_thenShouldBeInstanceOfLocalSecretService() {
        assertThat(secretService instanceof GcpSecretService).isTrue();
    }

    @Test
    void givenOverriddenSecretExists_whenGetSecret_thenShouldReturnOverriddenSecretValue() {
        byte[] secretValue = "kensentme".getBytes(StandardCharsets.UTF_8);
        config.getOverrides().put("some-overridden-secret-id", secretValue);
        byte[] secret = secretService.getSecret("some-overridden-secret-id");
        assertThat(secret).isEqualTo(secretValue);
    }

    @Test
    void givenSecretDoesNotExist_whenGetSecret_thenShouldThrowException() {
        assertThatExceptionOfType(GcpSecretService.SecretServiceException.class)
          .isThrownBy(() -> secretService.getSecret("secret-that-does-not-exist"));
    }

}