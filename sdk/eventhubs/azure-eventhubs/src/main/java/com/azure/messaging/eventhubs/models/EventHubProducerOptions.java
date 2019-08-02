// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs.models;

import com.azure.core.amqp.RetryOptions;
import com.azure.core.implementation.annotation.Fluent;
import com.azure.messaging.eventhubs.EventHubAsyncClient;
import com.azure.messaging.eventhubs.EventHubProducer;

/**
 * The set of options that can be specified when creating an {@link EventHubProducer} to configure its behavior.
 *
 * @see EventHubProducer
 * @see EventHubAsyncClient#createProducer(EventHubProducerOptions)
 */
@Fluent
public class EventHubProducerOptions implements Cloneable {
    private String partitionId;
    private RetryOptions retryOptions;

    /**
     * Sets the identifier of the Event Hub partition that the {@link EventHubProducer} will be bound to, limiting it to
     * sending events to only that partition.
     *
     * If the identifier is not specified, the Event Hubs service will be responsible for routing events that are sent
     * to an available partition.
     *
     * @param partitionId The identifier of the Event Hub partition that the {@link EventHubProducer} will be
     *         bound to. If the producer wishes the events to be automatically to partitions, {@code null}; otherwise,
     *         the identifier of the desired partition.
     * @return The updated {@link EventHubProducerOptions} object.
     */
    public EventHubProducerOptions partitionId(String partitionId) {
        this.partitionId = partitionId;
        return this;
    }

    /**
     * Sets the retry options used to govern retry attempts when an issue is encountered while sending.
     *
     * @param retry The retry options used to govern retry attempts when an issue is encountered while sending.
     * @return The updated SenderOptions object.
     */
    public EventHubProducerOptions retry(RetryOptions retry) {
        this.retryOptions = retry;
        return this;
    }

    /**
     * Gets the retry options used to govern retry attempts when an issue is encountered while sending.
     *
     * @return the retry options used to govern retry attempts when an issue is encountered while sending. If {@code
     *         null}, then the retry options configured on the associated {@link EventHubAsyncClient} is used.
     */
    public RetryOptions retry() {
        return retryOptions;
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
     * Creates a clone of this instance.
     *
     * @return A shallow clone of this object.
     */
    @Override
    public EventHubProducerOptions clone() {
        EventHubProducerOptions clone;
        try {
            clone = (EventHubProducerOptions) super.clone();
        } catch (CloneNotSupportedException e) {
            clone = new EventHubProducerOptions();
        }

        clone.partitionId(partitionId);

        if (retryOptions != null) {
            clone.retry(retryOptions.clone());
        }

        return clone;
    }
}
