package no.ssb.rawdata.converter.service.secret;

import io.micronaut.context.annotation.Requires;
import lombok.RequiredArgsConstructor;
import no.ssb.rawdata.converter.core.exception.RawdataConverterException;

import javax.inject.Singleton;
import java.util.Optional;

@RequiredArgsConstructor
@Singleton
@Requires(property = SecretServiceConfig.PREFIX + ".impl", value = "LOCAL")
public class LocalSecretService implements SecretService {

    private final SecretServiceConfig config;

    @Override
    public byte[] getSecret(String secretId) {
        return Optional.ofNullable(config.getOverrides().get(secretId))
          .orElseThrow(() -> new SecretNotFoundException("No local secret with id " + secretId + " found. Make sure to initialize " + SecretServiceConfig.PREFIX + ".overrides"));
    }

    @Override
    public byte[] getSecret(String secretId, String version) {
        return getSecret(secretId);
    }

    @Override
    public byte[] getCacheableSecret(String secretId) {
        return getSecret(secretId);
    }

    @Override
    public byte[] getCacheableSecret(String secretId, String version) {
        return getSecret(secretId);
    }

    class SecretNotFoundException extends RawdataConverterException {
        public SecretNotFoundException(String message) {
            super(message);
        }
    }

}
