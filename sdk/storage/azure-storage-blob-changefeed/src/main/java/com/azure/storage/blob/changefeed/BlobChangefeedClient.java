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

    /**
     * Returns a lazy loaded list of changefeed events for this account. The returned {@link
     * BlobChangefeedPagedIterable} can be consumed through while new items are automatically retrieved as needed.
     *
     * <p>
     * Changefeed events are returned in approximate temporal order.
     *
     * <p>
     * For more information, see the
     * <a href="https://docs.microsoft.com/en-us/azure/storage/blobs/storage-blob-change-feed?tabs=azure-portal">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.changfeed.BlobChangefeedClient.getEvents}
     *
     * @return The changefeed events.
     */
    public BlobChangefeedPagedIterable getEvents() {
        return getEvents(null, null);
    }

    /**
     * Returns a lazy loaded list of changefeed events for this account. The returned {@link
     * BlobChangefeedPagedIterable} can be consumed through while new items are automatically retrieved as needed.
     *
     * <p>
     * Changefeed events are returned in approximate temporal order.
     *
     * <p>
     * For more information, see the
     * <a href="https://docs.microsoft.com/en-us/azure/storage/blobs/storage-blob-change-feed?tabs=azure-portal">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.changfeed.BlobChangefeedClient.getEvents}
     *
     * @param startTime Filters the results to return events after the start time.
     * @param endTime Filters the results to return events before the end time.
     * @return The changefeed events.
     */
    public BlobChangefeedPagedIterable getEvents(OffsetDateTime startTime, OffsetDateTime endTime) {
        return new BlobChangefeedPagedIterable(client.getEvents(startTime, endTime));
    }

    /**
     * Returns a lazy loaded list of changefeed events for this account. The returned {@link
     * BlobChangefeedPagedIterable} can be consumed through while new items are automatically retrieved as needed.
     *
     * <p>
     * Changefeed events are returned in approximate temporal order.
     *
     * <p>
     * For more information, see the
     * <a href="https://docs.microsoft.com/en-us/azure/storage/blobs/storage-blob-change-feed?tabs=azure-portal">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.changfeed.BlobChangefeedClient.getEvents}
     *
     * @param cursor Identifies the portion of the events to be returned with the next get operation.
     * @return The changefeed events.
     */
    public BlobChangefeedPagedIterable getEvents(String cursor) {
        return new BlobChangefeedPagedIterable(client.getEvents(cursor));
    }
}
