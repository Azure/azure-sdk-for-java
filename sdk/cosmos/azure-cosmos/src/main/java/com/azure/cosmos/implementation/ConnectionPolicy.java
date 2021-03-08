// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation;

import com.azure.core.http.ProxyOptions;
import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.ConnectionMode;
import com.azure.cosmos.DirectConnectionConfig;
import com.azure.cosmos.GatewayConnectionConfig;
import com.azure.cosmos.ThrottlingRetryOptions;

import java.time.Duration;
import java.util.Collections;
import java.util.List;

/**
 * Represents the Connection policy associated with a Cosmos client in the Azure Cosmos DB service.
 */
public final class ConnectionPolicy {

    private static final int defaultGatewayMaxConnectionPoolSize = GatewayConnectionConfig.getDefaultConfig()
        .getMaxConnectionPoolSize();

    private ConnectionMode connectionMode;
    private boolean endpointDiscoveryEnabled;
    private boolean multipleWriteRegionsEnabled;
    private List<String> preferredRegions;
    private boolean readRequestsFallbackEnabled;
    private ThrottlingRetryOptions throttlingRetryOptions;
    private String userAgentSuffix;

    //  Gateway connection config properties
    private int maxConnectionPoolSize;
    private Duration requestTimeout;
    private ProxyOptions proxy;
    private Duration idleHttpConnectionTimeout;

    //  Direct connection config properties
    private Duration connectTimeout;
    private Duration idleTcpConnectionTimeout;
    private Duration idleTcpEndpointTimeout;
    private int maxConnectionsPerEndpoint;
    private int maxRequestsPerConnection;

    private boolean tcpConnectionEndpointRediscoveryEnabled;


    private boolean clientTelemetryEnabled;

    /**
     * Constructor.
     */
    public ConnectionPolicy(GatewayConnectionConfig gatewayConnectionConfig) {
        this(ConnectionMode.GATEWAY);
        this.idleHttpConnectionTimeout = gatewayConnectionConfig.getIdleConnectionTimeout();
        this.maxConnectionPoolSize = gatewayConnectionConfig.getMaxConnectionPoolSize();
        this.requestTimeout = BridgeInternal.getRequestTimeoutFromGatewayConnectionConfig(gatewayConnectionConfig);
        this.proxy = gatewayConnectionConfig.getProxy();
        this.tcpConnectionEndpointRediscoveryEnabled = false;
    }

    public ConnectionPolicy(DirectConnectionConfig directConnectionConfig) {
        this(ConnectionMode.DIRECT);
        this.connectTimeout = directConnectionConfig.getConnectTimeout();
        this.idleTcpConnectionTimeout = directConnectionConfig.getIdleConnectionTimeout();
        this.idleTcpEndpointTimeout = directConnectionConfig.getIdleEndpointTimeout();
        this.maxConnectionsPerEndpoint = directConnectionConfig.getMaxConnectionsPerEndpoint();
        this.maxRequestsPerConnection = directConnectionConfig.getMaxRequestsPerConnection();
        this.requestTimeout = BridgeInternal.getRequestTimeoutFromDirectConnectionConfig(directConnectionConfig);
        this.tcpConnectionEndpointRediscoveryEnabled = directConnectionConfig.isConnectionEndpointRediscoveryEnabled();
    }

    private ConnectionPolicy(ConnectionMode connectionMode) {
        this.connectionMode = connectionMode;
        //  Default values
        this.endpointDiscoveryEnabled = true;
        this.maxConnectionPoolSize = defaultGatewayMaxConnectionPoolSize;
        this.multipleWriteRegionsEnabled = true;
        this.readRequestsFallbackEnabled = true;
        this.throttlingRetryOptions = new ThrottlingRetryOptions();
        this.userAgentSuffix = "";
    }

    /**
     * Gets a value that indicates whether Direct TCP connection endpoint rediscovery is enabled.
     *
     * @return {@code true} if Direct TCP connection endpoint rediscovery should is enabled; {@code false} otherwise.
     */
    public boolean isTcpConnectionEndpointRediscoveryEnabled() {
        return this.tcpConnectionEndpointRediscoveryEnabled;
    }

    /**
     * Sets a value that indicates whether Direct TCP connection endpoint rediscovery is enabled.
     *
     * @return the {@linkplain ConnectionPolicy}.
     */
    public ConnectionPolicy setTcpConnectionEndpointRediscoveryEnabled(boolean tcpConnectionEndpointRediscoveryEnabled) {
        this.tcpConnectionEndpointRediscoveryEnabled = tcpConnectionEndpointRediscoveryEnabled;
        return this;
    }


