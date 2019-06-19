// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.eventhubs;

import com.azure.core.amqp.Retry;

import java.time.Duration;

/**
 * The set of options that can be specified when creating an {@link EventHubProducer} to configure its behavior.
 */
public class EventHubProducerOptions implements Cloneable {
    private String partitionId;
    private Retry retry;
    private Duration timeout;

    /**
     * Sets the identifier of the Event Hub partition that the {@link EventHubProducer} will be bound to, limiting it to
     * sending events to only that partition.
     *
     * If the identifier is not specified, the Event Hubs service will be responsible for routing events that are sent
     * to an available partition.
     *
     * @param partitionId The identifier of the Event Hub partition that the {@link EventHubProducer} will be bound to.
     *         If the producer wishes the events to be automatically to partitions, {@code null}; otherwise, the
     *         identifier of the desired partition.
     * @return The updated {@link EventHubProducerOptions} object.
     */
    public EventHubProducerOptions partitionId(String partitionId) {
        this.partitionId = partitionId;
        return this;
    }

    /**
     * Sets the retry policy used to govern retry attempts when an issue is encountered while sending.
     *
     * @param retry The retry policy used to govern retry attempts when an issue is encountered while sending.
     * @return The updated SenderOptions object.
     */
    public EventHubProducerOptions retry(Retry retry) {
        this.retry = retry;
        return this;
    }

    /**
     * Sets the default timeout to apply when sending events. If the timeout is reached, before the Event Hub
     * acknowledges receipt of the event data being sent, the attempt will be considered failed and will be retried.
     *
     * @param timeout The timeout to apply when sending events.
     * @return The updated {@link EventHubProducerOptions} object.
     */
    public EventHubProducerOptions timeout(Duration timeout) {
        this.timeout = timeout;
        return this;
    }

    /**
     * Gets the retry policy used to govern retry attempts when an issue is encountered while sending.
     *
     * @return the retry policy used to govern retry attempts when an issue is encountered while sending. If
     *         {@code null}, then the retry policy configured on the associated {@link EventHubClient} is used.
     */
    public Retry retry() {
        return retry;
    }

    /**
     * Gets the identifier of the Event Hub partition that the {@link EventHubProducer} will be bound to, limiting it to
     * sending events to only that partition.
     *
     * If the identifier is not specified, the Event Hubs service will be responsible for routing events that are sent
     * to an available partition.
     *
     * @return the identifier of the Event Hub partition that the {@link EventHubProducer} will be bound to.
     */
    public String partitionId() {
        return partitionId;
    }

    /**
     * Gets the default timeout when sending events.
     *
     * @return The default timeout when sending events.
     */
    public Duration timeout() {
        return timeout;
    }

    /**
     * Creates a shallow clone of this instance.
     *
     * The object is cloned, but the objects {@link #retry()}, {@link #timeout()} and {@link #partitionId()} are not
     * cloned. {@link Duration} and {@link String} are immutable objects and are not an issue. However, the
     * implementation of {@link Retry} could be mutable.
     *
     * @return A shallow clone of this object.
     */
    public EventHubProducerOptions clone() {
        EventHubProducerOptions clone;
        try {
            clone = (EventHubProducerOptions) super.clone();
        } catch (CloneNotSupportedException e) {
            clone = new EventHubProducerOptions();
        }

        clone.partitionId(this.partitionId);
        clone.retry(this.retry);
        clone.timeout(this.timeout);

        return clone;
    }
}
