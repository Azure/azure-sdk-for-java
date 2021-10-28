// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs.models;

import com.azure.messaging.eventhubs.EventData;
import com.azure.messaging.eventhubs.EventHubBufferedProducerAsyncClient;
import com.azure.messaging.eventhubs.EventHubBufferedProducerClient;

/**
 * Contains information about a batch that was published and the partition that it was published to.
 *
 * @see EventHubBufferedProducerAsyncClient
 * @see EventHubBufferedProducerClient
 */
public final class SendBatchSucceededContext {
    private final String partitionId;
    private final Iterable<EventData> events;

    /**
     * Initializes a new instance of the class.
     *
     * @param events The set of events in the batch that was published.
     * @param partitionId The identifier of the partition that the batch was published to.
     */
    public SendBatchSucceededContext(Iterable<EventData> events, String partitionId) {
        this.events = events;
        this.partitionId = partitionId;
    }

    /**
     * Gets the set of events in the batch that was published.
     *
     * @return The set of events in the batch that was published.
     */
    public Iterable<EventData> getEvents() {
        return events;
    }

    /**
     * Gets the identifier of the partition that the batch was published to.
     *
     * @return The identifier of the partition that the batch was published to.
     */
    public String getPartitionId() {
        return partitionId;
    }
}
