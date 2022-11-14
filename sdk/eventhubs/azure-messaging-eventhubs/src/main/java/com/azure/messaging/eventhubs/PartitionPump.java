// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.eventhubs.implementation.ReactorShim;
import com.azure.messaging.eventhubs.models.LastEnqueuedEventProperties;
import reactor.core.scheduler.Scheduler;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicBoolean;

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
    private final AtomicBoolean isDisposed = new AtomicBoolean();
    // Period for scheduler to be disposed of gracefully. In normal times, it won't take this long time to dispose the scheduler.
    private static final Duration DISPOSED_TIMEOUT = Duration.ofSeconds(1);

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
        // When client receive complete signal will call this method again in clean up method on scheduler thread,
        // the first dispose will cause a timeout exception. Add a check avoid close twice.
        if (isDisposed.getAndSet(true)) {
            return;
        }
        try {
            client.close();
        } catch (Exception error) {
            LOGGER.atInfo()
                .addKeyValue(PARTITION_ID_KEY, partitionId)
                .log("Exception occurred disposing of consumer client.", error);
        } finally {
            try {
                ReactorShim.disposeGracefully(scheduler).block(DISPOSED_TIMEOUT);
            } catch (IllegalStateException e) {
                // If the scheduler cannot be disposed of gracefully within the grace period,
                // then an IllegalStateException is thrown, on which we do eager disposal.
                scheduler.dispose();
            }
        }
    }
}
