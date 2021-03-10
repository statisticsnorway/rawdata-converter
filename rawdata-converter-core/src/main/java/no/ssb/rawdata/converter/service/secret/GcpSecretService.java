package no.ssb.rawdata.converter.service.secret;

import io.micronaut.cache.annotation.CacheConfig;
import io.micronaut.cache.annotation.Cacheable;
import io.micronaut.context.annotation.Requires;
import io.micronaut.gcp.condition.RequiresGoogleProjectId;
import io.micronaut.gcp.secretmanager.client.SecretManagerClient;
import io.micronaut.gcp.secretmanager.client.VersionedSecret;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import no.ssb.rawdata.converter.core.exception.RawdataConverterException;

import javax.inject.Singleton;

@RequiredArgsConstructor
@Singleton
@Requires(property = SecretServiceConfig.PREFIX + ".impl", value = "GCP")
@RequiresGoogleProjectId
@CacheConfig(cacheNames = {"secrets"})
public class GcpSecretService implements SecretService {

    private final static String LATEST_VERSION = "latest";

    @NonNull
    private final SecretManagerClient secretManager;

    @NonNull
    private final SecretServiceConfig config;

    public byte[] getCacheableSecret(String secretId) {
        return getCacheableSecret(secretId, LATEST_VERSION);
    }

    @Cacheable
    public byte[] getCacheableSecret(String secretId, String version) {
        return getSecret(secretId, version);
    }

    public byte[] getSecret(String secretId) {
        return getSecret(secretId, LATEST_VERSION);
    }

    public byte[] getSecret(String secretId, String version) {
        return fetchSecret(secretId, version);
    }

    byte[] fetchSecret(String secretId, String version) {
        // Get key from overrides map if it is defined
        if (config.getOverrides().containsKey(secretId)) {
            return config.getOverrides().get(secretId);
        }

        // ...else get it from GCP
        final VersionedSecret secret;
        try {
            secret = secretManager.getSecret(secretId, version).blockingGet();
        }
        catch (Exception e) {
            throw new SecretServiceException("Error fetching secret with id '" + secretId + "' and version=" + version + " from secret manager", e);
        }

        if (secret == null) {
            throw new SecretServiceException("Secret with id '" + secretId + "' and version='" + version + "' was not found. Make sure the provided GCP credentials have access to Secret Manager secrets (requires at least the secretmanager.secretAccessor role)");
        }

        return secret.getContents();
    }

    class SecretServiceException extends RawdataConverterException {
        public SecretServiceException(String message) {
            super(message);
        }

        public SecretServiceException(String message, Throwable cause) {
            super(message, cause);
        }
    }


}
