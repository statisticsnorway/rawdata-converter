package no.ssb.rawdata.converter.service.secret;

public interface SecretService {

    /**
     * Get secret by id (latest version).
     */
    byte[] getSecret(String secretId);

    /**
     * Get secret by id and explicit version.
     */
    byte[] getSecret(String secretId, String version);

    /**
     * Get secret by id (latest version) - from cache if cached.
     */
    byte[] getCacheableSecret(String secretId);

    /**
     * Get secret by id and explicit version - from cache if cached.
     */
    byte[] getCacheableSecret(String secretId, String version);

}
