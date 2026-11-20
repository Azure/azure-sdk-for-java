// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus.implementation;

import com.azure.core.annotation.Fluent;
import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.servicebus.ServiceBusProcessorClient;

import java.time.Duration;

/**
 * Additional options to configure {@link ServiceBusProcessorClient}.
 */
@Fluent
public final class ServiceBusProcessorClientOptions {

    private static final ClientLogger LOGGER = new ClientLogger(ServiceBusProcessorClientOptions.class);
    private static final Duration DEFAULT_DRAIN_TIMEOUT = Duration.ofSeconds(30);

    private int maxConcurrentCalls = 1;
    private boolean disableAutoComplete;
    private boolean isV2;
    private Duration drainTimeout = DEFAULT_DRAIN_TIMEOUT;

    /**
     * Returns true if the auto-complete and auto-abandon feature is disabled.
     * @return true if the auto-complete and auto-abandon feature is disabled.
     */
    public boolean isDisableAutoComplete() {
        return disableAutoComplete;
    }

    /**
     * Disables auto-complete and auto-abandon feature if this is set to {@code true}.
     * @param disableAutoComplete Disables auto-complete and auto-abandon feature if this is set to {@code true}.
     */
    public ServiceBusProcessorClientOptions setDisableAutoComplete(boolean disableAutoComplete) {
        this.disableAutoComplete = disableAutoComplete;
        return this;
    }

    /**
     * The max concurrent messages that should be processed by the processor.
     * @return The max concurrent message that should be processed by the processor.
     */
    public int getMaxConcurrentCalls() {
        return maxConcurrentCalls;
    }

    /**
     * Sets the max concurrent messages that can be processed by the processor. If not set, the default value will be 1.
     *
     * @param maxConcurrentCalls The max concurrent messages that can be processed by the processor.
     * @return The updated instance of {@link ServiceBusProcessorClientOptions}.
     */
    public ServiceBusProcessorClientOptions setMaxConcurrentCalls(int maxConcurrentCalls) {
        this.maxConcurrentCalls = maxConcurrentCalls;
        return this;
    }

    public ServiceBusProcessorClientOptions setV2(boolean isV2) {
        this.isV2 = isV2;
        return this;
    }

    public boolean isV2() {
        return isV2;
    }

    /**
     * Returns the maximum time to wait for in-flight message handlers to complete during processor shutdown.
     * @return the drain timeout duration.
     */
    public Duration getDrainTimeout() {
        return drainTimeout;
    }

    /**
     * Sets the maximum time to wait for in-flight message handlers to complete during processor shutdown.
     * Defaults to 30 seconds.
     *
     * @param drainTimeout the maximum time to wait for in-flight handlers. Must be positive.
     * @return The updated instance of {@link ServiceBusProcessorClientOptions}.
     * @throws NullPointerException if {@code drainTimeout} is null.
     * @throws IllegalArgumentException if {@code drainTimeout} is zero or negative.
     */
    public ServiceBusProcessorClientOptions setDrainTimeout(Duration drainTimeout) {
        if (drainTimeout == null) {
            throw LOGGER.logExceptionAsError(new NullPointerException("'drainTimeout' cannot be null."));
        }
        if (drainTimeout.isZero() || drainTimeout.isNegative()) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException("'drainTimeout' must be positive."));
        }
        this.drainTimeout = drainTimeout;
        return this;
    }
}
