package no.ssb.rawdata.converter.core.crypto;

import io.micronaut.gcp.secretmanager.client.SecretManagerClient;
import lombok.RequiredArgsConstructor;
import no.ssb.rawdata.converter.core.exception.RawdataConverterException;

import javax.inject.Singleton;
import java.nio.charset.StandardCharsets;

@RequiredArgsConstructor
@Singleton
public class SecretManagerEncryptionCredentialsService implements EncryptionCredentialsService {

    private final SecretManagerClient secretManager;

    @Override
    public char[] getKey(String keyId) {
        byte[] key = fetchSecret(keyId);
        return new String(key, StandardCharsets.UTF_8).toCharArray();
    }

    byte[] fetchSecret(String secretId) {
        try {
            return secretManager.getSecret(secretId).blockingGet().getContents();
        }
        catch (Exception e) {
            throw new SecretManagerEncryptionCredentialsServiceException("Error fetching secret with id " + secretId + " from secret manager", e);
        }
    }

    class SecretManagerEncryptionCredentialsServiceException extends RawdataConverterException {
        public SecretManagerEncryptionCredentialsServiceException(String message, Throwable cause) {
            super(message, cause);
        }
   }

}
