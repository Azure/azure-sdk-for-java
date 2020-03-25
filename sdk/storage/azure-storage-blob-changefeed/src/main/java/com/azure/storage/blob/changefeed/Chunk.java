package com.azure.storage.blob.changefeed;

import com.azure.core.util.FluxUtil;
import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.blob.BlobContainerAsyncClient;
import com.azure.storage.blob.changefeed.models.BlobChangefeedEvent;
import org.apache.avro.file.DataFileStream;
import org.apache.avro.generic.GenericDatumReader;
import org.apache.avro.generic.GenericRecord;
import reactor.core.publisher.Flux;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;


class Chunk {

    private static ClientLogger logger = new ClientLogger(Chunk.class);

    /* Changefeed container */
    private final BlobContainerAsyncClient client;

    /* Chunk event data location. */
    private final String path;

    Chunk(BlobContainerAsyncClient client, String path) {
        this.client = client;
        this.path = path;
    }

    Flux<BlobChangefeedEvent> getEvents() {
        /* Download Avro data file. */
        return client.getBlobAsyncClient(path)
            .download().reduce(new ByteArrayOutputStream(), (os, buffer) -> {
                try {
                    os.write(FluxUtil.byteBufferToArray(buffer));
                } catch (IOException e) {
                    throw logger.logExceptionAsError(new UncheckedIOException(e));
                }
                return os;
            })
            .map(os -> new ByteArrayInputStream(os.toByteArray()))
            /* Parse Avro for events. */
            .flatMapMany(avro -> {
                DataFileStream<GenericRecord> parsedStream = null;
                try {
                   parsedStream = new DataFileStream<>(avro, new GenericDatumReader<>());
                } catch (IOException e) {
                    throw logger.logExceptionAsError(new UncheckedIOException(e));
                }
                ArrayList<BlobChangefeedEvent> events = new ArrayList<>();
                while(parsedStream.hasNext()) {
                    GenericRecord r = parsedStream.next();
                    events.add(BlobChangefeedEvent.fromRecord(r));
                }
                return Flux.fromIterable(events);
            });
    }
}
