package no.ssb.rawdata.converter.core.crypto;

import io.micronaut.context.annotation.Requires;
import io.micronaut.context.env.Environment;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Singleton;

@Singleton
@Slf4j
@Requires(env = Environment.TEST)
public class MockEncryptionCredentialsService implements EncryptionCredentialsService {

    @Override
    public char[] getKey(String keyId) {
        return getKey(keyId, "latest");
    }

    @Override
    public char[] getKey(String keyId, String version) {
        return "ThisIsASecretDummyKeyUsedToTestRawdataEncryption".toCharArray();
    }

}
