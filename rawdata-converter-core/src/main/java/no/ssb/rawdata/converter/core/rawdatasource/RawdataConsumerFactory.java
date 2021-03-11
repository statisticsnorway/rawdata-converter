package no.ssb.rawdata.converter.core.rawdatasource;

import de.huxhorn.sulky.ulid.ULID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.ssb.dapla.dataset.uri.DatasetUri;
import no.ssb.dapla.storage.client.DatasetStorage;
import no.ssb.dapla.storage.client.backend.FileInfo;
import no.ssb.rawdata.api.RawdataClient;
import no.ssb.rawdata.api.RawdataConsumer;
import no.ssb.rawdata.converter.core.exception.RawdataConverterException;
import no.ssb.rawdata.converter.core.job.ConverterJobConfig;
import no.ssb.rawdata.converter.core.storage.DatasetStorageFactory;
import no.ssb.rawdata.converter.core.storage.StorageType;
import no.ssb.rawdata.converter.core.storage.UlidVisitor;
import no.ssb.rawdata.converter.util.DatasetUriBuilder;

import javax.inject.Singleton;
import java.util.concurrent.atomic.AtomicReference;

import static net.logstash.logback.argument.StructuredArguments.keyValue;

@Singleton
@Slf4j
@RequiredArgsConstructor
public class RawdataConsumerFactory {

    private final RawdataClientFactory rawdataClientFactory;
    private final DatasetStorageFactory datasetStorageFactory;

    public RawdataConsumers rawdataConsumersOf(ConverterJobConfig jobConfig) {
        DatasetUri datasetUri = datasetUriOf(jobConfig.getTargetStorage());
        StorageType storageType = StorageType.of(datasetUri);
        DatasetStorage datasetStorage = datasetStorageFactory.datasetStorageOf(storageType, jobConfig.getTargetStorage().getSaKeyFile());
        String configuredInitialPosition = jobConfig.getRawdataSource().getInitialPosition();
        ULID.Value initialPositionUlid = resolveInitialPosition(configuredInitialPosition, datasetStorage, datasetUri);

        RawdataClient rawdataClient = rawdataClientFactory.rawdataClientOf(jobConfig.getRawdataSource().getName());
        AtomicReference<RawdataConsumer> mainRawdataConsumerRef = new AtomicReference<>();

        return RawdataConsumers.builder()
          .mainRawdataConsumer(() -> {
              RawdataConsumer consumer = mainRawdataConsumerRef.get();
              if (consumer == null) {
                  consumer = rawdataClient.consumer(jobConfig.getRawdataSource().getTopic(), initialPositionUlid, includeInitialPosition(configuredInitialPosition));
                  mainRawdataConsumerRef.set(consumer);
              }
              return consumer;
          })
          .sampleRawdataConsumer(() -> rawdataClient.consumer(jobConfig.getRawdataSource().getTopic()))
          .metadataClient(() -> rawdataClient.metadata(jobConfig.getRawdataSource().getTopic()))
          .build();
    }

    /**
     * </p>When consuming a rawdata stream by starting at a specific position, we can select to include the position, or start
     * at position+1. What we do depends on where we get the position ulid from.</p>
     *
     * <p>If the configured initial position is set to "LAST", then we should start at position+1. When using "LAST" we
     * read the actual position ulid from an already converted dataset. Thus, if we were to include this position, that
     * would result in duplicates.</p>
     *
     * <p>On the other hand, if we have configured the converter to explicitly start at a given ulid, then we of course
     * expect to start at this and not position+1 (which would mean that we would skip a record).</p>
     *
     * @return whether or not to include the initial position
     */
    boolean includeInitialPosition(String configuredInitialPosition) {
        return ! "LAST".equalsIgnoreCase(configuredInitialPosition);
    }

    private static DatasetUri datasetUriOf(ConverterJobConfig.TargetStorage storage) {
        return DatasetUriBuilder.of()
          .root(storage.getRoot())
          .path(storage.getPath())
          .version(storage.getVersion())
          .build();
    }

    /**
     * Attempt to resolve the position from which the rawdata stream should start.
     */
    private ULID.Value resolveInitialPosition(String initialPosition, DatasetStorage datasetStorage, DatasetUri datasetUri) {
        final ULID.Value position;
        try {
            if ("FIRST".equalsIgnoreCase(initialPosition)) {
                position = null;
            } else if ("LAST".equalsIgnoreCase(initialPosition)) {
                log.info("Determine initial starting position by searching for last record in {}", datasetUri);
                position = attemptToFindLastRecord(datasetStorage, datasetUri);
            } else {
                position = ULID.parseULID(initialPosition);
            }

        } catch (LastPositionNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new RawdataConverterException("Unable to determine initial rawdata converter starting position. Make sure that the rawdata.converter.initial-position param is either 'LAST', 'FIRST' or a valid ULID. Was: '" + initialPosition + "'", e);
        }
        if (position == null) {
            log.info("Rawdata conversion will start from the beginning of the rawdata stream");
        } else {
            log.info("Start rawdata conversion at {}", keyValue("position", position.toString()));
        }
        return position;
    }

    private ULID.Value attemptToFindLastRecord(DatasetStorage datasetStorage, DatasetUri datasetUri) {
        FileInfo lastModifiedDatasetFile = datasetStorage.getLastModifiedDatasetFile(datasetUri).orElse(null);
        if (lastModifiedDatasetFile == null) {
            return null;
        }

        try {
            UlidVisitor ulidVisitor = new UlidVisitor();
            datasetStorage.readParquetFile(datasetUri, lastModifiedDatasetFile.getName(), UlidVisitor.ULID_PROJECTION_SCHEMA, ulidVisitor);
            return ulidVisitor.getLatest();
        }
        catch (Exception e) {
            throw new LastPositionNotFoundException(datasetUri, e);
        }
    }

    public static class LastPositionNotFoundException extends RawdataConverterException {
        public LastPositionNotFoundException(DatasetUri datasetUri, Exception e) {
            super("Unable to determine rawdata converter starting position. Error searching for LAST position of rawdata uri '" + datasetUri + "'", e);
        }
    }

}
