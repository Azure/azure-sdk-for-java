// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs.models;

import com.azure.core.amqp.RetryOptions;
import com.azure.core.annotation.Fluent;
import com.azure.messaging.eventhubs.EventHubAsyncClient;
import com.azure.messaging.eventhubs.EventHubAsyncProducer;

/**
 * The set of options that can be specified when creating an {@link EventHubAsyncProducer} to configure its behavior.
 *
 * @see EventHubAsyncProducer
 * @see EventHubAsyncClient#createProducer(EventHubProducerOptions)
 */
@Fluent
public class EventHubProducerOptions implements Cloneable {
    private RetryOptions retryOptions;

    /**
     * Sets the retry options used to govern retry attempts when an issue is encountered while sending.
     *
     * @param retry The retry options used to govern retry attempts when an issue is encountered while sending.
     * @return The updated SenderOptions object.
     */
    public EventHubProducerOptions setRetry(RetryOptions retry) {
        this.retryOptions = retry;
        return this;
    }

    /**
     * Gets the retry options used to govern retry attempts when an issue is encountered while sending.
     *
     * @return the retry options used to govern retry attempts when an issue is encountered while sending. If
     *         {@code null}, then the retry options configured on the associated {@link EventHubAsyncClient} is used.
     */
    public RetryOptions getRetry() {
        return retryOptions;
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

        if (retryOptions != null) {
            clone.setRetry(retryOptions.clone());
        }

        return clone;
    }
}
