// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import com.azure.core.util.logging.ClientLogger;
import reactor.core.scheduler.Scheduler;

/**
 * Contains the event hub consumer and scheduler that continuously receive events.
 */
class PartitionPump implements AutoCloseable {
    private final String partitionId;
    private final EventHubConsumerAsyncClient client;
    private final Scheduler scheduler;
    private final ClientLogger logger = new ClientLogger(PartitionPump.class);

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
     * Disposes of the scheduler and the consumer.
     */
    @Override
    public void close() {
        try {
            client.close();
        } catch (Exception error) {
            logger.info("partitionId[{}] Exception occurred disposing of consumer client.", partitionId, error);
        } finally {
            scheduler.dispose();
        }
    }
}
