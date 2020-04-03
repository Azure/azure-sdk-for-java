package com.azure.storage.blob.changefeed.implementation.util;

import com.azure.storage.blob.changefeed.models.BlobChangefeedEvent;

/**
 * FOR INTERNAL USE ONLY.
 * Represents a wrapper to store a BlobChangefeedEvent along with the BlobChangefeedCursor associated with it.
 * This wrapper is required since the paging functionality does not have any information about where the
 */
public class BlobChangefeedEventWrapper {

    private final BlobChangefeedEvent event;
    private final BlobChangefeedCursor cursor;

    public BlobChangefeedEventWrapper(BlobChangefeedEvent event, BlobChangefeedCursor cursor) {
        this.event = event;
        this.cursor = cursor;
    }

    public BlobChangefeedEvent getEvent() {
        return event;
    }

    public BlobChangefeedCursor getCursor() {
        return cursor;
    }
}
