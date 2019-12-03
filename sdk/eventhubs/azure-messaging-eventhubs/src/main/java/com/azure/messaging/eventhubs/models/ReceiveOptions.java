// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs.models;

import com.azure.core.annotation.Fluent;
import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.eventhubs.EventHubConsumerAsyncClient;
import com.azure.messaging.eventhubs.EventHubConsumerClient;

/**
 * Set of options that can be specified when receiving events from an {@link EventHubConsumerAsyncClient} or
 * {@link EventHubConsumerClient}.
 */
@Fluent
public class ReceiveOptions {
    private final ClientLogger logger = new ClientLogger(ReceiveOptions.class);
    private Long ownerLevel;
    private boolean trackLastEnqueuedEventProperties;

    /**
     * Gets the owner level for this consumer. If the value is {@code null}, then this is not an exclusive consumer.
     * Otherwise, it is an exclusive consumer, and there can only be one active consumer for each partition and consumer
     * group combination. The exclusive consumer is be based on which one has the higher owner level value.
     *
     * @return The owner level for this receive operation. If the {@code null}, then this is not an exclusive consumer.
     */
    public Long getOwnerLevel() {
        return ownerLevel;
    }

    /**
     * Sets the {@code ownerLevel} value on this receive operation. When populated, the level indicates that the receive
     * operation is intended to be the only reader of events for the requested partition and associated consumer group.
     * To do so, this receive operation will attempt to assert ownership over the partition; in the case where
     * there is more than one exclusive receive operation for the same partition/consumer group pair, the one having a
     * larger {@link ReceiveOptions#getOwnerLevel()} value will "win".
     *
     * <p>When an exclusive receive operation is used, those receive operations which are not exclusive or which have a
     * lower priority will either not be allowed to be created. If they already exist, will encounter an exception
     * during the next attempted operation.</p>
     *
     * @param priority The priority associated with an exclusive receive operation; for a non-exclusive receive
     *     operation, this value should be {@code null}.
     *
     * @return The updated {@link ReceiveOptions} object.
     *
     * @throws IllegalArgumentException if {@code priority} is not {@code null} and is less than 0.
     */
    public ReceiveOptions setOwnerLevel(Long priority) {
        if (priority != null && priority < 0) {
            throw logger.logExceptionAsError(new IllegalArgumentException(
                "'priority' cannot be a negative value. Please specify a zero or positive long value."));
        }

        this.ownerLevel = priority;
        return this;
    }

    /**
     * Gets whether or not the receive operation should request information on the last enqueued event on its associated
     * partition, and track that information as events are received.
     *
     * @return {@code true} if the resulting receive operation will keep track of the last enqueued information for that
     *     partition; {@code false} otherwise.
     */
    public boolean getTrackLastEnqueuedEventProperties() {
        return trackLastEnqueuedEventProperties;
    }

    /**
     * Sets whether or not the receive operation should request information on the last enqueued event on its associated
     * partition, and track that information as events are received.
     *
     * <p>When information about the partition's last enqueued event is being tracked, each event received from the
     * Event Hubs service will carry metadata about the partition that it otherwise would not. This results in a small
     * amount of additional network bandwidth consumption that is generally a favorable trade-off when considered
     * against periodically making requests for partition properties using the Event Hub client.</p>
     *
     * @param trackLastEnqueuedEventProperties {@code true} if the resulting events will keep track of the last
     *     enqueued information for that partition; {@code false} otherwise.
     *
     * @return The updated {@link ReceiveOptions} object.
     */
    public ReceiveOptions setTrackLastEnqueuedEventProperties(boolean trackLastEnqueuedEventProperties) {
        this.trackLastEnqueuedEventProperties = trackLastEnqueuedEventProperties;
        return this;
    }
}
