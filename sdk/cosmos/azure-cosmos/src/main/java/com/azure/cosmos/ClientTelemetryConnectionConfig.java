// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

import com.azure.core.http.ProxyOptions;

import java.time.Duration;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkArgument;
import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

/**
 * Represents the client telemetry config.
 */
public class ClientTelemetryConnectionConfig {
    private static final Duration MIN_NETWORK_REQUEST_TIMEOUT = Duration.ofSeconds(60);
    private static final Duration DEFAULT_NETWORK_REQUEST_TIMEOUT = Duration.ofSeconds(60);
    private static final Duration DEFAULT_IDLE_CONNECTION_TIMEOUT = Duration.ofSeconds(60);
    private static final int DEFAULT_MAX_CONNECTION_POOL_SIZE = 1000;

    private Duration networkRequestTimeout;
    private int maxConnectionPoolSize;
    private Duration idleConnectionTimeout;
    private ProxyOptions proxy;

    public ClientTelemetryConnectionConfig() {
        this.idleConnectionTimeout = DEFAULT_IDLE_CONNECTION_TIMEOUT;
        this.maxConnectionPoolSize = DEFAULT_MAX_CONNECTION_POOL_SIZE;
        this.networkRequestTimeout = DEFAULT_NETWORK_REQUEST_TIMEOUT;
    }

    public static ClientTelemetryConnectionConfig getDefaultConfig() {
        return new ClientTelemetryConnectionConfig();
    }

    /**
     * Gets the network request timeout interval (time to wait for response from network peer).
     * The default is 60 seconds.
     *
     * @return the network request timeout duration.
     */
    public Duration getNetworkRequestTimeout() {
        return this.networkRequestTimeout;
    }

    /**
     * Sets the network request timeout interval (time to wait for response from network peer).
     * The default is 60 seconds.
     *
     * @param networkRequestTimeout the network request timeout duration.
     * @return the {@link ClientTelemetryConnectionConfig}.
     */
    ClientTelemetryConnectionConfig setNetworkRequestTimeout(Duration networkRequestTimeout) {
        checkNotNull(networkRequestTimeout, "NetworkRequestTimeout can not be null");
        checkArgument(networkRequestTimeout.toMillis() >= MIN_NETWORK_REQUEST_TIMEOUT.toMillis(),
                "NetworkRequestTimeout can not be less than %s millis", MIN_NETWORK_REQUEST_TIMEOUT.toMillis());
        this.networkRequestTimeout = networkRequestTimeout;
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
     * @return the {@link ClientTelemetryConnectionConfig}.
     */
    ClientTelemetryConnectionConfig setMaxConnectionPoolSize(int maxConnectionPoolSize) {
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
     * @return the {@link ClientTelemetryConnectionConfig}.
     */
    ClientTelemetryConnectionConfig setIdleConnectionTimeout(Duration idleConnectionTimeout) {
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
     * Currently, only support Http proxy type with just the routing address. Username and password will be ignored.
     *
     * @param proxy The proxy options.
     * @return the {@link ClientTelemetryConnectionConfig}.
     */

    public ClientTelemetryConnectionConfig setProxy(ProxyOptions proxy) {
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

        return "ClientTelemetryConfig{" +
                ", maxConnectionPoolSize=" + maxConnectionPoolSize +
                ", idleConnectionTimeout=" + idleConnectionTimeout +
                ", networkRequestTimeout=" + networkRequestTimeout +
                ", proxyType=" + proxyType +
                ", inetSocketProxyAddress=" + proxyAddress +
                '}';
    }
}