    /**
     * Gets the default connection policy.
     *
     * @return the default connection policy.
     */
    public static ConnectionPolicy getDefaultPolicy() {
        return new ConnectionPolicy(DirectConnectionConfig.getDefaultConfig());
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
     * Gets the value of the timeout for an idle http connection, the default is 60
     * seconds.
     *
     * @return Idle connection timeout duration.
     */
    public Duration getIdleHttpConnectionTimeout() {
        return this.idleHttpConnectionTimeout;
    }

    /**
     * sets the value of the timeout for an idle http connection. After that time,
     * the connection will be automatically closed.
     *
     * @param idleHttpConnectionTimeout the duration for an idle connection.
     * @return the ConnectionPolicy.
     */
    public ConnectionPolicy setIdleHttpConnectionTimeout(Duration idleHttpConnectionTimeout) {
        this.idleHttpConnectionTimeout = idleHttpConnectionTimeout;
        return this;
    }

    /**
     * Gets the idle tcp connection timeout for direct client
     *
     * Default value is {@link Duration#ZERO}
     *
     * Direct client doesn't close a single connection to an endpoint
     * by default unless specified.
     *
     * @return idle tcp connection timeout
     */
    public Duration getIdleTcpConnectionTimeout() {
        return idleTcpConnectionTimeout;
    }

    /**
     * Sets the idle tcp connection timeout
     *
     * Default value is {@link Duration#ZERO}
     *
     * Direct client doesn't close a single connection to an endpoint
     * by default unless specified.
     *
     * @param idleTcpConnectionTimeout idle connection timeout
     * @return the {@link ConnectionPolicy}
     */
    public ConnectionPolicy setIdleTcpConnectionTimeout(Duration idleTcpConnectionTimeout) {
        this.idleTcpConnectionTimeout = idleTcpConnectionTimeout;
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
     * @return the ConnectionPolicy.
     */

    public ConnectionPolicy setProxy(ProxyOptions proxy) {
        this.proxy = proxy;
        return this;
    }

    /**
     * Gets the direct connect timeout
     * @return direct connect timeout
     */
    public Duration getConnectTimeout() {
        return connectTimeout;
    }

    /**
     *  Sets the direct connect timeout
     * @param connectTimeout the connect timeout
     * @return the {@link ConnectionPolicy}
     */
    public ConnectionPolicy setConnectTimeout(Duration connectTimeout) {
        this.connectTimeout = connectTimeout;
        return this;
    }

    /**
     * Gets the idle endpoint timeout
     * @return the idle endpoint timeout
     */
    public Duration getIdleTcpEndpointTimeout() {
        return idleTcpEndpointTimeout;
    }

    /**
     * Sets the idle endpoint timeout
     * @param idleTcpEndpointTimeout the idle endpoint timeout
     * @return the {@link ConnectionPolicy}
     */
    public ConnectionPolicy setIdleTcpEndpointTimeout(Duration idleTcpEndpointTimeout) {
        this.idleTcpEndpointTimeout = idleTcpEndpointTimeout;
        return this;
    }

    /**
     * Gets the max channels per endpoint
     * @return the max channels per endpoint
     */
    public int getMaxConnectionsPerEndpoint() {
        return maxConnectionsPerEndpoint;
    }

    /**
     * Sets the max channels per endpoint
     * @param maxConnectionsPerEndpoint the max channels per endpoint
     * @return the {@link ConnectionPolicy}
     */
    public ConnectionPolicy setMaxConnectionsPerEndpoint(int maxConnectionsPerEndpoint) {
        this.maxConnectionsPerEndpoint = maxConnectionsPerEndpoint;
        return this;
    }

    /**
     * Gets the max requests per endpoint
     * @return the max requests per endpoint
     */
    public int getMaxRequestsPerConnection() {
        return maxRequestsPerConnection;
    }

    /**
     * Sets the max requests per endpoint
     * @param maxRequestsPerConnection the max requests per endpoint
     * @return the {@link ConnectionPolicy}
     */
    public ConnectionPolicy setMaxRequestsPerConnection(int maxRequestsPerConnection) {
        this.maxRequestsPerConnection = maxRequestsPerConnection;
        return this;
    }

    public boolean isClientTelemetryEnabled() {
        return clientTelemetryEnabled;
    }

    public void setClientTelemetryEnabled(boolean clientTelemetryEnabled) {
        this.clientTelemetryEnabled = clientTelemetryEnabled;
    }

    @Override
    public String toString() {
        return "ConnectionPolicy{" +
            "requestTimeout=" + requestTimeout +
            ", connectionMode=" + connectionMode +
            ", maxConnectionPoolSize=" + maxConnectionPoolSize +
            ", idleHttpConnectionTimeout=" + idleHttpConnectionTimeout +
            ", idleTcpConnectionTimeout=" + idleTcpConnectionTimeout +
            ", userAgentSuffix='" + userAgentSuffix + '\'' +
            ", throttlingRetryOptions=" + throttlingRetryOptions +
            ", endpointDiscoveryEnabled=" + endpointDiscoveryEnabled +
            ", preferredRegions=" + preferredRegions +
            ", multipleWriteRegionsEnabled=" + multipleWriteRegionsEnabled +
            ", proxyType=" + (proxy != null ? proxy.getType() : null) +
            ", inetSocketProxyAddress=" + (proxy != null ? proxy.getAddress() : null) +
            ", readRequestsFallbackEnabled=" + readRequestsFallbackEnabled +
            ", connectTimeout=" + connectTimeout +
            ", idleTcpEndpointTimeout=" + idleTcpEndpointTimeout +
            ", maxConnectionsPerEndpoint=" + maxConnectionsPerEndpoint +
            ", maxRequestsPerConnection=" + maxRequestsPerConnection +
            ", tcpConnectionEndpointRediscoveryEnabled=" + tcpConnectionEndpointRediscoveryEnabled +
            ", clientTelemetryEnabled=" + clientTelemetryEnabled +
            '}';
    }
}
