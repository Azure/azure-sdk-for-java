// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.changefeed.implementation.models;

import com.azure.storage.blob.changefeed.models.BlobChangefeedEvent;

/**
 * FOR INTERNAL USE ONLY.
 * Represents a wrapper to store a BlobChangefeedEvent along with the BlobChangefeedCursor associated with it.
 * This wrapper is required since the paging functionality does not have any information about where the
 */
public class BlobChangefeedEventWrapper {

    private final BlobChangefeedEvent event;
    private final ChangefeedCursor cursor;

    public BlobChangefeedEventWrapper(BlobChangefeedEvent event, ChangefeedCursor cursor) {
        this.event = event;
        this.cursor = cursor;
    }

    public BlobChangefeedEvent getEvent() {
        return event;
    }

    public ChangefeedCursor getCursor() {
        return cursor;
    }
}
