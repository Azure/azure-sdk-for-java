// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.changefeed;

import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.util.Context;

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
     * <a href="https://docs.microsoft.com/azure/storage/blobs/storage-blob-change-feed?tabs=azure-portal">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.changefeed.BlobChangefeedClient.getEvents}
     *
     * @return The changefeed events.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public BlobChangefeedPagedIterable getEvents() {
        return getEvents((OffsetDateTime) null, null);
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
     * <a href="https://docs.microsoft.com/azure/storage/blobs/storage-blob-change-feed?tabs=azure-portal">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.changefeed.BlobChangefeedClient.getEvents#OffsetDateTime-OffsetDateTime}
     *
     * @param startTime Filters the results to return events approximately after the start time. Note: A few events
     * belonging to the previous hour can also be returned. A few events belonging to this hour can be missing; to
     * ensure all events from the hour are returned, round the start time down by an hour.
     * @param endTime Filters the results to return events approximately before the end time. Note: A few events
     * belonging to the next hour can also be returned. A few events belonging to this hour can be missing; to ensure
     * all events from the hour are returned, round the end time up by an hour.
     * @return The changefeed events.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public BlobChangefeedPagedIterable getEvents(OffsetDateTime startTime, OffsetDateTime endTime) {
        return getEvents(startTime, endTime, Context.NONE);
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
     * <a href="https://docs.microsoft.com/azure/storage/blobs/storage-blob-change-feed?tabs=azure-portal">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.changefeed.BlobChangefeedClient.getEvents#OffsetDateTime-OffsetDateTime-Context}
     *
     * @param startTime Filters the results to return events approximately after the start time. Note: A few events
     * belonging to the previous hour can also be returned. A few events belonging to this hour can be missing; to
     * ensure all events from the hour are returned, round the start time down by an hour.
     * @param endTime Filters the results to return events approximately before the end time. Note: A few events
     * belonging to the next hour can also be returned. A few events belonging to this hour can be missing; to ensure
     * all events from the hour are returned, round the end time up by an hour.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return The changefeed events.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public BlobChangefeedPagedIterable getEvents(OffsetDateTime startTime, OffsetDateTime endTime, Context context) {
        return new BlobChangefeedPagedIterable(client.getEvents(startTime, endTime).setSubscriberContext(context));
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
     * <a href="https://docs.microsoft.com/azure/storage/blobs/storage-blob-change-feed?tabs=azure-portal">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.changefeed.BlobChangefeedClient.getEvents#String}
     *
     * @param cursor Identifies the portion of the events to be returned with the next get operation. Events that
     * take place after the event identified by the cursor will be returned.
     * @return The changefeed events.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public BlobChangefeedPagedIterable getEvents(String cursor) {
        return getEvents(cursor, Context.NONE);
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
     * <a href="https://docs.microsoft.com/azure/storage/blobs/storage-blob-change-feed?tabs=azure-portal">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.changefeed.BlobChangefeedClient.getEvents#String-Context}
     *
     * @param cursor Identifies the portion of the events to be returned with the next get operation. Events that
     * take place after the event identified by the cursor will be returned.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return The changefeed events.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public BlobChangefeedPagedIterable getEvents(String cursor, Context context) {
        return new BlobChangefeedPagedIterable(client.getEvents(cursor).setSubscriberContext(context));
    }
}
