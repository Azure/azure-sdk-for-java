// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

import java.net.InetSocketAddress;
import java.time.Duration;
import java.util.List;

import static com.azure.cosmos.implementation.ConnectionPolicy.DEFAULT_IDLE_CONNECTION_TIMEOUT;
import static com.azure.cosmos.implementation.ConnectionPolicy.DEFAULT_MAX_POOL_SIZE;
import static com.azure.cosmos.implementation.ConnectionPolicy.DEFAULT_REQUEST_TIMEOUT;

/**
 * Represents the connection config with {@link ConnectionMode#GATEWAY} associated with Cosmos Client in the Azure Cosmos DB database service.
 */
public final class GatewayConnectionConfig extends ConnectionConfig {

    private static final GatewayConnectionConfig defaultConfig = new GatewayConnectionConfig();

    private Duration requestTimeout;
    private int maxPoolSize;
    private Duration idleConnectionTimeout;
    private InetSocketAddress inetSocketProxyAddress;

    /**
     * Constructor.
     */
    public GatewayConnectionConfig() {
        super(ConnectionMode.GATEWAY);
        this.idleConnectionTimeout = DEFAULT_IDLE_CONNECTION_TIMEOUT;
        this.maxPoolSize = DEFAULT_MAX_POOL_SIZE;
        this.requestTimeout = DEFAULT_REQUEST_TIMEOUT;
    }

    /**
     * Gets the default Gateway connection configuration.
     *
     * @return the default gateway connection configuration.
     */
    public static GatewayConnectionConfig getDefaultConfig() {
        return GatewayConnectionConfig.defaultConfig;
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
    public int getMaxPoolSize() {
        return this.maxPoolSize;
    }

    /**
     * Sets the value of the connection pool size, the default
     * is 1000.
     *
     * @param maxPoolSize The value of the connection pool size.
     * @return the {@link GatewayConnectionConfig}.
     */
    public GatewayConnectionConfig setMaxPoolSize(int maxPoolSize) {
        this.maxPoolSize = maxPoolSize;
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
     * Sets the retry policy options associated with the DocumentClient instance.
     * <p>
     * Properties in the RetryOptions class allow application to customize the built-in
     * retry policies. This property is optional. When it's not set, the SDK uses the
     * default values for configuring the retry policies.  See RetryOptions class for
     * more details.
     *
     * @param throttlingRetryOptions the RetryOptions instance.
     * @return the {@link GatewayConnectionConfig}.
     * @throws IllegalArgumentException thrown if an error occurs
     */
    @Override
    public GatewayConnectionConfig setThrottlingRetryOptions(ThrottlingRetryOptions throttlingRetryOptions) {
        super.setThrottlingRetryOptions(throttlingRetryOptions);
        return this;
    }

    /**
     * Sets the flag to enable endpoint discovery for geo-replicated database accounts.
     * <p>
     * When EnableEndpointDiscovery is true, the SDK will automatically discover the
     * current write and read regions to ensure requests are sent to the correct region
     * based on the capability of the region and the user's preference.
     * <p>
     * The default value for this property is true indicating endpoint discovery is enabled.
     *
     * @param endpointDiscoveryEnabled true if EndpointDiscovery is enabled.
     * @return the {@link GatewayConnectionConfig}.
     */
    @Override
    public GatewayConnectionConfig setEndpointDiscoveryEnabled(boolean endpointDiscoveryEnabled) {
        super.setEndpointDiscoveryEnabled(endpointDiscoveryEnabled);
        return this;
    }

    /**
     * Sets the flag to enable writes on any regions for geo-replicated database accounts in the Azure
     * Cosmos DB service.
     * <p>
     * When the value of this property is true, the SDK will direct write operations to
     * available writable regions of geo-replicated database account. Writable regions
     * are ordered by PreferredRegions property. Setting the property value
     * to true has no effect until EnableMultipleWriteRegions in DatabaseAccount
     * is also set to true.
     * <p>
     * DEFAULT value is false indicating that writes are only directed to
     * first region in PreferredRegions property.
     *
     * @param usingMultipleWriteRegions flag to enable writes on any regions for geo-replicated
     * database accounts.
     * @return the {@link GatewayConnectionConfig}.
     */
    @Override
    public GatewayConnectionConfig setUsingMultipleWriteRegions(boolean usingMultipleWriteRegions) {
        this.usingMultipleWriteRegions = usingMultipleWriteRegions;
        return this;
    }

    /**
     * Sets whether to allow for reads to go to multiple regions configured on an account of Azure Cosmos DB service.
     * <p>
     * DEFAULT value is null.
     * <p>
     * If this property is not set, the default is true for all Consistency Levels other than Bounded Staleness,
     * The default is false for Bounded Staleness.
     * 1. {@link #endpointDiscoveryEnabled} is true
     * 2. the Azure Cosmos DB account has more than one region
     *
     * @param readRequestsFallbackEnabled flag to enable reads to go to multiple regions configured on an account of
     * Azure Cosmos DB service.
     * @return the {@link GatewayConnectionConfig}.
     */
    @Override
    public GatewayConnectionConfig setReadRequestsFallbackEnabled(Boolean readRequestsFallbackEnabled) {
        this.readRequestsFallbackEnabled = readRequestsFallbackEnabled;
        return this;
    }

    /**
     * Sets the preferred regions for geo-replicated database accounts. For example,
     * "East US" as the preferred region.
     * <p>
     * When EnableEndpointDiscovery is true and PreferredRegions is non-empty,
     * the SDK will prefer to use the regions in the collection in the order
     * they are specified to perform operations.
     * <p>
     * If EnableEndpointDiscovery is set to false, this property is ignored.
     *
     * @param preferredRegions the list of preferred regions.
     * @return the {@link GatewayConnectionConfig}.
     */
    @Override
    public GatewayConnectionConfig setPreferredRegions(List<String> preferredRegions) {
        super.setPreferredRegions(preferredRegions);
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

    /**
     * sets the value of the user-agent suffix.
     *
     * @param userAgentSuffix The value to be appended to the user-agent header, this is
     * used for monitoring purposes.
     *
     * @return the {@link ConnectionConfig}
     */
    @Override
    ConnectionConfig setUserAgentSuffix(String userAgentSuffix) {
        super.setUserAgentSuffix(userAgentSuffix);
        return this;
    }

    @Override
    public String toString() {
        return "GatewayConnectionConfig{" +
            "requestTimeout=" + requestTimeout +
            ", maxPoolSize=" + maxPoolSize +
            ", idleConnectionTimeout=" + idleConnectionTimeout +
            ", inetSocketProxyAddress=" + inetSocketProxyAddress +
            ", connectionMode=" + connectionMode +
            ", userAgentSuffix='" + userAgentSuffix + '\'' +
            ", throttlingRetryOptions=" + throttlingRetryOptions +
            ", preferredRegions=" + preferredRegions +
            ", endpointDiscoveryEnabled=" + endpointDiscoveryEnabled +
            ", usingMultipleWriteRegions=" + usingMultipleWriteRegions +
            ", readRequestsFallbackEnabled=" + readRequestsFallbackEnabled +
            '}';
    }
}
