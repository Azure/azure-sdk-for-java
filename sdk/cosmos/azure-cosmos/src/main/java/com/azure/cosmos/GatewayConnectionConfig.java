// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

import java.net.InetSocketAddress;
import java.time.Duration;

/**
 * Represents the connection config with {@link ConnectionMode#GATEWAY} associated with Cosmos Client in the Azure Cosmos DB database service.
 */
public final class GatewayConnectionConfig {
    //  Constants
    private static final Duration DEFAULT_REQUEST_TIMEOUT = Duration.ofSeconds(60);
    private static final Duration DEFAULT_IDLE_CONNECTION_TIMEOUT = Duration.ofSeconds(60);
    private static final int DEFAULT_MAX_POOL_SIZE = 1000;

    private Duration requestTimeout;
    private int maxConnectionPoolSize;
    private Duration idleConnectionTimeout;
    private InetSocketAddress inetSocketProxyAddress;

    /**
     * Constructor.
     */
    public GatewayConnectionConfig() {
        this.idleConnectionTimeout = DEFAULT_IDLE_CONNECTION_TIMEOUT;
        this.maxConnectionPoolSize = DEFAULT_MAX_POOL_SIZE;
        this.requestTimeout = DEFAULT_REQUEST_TIMEOUT;
    }

    /**
     * Gets the default Gateway connection configuration.
     *
     * @return the default gateway connection configuration.
     */
    public static GatewayConnectionConfig getDefaultConfig() {
        return new GatewayConnectionConfig();
    }

    /**
     * Gets the request timeout (time to wait for response from network peer).
     *
     * @return the request timeout duration.
     */
    public Duration getRequestTimeout() {
        return this.requestTimeout;
    }

    /**
     * Sets the request timeout (time to wait for response from network peer).
     * The default is 60 seconds.
     *
     * @param requestTimeout the request timeout duration.
     * @return the {@link GatewayConnectionConfig}.
     */
    public GatewayConnectionConfig setRequestTimeout(Duration requestTimeout) {
        this.requestTimeout = requestTimeout;
        return this;
    }

    /**
     * Gets the value of the connection pool size the client is using.
     *
     * @return connection pool size.
     */
    public int getMaxConnectionPoolSize() {
        return this.maxConnectionPoolSize;
    }

    /**
     * Sets the value of the connection pool size, the default
     * is 1000.
     *
     * @param maxConnectionPoolSize The value of the connection pool size.
     * @return the {@link GatewayConnectionConfig}.
     */
    public GatewayConnectionConfig setMaxConnectionPoolSize(int maxConnectionPoolSize) {
        this.maxConnectionPoolSize = maxConnectionPoolSize;
        return this;
    }

    /**
     * Gets the value of the timeout for an idle connection, the default is 60
     * seconds.
     *
     * @return Idle connection timeout duration.
     */
    public Duration getIdleConnectionTimeout() {
        return this.idleConnectionTimeout;
    }

    /**
     * sets the value of the timeout for an idle connection. After that time,
     * the connection will be automatically closed.
     *
     * @param idleConnectionTimeout the duration for an idle connection.
     * @return the {@link GatewayConnectionConfig}.
     */
    public GatewayConnectionConfig setIdleConnectionTimeout(Duration idleConnectionTimeout) {
        this.idleConnectionTimeout = idleConnectionTimeout;
        return this;
    }

    /**
     * Gets the InetSocketAddress of proxy server.
     *
     * @return the value of proxyHost.
     */
    public InetSocketAddress getProxy() {
        return this.inetSocketProxyAddress;
    }

    /**
     * This will create the InetSocketAddress for proxy server,
     * all the requests to cosmoDB will route from this address.
     *
     * @param proxy The proxy server.
     * @return the {@link GatewayConnectionConfig}.
     */

    public GatewayConnectionConfig setProxy(InetSocketAddress proxy) {
        this.inetSocketProxyAddress = proxy;
        return this;
    }

    @Override
    public String toString() {
        return "GatewayConnectionConfig{" +
            "requestTimeout=" + requestTimeout +
            ", maxConnectionPoolSize=" + maxConnectionPoolSize +
            ", idleConnectionTimeout=" + idleConnectionTimeout +
            ", inetSocketProxyAddress=" + inetSocketProxyAddress +
            '}';
    }
}
