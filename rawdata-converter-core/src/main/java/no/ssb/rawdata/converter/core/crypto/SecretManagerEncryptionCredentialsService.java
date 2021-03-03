package no.ssb.rawdata.converter.core.crypto;

import io.micronaut.context.annotation.Primary;
import io.micronaut.context.annotation.Requires;
import io.micronaut.gcp.secretmanager.client.SecretManagerClient;
import lombok.RequiredArgsConstructor;
import no.ssb.rawdata.converter.core.exception.RawdataConverterException;

import javax.inject.Singleton;
import java.nio.charset.StandardCharsets;

@RequiredArgsConstructor
@Singleton
@Primary
@Requires(property = "gcp.project-id")
public class SecretManagerEncryptionCredentialsService implements EncryptionCredentialsService {

    private final SecretManagerClient secretManager;

    @Override
    public char[] getKey(String keyId) {
        return getKey(keyId, "latest");
    }

    @Override
    public char[] getKey(String keyId, String version) {
        byte[] key = fetchSecret(keyId, version);
        return new String(key, StandardCharsets.UTF_8).toCharArray();
    }

    byte[] fetchSecret(String secretId, String version) {
        try {
            return secretManager.getSecret(secretId, version).blockingGet().getContents();
        } catch (Exception e) {
            throw new SecretManagerEncryptionCredentialsServiceException("Error fetching secret with id '" + secretId + "' and version=" + version + " from secret manager", e);
        }
    }

    class SecretManagerEncryptionCredentialsServiceException extends RawdataConverterException {
        public SecretManagerEncryptionCredentialsServiceException(String message, Throwable cause) {
            super(message, cause);
        }
    }

}
