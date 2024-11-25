// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

import com.azure.core.http.ProxyOptions;
import com.azure.cosmos.implementation.Configs;
import com.azure.cosmos.util.Beta;

import java.time.Duration;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkArgument;
import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

/**
 * Represents the connection config with {@link ConnectionMode#GATEWAY} associated with Cosmos Client in the Azure Cosmos DB database service.
 */
public final class GatewayConnectionConfig {
    //  Constants
    private static final Duration MIN_NETWORK_REQUEST_TIMEOUT = Duration.ofSeconds(60);
    private static final Duration DEFAULT_NETWORK_REQUEST_TIMEOUT = Duration.ofSeconds(60);
    private static final Duration DEFAULT_IDLE_CONNECTION_TIMEOUT = Duration.ofSeconds(60);

    private Duration networkRequestTimeout;
    private int maxConnectionPoolSize;
    private Duration idleConnectionTimeout;
    private ProxyOptions proxy;
    private Http2ConnectionConfig http2ConnectionConfig;

    /**
     * Constructor.
     */
    public GatewayConnectionConfig() {
        this.idleConnectionTimeout = DEFAULT_IDLE_CONNECTION_TIMEOUT;
        this.maxConnectionPoolSize = Configs.getDefaultHttpPoolSize();
        this.networkRequestTimeout = DEFAULT_NETWORK_REQUEST_TIMEOUT;
        this.http2ConnectionConfig = new Http2ConnectionConfig();
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
     * Gets the network request timeout interval (time to wait for response from network peer).
     * The default is 60 seconds.
     *
     * @return the network request timeout duration.
     */
    Duration getNetworkRequestTimeout() {
        return this.networkRequestTimeout;
    }

    /**
     * Sets the network request timeout interval (time to wait for response from network peer).
     * The default is 60 seconds.
     *
     * @param networkRequestTimeout the network request timeout duration.
     * @return the {@link GatewayConnectionConfig}.
     */
    GatewayConnectionConfig setNetworkRequestTimeout(Duration networkRequestTimeout) {
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

    /***
     * Get the http2 connection config.
     * @return the {@link Http2ConnectionConfig}.
     */
    @Beta(value = Beta.SinceVersion.V4_66_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    public Http2ConnectionConfig getHttp2ConnectionConfig() {
        return http2ConnectionConfig;
    }

    /***
     * Set the http2 connection config.
     * @param http2ConnectionConfig the {@link Http2ConnectionConfig}.
     */
    @Beta(value = Beta.SinceVersion.V4_66_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    public GatewayConnectionConfig setHttp2ConnectionConfig(Http2ConnectionConfig http2ConnectionConfig) {
        this.http2ConnectionConfig = http2ConnectionConfig;
        return this;
    }

    @Override
    public String toString() {
        String proxyType = proxy != null ? proxy.getType().toString() : null;
        String proxyAddress = proxy != null ? proxy.getAddress().toString() : null;

        return "GatewayConnectionConfig{" +
            ", maxConnectionPoolSize=" + maxConnectionPoolSize +
            ", idleConnectionTimeout=" + idleConnectionTimeout +
            ", networkRequestTimeout=" + networkRequestTimeout +
            ", proxyType=" + proxyType +
            ", inetSocketProxyAddress=" + proxyAddress +
            ", http2ConnectionConfig=" + http2ConnectionConfig.toString() +
            '}';
    }
}
