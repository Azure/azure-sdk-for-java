// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.changefeed;

import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;

import java.time.OffsetDateTime;

/**
 * This class provides a client that contains all operations that apply to Azure Storage Blob changefeed.
 *
 * @see BlobChangefeedClientBuilder
 */
@ServiceClient(builder = BlobChangefeedClientBuilder.class, isAsync = true)
public class BlobChangefeedAsyncClient {


    private final ChangefeedFactory changefeedFactory;

    /**
     * Package-private constructor for use by {@link BlobChangefeedClientBuilder}.
     *
     * @param changefeedFactory The changefeed factory to create the Changefeed from.
     */
    BlobChangefeedAsyncClient(ChangefeedFactory changefeedFactory) {
        this.changefeedFactory = changefeedFactory;
    }

    /**
     * Returns a reactive Publisher emitting all the changefeed events for this account lazily as needed.
     *
     * <p>
     * Changefeed events are returned in approximate temporal order.
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/azure/storage/blobs/storage-blob-change-feed?tabs=azure-portal">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.changefeed.BlobChangefeedAsyncClient.getEvents}
     *
     * @return A reactive response emitting the changefeed events.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public BlobChangefeedPagedFlux getEvents() {
        return getEvents(null, null);
    }

    /**
     * Returns a reactive Publisher emitting all the changefeed events for this account lazily as needed.
     *
     * <p>
     * Changefeed events are returned in approximate temporal order.
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/azure/storage/blobs/storage-blob-change-feed?tabs=azure-portal">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.changefeed.BlobChangefeedAsyncClient.getEvents#OffsetDateTime-OffsetDateTime}
     *
     * @param startTime Filters the results to return events approximately after the start time. Note: A few events
     * belonging to the previous hour can also be returned. A few events belonging to this hour can be missing; to
     * ensure all events from the hour are returned, round the start time down by an hour.
     * @param endTime Filters the results to return events approximately before the end time. Note: A few events
     * belonging to the next hour can also be returned. A few events belonging to this hour can be missing; to ensure
     * all events from the hour are returned, round the end time up by an hour.
     * @return A reactive response emitting the changefeed events.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public BlobChangefeedPagedFlux getEvents(OffsetDateTime startTime, OffsetDateTime endTime) {
        return new BlobChangefeedPagedFlux(changefeedFactory, startTime, endTime);
    }

    /**
     * Returns a reactive Publisher emitting all the changefeed events for this account lazily as needed.
     *
     * <p>
     * Changefeed events are returned in approximate temporal order.
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/azure/storage/blobs/storage-blob-change-feed?tabs=azure-portal">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.changefeed.BlobChangefeedAsyncClient.getEvents#String}
     *
     * @param cursor Identifies the portion of the events to be returned with the next get operation. Events that
     * take place after the event identified by the cursor will be returned.
     * @return A reactive response emitting the changefeed events.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public BlobChangefeedPagedFlux getEvents(String cursor) {
        return new BlobChangefeedPagedFlux(changefeedFactory, cursor);
    }

}
