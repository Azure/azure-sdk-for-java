// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

import com.azure.core.http.ProxyOptions;

import java.time.Duration;

/**
 * Represents the connection config with {@link ConnectionMode#GATEWAY} associated with Cosmos Client in the Azure Cosmos DB database service.
 */
public final class GatewayConnectionConfig {
    //  Constants
    private static final Duration DEFAULT_REQUEST_TIMEOUT = Duration.ofSeconds(60);
    private static final Duration DEFAULT_IDLE_CONNECTION_TIMEOUT = Duration.ofSeconds(60);
    private static final int DEFAULT_MAX_CONNECTION_POOL_SIZE = 1000;

    private Duration requestTimeout;
    private int maxConnectionPoolSize;
    private Duration idleConnectionTimeout;
    private ProxyOptions proxy;

    /**
     * Constructor.
     */
    public GatewayConnectionConfig() {
        this.idleConnectionTimeout = DEFAULT_IDLE_CONNECTION_TIMEOUT;
        this.maxConnectionPoolSize = DEFAULT_MAX_CONNECTION_POOL_SIZE;
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
     * Gets the proxy options which contain the InetSocketAddress of proxy server.
     *
     * @return the proxy options.
     */
    public ProxyOptions getProxy() {
        return this.proxy;
    }

    /**
     * Sets the proxy options.
     *
     * Currently only support Http proxy type with just the routing address. Username and password will be ignored.
     *
     * @param proxy The proxy options.
     * @return the {@link GatewayConnectionConfig}.
     */

    public GatewayConnectionConfig setProxy(ProxyOptions proxy) {
        if (proxy.getType() != ProxyOptions.Type.HTTP) {
            throw new IllegalArgumentException("Only http proxy type is supported.");
        }

        this.proxy = proxy;
        return this;
    }

    @Override
    public String toString() {
        String proxyType = proxy != null ? proxy.getType().toString() : null;
        String proxyAddress = proxy != null ? proxy.getAddress().toString() : null;

        return "GatewayConnectionConfig{" +
            "requestTimeout=" + requestTimeout +
            ", maxConnectionPoolSize=" + maxConnectionPoolSize +
            ", idleConnectionTimeout=" + idleConnectionTimeout +
            ", proxyType=" + proxyType +
            ", inetSocketProxyAddress=" + proxyAddress +
            '}';
    }
}
