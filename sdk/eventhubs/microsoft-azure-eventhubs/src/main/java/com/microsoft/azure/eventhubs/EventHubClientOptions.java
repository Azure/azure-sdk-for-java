// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.eventhubs;

import java.time.Duration;

/**
 * Convenient container for options for creating an {@link EventHubClient}
 * All options default to not specified (null)
 */
public class EventHubClientOptions {
    public static final int WATCHDOG_OFF = 0;
    public static final int WATCHDOG_MINIMUM_SECONDS = 30;

    private Duration operationTimeout = null;
    private TransportType transportType = null;
    private RetryPolicy retryPolicy = null;
    private ProxyConfiguration proxyConfiguration = null;
    private int watchdogTriggerSeconds = WATCHDOG_OFF;

    /**
     * Create with all defaults
     */
    public EventHubClientOptions() {
    }

    /**
     * Set the operation timeout.
     * @param operationTimeout  new operation timeout, null to unset any previous value
     * @return  this options object
     */
    public EventHubClientOptions setOperationTimeout(Duration operationTimeout) {
        this.operationTimeout = operationTimeout;
        return this;
    }

    /**
     * Get the operation timeout.
     * @return  operation timeout or null if not set
     */
    public Duration getOperationTimeout() {
        return this.operationTimeout;
    }

    /**
     * Set the {@link TransportType} for the connection to the Event Hubs service
     * @param transportType  new transport type, null to unset any previous value
     * @return  this options object
     */
    public EventHubClientOptions setTransportType(TransportType transportType) {
        this.transportType = transportType;
        return this;
    }

    /**
     * Get the transport type
     * @return  {@link TransportType} or null if not set
     */
    public TransportType getTransportType() {
        return this.transportType;
    }

    /**
     * Set the {@link RetryPolicy} for operations
     * @param retryPolicy  new retry policy, null to unset any previous value
     * @return  this options object
     */
    public EventHubClientOptions setRetryPolicy(RetryPolicy retryPolicy) {
        this.retryPolicy = retryPolicy;
        return this;
    }

    /**
     * Get the retry policy
     * @return  {@link RetryPolicy} or null if not set
     */
    public RetryPolicy getRetryPolicy() {
        return this.retryPolicy;
    }

    /**
     * Sets the proxy configuration for the client options.
     *
     * @param proxyConfiguration The proxy configuration to set on the options.
     * @return The updated options object.
     */
    public EventHubClientOptions setProxyConfiguration(ProxyConfiguration proxyConfiguration) {
        this.proxyConfiguration = proxyConfiguration;
        return this;
    }

    /**
     * Gets the proxy configuration for this set of options.
     *
     * @return Gets the proxy configuration.
     */
    public ProxyConfiguration getProxyConfiguration() {
        return this.proxyConfiguration;
    }

    /**
     * Sets the watchdog trigger time in seconds.
     * 
     * @param watchdogTriggerSeconds The time in seconds, or WATCHDOG_OFF. Time must be at least WATCHDOG_MINIMUM_SECONDS.
     * @return The updated options object.
     */
    public EventHubClientOptions setWatchdogTriggerTime(int watchdogTriggerSeconds) {
        if (watchdogTriggerSeconds < EventHubClientOptions.WATCHDOG_MINIMUM_SECONDS) {
            throw new IllegalArgumentException("Watchdog trigger time must be at least " + EventHubClientOptions.WATCHDOG_MINIMUM_SECONDS + " seconds");
        }
        this.watchdogTriggerSeconds = watchdogTriggerSeconds;
        return this;
    }

    /**
     * Gets the watchdog trigger time in seconds.
     * 
     * @return The watchdog trigger time, or WATCHDOG_OFF.
     */
    public int getWatchdogTriggerTime() {
        return this.watchdogTriggerSeconds;
    }
}
