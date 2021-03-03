package no.ssb.rawdata.converter.core.crypto;

public interface EncryptionCredentialsService {

    /**
     * Resolve latest version of rawdata encryption key
     * @param keyId id of rawdata encryption key to resolve
     * @return the rawdata encryption key for the specified id (latest version)
     */
    char[] getKey(String keyId);

    /**
     * Resolve a specific version of a rawdata encryption key
     * @param keyId id of rawdata encryption key to resolve
     * @param version version of rawdata encryption key to resolve
     * @return the rawdata encryption key for the specified id and version
     */
    char[] getKey(String keyId, String version);

}
