// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import com.azure.messaging.servicebus.models.ReceiveMode;

import java.time.Duration;

/**
 * Options set when receiving a message.
 */
class ReceiveMessageOptions {
    private final boolean autoComplete;
    private final ReceiveMode receiveMode;
    private final int prefetchCount;
    private final boolean isLockAutoRenewed;
    private final Duration maxAutoRenewDuration;

    ReceiveMessageOptions(boolean autoComplete, ReceiveMode receiveMode, int prefetchCount, boolean isLockAutoRenewed,
        Duration maxAutoRenewDuration) {
        this.autoComplete = autoComplete;
        this.receiveMode = receiveMode;
        this.prefetchCount = prefetchCount;
        this.isLockAutoRenewed = isLockAutoRenewed;
        this.maxAutoRenewDuration = maxAutoRenewDuration;
    }

    /**
     * Gets whether or not to autocomplete messages after they have been processed.
     *
     * @return {@code true} if the message should be completed/abandoned; {@code false} otherwise.
     */
    boolean isAutoComplete() {
        return autoComplete;
    }

    /**
     * Gets if lock should be automatically renewed.
     *
     * @return {@code true} if the lock should be automatically renewed; {@code false} otherwise.
     */
    boolean isLockAutoRenewed() {
        return isLockAutoRenewed;
    }

    /**
     * Gets the receive mode for the message.
     *
     * @return the receive mode for the message.
     */
    ReceiveMode getReceiveMode() {
        return receiveMode;
    }

    /**
     * Gets the prefetch count of the receiver.
     *
     * @return The prefetch count of the receiver.
     */
    int getPrefetchCount() {
        return prefetchCount;
    }

    /**
     * Gets the maximum duration within which the lock will be renewed automatically. This value should be greater than
     * the longest message lock duration.
     *
     * @return The maximum duration within which the lock will be renewed automatically.
     */
    Duration getMaxAutoRenewDuration() {
        return maxAutoRenewDuration;
    }
}
