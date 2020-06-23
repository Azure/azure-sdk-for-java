// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

import io.netty.channel.ChannelOption;

import java.time.Duration;

/**
 * Represents the connection config with {@link ConnectionMode#DIRECT} associated with Cosmos Client in the Azure Cosmos DB database service.
 * For performance tips on how to optimize Direct connection configuration,
 * refer to performance tips guide:
 * <a href="https://docs.microsoft.com/en-us/azure/cosmos-db/performance-tips-java-sdk-v4-sql?tabs=api-async">Performance tips guide</a>
 */
public final class DirectConnectionConfig {
    //  Constants
    private static final Duration DEFAULT_IDLE_ENDPOINT_TIMEOUT = Duration.ofSeconds(70L);
    private static final Duration DEFAULT_CONNECT_TIMEOUT = Duration.ofSeconds(60L);
    private static final Duration DEFAULT_REQUEST_TIMEOUT = Duration.ofSeconds(5L);
    private static final int DEFAULT_MAX_CONNECTIONS_PER_ENDPOINT = 30;
    private static final int DEFAULT_MAX_REQUESTS_PER_CONNECTION = 10;

    private Duration connectTimeout;
    private Duration idleConnectionTimeout;
    private Duration idleEndpointTimeout;
    private Duration requestTimeout;
    private int maxConnectionsPerEndpoint;
    private int maxRequestsPerConnection;

    /**
     * Constructor.
     */
    public DirectConnectionConfig() {
        this.connectTimeout = DEFAULT_CONNECT_TIMEOUT;
        this.idleConnectionTimeout = Duration.ZERO;
        this.idleEndpointTimeout = DEFAULT_IDLE_ENDPOINT_TIMEOUT;
        this.maxConnectionsPerEndpoint = DEFAULT_MAX_CONNECTIONS_PER_ENDPOINT;
        this.maxRequestsPerConnection = DEFAULT_MAX_REQUESTS_PER_CONNECTION;
        this.requestTimeout = DEFAULT_REQUEST_TIMEOUT;
    }

    /**
     * Gets the default DIRECT connection configuration.
     *
     * @return the default direct connection configuration.
     */
    public static DirectConnectionConfig getDefaultConfig() {
        return new DirectConnectionConfig();
    }

    /**
     * Gets the connect timeout for direct client,
     * represents timeout for establishing connections with an endpoint.
     *
     * Configures timeout for underlying Netty Channel {@link ChannelOption#CONNECT_TIMEOUT_MILLIS}
     *
     * By default, the connect timeout is 60 seconds.
     *
     * @return direct connect timeout
     */
    public Duration getConnectTimeout() {
        return connectTimeout;
    }

    /**
     * Sets the connect timeout for direct client,
     * represents timeout for establishing connections with an endpoint.
     *
     * Configures timeout for underlying Netty Channel {@link ChannelOption#CONNECT_TIMEOUT_MILLIS}
     *
     * By default, the connect timeout is 60 seconds.
     *
     * @param connectTimeout the connection timeout
     * @return the {@link DirectConnectionConfig}
     */
    public DirectConnectionConfig setConnectTimeout(Duration connectTimeout) {
        this.connectTimeout = connectTimeout;
        return this;
    }

    /**
     * Gets the idle connection timeout for direct client
     *
     * Default value is {@link Duration#ZERO}
     *
     * Direct client doesn't close a single connection to an endpoint
     * by default unless specified.
     *
     * @return idle connection timeout
     */
    public Duration getIdleConnectionTimeout() {
        return idleConnectionTimeout;
    }

    /**
     * Sets the idle connection timeout
     *
     * Default value is {@link Duration#ZERO}
     *
     * Direct client doesn't close a single connection to an endpoint
     * by default unless specified.
     *
     * @param idleConnectionTimeout idle connection timeout
     * @return the {@link DirectConnectionConfig}
     */
    public DirectConnectionConfig setIdleConnectionTimeout(Duration idleConnectionTimeout) {
        this.idleConnectionTimeout = idleConnectionTimeout;
        return this;
    }

    /**
     * Gets the idle endpoint timeout
     *
     * Default value is 70 seconds.
     *
     * If there are no requests to a specific endpoint for idle endpoint timeout duration,
     * direct client closes all connections to that endpoint to save resources and I/O cost.
     *
     * @return the idle endpoint timeout
     */
    public Duration getIdleEndpointTimeout() {
        return idleEndpointTimeout;
    }

    /**
     * Sets the idle endpoint timeout
     *
     * Default value is 70 seconds.
     *
     * If there are no requests to a specific endpoint for idle endpoint timeout duration,
     * direct client closes all connections to that endpoint to save resources and I/O cost.
     *
     * @param idleEndpointTimeout the idle endpoint timeout
     * @return the {@link DirectConnectionConfig}
     */
    public DirectConnectionConfig setIdleEndpointTimeout(Duration idleEndpointTimeout) {
        this.idleEndpointTimeout = idleEndpointTimeout;
        return this;
    }

    /**
     * Gets the max connections per endpoint
     * This represents the size of connection pool for a specific endpoint
     *
     * Default value is 30
     *
     * @return the max connections per endpoint
     */
    public int getMaxConnectionsPerEndpoint() {
        return maxConnectionsPerEndpoint;
    }

    /**
     * Sets the max connections per endpoint
     * This represents the size of connection pool for a specific endpoint
     *
     * Default value is 30
     *
     * @param maxConnectionsPerEndpoint the max connections per endpoint
     * @return the {@link DirectConnectionConfig}
     */
    public DirectConnectionConfig setMaxConnectionsPerEndpoint(int maxConnectionsPerEndpoint) {
        this.maxConnectionsPerEndpoint = maxConnectionsPerEndpoint;
        return this;
    }

    /**
     * Gets the max requests per connection
     * This represents the number of requests that will be queued
     * on a single connection for a specific endpoint
     *
     * Default value is 10
     *
     * @return the max requests per endpoint
     */
    public int getMaxRequestsPerConnection() {
        return maxRequestsPerConnection;
    }

    /**
     * Sets the max requests per connection
     * This represents the number of requests that will be queued
     * on a single connection for a specific endpoint
     *
     * Default value is 10
     *
     * @param maxRequestsPerConnection the max requests per endpoint
     * @return the {@link DirectConnectionConfig}
     */
    public DirectConnectionConfig setMaxRequestsPerConnection(int maxRequestsPerConnection) {
        this.maxRequestsPerConnection = maxRequestsPerConnection;
        return this;
    }

    /**
     * Gets the request timeout interval
     * This represents the timeout interval for requests
     *
     * Default value is 60 seconds
     *
     * @return the request timeout interval
     */
    Duration getRequestTimeout() {
        return requestTimeout;
    }

    /**
     * Sets the request timeout interval
     * This represents the timeout interval for requests
     *
     * Default value is 5 seconds
     *
     * @param requestTimeout the request timeout interval
     * @return the {@link DirectConnectionConfig}
     */
    DirectConnectionConfig setRequestTimeout(Duration requestTimeout) {
        this.requestTimeout = requestTimeout;
        return this;
    }

    @Override
    public String toString() {
        return "DirectConnectionConfig{" +
            "connectTimeout=" + connectTimeout +
            ", idleConnectionTimeout=" + idleConnectionTimeout +
            ", idleEndpointTimeout=" + idleEndpointTimeout +
            ", maxConnectionsPerEndpoint=" + maxConnectionsPerEndpoint +
            ", maxRequestsPerConnection=" + maxRequestsPerConnection +
            '}';
    }
}
