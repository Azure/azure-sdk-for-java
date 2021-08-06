// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.eventhubs;

import java.time.Duration;

/**
 * Convenient container for options for creating an {@link EventHubClient}
 * All options default to not specified (null)
 */
public class EventHubClientOptions {
    public static final Duration SILENT_OFF = Duration.ofSeconds(0);
    public static final Duration SILENT_MINIMUM = Duration.ofSeconds(30);

    private Duration operationTimeout = null;
    private TransportType transportType = null;
    private RetryPolicy retryPolicy = null;
    private ProxyConfiguration proxyConfiguration = null;
    private Duration maximumSilentTime = SILENT_OFF;

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
     * Sets the maximum silent time, in seconds.
     * Use only on recommendation from the product group.
     * 
     * @param maximumSilentTime The time, or SILENT_OFF. Time must be at least SILENT_MINIMUM.
     * @return The updated options object.
     */
    public EventHubClientOptions setMaximumSilentTime(Duration maximumSilentTime) {
        if (!maximumSilentTime.equals(EventHubClientOptions.SILENT_OFF) && (maximumSilentTime.compareTo(EventHubClientOptions.SILENT_MINIMUM) < 0)) {
            throw new IllegalArgumentException("Maximum silent time must be at least " + EventHubClientOptions.SILENT_MINIMUM.toMillis() + " milliseconds");
        }
        this.maximumSilentTime = maximumSilentTime;
        return this;
    }

    /**
     * Gets the maximum silent time in seconds.
     * 
     * @return The maximum silent time, or SILENT_OFF.
     */
    public Duration getMaximumSilentTime() {
        return this.maximumSilentTime;
    }
}
