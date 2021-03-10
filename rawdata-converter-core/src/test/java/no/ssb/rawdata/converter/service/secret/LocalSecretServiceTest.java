package no.ssb.rawdata.converter.service.secret;

import io.micronaut.context.annotation.Property;
import io.micronaut.test.annotation.MockBean;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import no.ssb.rawdata.converter.core.convert.RawdataConverterFactory;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

@MicronautTest
@Property(name = "services.secrets.impl", value = "LOCAL")
class LocalSecretServiceTest {

    @Inject SecretServiceConfig config;

    @Inject SecretService secretService;

    @Test
    void givenConfiguredImplPropertyIsLocal_whenSecretServiceIsInjected_thenShouldBeInstanceOfLocalSecretService() {
        assertThat(secretService instanceof LocalSecretService).isTrue();
    }

    @Test
    void givenSecretExists_whenGetSecret_thenShouldReturnSecretValue() {
        byte[] secretValue = "kensentme".getBytes(StandardCharsets.UTF_8);
        config.getOverrides().put("some-secret-id", secretValue);
        byte[] secret = secretService.getSecret("some-secret-id");
        assertThat(secret).isEqualTo(secretValue);
    }

    @Test
    void givenSecretDoesNotExist_whenGetSecret_thenShouldThrowException() {
        assertThatExceptionOfType(LocalSecretService.SecretNotFoundException.class)
          .isThrownBy(() -> secretService.getSecret("secret-that-does-not-exist"));
    }

    @MockBean(RawdataConverterFactory.class)
    RawdataConverterFactory rawdataConverterFactory() {
        return jobConfig -> null;
    }

}