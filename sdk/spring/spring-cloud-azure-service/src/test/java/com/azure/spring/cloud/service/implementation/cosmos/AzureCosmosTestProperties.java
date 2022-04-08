// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.service.implementation.cosmos;

import com.azure.cosmos.ConnectionMode;
import com.azure.cosmos.ConsistencyLevel;
import com.azure.cosmos.ThrottlingRetryOptions;
import com.azure.spring.cloud.core.implementation.properties.AzureSdkProperties;
import com.azure.spring.cloud.core.properties.client.ClientProperties;
import com.azure.spring.cloud.core.properties.proxy.HttpProxyProperties;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * Configuration properties for Cosmos database, consistency, telemetry, connection, query metrics and diagnostics.
 */
class AzureCosmosTestProperties extends AzureSdkProperties implements CosmosClientProperties {

    private String endpoint;

    private String key;

    private String database;

    private String resourceToken;

    private Boolean clientTelemetryEnabled;
    private Boolean endpointDiscoveryEnabled;
    private Boolean connectionSharingAcrossClientsEnabled;
    private Boolean contentResponseOnWriteEnabled;
    private Boolean multipleWriteRegionsEnabled;
    /**
     * Override enabled, session capturing is enabled by default for {@link ConsistencyLevel#SESSION}
     */
    private Boolean sessionCapturingOverrideEnabled;
    private Boolean readRequestsFallbackEnabled;

    private final List<String> preferredRegions = new ArrayList<>();

    private final ThrottlingRetryOptions throttlingRetryOptions = new ThrottlingRetryOptions();

    private ConsistencyLevel consistencyLevel;
    private ConnectionMode connectionMode = ConnectionMode.DIRECT;
    private final GatewayConnection gatewayConnection = new GatewayConnection();
    private final DirectConnection directConnection = new DirectConnection();

    /**
     * Populate Diagnostics Strings and Query metrics
     */
    private boolean populateQueryMetrics;

    private final ClientProperties client = new ClientProperties();
    private final HttpProxyProperties proxy = new HttpProxyProperties();

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    @Override
    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getResourceToken() {
        return resourceToken;
    }

    public void setResourceToken(String resourceToken) {
        this.resourceToken = resourceToken;
    }

    public Boolean getClientTelemetryEnabled() {
        return clientTelemetryEnabled;
    }

    public void setClientTelemetryEnabled(Boolean clientTelemetryEnabled) {
        this.clientTelemetryEnabled = clientTelemetryEnabled;
    }

    public Boolean getEndpointDiscoveryEnabled() {
        return endpointDiscoveryEnabled;
    }

    public void setEndpointDiscoveryEnabled(Boolean endpointDiscoveryEnabled) {
        this.endpointDiscoveryEnabled = endpointDiscoveryEnabled;
    }

    public Boolean getConnectionSharingAcrossClientsEnabled() {
        return connectionSharingAcrossClientsEnabled;
    }

    public void setConnectionSharingAcrossClientsEnabled(Boolean connectionSharingAcrossClientsEnabled) {
        this.connectionSharingAcrossClientsEnabled = connectionSharingAcrossClientsEnabled;
    }

    public Boolean getContentResponseOnWriteEnabled() {
        return contentResponseOnWriteEnabled;
    }

    public void setContentResponseOnWriteEnabled(Boolean contentResponseOnWriteEnabled) {
        this.contentResponseOnWriteEnabled = contentResponseOnWriteEnabled;
    }

    public Boolean getMultipleWriteRegionsEnabled() {
        return multipleWriteRegionsEnabled;
    }

    public void setMultipleWriteRegionsEnabled(Boolean multipleWriteRegionsEnabled) {
        this.multipleWriteRegionsEnabled = multipleWriteRegionsEnabled;
    }

    public Boolean getSessionCapturingOverrideEnabled() {
        return sessionCapturingOverrideEnabled;
    }

    public void setSessionCapturingOverrideEnabled(Boolean sessionCapturingOverrideEnabled) {
        this.sessionCapturingOverrideEnabled = sessionCapturingOverrideEnabled;
    }

    public Boolean getReadRequestsFallbackEnabled() {
        return readRequestsFallbackEnabled;
    }

    public void setReadRequestsFallbackEnabled(Boolean readRequestsFallbackEnabled) {
        this.readRequestsFallbackEnabled = readRequestsFallbackEnabled;
    }

