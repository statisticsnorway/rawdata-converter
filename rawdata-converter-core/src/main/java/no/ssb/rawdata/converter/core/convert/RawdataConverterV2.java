package no.ssb.rawdata.converter.core.convert;

import no.ssb.rawdata.api.RawdataMessage;
import no.ssb.rawdata.api.RawdataMetadataClient;

import java.util.Collection;

public interface RawdataConverterV2 extends RawdataConverter {

    /**
     * Perform initialization and necessary preparations of the converter, such as calculating targetAvroSchema based on
     * metadata.
     *
     * @param metadataClient
     */
    void initialize(RawdataMetadataClient metadataClient);

    default void init(Collection<RawdataMessage> sampleRawdataMessages) {
        // do nothing, we expect call to initialize instead in V2.
    }

    void onError(Throwable t);

    void onComplete();
}
