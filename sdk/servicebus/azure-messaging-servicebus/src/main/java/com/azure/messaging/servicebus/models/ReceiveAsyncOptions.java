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
    public boolean isAutoCompleteEnabled() {
        return enableAutoComplete;
    }

    /**
     * Sets whether the message should be automatically completed when consumers are finished processing the message.
     *
     * @param isAutoCompleteEnabled {@code true} to automatically complete the message; {@code false} otherwise.
     *
     * @return The updated {@link ReceiveAsyncOptions} object.
     */
    public ReceiveAsyncOptions setIsAutoCompleteEnabled(boolean isAutoCompleteEnabled) {
        this.enableAutoComplete = isAutoCompleteEnabled;
        return this;
    }

    /**
     * Gets the amount of time to continue auto-renewing the message lock.
     *
     * @return the amount of time to continue auto-renewing the message lock. {@link Duration#ZERO} or {@code null}
     *     indicates that auto-renewal is disabled.
     */
    public Duration getMaxAutoLockRenewalDuration() {
        return maxAutoRenewDuration;
    }

    /**
     * Sets the amount of time to continue auto-renewing the message lock. Setting {@link Duration#ZERO} or {@code null}
     * disables auto-renewal.
     *
     * @param maxAutoLockRenewalDuration the amount of time to continue auto-renewing the message lock. {@link
     *     Duration#ZERO} or {@code null} indicates that auto-renewal is disabled.
     *
     * @return The updated {@link ReceiveAsyncOptions} object.
     */
    public ReceiveAsyncOptions setMaxAutoLockRenewalDuration(Duration maxAutoLockRenewalDuration) {
        this.maxAutoRenewDuration = maxAutoLockRenewalDuration;
        return this;
    }
}
