// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.changefeed.implementation.models;

import com.azure.storage.blob.changefeed.models.BlobChangefeedEvent;

/**
 * FOR INTERNAL USE ONLY.
 * Represents a wrapper to store a BlobChangefeedEvent along with the BlobChangefeedCursor associated with it.
 * This wrapper is required since the paging functionality does not have the ability to determine where to
 * start to get the next page. If we store the cursor with each event we can divide the wrappers into pages,
 * unwrap each of the events and combine them with the cursor of the last event.
 */
public class BlobChangefeedEventWrapper {

    private final BlobChangefeedEvent event;
    private final ChangefeedCursor cursor;

    /**
     * Creates a new instance of a BlobChangefeedEventWrapper
     * @param event {@link BlobChangefeedEvent}
     * @param cursor {@link ChangefeedCursor}
     */
    public BlobChangefeedEventWrapper(BlobChangefeedEvent event, ChangefeedCursor cursor) {
        this.event = event;
        this.cursor = cursor;
    }

    /**
     * @return {@link BlobChangefeedEvent}
     */
    public BlobChangefeedEvent getEvent() {
        return event;
    }

    /**
     * @return {@link ChangefeedCursor}
     */
    public ChangefeedCursor getCursor() {
        return cursor;
    }
}
