// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.changefeed;

import com.azure.core.util.IterableStream;
import com.azure.core.util.paging.ContinuablePage;
import com.azure.storage.blob.changefeed.implementation.models.ChangefeedCursor;
import com.azure.storage.blob.changefeed.models.BlobChangefeedEvent;

import java.util.List;

/**
 * Represents a page returned in BlobChangefeed.
 *
 * <p>A {@link BlobChangefeedPagedResponse} consists of {@link BlobChangefeedEvent} elements and {@link String}
 * continuation token (known as a cursor in Changefeed). </p>
 *
 * <p>A cursor can be used to re-initialize a BlobChangefeed to point to the next expected page. </p>
 *
 * @see BlobChangefeedPagedFlux
 * @see BlobChangefeedPagedIterable
 */
public class BlobChangefeedPagedResponse implements ContinuablePage<String, BlobChangefeedEvent> {

    private final List<BlobChangefeedEvent> events;
    private final ChangefeedCursor cursor;

    /**
     * Package-private constructor for use by {@link BlobChangefeedPagedFlux}
     * @param events A {@link List} of {@link BlobChangefeedEvent BlobChangefeedEvents}.
     * @param cursor A {@link ChangefeedCursor cursor}.
     */
    BlobChangefeedPagedResponse(List<BlobChangefeedEvent> events, ChangefeedCursor cursor) {
        this.events = events;
        this.cursor = cursor;
    }

    /**
     * {@inheritDoc}
     */
    public IterableStream<BlobChangefeedEvent> getElements() {
        return new IterableStream<>(this.events);
    }

    /**
     * Gets a {@link List} of elements in the page.
     *
     * @return A {@link List} containing the elements in the page.
     */
    public List<BlobChangefeedEvent> getValue() {
        return this.events;
    }

    public String getContinuationToken() {
        throw new UnsupportedOperationException("continuationToken not supported. Use "
            + "client.getEvents(String) to pass in a cursor.");
    }

    /**
     * Gets a reference to the next page, should you want to re-initialize the BlobChangefeed.
     *
     * @return The {@link String cursor} that references the next page.
     */
    public String getCursor() {
        /* Serialize the cursor and return it to the user as a String. */
        return cursor.serialize();
    }
}
