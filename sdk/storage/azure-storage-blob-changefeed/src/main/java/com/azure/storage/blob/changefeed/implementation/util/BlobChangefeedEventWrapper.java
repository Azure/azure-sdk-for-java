package com.azure.storage.blob.changefeed.implementation.util;

import com.azure.storage.blob.changefeed.models.BlobChangefeedEvent;

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
