package no.ssb.rawdata.converter.service.secret;

import io.micronaut.context.annotation.Requires;
import lombok.RequiredArgsConstructor;
import no.ssb.rawdata.converter.core.exception.RawdataConverterException;

import javax.inject.Singleton;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Base64;
import java.util.Properties;

import static java.util.Optional.ofNullable;

@RequiredArgsConstructor
@Singleton
@Requires(property = SecretServiceConfig.PREFIX + ".impl", value = "PROPFILE")
@Requires(property = SecretServiceConfig.PREFIX + ".file")
public class PropertyFileBasedSecretService implements SecretService {

    private final SecretServiceConfig config;

    @Override
    public byte[] getSecret(String secretId) {
        Properties properties = new Properties();
        try (InputStream inStream = Files.newInputStream(Path.of(config.getFile()), StandardOpenOption.READ)) {
            properties.load(inStream);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        byte[] bytes = ofNullable(properties.getProperty(secretId))
                .map(encoded -> Base64.getDecoder().decode(encoded))
                .orElseThrow(() -> new SecretNotFoundException("No local secret with id " + secretId + " found. Make sure to initialize " + SecretServiceConfig.PREFIX + ".overrides"));
        return bytes;
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
