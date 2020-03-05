// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus.models;

import com.azure.messaging.servicebus.ServiceBusReceiverAsyncClient;

import java.time.Duration;

/**
 * Options set when receiving a message.
 */
public class ReceiveMessageOptions {
    // Using 0 pre-fetch count for both receive modes, to avoid message lock lost exceptions in application receiving
    // messages at a slow rate. Applications can set it to a higher value if they need better performance.
    private static final int DEFAULT_PREFETCH_COUNT = 1;

    private boolean autoComplete;
    private ReceiveMode receiveMode = ReceiveMode.PEEK_LOCK;
    private int prefetchCount = DEFAULT_PREFETCH_COUNT;
    private Duration maxAutoRenewDuration;

    /**
     * Gets whether or not to automatically complete a received message after it has been processed.
     *
     * @return {@code true} to automatically complete a received message after it has been processed; {@code false}
     *     otherwise.
     */
    public boolean isAutoComplete() {
        return autoComplete;
    }

    /**
     * Sets whether or not to automatically complete a received message after it has been processed.
     *
     * @param autoComplete {@code true} to automatically complete a received message after it has been processed;
     *     {@code false} otherwise.
     *
     * @return The modified {@link ReceiveMessageOptions} object.
     */
    public ReceiveMessageOptions setAutoComplete(boolean autoComplete) {
        this.autoComplete = autoComplete;
        return this;
    }

    /**
     * Gets the receive mode for the message.
     *
     * @return the receive mode for the message.
     */
    public ReceiveMode getReceiveMode() {
        return receiveMode;
    }

    /**
     * Sets the receive mode for the message.
     *
     * @param receiveMode Mode for receiving messages.
     *
     * @return The modified {@link ReceiveMessageOptions} object.
     */
    public ReceiveMessageOptions setReceiveMode(ReceiveMode receiveMode) {
        this.receiveMode = receiveMode;
        return this;
    }

    /**
     * Gets the prefetch count of the receiver.
     *
     * @return The prefetch count of the receiver.
     */
    public int getPrefetchCount() {
        return prefetchCount;
    }

    /**
     * Sets the prefetch count of the receiver. Prefetch speeds up the message flow by aiming to have a message readily
     * available for local retrieval when and before the application asks for one using {@link
     * ServiceBusReceiverAsyncClient#receive()}. Setting a non-zero value will prefetch that number of messages. Setting
     * the value to zero turns prefetch off. For both {@link ReceiveMode#PEEK_LOCK PEEK_LOCK} and {@link
     * ReceiveMode#RECEIVE_AND_DELETE RECEIVE_AND_DELETE} modes the default value is 0.
     *
     * @param prefetchCount The prefetch count.
     *
     * @return The modified {@link ReceiveMessageOptions} object.
     */
    public ReceiveMessageOptions setPrefetchCount(int prefetchCount) {
        this.prefetchCount = prefetchCount;
        return this;
    }

    /**
     * Gets the maximum duration within which the lock will be renewed automatically. This value should be greater than
     * the longest message lock duration.
     *
     * @return The maximum duration within which the lock will be renewed automatically.
     */
    public Duration getMaxAutoRenewDuration() {
        return maxAutoRenewDuration;
    }

    /**
     * Sets the maximum duration within which the lock will be renewed automatically. This value should be greater than
     * the longest message lock duration.
     *
     * @param maxAutoRenewDuration The maximum duration within which the lock will be renewed automatically.
     *
     * @return The modified {@link ReceiveMessageOptions} object.
     */
    public ReceiveMessageOptions setMaxAutoRenewDuration(Duration maxAutoRenewDuration) {
        this.maxAutoRenewDuration = maxAutoRenewDuration;
        return this;
    }
}
