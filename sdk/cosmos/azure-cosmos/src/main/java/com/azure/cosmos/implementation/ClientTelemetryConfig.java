// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation;

import com.azure.core.http.ProxyOptions;
import com.azure.cosmos.ClientTelemetryConnectionConfig;

import java.time.Duration;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

public class ClientTelemetryConfig {
    private final boolean clientTelemetryEnabled;
    private final Duration httpNetworkRequestTimeout;
    private final int maxConnectionPoolSize;
    private final Duration idleHttpConnectionTimeout;
    private final ProxyOptions proxy;

    public ClientTelemetryConfig(boolean clientTelemetryEnabled, ClientTelemetryConnectionConfig connectionConfig) {
        checkNotNull(connectionConfig, "Argument 'connectionConfig' should not be null");

        this.clientTelemetryEnabled = clientTelemetryEnabled;
        this.httpNetworkRequestTimeout = connectionConfig.getNetworkRequestTimeout();
        this.maxConnectionPoolSize = connectionConfig.getMaxConnectionPoolSize();
        this.idleHttpConnectionTimeout = connectionConfig.getIdleConnectionTimeout();
        this.proxy = connectionConfig.getProxy();
    }

    public boolean isClientTelemetryEnabled() {
        return this.clientTelemetryEnabled;
    }

    public Duration getHttpNetworkRequestTimeout() {
        return this.httpNetworkRequestTimeout;
    }

    public int getMaxConnectionPoolSize() {
        return this.maxConnectionPoolSize;
    }

    public Duration getIdleHttpConnectionTimeout() {
        return this.idleHttpConnectionTimeout;
    }

    public ProxyOptions getProxy() {
        return this.proxy;
    }
}
