// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.eventhubs.models.LastEnqueuedEventProperties;
import reactor.core.scheduler.Scheduler;

import static com.azure.messaging.eventhubs.implementation.ClientConstants.PARTITION_ID_KEY;

/**
 * Contains the event hub consumer and scheduler that continuously receive events.
 */
class PartitionPump implements AutoCloseable {
    private static final ClientLogger LOGGER = new ClientLogger(PartitionPump.class);

    private final String partitionId;
    private final EventHubConsumerAsyncClient client;
    private final Scheduler scheduler;
    private LastEnqueuedEventProperties lastEnqueuedEventProperties;

    /**
     * Creates an instance with the given client and scheduler.
     *
     * @param partitionId Partition id for the pump.
     * @param client Consumer associated with partition id.
     * @param scheduler Scheduler for the consumer.
     */
    PartitionPump(String partitionId, EventHubConsumerAsyncClient client, Scheduler scheduler) {
        this.partitionId = partitionId;
        this.client = client;
        this.scheduler = scheduler;
    }

    EventHubConsumerAsyncClient getClient() {
        return client;
    }

    /**
     * Gets the last enqueued event properties.
     *
     * @return the last enqueued event properties or null if there has been no events received or {@link
     *     EventProcessorClientBuilder#trackLastEnqueuedEventProperties(boolean)} is false.
     */
    LastEnqueuedEventProperties getLastEnqueuedEventProperties() {
        return lastEnqueuedEventProperties;
    }

    /**
     * Sets the last enqueued event properties seen.
     *
     * @param lastEnqueuedEventProperties the last enqueued event properties.
     */
    void setLastEnqueuedEventProperties(LastEnqueuedEventProperties lastEnqueuedEventProperties) {
        this.lastEnqueuedEventProperties = lastEnqueuedEventProperties;
    }

    /**
     * Disposes of the scheduler and the consumer.
     */
    @Override
    public void close() {
        try {
            client.close();
        } catch (Exception error) {
            LOGGER.atInfo()
                .addKeyValue(PARTITION_ID_KEY, partitionId)
                .log("Exception occurred disposing of consumer client.", error);
        } finally {
            scheduler.dispose();
        }
    }
}
