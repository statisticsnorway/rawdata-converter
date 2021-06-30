package no.ssb.rawdata.converter.core.crypto;

import lombok.RequiredArgsConstructor;
import no.ssb.dapla.ingest.rawdata.metadata.RawdataStructure;
import no.ssb.rawdata.api.RawdataMetadataClient;
import no.ssb.rawdata.converter.core.exception.RawdataConverterException;
import no.ssb.rawdata.converter.core.job.ConverterJobConfig;
import no.ssb.rawdata.payload.encryption.Algorithm;

import javax.inject.Singleton;
import java.util.Map;
import java.util.Optional;

import static java.util.Optional.ofNullable;

@Singleton
@RequiredArgsConstructor
public class RawdataDecryptorFactory {

    /**
     * All rawdata-source configs as defined in application.yml (rawdata-sources)
     */
    private final EncryptionCredentialsService encryptionCredentialsService;

    public RawdataDecryptor rawdataDecryptorOf(RawdataMetadataClient metadataClient, ConverterJobConfig jobConfig) {
        RawdataStructure rawdataStructure = tryResolveRawdataStructure(metadataClient).orElse(null);

        return new RawdataDecryptor(
                rawdataStructure != null ? rawdataEncryptionKeyOfRawdataStructure(rawdataStructure) : rawdataEncryptionKeyOf(jobConfig.getRawdataSource()),
                rawdataStructure != null ? rawdataEncryptionSaltOfRawdataStructure(rawdataStructure) : rawdataEncryptionSaltOf(jobConfig.getRawdataSource()),
                rawdataStructure != null ? rawdataEncryptionAlgorithmOfRawdataStructure(rawdataStructure) : Algorithm.AES128,
                jobConfig.getRawdataSource().getNonEncryptedItems()
        );
    }

    private Optional<RawdataStructure> tryResolveRawdataStructure(RawdataMetadataClient metadataClient) {
        try {
            return ofNullable(metadataClient.get("structure.json"))
              .map(RawdataStructure::of)
              .map(RawdataStructure.Builder::build);
        }
        catch (Exception e) {
            // ok to swallow - could be "StorageException: 404 Not Found" if structure.json metadata was not found
            return Optional.empty();
        }
    }

    private char[] rawdataEncryptionKeyOfRawdataStructure(RawdataStructure rawdataStructure) {
        String keyId = null;
        String version = null;
        for (Map.Entry<String, RawdataStructure.Document> entry : rawdataStructure.documents().entrySet()) {
            RawdataStructure.Document.Encryption encryption = entry.getValue().encryption();
            if (encryption != null) {
                keyId = encryption.key();
                version = encryption.version();
                break;
            }
        }
        if (keyId == null) {
            return null;
        }
        if (version == null || version.isEmpty()) {
            return encryptionCredentialsService.getKey(keyId);
        } else {
            return encryptionCredentialsService.getKey(keyId, version);
        }
    }

    private char[] rawdataEncryptionKeyOf(ConverterJobConfig.RawdataSourceRef rawdataSource) {
        if (rawdataSource.getEncryptionKey() != null) {
            return rawdataSource.getEncryptionKey();
        }

        if (rawdataSource.getEncryptionKeyId() == null) {
            return null;
        }
        else {
            String version = rawdataSource.getEncryptionKeyVersion();
            if (version == null || version.isEmpty()) {
                return encryptionCredentialsService.getKey(rawdataSource.getEncryptionKeyId());
            }
            else {
                return encryptionCredentialsService.getKey(rawdataSource.getEncryptionKeyId(), version);
            }
        }
    }

    private boolean isEncryptionConfigured(ConverterJobConfig.RawdataSourceRef rawdataSource) {
        return rawdataSource.getEncryptionKeyId() != null || rawdataSource.getEncryptionKey() != null;
    }

    private Algorithm rawdataEncryptionAlgorithmOfRawdataStructure(RawdataStructure rawdataStructure) {
        for (Map.Entry<String, RawdataStructure.Document> entry : rawdataStructure.documents().entrySet()) {
            RawdataStructure.Document.Encryption encryption = entry.getValue().encryption();
            if (encryption != null) {
                return encryption.algorithm();
            }
        }
        throw new MissingRawdataEncryptionCredentialsException("No rawdata encryption algorithm configured in metadata for topic.");
    }

    private byte[] rawdataEncryptionSaltOfRawdataStructure(RawdataStructure rawdataStructure) {
        for (Map.Entry<String, RawdataStructure.Document> entry : rawdataStructure.documents().entrySet()) {
            RawdataStructure.Document.Encryption encryption = entry.getValue().encryption();
            if (encryption != null) {
                return encryption.salt();
            }
        }
        throw new MissingRawdataEncryptionCredentialsException("No rawdata encryption salt configured in metadata for topic.");
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
