package no.ssb.rawdata.converter.service.secret;

import io.micronaut.test.annotation.MockBean;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import no.ssb.rawdata.converter.core.convert.RawdataConverterFactory;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;

import static org.assertj.core.api.Assertions.assertThat;

@MicronautTest
class MockSecretServiceTest {

    @Inject SecretService secretService;

    @MockBean(RawdataConverterFactory.class)
    RawdataConverterFactory rawdataConverterFactory() {
        return jobConfig -> null;
    }

    @Test
    void givenNoConfiguredImplProperty_whenSecretServiceIsInjected_thenShouldBeInstanceOfMockSecretService() {
        assertThat(secretService instanceof MockSecretService).isTrue();
    }

    @Test
    void givenAnySecretId_whenGetSecret_thenShouldReturnMockedValue() {
        assertThat(secretService.getSecret("any-secret-id")).isEqualTo(MockSecretService.MOCKED_VALUE);
        assertThat(secretService.getSecret("")).isEqualTo(MockSecretService.MOCKED_VALUE);
        assertThat(secretService.getSecret(null)).isEqualTo(MockSecretService.MOCKED_VALUE);
    }

}