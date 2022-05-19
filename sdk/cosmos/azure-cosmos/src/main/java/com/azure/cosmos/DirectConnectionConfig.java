// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

import com.azure.cosmos.implementation.ImplementationBridgeHelpers;
import io.netty.channel.ChannelOption;

import java.time.Duration;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkArgument;
import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

/**
 * Represents the connection config with {@link ConnectionMode#DIRECT} associated with Cosmos Client in the Azure Cosmos DB database service.
 * For performance tips on how to optimize Direct connection configuration,
 * refer to performance tips guide:
 * <a href="https://docs.microsoft.com/en-us/azure/cosmos-db/performance-tips-java-sdk-v4-sql?tabs=api-async">Performance tips guide</a>
 */
public final class DirectConnectionConfig {
    //  Constants
    private static final Boolean DEFAULT_CONNECTION_ENDPOINT_REDISCOVERY_ENABLED = true;
    private static final Duration DEFAULT_IDLE_ENDPOINT_TIMEOUT = Duration.ofHours(1l);
    private static final Duration DEFAULT_CONNECT_TIMEOUT = Duration.ofSeconds(5L);
    private static final Duration DEFAULT_NETWORK_REQUEST_TIMEOUT = Duration.ofSeconds(5L);
    private static final Duration MIN_NETWORK_REQUEST_TIMEOUT = Duration.ofSeconds(5L);
    private static final Duration MAX_NETWORK_REQUEST_TIMEOUT = Duration.ofSeconds(10L);
    private static final int DEFAULT_MAX_CONNECTIONS_PER_ENDPOINT = 130;
    private static final int DEFAULT_MAX_REQUESTS_PER_CONNECTION = 30;
    private static final int DEFAULT_IO_THREAD_COUNT_PER_CORE_FACTOR = 2;
    private static final int DEFAULT_IO_THREAD_PRIORITY = Thread.NORM_PRIORITY;

    private boolean connectionEndpointRediscoveryEnabled;
    private Duration connectTimeout;
    private Duration idleConnectionTimeout;
    private Duration idleEndpointTimeout;
    private Duration networkRequestTimeout;
    private int maxConnectionsPerEndpoint;
    private int maxRequestsPerConnection;
    private int ioThreadCountPerCoreFactor;
    private int ioThreadPriority;

    /**
     * Constructor
     */
    public DirectConnectionConfig() {
        this.connectionEndpointRediscoveryEnabled = DEFAULT_CONNECTION_ENDPOINT_REDISCOVERY_ENABLED;
        this.connectTimeout = DEFAULT_CONNECT_TIMEOUT;
        this.idleConnectionTimeout = Duration.ZERO;
        this.idleEndpointTimeout = DEFAULT_IDLE_ENDPOINT_TIMEOUT;
        this.maxConnectionsPerEndpoint = DEFAULT_MAX_CONNECTIONS_PER_ENDPOINT;
        this.maxRequestsPerConnection = DEFAULT_MAX_REQUESTS_PER_CONNECTION;
        this.networkRequestTimeout = DEFAULT_NETWORK_REQUEST_TIMEOUT;
        this.ioThreadCountPerCoreFactor = DEFAULT_IO_THREAD_COUNT_PER_CORE_FACTOR;
        this.ioThreadPriority = DEFAULT_IO_THREAD_PRIORITY;
    }

    /**
     * Gets a value indicating whether Direct TCP connection endpoint rediscovery is enabled.
     * <p>
     * The connection endpoint rediscovery feature is designed to reduce and spread-out latency spikes that may occur during maintenance operations.
     *
     * By default, connection endpoint rediscovery is disabled.
     *
     * @return {@code true} if Direct TCP connection endpoint rediscovery is enabled; {@code false} otherwise.
     */
    public boolean isConnectionEndpointRediscoveryEnabled() {
        return this.connectionEndpointRediscoveryEnabled;
    }

