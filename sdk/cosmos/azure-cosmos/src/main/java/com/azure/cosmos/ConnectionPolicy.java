// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

import java.net.InetSocketAddress;
import java.time.Duration;
import java.util.Collections;
import java.util.List;

/**
 * Represents the Connection policy associated with a DocumentClient in the Azure Cosmos DB database service.
 */
public final class ConnectionPolicy {
    private static final Duration DEFAULT_REQUEST_TIMEOUT = Duration.ofSeconds(60);
    // defaultMediaRequestTimeout is based upon the blob client timeout and the
    // retry policy.
    private static final Duration DEFAULT_MEDIA_REQUEST_TIMEOUT = Duration.ofSeconds(300);
    private static final Duration DEFAULT_IDLE_CONNECTION_TIMEOUT = Duration.ofSeconds(60);

    private static final int DEFAULT_MAX_POOL_SIZE = 1000;

    private static ConnectionPolicy defaultPolicy = null;
    private Duration requestTimeout;
    private final Duration mediaRequestTimeout;
    private ConnectionMode connectionMode;
    private int maxPoolSize;
    private Duration idleConnectionTimeout;
    private String userAgentSuffix;
    private ThrottlingRetryOptions throttlingRetryOptions;
    private boolean endpointDiscoveryEnabled = true;
    private List<String> preferredLocations;
    private boolean usingMultipleWriteLocations = true;
    private InetSocketAddress inetSocketProxyAddress;
    private Boolean readRequestsFallbackEnabled;

    /**
     * Constructor.
     */
    public ConnectionPolicy() {
        this.connectionMode = ConnectionMode.DIRECT;
        this.readRequestsFallbackEnabled = null;
        this.idleConnectionTimeout = DEFAULT_IDLE_CONNECTION_TIMEOUT;
        this.maxPoolSize = DEFAULT_MAX_POOL_SIZE;
        this.mediaRequestTimeout = DEFAULT_MEDIA_REQUEST_TIMEOUT;
        this.requestTimeout = DEFAULT_REQUEST_TIMEOUT;
        this.throttlingRetryOptions = new ThrottlingRetryOptions();
        this.userAgentSuffix = "";
    }

    /**
     * Gets the default connection policy.
     *
     * @return the default connection policy.
     */
    public static ConnectionPolicy getDefaultPolicy() {
        if (ConnectionPolicy.defaultPolicy == null) {
            ConnectionPolicy.defaultPolicy = new ConnectionPolicy();
        }
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
    public int getMaxPoolSize() {
        return this.maxPoolSize;
    }

    /**
     * Sets the value of the connection pool size, the default
     * is 1000.
     *
     * @param maxPoolSize The value of the connection pool size.
     * @return the ConnectionPolicy.
     */
    public ConnectionPolicy setMaxPoolSize(int maxPoolSize) {
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
     * Gets the flag to enable writes on any locations (regions) for geo-replicated database accounts in the Azure
     * Cosmos DB service.
     * <p>
     * When the value of this property is true, the SDK will direct write operations to
     * available writable locations of geo-replicated database account. Writable locations
     * are ordered by PreferredLocations property. Setting the property value
     * to true has no effect until EnableMultipleWriteLocations in DatabaseAccount
     * is also set to true.
     * <p>
     * DEFAULT value is true indicating that writes are directed to
     * available writable locations of geo-replicated database account.
     *
     * @return flag to enable writes on any locations (regions) for geo-replicated database accounts.
     */
    public boolean isUsingMultipleWriteLocations() {
        return this.usingMultipleWriteLocations;
    }

    /**
     * Gets whether to allow for reads to go to multiple regions configured on an account of Azure Cosmos DB service.
     * <p>
     * DEFAULT value is null.
     * <p>
     * If this property is not set, the default is true for all Consistency Levels other than Bounded Staleness,
     * The default is false for Bounded Staleness.
     * 1. {@link #endpointDiscoveryEnabled} is true
     * 2. the Azure Cosmos DB account has more than one region
     *
     * @return flag to allow for reads to go to multiple regions configured on an account of Azure Cosmos DB service.
     */
    public Boolean isReadRequestsFallbackEnabled() {
        return this.readRequestsFallbackEnabled;
    }

    /**
     * Sets the flag to enable writes on any locations (regions) for geo-replicated database accounts in the Azure
     * Cosmos DB service.
     * <p>
     * When the value of this property is true, the SDK will direct write operations to
     * available writable locations of geo-replicated database account. Writable locations
     * are ordered by PreferredLocations property. Setting the property value
     * to true has no effect until EnableMultipleWriteLocations in DatabaseAccount
     * is also set to true.
     * <p>
     * DEFAULT value is false indicating that writes are only directed to
     * first region in PreferredLocations property.
     *
     * @param usingMultipleWriteLocations flag to enable writes on any locations (regions) for geo-replicated
     * database accounts.
     * @return the ConnectionPolicy.
     */
    public ConnectionPolicy setUsingMultipleWriteLocations(boolean usingMultipleWriteLocations) {
        this.usingMultipleWriteLocations = usingMultipleWriteLocations;
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
     * @return the ConnectionPolicy.
     */
    public ConnectionPolicy setReadRequestsFallbackEnabled(Boolean readRequestsFallbackEnabled) {
        this.readRequestsFallbackEnabled = readRequestsFallbackEnabled;
        return this;
    }

    /**
     * Gets the preferred locations for geo-replicated database accounts
     *
     * @return the list of preferred location.
     */
    public List<String> getPreferredLocations() {
        return this.preferredLocations != null ? preferredLocations : Collections.emptyList();
    }

    /**
     * Sets the preferred locations for geo-replicated database accounts. For example,
     * "East US" as the preferred location.
     * <p>
     * When EnableEndpointDiscovery is true and PreferredRegions is non-empty,
     * the SDK will prefer to use the locations in the collection in the order
     * they are specified to perform operations.
     * <p>
     * If EnableEndpointDiscovery is set to false, this property is ignored.
     *
     * @param preferredLocations the list of preferred locations.
     * @return the ConnectionPolicy.
     */
    public ConnectionPolicy setPreferredLocations(List<String> preferredLocations) {
        this.preferredLocations = preferredLocations;
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

    @Override
    public String toString() {
        return "ConnectionPolicy{"
                   + "requestTimeout=" + requestTimeout
                   + ", mediaRequestTimeout=" + mediaRequestTimeout
                   + ", connectionMode=" + connectionMode
                   + ", maxPoolSize=" + maxPoolSize
                   + ", idleConnectionTimeout=" + idleConnectionTimeout
                   + ", userAgentSuffix='" + userAgentSuffix + '\''
                   + ", retryOptions=" + throttlingRetryOptions
                   + ", enableEndpointDiscovery=" + endpointDiscoveryEnabled
                   + ", preferredLocations=" + preferredLocations
                   + ", usingMultipleWriteLocations=" + usingMultipleWriteLocations
                   + ", inetSocketProxyAddress=" + inetSocketProxyAddress
                   + '}';
    }
}
