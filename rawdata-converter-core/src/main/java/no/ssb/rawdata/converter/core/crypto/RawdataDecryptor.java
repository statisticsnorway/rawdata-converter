package no.ssb.rawdata.converter.core.crypto;

import lombok.extern.slf4j.Slf4j;
import no.ssb.rawdata.api.RawdataMessage;
import no.ssb.rawdata.payload.encryption.EncryptionClient;

import javax.annotation.Nullable;

@Slf4j
public class RawdataDecryptor {

    private static final String PROP_ENCRYPTION_KEY = "<job>.rawdata-source.encryption-key-id";
    private static final String PROP_ENCRYPTION_SALT = "<job>.rawdata-source.encryption-salt";

    private final EncryptionClient encryptionClient;
    private final boolean isEncryptionActive;
    private final byte[] rawdataStorageSecretKey;

    public RawdataDecryptor(
      @Nullable char[] encryptionKey,
      @Nullable byte[] encryptionSalt
    ) {
        this.encryptionClient = new EncryptionClient();
        isEncryptionActive = encryptionKey != null && encryptionSalt != null;

        if (isEncryptionActive) {
            rawdataStorageSecretKey = encryptionClient.generateSecretKey(encryptionKey, encryptionSalt).getEncoded();
            log.info("Rawdata decryptor initialized. Assuming that rawdata is encrypted.");
        }
        else {
            rawdataStorageSecretKey = null;
            log.warn("Rawdata decryptor is NOT configured. Assuming that rawdata is not encrypted. To enable, specify '" + PROP_ENCRYPTION_KEY + "' and '" + PROP_ENCRYPTION_SALT + "'");
        }
    }

    /**
     * Attempt to decrypt the RawdataMessage.data content
     *
     * @return Wrapped RawdataMessage through DecryptedRawdataMessageDelegate
     */
    public RawdataMessage tryDecrypt(RawdataMessage rawdataMessage) {
        return isEncryptionActive
          ? new DecryptedRawdataMessage(rawdataMessage, encryptionClient, rawdataStorageSecretKey)
          : rawdataMessage;
    }

}

