// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation;

import com.azure.cosmos.ConnectionMode;
import com.azure.cosmos.DirectConnectionConfig;
import com.azure.cosmos.GatewayConnectionConfig;
import com.azure.cosmos.ThrottlingRetryOptions;

import java.net.InetSocketAddress;
import java.time.Duration;
import java.util.Collections;
import java.util.List;

/**
 * Represents the Connection policy associated with a DocumentClient in the Azure Cosmos DB database service.
 */
public final class ConnectionPolicy {

    //  Constants
    public static final Duration DEFAULT_REQUEST_TIMEOUT = Duration.ofSeconds(60);
    public static final Duration DEFAULT_IDLE_CONNECTION_TIMEOUT = Duration.ofSeconds(60);
    public static final int DEFAULT_MAX_POOL_SIZE = 1000;

    private static final ConnectionPolicy defaultPolicy = new ConnectionPolicy(DirectConnectionConfig.getDefaultConfig());

    private ConnectionMode connectionMode;
    private String userAgentSuffix;
    private ThrottlingRetryOptions throttlingRetryOptions;
    private boolean endpointDiscoveryEnabled;
    private List<String> preferredRegions;
    private boolean multipleWriteRegionsEnabled;
    private boolean readRequestsFallbackEnabled;

    //  Gateway connection config properties
    private int maxConnectionPoolSize = DEFAULT_MAX_POOL_SIZE;
    private Duration requestTimeout = DEFAULT_REQUEST_TIMEOUT;
    private Duration idleConnectionTimeout = DEFAULT_IDLE_CONNECTION_TIMEOUT;
    private InetSocketAddress inetSocketProxyAddress;

    //  Direct connection config properties
    private Duration connectionTimeout;
    private Duration idleChannelTimeout;
    private Duration idleEndpointTimeout;
    private int maxChannelsPerEndpoint;
    private int maxRequestsPerChannel;

    /**
     * Constructor.
     */
    public ConnectionPolicy(GatewayConnectionConfig gatewayConnectionConfig) {
        this(ConnectionMode.GATEWAY);
        this.idleConnectionTimeout = gatewayConnectionConfig.getIdleConnectionTimeout();
        this.maxConnectionPoolSize = gatewayConnectionConfig.getMaxConnectionPoolSize();
        this.requestTimeout = gatewayConnectionConfig.getRequestTimeout();
        this.inetSocketProxyAddress = gatewayConnectionConfig.getProxy();
    }

    public ConnectionPolicy(DirectConnectionConfig directConnectionConfig) {
        this(ConnectionMode.DIRECT);
        this.connectionTimeout = directConnectionConfig.getConnectionTimeout();
        this.idleChannelTimeout = directConnectionConfig.getIdleChannelTimeout();
        this.idleEndpointTimeout = directConnectionConfig.getIdleEndpointTimeout();
        this.maxChannelsPerEndpoint = directConnectionConfig.getMaxChannelsPerEndpoint();
        this.maxRequestsPerChannel = directConnectionConfig.getMaxRequestsPerChannel();
    }

    private ConnectionPolicy(ConnectionMode connectionMode) {
        this.connectionMode = connectionMode;
        //  Default values
        this.throttlingRetryOptions = new ThrottlingRetryOptions();
        this.userAgentSuffix = "";
        this.readRequestsFallbackEnabled = true;
        this.endpointDiscoveryEnabled = true;
        this.multipleWriteRegionsEnabled = true;
    }

