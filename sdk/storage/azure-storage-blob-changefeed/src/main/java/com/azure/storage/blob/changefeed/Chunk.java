// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.changefeed;

import com.azure.storage.blob.BlobContainerAsyncClient;
import com.azure.storage.blob.changefeed.implementation.models.BlobChangefeedCursor;
import com.azure.storage.blob.changefeed.implementation.models.BlobChangefeedEventWrapper;
import com.azure.storage.blob.changefeed.models.BlobChangefeedEvent;
import com.azure.storage.common.implementation.Constants;
import com.azure.storage.internal.avro.implementation.AvroParser;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple3;

import java.nio.ByteBuffer;

/**
 * A class that represents a Chunk in Changefeed.
 *
 * A chunk is an append blob that contains avro encoded changefeed events.
 *
 * The log files in each chunkFilePath are guaranteed to contain mutually exclusive blobs.
 */
class Chunk {

    private final BlobContainerAsyncClient client; /* Changefeed container */
    private final String chunkPath; /* Chunk location. */
    private AvroParser parser; /* Avro parser for this chunk. */
    private final BlobChangefeedCursor shardCursor; /* Cursor associated with parent shard. */
    private long eventNumber; /* Keeps track of the event number to associate with each event in the chunk. */
    private final BlobChangefeedCursor userCursor; /* Cursor provided by user. */
    private boolean collectEvents; /* Whether or not to collect events in this chunk. */

    /**
     * Creates a new Chunk with the associated path and cursors.
     */
    Chunk(BlobContainerAsyncClient client, String chunkPath, BlobChangefeedCursor shardCursor,
        BlobChangefeedCursor userCursor) {
        this.client = client;
        this.chunkPath = chunkPath;
        this.shardCursor = shardCursor;
        this.eventNumber = 0;
        this.userCursor = userCursor;
        this.parser = new AvroParser();
        this.collectEvents = false;
    }

    Flux<BlobChangefeedEventWrapper> getEvents() {
        /* Download the chunk Avro File. */
        return downloadChunk()
            /* Parse the file using the Avro Parser. */
            .concatMap(this.parser::parse)
            .map(Tuple3::getT3)
            /* Map each object into an event. */
            .concatMap(this::parseRecord);
    }

    private Flux<ByteBuffer> downloadChunk() {

        return new BlobLazyDownloader(client.getBlobAsyncClient(chunkPath), Constants.MB, 0)
            .download();
    }

    /*
    * Flux<ByteBuffer> header = FluxUtil.readFile(fileChannel, 0, 5 * Constants.KB)
        Flux<ByteBuffer> body = FluxUtil.readFile(fileChannel, blockOffset, fileChannel.size())
        def complexVerifier = StepVerifier.create(
            header
                .concatMap({ buffer -> complexParser.parse(buffer) })
                .then(Mono.defer( { -> complexParser.prepareParserToReadBlock(blockOffset) } ))
                .thenMany(body.concatMap({buffer -> complexParser.parse(buffer)} )
                    .map({tuple3 -> tuple3.getT3()})
                )
                .map({o -> (String)((Map<String, Object>) o).get("subject")})
                .index()
                .map({ tuple2 -> Tuples.of(tuple2.getT1() + 1000 - numObjects, tuple2.getT2()) })
        )*/

    private Mono<BlobChangefeedEventWrapper> parseRecord(Object record) {
        BlobChangefeedCursor eventCursor = shardCursor.toEventCursor(eventNumber++);
        /* If no user cursor was provided, we want to return all events no matter what. */
        if (userCursor == null) {
            this.collectEvents = true;
        /* If a user cursor was provided, we need to determine the point at which we hit the cursor and start
           collecting the next sequential event. */
        } else {
            /* If the user cursor has its eventToBeProcessed flag set to false, determine if this is the event to
               start at. If so, mark the eventToBeProcessed flag to true. */
            if (userCursor.isEventToBeProcessed() == null || !userCursor.isEventToBeProcessed()) {
                if (userCursor.equals(eventCursor)) {
                    userCursor.setEventToBeProcessed(true);
                }
            /* If the user cursor has its eventToBeProcessed flag set to true, that means we have already hit the event
               that the user cursor represented, we should collect the event. */
            } else {
                this.collectEvents = true;
            }
        }
        if (this.collectEvents) {
            return Mono.just(
                new BlobChangefeedEventWrapper(BlobChangefeedEvent.fromRecord(record), eventCursor));
        } else {
            return Mono.empty();
        }
    }

}