    public List<String> getPreferredRegions() {
        return preferredRegions;
    }

    public ConsistencyLevel getConsistencyLevel() {
        return consistencyLevel;
    }

    public void setConsistencyLevel(ConsistencyLevel consistencyLevel) {
        this.consistencyLevel = consistencyLevel;
    }

    public String getDatabase() {
        return database;
    }

    public void setDatabase(String database) {
        this.database = database;
    }

    public boolean isPopulateQueryMetrics() {
        return populateQueryMetrics;
    }

    public void setPopulateQueryMetrics(boolean populateQueryMetrics) {
        this.populateQueryMetrics = populateQueryMetrics;
    }

    public ConnectionMode getConnectionMode() {
        return connectionMode;
    }

    public void setConnectionMode(ConnectionMode connectionMode) {
        this.connectionMode = connectionMode;
    }

    public ThrottlingRetryOptions getThrottlingRetryOptions() {
        return throttlingRetryOptions;
    }

    @Override
    public GatewayConnection getGatewayConnection() {
        return gatewayConnection;
    }

    @Override
    public DirectConnection getDirectConnection() {
        return directConnection;
    }

    @Override
    public ClientProperties getClient() {
        return client;
    }

    @Override
    public HttpProxyProperties getProxy() {
        return proxy;
    }

    static class GatewayConnection implements GatewayConnectionProperties {

        private Integer maxConnectionPoolSize;
        private Duration idleConnectionTimeout;

        @Override
        public Integer getMaxConnectionPoolSize() {
            return maxConnectionPoolSize;
        }

        public void setMaxConnectionPoolSize(Integer maxConnectionPoolSize) {
            this.maxConnectionPoolSize = maxConnectionPoolSize;
        }

        @Override
        public Duration getIdleConnectionTimeout() {
            return idleConnectionTimeout;
        }

        public void setIdleConnectionTimeout(Duration idleConnectionTimeout) {
            this.idleConnectionTimeout = idleConnectionTimeout;
        }
    }

    static class DirectConnection implements DirectConnectionProperties {

        private Boolean connectionEndpointRediscoveryEnabled;
        private Duration connectTimeout;
        private Duration idleConnectionTimeout;
        private Duration idleEndpointTimeout;
        private Duration networkRequestTimeout;
        private Integer maxConnectionsPerEndpoint;
        private Integer maxRequestsPerConnection;

        @Override
        public Boolean getConnectionEndpointRediscoveryEnabled() {
            return connectionEndpointRediscoveryEnabled;
        }

        public void setConnectionEndpointRediscoveryEnabled(Boolean connectionEndpointRediscoveryEnabled) {
            this.connectionEndpointRediscoveryEnabled = connectionEndpointRediscoveryEnabled;
        }

        @Override
        public Duration getConnectTimeout() {
            return connectTimeout;
        }

        public void setConnectTimeout(Duration connectTimeout) {
            this.connectTimeout = connectTimeout;
        }

        @Override
        public Duration getIdleConnectionTimeout() {
            return idleConnectionTimeout;
        }

        public void setIdleConnectionTimeout(Duration idleConnectionTimeout) {
            this.idleConnectionTimeout = idleConnectionTimeout;
        }

        @Override
        public Duration getIdleEndpointTimeout() {
            return idleEndpointTimeout;
        }

        public void setIdleEndpointTimeout(Duration idleEndpointTimeout) {
            this.idleEndpointTimeout = idleEndpointTimeout;
        }

        @Override
        public Duration getNetworkRequestTimeout() {
            return networkRequestTimeout;
        }

        public void setNetworkRequestTimeout(Duration networkRequestTimeout) {
            this.networkRequestTimeout = networkRequestTimeout;
        }

        @Override
        public Integer getMaxConnectionsPerEndpoint() {
            return maxConnectionsPerEndpoint;
        }

        public void setMaxConnectionsPerEndpoint(Integer maxConnectionsPerEndpoint) {
            this.maxConnectionsPerEndpoint = maxConnectionsPerEndpoint;
        }

        @Override
        public Integer getMaxRequestsPerConnection() {
            return maxRequestsPerConnection;
        }

        public void setMaxRequestsPerConnection(Integer maxRequestsPerConnection) {
            this.maxRequestsPerConnection = maxRequestsPerConnection;
        }
    }
}
