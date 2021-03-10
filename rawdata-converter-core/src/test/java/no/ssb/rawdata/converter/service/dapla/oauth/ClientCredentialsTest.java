package no.ssb.rawdata.converter.service.dapla.oauth;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class ClientCredentialsTest {

    private byte[] clientId = "someClientId".getBytes(StandardCharsets.UTF_8);
    private byte[] clientSecret = "someClientSecret".getBytes(StandardCharsets.UTF_8);

    @Test
    void givenClientCredentials_asBase64Encoded_shouldCreateProperBase64Representation() {
        ClientCredentials cred = new ClientCredentials(clientId, clientSecret);
        String decoded = new String(Base64.getDecoder().decode(cred.asBase64Encoded()), StandardCharsets.UTF_8);
        assertThat(decoded).isEqualTo("someClientId:someClientSecret");
    }

    @Test
    void givenInvalidClientCredentials_whenConstructing_thenShouldThrowException() {
        assertThatExceptionOfType(NullPointerException.class)
          .isThrownBy(() -> new ClientCredentials(null, null))
          .withMessageContaining("clientId is marked non-null but is null");

        assertThatExceptionOfType(NullPointerException.class)
          .isThrownBy(() -> new ClientCredentials(null, clientSecret))
          .withMessageContaining("clientId is marked non-null but is null");

        assertThatExceptionOfType(NullPointerException.class)
          .isThrownBy(() -> new ClientCredentials(clientId, null))
          .withMessageContaining("clientSecret is marked non-null but is null");
    }

}