    /**
     * Gets the default connection policy.
     *
     * @return the default connection policy.
     */
    public static ConnectionPolicy getDefaultPolicy() {
        return ConnectionPolicy.defaultPolicy;
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
     * @return the ConnectionPolicy.
     */
    public ConnectionPolicy setRequestTimeout(Duration requestTimeout) {
        this.requestTimeout = requestTimeout;
        return this;
    }

    /**
     * Gets the connection mode used in the client.
     *
     * @return the connection mode.
     */
    public ConnectionMode getConnectionMode() {
        return this.connectionMode;
    }

    /**
     * Sets the connection mode used in the client.
     *
     * @param connectionMode the connection mode.
     * @return the ConnectionPolicy.
     */
    public ConnectionPolicy setConnectionMode(ConnectionMode connectionMode) {
        this.connectionMode = connectionMode;
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
     * @return the ConnectionPolicy.
     */
    public ConnectionPolicy setMaxConnectionPoolSize(int maxConnectionPoolSize) {
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
     * @return the ConnectionPolicy.
     */
    public ConnectionPolicy setIdleConnectionTimeout(Duration idleConnectionTimeout) {
        this.idleConnectionTimeout = idleConnectionTimeout;
        return this;
    }

    /**
     * Gets the value of user-agent suffix.
     *
     * @return the value of user-agent suffix.
     */
    public String getUserAgentSuffix() {
        return this.userAgentSuffix;
    }

    /**
     * sets the value of the user-agent suffix.
     *
     * @param userAgentSuffix The value to be appended to the user-agent header, this is
     * used for monitoring purposes.
     * @return the ConnectionPolicy.
     */
    public ConnectionPolicy setUserAgentSuffix(String userAgentSuffix) {
        this.userAgentSuffix = userAgentSuffix;
        return this;
    }

    /**
     * Gets the retry policy options associated with the DocumentClient instance.
     *
     * @return the RetryOptions instance.
     */
    public ThrottlingRetryOptions getThrottlingRetryOptions() {
        return this.throttlingRetryOptions;
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
     * @return the ConnectionPolicy.
     * @throws IllegalArgumentException thrown if an error occurs
     */
    public ConnectionPolicy setThrottlingRetryOptions(ThrottlingRetryOptions throttlingRetryOptions) {
        if (throttlingRetryOptions == null) {
            throw new IllegalArgumentException("retryOptions value must not be null.");
        }

        this.throttlingRetryOptions = throttlingRetryOptions;
        return this;
    }

    /**
     * Gets the flag to enable endpoint discovery for geo-replicated database accounts.
     *
     * @return whether endpoint discovery is enabled.
     */
    public boolean isEndpointDiscoveryEnabled() {
        return this.endpointDiscoveryEnabled;
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
     * @return the ConnectionPolicy.
     */
    public ConnectionPolicy setEndpointDiscoveryEnabled(boolean endpointDiscoveryEnabled) {
        this.endpointDiscoveryEnabled = endpointDiscoveryEnabled;
        return this;
    }

    /**
     * Gets the flag to enable writes on any regions for geo-replicated database accounts in the Azure
     * Cosmos DB service.
     * <p>
     * When the value of this property is true, the SDK will direct write operations to
     * available writable regions of geo-replicated database account. Writable regions
     * are ordered by PreferredRegions property. Setting the property value
     * to true has no effect until EnableMultipleWriteRegions in DatabaseAccount
     * is also set to true.
     * <p>
     * DEFAULT value is true indicating that writes are directed to
     * available writable regions of geo-replicated database account.
     *
     * @return flag to enable writes on any regions for geo-replicated database accounts.
     */
    public boolean isMultipleWriteRegionsEnabled() {
        return this.multipleWriteRegionsEnabled;
    }

    /**
     * Gets whether to allow for reads to go to multiple regions configured on an account of Azure Cosmos DB service.
     * <p>
     * DEFAULT value is true.
     * <p>
     * If this property is not set, the default is true for all Consistency Levels other than Bounded Staleness,
     * The default is false for Bounded Staleness.
     * 1. {@link #endpointDiscoveryEnabled} is true
     * 2. the Azure Cosmos DB account has more than one region
     *
     * @return flag to allow for reads to go to multiple regions configured on an account of Azure Cosmos DB service.
     */
    public boolean isReadRequestsFallbackEnabled() {
        return this.readRequestsFallbackEnabled;
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
     * @param multipleWriteRegionsEnabled flag to enable writes on any regions for geo-replicated
     * database accounts.
     * @return the ConnectionPolicy.
     */
    public ConnectionPolicy setMultipleWriteRegionsEnabled(boolean multipleWriteRegionsEnabled) {
        this.multipleWriteRegionsEnabled = multipleWriteRegionsEnabled;
        return this;
    }

    /**
     * Sets whether to allow for reads to go to multiple regions configured on an account of Azure Cosmos DB service.
     * <p>
     * DEFAULT value is true.
     * <p>
     * If this property is not set, the default is true for all Consistency Levels other than Bounded Staleness,
     * The default is false for Bounded Staleness.
     * 1. {@link #endpointDiscoveryEnabled} is true
     * 2. the Azure Cosmos DB account has more than one region
     *
     * @param readRequestsFallbackEnabled flag to enable reads to go to multiple regions configured on an account of
     * Azure Cosmos DB service.
     * @return the ConnectionPolicy.
     */
    public ConnectionPolicy setReadRequestsFallbackEnabled(boolean readRequestsFallbackEnabled) {
        this.readRequestsFallbackEnabled = readRequestsFallbackEnabled;
        return this;
    }

    /**
     * Gets the preferred regions for geo-replicated database accounts
     *
     * @return the list of preferred region.
     */
    public List<String> getPreferredRegions() {
        return this.preferredRegions != null ? this.preferredRegions : Collections.emptyList();
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
     * @return the ConnectionPolicy.
     */
    public ConnectionPolicy setPreferredRegions(List<String> preferredRegions) {
        this.preferredRegions = preferredRegions;
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
     * @return the ConnectionPolicy.
     */

    public ConnectionPolicy setProxy(InetSocketAddress proxy) {
        this.inetSocketProxyAddress = proxy;
        return this;
    }

    /**
     * Gets the direct connection timeout
     * @return direct connection timeout
     */
    public Duration getConnectionTimeout() {
        return connectionTimeout;
    }

    /**
     *  Sets the direct connection timeout
     * @param connectionTimeout the connection timeout
     * @return the {@link ConnectionPolicy}
     */
    public ConnectionPolicy setConnectionTimeout(Duration connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
        return this;
    }

    /**
     * Gets the idle channel timeout
     * @return idle channel timeout
     */
    public Duration getIdleChannelTimeout() {
        return idleChannelTimeout;
    }

    /**
     * Sets the idle channel timeout
     * @param idleChannelTimeout idle channel timeout
     * @return the {@link ConnectionPolicy}
     */
    public ConnectionPolicy setIdleChannelTimeout(Duration idleChannelTimeout) {
        this.idleChannelTimeout = idleChannelTimeout;
        return this;
    }

    /**
     * Gets the idle endpoint timeout
     * @return the idle endpoint timeout
     */
    public Duration getIdleEndpointTimeout() {
        return idleEndpointTimeout;
    }

    /**
     * Sets the idle endpoint timeout
     * @param idleEndpointTimeout the idle endpoint timeout
     * @return the {@link ConnectionPolicy}
     */
    public ConnectionPolicy setIdleEndpointTimeout(Duration idleEndpointTimeout) {
        this.idleEndpointTimeout = idleEndpointTimeout;
        return this;
    }

    /**
     * Gets the max channels per endpoint
     * @return the max channels per endpoint
     */
    public int getMaxChannelsPerEndpoint() {
        return maxChannelsPerEndpoint;
    }

    /**
     * Sets the max channels per endpoint
     * @param maxChannelsPerEndpoint the max channels per endpoint
     * @return the {@link ConnectionPolicy}
     */
    public ConnectionPolicy setMaxChannelsPerEndpoint(int maxChannelsPerEndpoint) {
        this.maxChannelsPerEndpoint = maxChannelsPerEndpoint;
        return this;
    }

    /**
     * Gets the max requests per endpoint
     * @return the max requests per endpoint
     */
    public int getMaxRequestsPerChannel() {
        return maxRequestsPerChannel;
    }

    /**
     * Sets the max requests per endpoint
     * @param maxRequestsPerChannel the max requests per endpoint
     * @return the {@link ConnectionPolicy}
     */
    public ConnectionPolicy setMaxRequestsPerChannel(int maxRequestsPerChannel) {
        this.maxRequestsPerChannel = maxRequestsPerChannel;
        return this;
    }

    @Override
    public String toString() {
        return "ConnectionPolicy{" +
            "requestTimeout=" + requestTimeout +
            ", connectionMode=" + connectionMode +
            ", maxConnectionPoolSize=" + maxConnectionPoolSize +
            ", idleConnectionTimeout=" + idleConnectionTimeout +
            ", userAgentSuffix='" + userAgentSuffix + '\'' +
            ", throttlingRetryOptions=" + throttlingRetryOptions +
            ", endpointDiscoveryEnabled=" + endpointDiscoveryEnabled +
            ", preferredRegions=" + preferredRegions +
            ", multipleWriteRegionsEnabled=" + multipleWriteRegionsEnabled +
            ", inetSocketProxyAddress=" + inetSocketProxyAddress +
            ", readRequestsFallbackEnabled=" + readRequestsFallbackEnabled +
            ", connectionTimeout=" + connectionTimeout +
            ", idleChannelTimeout=" + idleChannelTimeout +
            ", idleEndpointTimeout=" + idleEndpointTimeout +
            ", maxChannelsPerEndpoint=" + maxChannelsPerEndpoint +
            ", maxRequestsPerChannel=" + maxRequestsPerChannel +
            '}';
    }
}
