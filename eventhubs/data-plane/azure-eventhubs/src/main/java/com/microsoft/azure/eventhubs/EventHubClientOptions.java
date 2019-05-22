// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.eventhubs;

import java.time.Duration;

public class EventHubClientOptions {
	private Duration operationTimeout = null;
	private TransportType transportType = null;
	private RetryPolicy retryPolicy = null;

	public EventHubClientOptions() {
	}
	
	public EventHubClientOptions setOperationTimeout(Duration operationTimeout) {
		this.operationTimeout = operationTimeout;
		return this;
	}
	
	public Duration getOperationTimeout() {
		return this.operationTimeout;
	}
	
	public EventHubClientOptions setTransportType(TransportType transportType) {
		this.transportType = transportType;
		return this;
	}
	
	public TransportType getTransportType() {
		return this.transportType;
	}
	
	public EventHubClientOptions setRetryPolicy(RetryPolicy retryPolicy) {
		this.retryPolicy = retryPolicy;
		return this;
	}
	
	public RetryPolicy getRetryPolicy() {
		return this.retryPolicy;
	}
}
