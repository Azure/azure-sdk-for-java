// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.changefeed;

import com.azure.core.annotation.ServiceClient;

import java.time.OffsetDateTime;

/**
 * This class provides a client that contains all operations that apply to Azure Storage Blob changefeed.
 *
 * @see BlobChangefeedClientBuilder
 */
@ServiceClient(builder = BlobChangefeedClientBuilder.class)
public class BlobChangefeedClient {

    private final BlobChangefeedAsyncClient client;

    /**
     * Package-private constructor for use by {@link BlobChangefeedClientBuilder}.
     *
     * @param client {@link BlobChangefeedAsyncClient}.
     */
    BlobChangefeedClient(BlobChangefeedAsyncClient client) {
        this.client = client;
    }

    public BlobChangefeedPagedIterable getEvents() {
        return getEvents(null, null);
    }

    public BlobChangefeedPagedIterable getEvents(OffsetDateTime startTime, OffsetDateTime endTime) {
        return new BlobChangefeedPagedIterable(client.getEvents(startTime, endTime));
    }

    public BlobChangefeedPagedIterable getEvents(String cursor) {
        return new BlobChangefeedPagedIterable(client.getEvents(cursor));
    }

}
