// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus.models;

import com.azure.messaging.servicebus.ServiceBusReceiverAsyncClient;

import java.time.Duration;

/**
 * Options set when receiving using {@link ServiceBusReceiverAsyncClient}.
 */
public class ReceiveAsyncOptions {
    private boolean enableAutoComplete;
    private Duration maxAutoRenewDuration;

    /**
     * Gets whether the message should be automatically completed when consumers are finished processing the message.
     *
     * @return {@code true} to automatically complete the message; {@code false} otherwise.
     */
    public boolean isEnableAutoComplete() {
        return enableAutoComplete;
    }

    /**
     * Sets whether the message should be automatically completed when consumers are finished processing the message.
     *
     * @param enableAutoComplete {@code true} to automatically complete the message; {@code false} otherwise.
     *
     * @return The updated {@link ReceiveAsyncOptions} object.
     */
    public ReceiveAsyncOptions setEnableAutoComplete(boolean enableAutoComplete) {
        this.enableAutoComplete = enableAutoComplete;
        return this;
    }

    /**
     * Gets the amount of time to continue auto-renewing the message lock.
     *
     * @return the amount of time to continue auto-renewing the message lock. {@link Duration#ZERO} or {@code null}
     *     indicates that auto-renewal is disabled.
     */
    public Duration getMaxAutoRenewDuration() {
        return maxAutoRenewDuration;
    }

    /**
     * Sets the amount of time to continue auto-renewing the message lock. Setting {@link Duration#ZERO} or {@code
     * null} disables auto-renewal.
     *
     * @param maxAutoRenewDuration the amount of time to continue auto-renewing the message lock. {@link
     *     Duration#ZERO} or {@code null} indicates that auto-renewal is disabled.
     *
     * @return The updated {@link ReceiveAsyncOptions} object.
     */
    public ReceiveAsyncOptions setMaxAutoRenewDuration(Duration maxAutoRenewDuration) {
        this.maxAutoRenewDuration = maxAutoRenewDuration;
        return this;
    }
}
