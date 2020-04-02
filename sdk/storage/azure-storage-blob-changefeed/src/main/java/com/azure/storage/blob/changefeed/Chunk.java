package com.azure.storage.blob.changefeed;

import com.azure.core.util.FluxUtil;
import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.blob.BlobContainerAsyncClient;
import com.azure.storage.blob.changefeed.implementation.util.BlobChangefeedCursor;
import com.azure.storage.blob.changefeed.implementation.util.BlobChangefeedEventWrapper;
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

/**
 * Gets events for a chunk.
 * A chunk is an avro formatted append blob that contains change feed events.
 */
class Chunk {

    private static ClientLogger logger = new ClientLogger(Chunk.class);

    /* Changefeed container */
    private final BlobContainerAsyncClient client;

    /* Chunk event data location. */
    private final String chunkPath;

    /* Cursor associated with parent shard. */
    private final BlobChangefeedCursor shardCursor;

    private long eventNumber;

    private final BlobChangefeedCursor userCursor;


    Chunk(BlobContainerAsyncClient client, String chunkPath, BlobChangefeedCursor shardCursor,
        BlobChangefeedCursor userCursor) {
        this.client = client;
        this.chunkPath = chunkPath;
        this.shardCursor = shardCursor;
        this.eventNumber = 0;
        this.userCursor = userCursor;
    }

    Flux<BlobChangefeedEventWrapper> getEvents() {
        /* Download Avro data file. */
        /* TODO (gapra): Lazy download. */
        return client.getBlobAsyncClient(chunkPath)
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
                ArrayList<BlobChangefeedEventWrapper> events = new ArrayList<>();
                while (parsedStream.hasNext()) {
                    BlobChangefeedCursor eventCursor = shardCursor.toEventCursor(eventNumber);
                    GenericRecord r = parsedStream.next();
                    boolean collectEvents = false;
                    if (userCursor == null) {
                        collectEvents = true;
                    } else {
                        if (userCursor.isEventToBeProcessed() == null || !userCursor.isEventToBeProcessed()) {
                            if (userCursor.equals(eventCursor)) {
                                userCursor.setEventToBeProcessed(true);
                            }
                        } else {
                            collectEvents = true;
                        }
                    }
                    if (collectEvents) {
                        BlobChangefeedEventWrapper wrapper
                            = new BlobChangefeedEventWrapper(BlobChangefeedEvent.fromRecord(r),
                            eventCursor);
                        events.add(wrapper);
                    }
                    eventNumber++;
                }
                return Flux.fromIterable(events);
            });
    }
}
