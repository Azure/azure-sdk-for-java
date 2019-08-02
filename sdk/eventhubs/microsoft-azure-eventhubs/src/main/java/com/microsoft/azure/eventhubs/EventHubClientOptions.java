// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.eventhubs;

import java.time.Duration;

/**
 * Convenient container for options for creating an {@link EventHubClient}
 * All options default to not specified (null)
 */
public class EventHubClientOptions {
    private Duration operationTimeout = null;
    private TransportType transportType = null;
    private RetryPolicy retryPolicy = null;

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
}
