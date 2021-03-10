package no.ssb.rawdata.converter.service.dapla.oauth;

import com.google.common.primitives.Bytes;
import lombok.NonNull;
import lombok.Value;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Value
public class ClientCredentials {

    @NonNull
    private byte[] clientId;

    @NonNull
    private byte[] clientSecret;

    public String asBase64Encoded() {
        return Base64.getEncoder().encodeToString(Bytes.concat(
          clientId,
          ":".getBytes(StandardCharsets.UTF_8),
          clientSecret
        ));
    }

}
