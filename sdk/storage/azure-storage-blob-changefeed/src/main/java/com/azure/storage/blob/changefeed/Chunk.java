// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.changefeed;

import com.azure.storage.blob.changefeed.implementation.models.ChangefeedCursor;
import com.azure.storage.blob.changefeed.implementation.models.BlobChangefeedEventWrapper;
import com.azure.storage.blob.changefeed.implementation.models.InternalBlobChangefeedEvent;
import com.azure.storage.blob.changefeed.models.BlobChangefeedEvent;
import com.azure.storage.common.implementation.StorageImplUtils;
import com.azure.storage.internal.avro.implementation.AvroReader;
import reactor.core.publisher.Flux;

/**
 * A class that represents a Chunk in Changefeed.
 *
 * A chunk is an append blob that contains avro encoded changefeed events.
 */
class Chunk {

    private final String chunkPath;
    private final ChangefeedCursor changefeedCursor; /* Cursor associated with parent shard. */
    private final AvroReader avroReader; /* AvroReader to read objects off of. */

    /**
     * Creates a new Chunk.
     */
    Chunk(String chunkPath, ChangefeedCursor changefeedCursor, AvroReader avroReader) {
        StorageImplUtils.assertNotNull("chunkPath", chunkPath);
        StorageImplUtils.assertNotNull("changefeedCursor", changefeedCursor);
        StorageImplUtils.assertNotNull("avroReader", avroReader);
        this.chunkPath = chunkPath;
        this.changefeedCursor = changefeedCursor;
        this.avroReader = avroReader;
    }

    /**
     * Get events for the Chunk.
     * @return A reactive stream of {@link BlobChangefeedEventWrapper}
     */
    Flux<BlobChangefeedEventWrapper> getEvents() {
            /* Read avro objects. The AvroReader will only return relevant objects. */
        return avroReader.read()
            /* Convert AvroObjects into BlobChangefeedEventWrappers. */
            .map(avroObject -> {
                /* Unwrap AvroObject. */
                long blockOffset = avroObject.getNextBlockOffset();
                long eventIndex = avroObject.getNextObjectIndex();
                Object object = avroObject.getObject();

                /* Get the event cursor associated with this event. */
                ChangefeedCursor eventCursor = changefeedCursor.toEventCursor(chunkPath, blockOffset, eventIndex);

                BlobChangefeedEvent event = InternalBlobChangefeedEvent.fromRecord(object);

                /* Wrap the event and cursor. */
                return new BlobChangefeedEventWrapper(event, eventCursor);
            });
    }
}