    /**
     * Sets a value indicating whether Direct TCP connection endpoint rediscovery should be enabled.
     * <p>
     * The connection endpoint rediscovery feature is designed to reduce and spread-out latency spikes that may occur during maintenance operations.
     *
     * By default, connection endpoint rediscovery is disabled.
     *
     * @param connectionEndpointRediscoveryEnabled {@code true} if connection endpoint rediscovery is enabled; {@code
     *                                             false} otherwise.
     *
     * @return the {@linkplain DirectConnectionConfig}.
     */
    public DirectConnectionConfig setConnectionEndpointRediscoveryEnabled(boolean connectionEndpointRediscoveryEnabled) {
        this.connectionEndpointRediscoveryEnabled = connectionEndpointRediscoveryEnabled;
        return this;
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
     * By default, the connect timeout is 5 seconds.
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
     * By default, the connect timeout is 5 seconds.
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
     * Default value is 1 hour.
     * If set to {@link Duration#ZERO}, idle endpoint check will be disabled.
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
     * Default value is 1 hour.
     * If set to {@link Duration#ZERO}, idle endpoint check will be disabled.
     *
     * If there are no requests to a specific endpoint for idle endpoint timeout duration,
     * direct client closes all connections to that endpoint to save resources and I/O cost.
     *
     * @param idleEndpointTimeout the idle endpoint timeout
     * @return the {@link DirectConnectionConfig}
     */
    public DirectConnectionConfig setIdleEndpointTimeout(Duration idleEndpointTimeout) {
        checkArgument(!idleEndpointTimeout.isNegative(), "IdleEndpointTimeout cannot be less than 0");

        this.idleEndpointTimeout = idleEndpointTimeout;
        return this;
    }

    /**
     * Gets the max connections per endpoint
     * This represents the size of connection pool for a specific endpoint
     *
     * Default value is 130
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
     * Default value is 130
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
     * Default value is 30
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
     * Default value is 30
     *
     * @param maxRequestsPerConnection the max requests per endpoint
     * @return the {@link DirectConnectionConfig}
     */
    public DirectConnectionConfig setMaxRequestsPerConnection(int maxRequestsPerConnection) {
        this.maxRequestsPerConnection = maxRequestsPerConnection;
        return this;
    }

    /**
     * Gets the network request timeout interval (time to wait for response from network peer).
     *
     * Default value is 5 seconds
     *
     * @return the network request timeout interval
     */
    public Duration getNetworkRequestTimeout() {
        return networkRequestTimeout;
    }

    /**
     * Sets the network request timeout interval (time to wait for response from network peer).
     *
     * Default value is 5 seconds.
     * It only allows values &ge;5s and &le;10s. (backend allows requests to take up-to 5 seconds processing time - 5 seconds
     * buffer so 10 seconds in total for transport is more than sufficient).
     *
     * Attention! Please adjust this value with caution.
     * This config represents the max time allowed to wait for and consume a service response after the request has been written to the network connection.
     * Setting a value too low can result in having not enough time to wait for the service response - which could cause too aggressive retries and degrade performance.
     * Setting a value too high can result in fewer retries and reduce chances of success by retries.
     *
     * @param networkRequestTimeout the network request timeout interval.
     * @return the {@link DirectConnectionConfig}
     */
    public DirectConnectionConfig setNetworkRequestTimeout(Duration networkRequestTimeout) {
        checkNotNull(networkRequestTimeout, "NetworkRequestTimeout can not be null");
        checkArgument(networkRequestTimeout.toMillis() >= MIN_NETWORK_REQUEST_TIMEOUT.toMillis(),
            "NetworkRequestTimeout can not be less than %s Millis", MIN_NETWORK_REQUEST_TIMEOUT.toMillis());
        checkArgument(networkRequestTimeout.toMillis() <= MAX_NETWORK_REQUEST_TIMEOUT.toMillis(),
            "NetworkRequestTimeout can not be larger than %s Millis", MAX_NETWORK_REQUEST_TIMEOUT.toMillis());

        this.networkRequestTimeout = networkRequestTimeout;
        return this;
    }

    int getIoThreadCountPerCoreFactor() {
        return ioThreadCountPerCoreFactor;
    }

    DirectConnectionConfig setIoThreadCountPerCoreFactor(int ioThreadCountPerCoreFactor) {
        this.ioThreadCountPerCoreFactor = ioThreadCountPerCoreFactor;
        return this;
    }

    int getIoThreadPriority() {
        return ioThreadPriority;
    }

    DirectConnectionConfig setIoThreadPriority(int ioThreadPriority) {
        this.ioThreadPriority = ioThreadPriority;
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
            ", networkRequestTimeout=" + networkRequestTimeout +
            ", ioThreadCountPerCoreFactor=" + ioThreadCountPerCoreFactor +
            ", ioThreadPriority=" + ioThreadPriority +
            '}';
    }

    ///////////////////////////////////////////////////////////////////////////////////////////
    // the following helper/accessor only helps to access this class outside of this package.//
    ///////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Should not be called form user-code. This method is a no-op and is just used internally
     * to force loading this class
     */
    public static void doNothingButEnsureLoadingClass() {}

    static {
        ImplementationBridgeHelpers.DirectConnectionConfigHelper.setDirectConnectionConfigAccessor(
            new ImplementationBridgeHelpers.DirectConnectionConfigHelper.DirectConnectionConfigAccessor() {
                @Override
                public int getIoThreadCountPerCoreFactor(DirectConnectionConfig config) {
                    return config.getIoThreadCountPerCoreFactor();
                }

                @Override
                public DirectConnectionConfig setIoThreadCountPerCoreFactor(DirectConnectionConfig config,
                                                                            int ioThreadCountPerCoreFactor) {
                    return config.setIoThreadCountPerCoreFactor(ioThreadCountPerCoreFactor);
                }

                @Override
                public int getIoThreadPriority(DirectConnectionConfig config) {
                    return config.getIoThreadPriority();
                }

                @Override
                public DirectConnectionConfig setIoThreadPriority(DirectConnectionConfig config,
                                                                  int ioThreadPriority) {
                    return config.setIoThreadPriority(ioThreadPriority);
                }
            });
    }
}
