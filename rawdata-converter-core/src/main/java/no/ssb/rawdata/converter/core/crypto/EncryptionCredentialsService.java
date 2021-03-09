package no.ssb.rawdata.converter.core.crypto;

import lombok.RequiredArgsConstructor;
import no.ssb.rawdata.converter.service.secret.SecretService;

import javax.inject.Singleton;
import java.nio.charset.StandardCharsets;

@Singleton
@RequiredArgsConstructor
public class EncryptionCredentialsService {

    private final SecretService secretService;

    /**
     * Resolve latest version of rawdata encryption key
     * @param keyId id of rawdata encryption key to resolve
     * @return the rawdata encryption key for the specified id (latest version)
     */
    char[] getKey(String keyId) {
        return new String(secretService.getSecret(keyId), StandardCharsets.UTF_8).toCharArray();
    }

    /**
     * Resolve a specific version of a rawdata encryption key
     * @param keyId id of rawdata encryption key to resolve
     * @param version version of rawdata encryption key to resolve
     * @return the rawdata encryption key for the specified id and version
     */
    char[] getKey(String keyId, String version) {
        return new String(secretService.getSecret(keyId, version), StandardCharsets.UTF_8).toCharArray();
    }

}
