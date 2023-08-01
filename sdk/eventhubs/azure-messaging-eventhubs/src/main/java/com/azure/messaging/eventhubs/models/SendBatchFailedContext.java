// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs.models;

import com.azure.messaging.eventhubs.EventData;
import com.azure.messaging.eventhubs.EventHubBufferedProducerAsyncClient;
import com.azure.messaging.eventhubs.EventHubBufferedProducerClient;

/**
 * Contains information about a batch that was unable to be published, as well as the exception that occurred and the
 * partition that the batch was being published to.
 *
 * @see EventHubBufferedProducerClient
 * @see EventHubBufferedProducerAsyncClient
 */
public final class SendBatchFailedContext {
    private final Iterable<EventData> events;
    private final String partitionId;
    private final Throwable throwable;

    /**
     * Creates a new instance.
     *
     * @param events Events associated with the failed batch.
     * @param partitionId Partition that the events went to.
     * @param throwable Error associated with the failed batch.
     */
    public SendBatchFailedContext(Iterable<EventData> events, String partitionId, Throwable throwable) {
        this.events = events;
        this.partitionId = partitionId;
        this.throwable = throwable;
    }

    /**
     * Gets the events that failed to send.
     *
     * @return The events that failed to send.
     */
    public Iterable<EventData> getEvents() {
        return events;
    }

    /**
     * Gets the partition id that the failed batch went to.
     *
     * @return The partition id that the failed batch went to.
     */
    public String getPartitionId() {
        return partitionId;
    }

    /**
     * Gets the error that occurred when sending the batch.
     *
     * @return The error that occurred when sending the batch.
     */
    public Throwable getThrowable() {
        return throwable;
    }
}
