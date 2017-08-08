// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.
package com.microsoft.azure.servicebus;

import java.time.Duration;
import java.util.Locale;

/**
 * The OnMessage handler processing options.
 */
public final class MessageHandlerOptions {
    private static final boolean DEFAULT_AUTO_COMPLETE = true;
    private static final int DEFAULT_MAX_CONCURRENT_CALLS = 1;
    private static final int DEFAULT_MAX_RENEW_TIME_MINUTES = 5;

    private boolean autoComplete;
    private Duration maxAutoRenewDuration;
    private int maxConcurrentCalls;

    /**
     * Default constructor for create {@link MessageHandlerOptions} with default settings.
     * {@link MessageHandlerOptions#getMaxConcurrentCalls()} default value is 1.
     * {@link MessageHandlerOptions#getMaxAutoRenewDuration()} default value is 5 minutes.
     * {@link MessageHandlerOptions#isAutoComplete()} default is true.
     */
    public MessageHandlerOptions() {
        this(DEFAULT_MAX_CONCURRENT_CALLS, DEFAULT_AUTO_COMPLETE, Duration.ofMinutes(DEFAULT_MAX_RENEW_TIME_MINUTES));
    }

    /**
     * Create a instance of {@link MessageHandlerOptions}.
     *
     * @param maxConcurrentCalls   maximum number of concurrent calls to the onMessage handler
     * @param autoComplete         true if the pump should automatically complete message after onMessageHandler action is completed. false otherwise.
     * @param maxAutoRenewDuration - Maximum duration within which the client keeps renewing the message lock if the processing of the message is not completed by the handler.
     */
    public MessageHandlerOptions(int maxConcurrentCalls, boolean autoComplete, Duration maxAutoRenewDuration) {
        this.autoComplete = autoComplete;
        this.maxAutoRenewDuration = maxAutoRenewDuration;
        this.maxConcurrentCalls = maxConcurrentCalls;
    }

    /**
     * Whether the auto complete is set to true.
     *
     * @return true to complete the message processing automatically on successful execution of the operation; otherwise, false.
     */
    public boolean isAutoComplete() {
        return this.autoComplete;
    }

    /**
     * Gets the maximum number of concurrent calls to the callback the message pump should initiate.
     *
     * @return The maximum number of concurrent calls to the callback.
     */
    public int getMaxConcurrentCalls() {
        return this.maxConcurrentCalls;
    }

    /**
     * Gets the maximum duration within which the lock will be renewed automatically. This value should be greater than the longest message lock duration; for example, the LockDuration Property.
     *
     * @return The maximum duration during which locks are automatically renewed.
     */
    public Duration getMaxAutoRenewDuration() {
        return this.maxAutoRenewDuration;
    }

    @Override
    public String toString() {
        return String.format(Locale.US, "MessageHandlerOptions - AutoComplete:%s, MaxConcurrentCalls:%s, MaxAutoRenewDuration:%s", this.autoComplete, this.maxConcurrentCalls, this.maxAutoRenewDuration);
    }
}
