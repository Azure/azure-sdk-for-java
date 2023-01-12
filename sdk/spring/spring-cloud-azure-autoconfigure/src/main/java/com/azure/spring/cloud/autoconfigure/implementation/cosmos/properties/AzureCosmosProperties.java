// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.cosmos.properties;

import com.azure.cosmos.ConnectionMode;
import com.azure.cosmos.ConsistencyLevel;
import com.azure.cosmos.ThrottlingRetryOptions;
import com.azure.spring.cloud.autoconfigure.implementation.properties.core.AbstractAzureServiceConfigurationProperties;
import com.azure.spring.cloud.autoconfigure.implementation.properties.core.client.ClientConfigurationProperties;
import com.azure.spring.cloud.autoconfigure.implementation.properties.core.proxy.HttpProxyConfigurationProperties;
import com.azure.spring.cloud.service.implementation.cosmos.CosmosClientProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * Configuration properties for Cosmos database, consistency, telemetry, connection, query metrics and diagnostics.
 */
public class AzureCosmosProperties extends AbstractAzureServiceConfigurationProperties implements CosmosClientProperties {

    public static final String PREFIX = "spring.cloud.azure.cosmos";

    @NestedConfigurationProperty
    private final HttpProxyConfigurationProperties proxy = new HttpProxyConfigurationProperties();

    @NestedConfigurationProperty
    private final ClientConfigurationProperties client = new ClientConfigurationProperties();

    /**
     * Endpoint of the Cosmos DB.
     */
    private String endpoint;
    /**
     * Key to authenticate for accessing the Cosmos DB.
     */
    private String key;
    /**
     * Database name of the Cosmos DB.
     */
    private String database;
    /**
     * Resource token to authenticate for accessing the Cosmos DB.
     */
    private String resourceToken;
    /**
     * Whether to enable client telemetry which will periodically collect database operations aggregation statistics,
     * system information like cpu/memory and send it to cosmos monitoring service, which will be helpful during
     * debugging.
     */
    private Boolean clientTelemetryEnabled;
    /**
     * Whether to enable endpoint discovery for geo-replicated database accounts.
     */
    private Boolean endpointDiscoveryEnabled;
    /**
     * Whether to enable connections sharing across multiple Cosmos Clients.
     */
    private Boolean connectionSharingAcrossClientsEnabled;
    /**
     * Whether to only return the headers and status code in Cosmos DB response in case of Create, Update and Delete
     * operations on CosmosItem.  If set to false, service doesn't return payload in the response.
     */
    private Boolean contentResponseOnWriteEnabled;
    /**
     * Whether to enable writes on any regions for geo-replicated database accounts in the Azure Cosmos DB service.
     */
    private Boolean multipleWriteRegionsEnabled;
    /**
     * Whether to enable session capturing. Session capturing is enabled by default for SESSION consistency level.
     */
    private Boolean sessionCapturingOverrideEnabled;
    /**
     * Whether to allow for reads to go to multiple regions configured on an account of Azure Cosmos DB service.
     */
    private Boolean readRequestsFallbackEnabled;
    /**
     * Preferred regions for geo-replicated database accounts. For example, "East US" as the preferred region.
     */
    private final List<String> preferredRegions = new ArrayList<>();

    @NestedConfigurationProperty
    private final ThrottlingRetryOptions throttlingRetryOptions = new ThrottlingRetryOptions();
    /**
     * Consistency level. The requested ConsistencyLevel must match or be weaker than that provisioned for the database
     * account.
     */
    private ConsistencyLevel consistencyLevel;
    /**
     * Connection mode to be used by the client in the Azure Cosmos DB database service.
     */
    private ConnectionMode connectionMode;

    private final GatewayConnection gatewayConnection = new GatewayConnection();

    private final DirectConnection directConnection = new DirectConnection();

    /**
     * Whether to populate diagnostics strings and query metrics.
     */
    private boolean populateQueryMetrics;

    @Override
    public HttpProxyConfigurationProperties getProxy() {
        return proxy;
    }

    @Override
    public ClientConfigurationProperties getClient() {
        return client;
    }

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

    public GatewayConnection getGatewayConnection() {
        return gatewayConnection;
    }

    public DirectConnection getDirectConnection() {
        return directConnection;
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

    public static class GatewayConnection implements GatewayConnectionProperties {

        /**
         * Size of the connection pool.
         */
        private Integer maxConnectionPoolSize;
        /**
         * Timeout for an idle connection. After that time, the connection will be automatically closed.
         */
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

    public static class DirectConnection implements DirectConnectionProperties {

        /**
         * Whether to enable the direct TCP connection endpoint rediscovery.
         */
        private Boolean connectionEndpointRediscoveryEnabled;
        /**
         * Connect timeout for direct client, represents timeout for establishing connections with an endpoint.
         */
        private Duration connectTimeout;
        /**
         * Idle connection timeout for the direct client. Direct client doesn't close a single connection to an
         * endpoint by default unless specified.
         */
        private Duration idleConnectionTimeout;
        /**
         * Idle endpoint timeout for the direct client. If there are no requests to a specific endpoint for idle
         * endpoint timeout duration, direct client closes all connections to that endpoint to save resources and I/O
         * cost.
         */
        private Duration idleEndpointTimeout;
        /**
         * Network request timeout interval (time to wait for response from network peer).
         */
        private Duration networkRequestTimeout;
        /**
         * Max connections per endpoint, represents the size of connection pool for a specific endpoint.
         */
        private Integer maxConnectionsPerEndpoint;
        /**
         * Max requests per connection, represents the number of requests that will be queued on a single connection
         * for a specific endpoint.
         */
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
