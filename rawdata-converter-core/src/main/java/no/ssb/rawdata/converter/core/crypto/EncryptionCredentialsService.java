package no.ssb.rawdata.converter.core.crypto;

public interface EncryptionCredentialsService {

    /**
     * Resolve rawdata encryption key
     * @param keyId id of rawdata encryption key to resolve
     * @return the rawdata encryption key for the specified id
     */
    char[] getKey(String keyId);

}
