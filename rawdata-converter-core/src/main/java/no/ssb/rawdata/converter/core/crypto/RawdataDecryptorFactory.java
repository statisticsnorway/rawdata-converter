package no.ssb.rawdata.converter.core.crypto;

import lombok.RequiredArgsConstructor;
import no.ssb.rawdata.converter.core.exception.RawdataConverterException;
import no.ssb.rawdata.converter.core.job.ConverterJobConfig;

import javax.inject.Singleton;

@Singleton
@RequiredArgsConstructor
public class RawdataDecryptorFactory {

    /**
     * All rawdata-source configs as defined in application.yml (rawdata-sources)
     */
    private final EncryptionCredentialsService encryptionCredentialsService;

    public RawdataDecryptor rawdataDecryptorOf(ConverterJobConfig jobConfig) {
        return new RawdataDecryptor(rawdataEncryptionKeyOf(jobConfig.getRawdataSource()), rawdataEncryptionSaltOf(jobConfig.getRawdataSource()));
    }

    private char[] rawdataEncryptionKeyOf(ConverterJobConfig.RawdataSourceRef rawdataSource) {
        if (rawdataSource.getEncryptionKey() != null) {
            return rawdataSource.getEncryptionKey();
        }

        if (rawdataSource.getEncryptionKeyId() == null) {
            return null;
        }
        else {
            return encryptionCredentialsService.getKey(rawdataSource.getEncryptionKeyId());
        }
    }

    private boolean isEncryptionConfigured(ConverterJobConfig.RawdataSourceRef rawdataSource) {
        return rawdataSource.getEncryptionKeyId() != null || rawdataSource.getEncryptionKey() != null;
    }

    private byte[] rawdataEncryptionSaltOf(ConverterJobConfig.RawdataSourceRef rawdataSource) {
        if (isEncryptionConfigured(rawdataSource) && rawdataSource.getEncryptionSalt() == null) {
            throw new MissingRawdataEncryptionCredentialsException("No rawdata encryption salt configured for rawdata source " + rawdataSource.getName());
        }

        return rawdataSource.getEncryptionSalt();
    }

    class MissingRawdataEncryptionCredentialsException extends RawdataConverterException {
        public MissingRawdataEncryptionCredentialsException(String message) {
            super(message);
        }
    }

}
