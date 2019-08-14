// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.cosmos;

import java.net.InetSocketAddress;
import java.util.Collections;
import java.util.List;

/**
 * Represents the Connection policy associated with a DocumentClient in the Azure Cosmos DB database service.
 */
public final class ConnectionPolicy {

    private static final int DEFAULT_REQUEST_TIMEOUT_IN_MILLIS = 60 * 1000;
    // defaultMediaRequestTimeout is based upon the blob client timeout and the
    // retry policy.
    private static final int DEFAULT_MEDIA_REQUEST_TIMEOUT_IN_MILLIS = 300 * 1000;
    private static final int DEFAULT_IDLE_CONNECTION_TIMEOUT_IN_MILLIS = 60 * 1000;

    private static final int DEFAULT_MAX_POOL_SIZE = 1000;

    private static ConnectionPolicy default_policy = null;
    private int requestTimeoutInMillis;
    private int mediaRequestTimeoutInMillis;
    private ConnectionMode connectionMode;
    private int maxPoolSize;
    private int idleConnectionTimeoutInMillis;
    private String userAgentSuffix;
    private RetryOptions retryOptions;
    private boolean enableEndpointDiscovery = true;
    private List<String> preferredLocations;
    private boolean usingMultipleWriteLocations;
    private InetSocketAddress inetSocketProxyAddress;
    private Boolean enableReadRequestsFallback;

    /**
     * Constructor.
     */
    public ConnectionPolicy() {
        this.connectionMode = ConnectionMode.GATEWAY;
        this.enableReadRequestsFallback = null;
        this.idleConnectionTimeoutInMillis = DEFAULT_IDLE_CONNECTION_TIMEOUT_IN_MILLIS;
        this.maxPoolSize = DEFAULT_MAX_POOL_SIZE;
        this.mediaRequestTimeoutInMillis = ConnectionPolicy.DEFAULT_MEDIA_REQUEST_TIMEOUT_IN_MILLIS;
        this.requestTimeoutInMillis = ConnectionPolicy.DEFAULT_REQUEST_TIMEOUT_IN_MILLIS;
        this.retryOptions = new RetryOptions();
        this.userAgentSuffix = "";
    }

    /**
     * Gets the default connection policy.
     *
     * @return the default connection policy.
     */
    public static ConnectionPolicy defaultPolicy() {
        if (ConnectionPolicy.default_policy == null) {
            ConnectionPolicy.default_policy = new ConnectionPolicy();
        }
        return ConnectionPolicy.default_policy;
    }

    /**
     * Gets the request timeout (time to wait for response from network peer) in
     * milliseconds. 
     *
     * @return the request timeout in milliseconds.
     */
    public int requestTimeoutInMillis() {
        return this.requestTimeoutInMillis;
    }

    /**
     * Sets the request timeout (time to wait for response from network peer) in
     * milliseconds. The default is 60 seconds.
     *
     * @param requestTimeoutInMillis the request timeout in milliseconds.
     * @return the ConnectionPolicy.
     */
    public ConnectionPolicy requestTimeoutInMillis(int requestTimeoutInMillis) {
        this.requestTimeoutInMillis = requestTimeoutInMillis;
        return this;
    }

    /**
     * Gets the connection mode used in the client.
     *
     * @return the connection mode.
     */
    public ConnectionMode connectionMode() {
        return this.connectionMode;
    }

    /**
     * Sets the connection mode used in the client.
     *
     * @param connectionMode the connection mode.
     * @return the ConnectionPolicy.
     */
    public ConnectionPolicy connectionMode(ConnectionMode connectionMode) {
        this.connectionMode = connectionMode;
        return this;
    }

    /**
     * Gets the value of the connection pool size the client is using.
     *
     * @return connection pool size.
     */
    public int maxPoolSize() {
        return this.maxPoolSize;
    }

    /**
     * Sets the value of the connection pool size, the default
     * is 1000.
     *
     * @param maxPoolSize The value of the connection pool size.
     * @return the ConnectionPolicy.
     */
    public ConnectionPolicy maxPoolSize(int maxPoolSize) {
        this.maxPoolSize = maxPoolSize;
        return this;
    }

    /**
     * Gets the value of the timeout for an idle connection, the default is 60
     * seconds.
     *
     * @return Idle connection timeout.
     */
    public int idleConnectionTimeoutInMillis() {
        return this.idleConnectionTimeoutInMillis;
    }

    /**
     * sets the value of the timeout for an idle connection. After that time,
     * the connection will be automatically closed.
     *
     * @param idleConnectionTimeoutInMillis the timeout for an idle connection in seconds.
     * @return the ConnectionPolicy.
     */
    public ConnectionPolicy idleConnectionTimeoutInMillis(int idleConnectionTimeoutInMillis) {
        this.idleConnectionTimeoutInMillis = idleConnectionTimeoutInMillis;
        return this;
    }

    /**
     * Gets the value of user-agent suffix.
     *
     * @return the value of user-agent suffix.
     */
    public String userAgentSuffix() {
        return this.userAgentSuffix;
    }

    /**
     * sets the value of the user-agent suffix.
     *
     * @param userAgentSuffix The value to be appended to the user-agent header, this is
     *                        used for monitoring purposes.
     * @return the ConnectionPolicy.
     */
    public ConnectionPolicy userAgentSuffix(String userAgentSuffix) {
        this.userAgentSuffix = userAgentSuffix;
        return this;
    }

    /**
     * Gets the retry policy options associated with the DocumentClient instance.
     *
     * @return the RetryOptions instance.
     */
    public RetryOptions retryOptions() {
        return this.retryOptions;
    }

    /**
     * Sets the retry policy options associated with the DocumentClient instance.
     * <p>
     * Properties in the RetryOptions class allow application to customize the built-in
     * retry policies. This property is optional. When it's not set, the SDK uses the
     * default values for configuring the retry policies.  See RetryOptions class for
     * more details.
     *
     * @param retryOptions the RetryOptions instance.
     * @return the ConnectionPolicy.
     */
    public ConnectionPolicy retryOptions(RetryOptions retryOptions) {
        if (retryOptions == null) {
            throw new IllegalArgumentException("retryOptions value must not be null.");
        }

        this.retryOptions = retryOptions;
        return this;
    }

    /**
     * Gets the flag to enable endpoint discovery for geo-replicated database accounts.
     *
     * @return whether endpoint discovery is enabled.
     */
    public boolean enableEndpointDiscovery() {
        return this.enableEndpointDiscovery;
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
     * @param enableEndpointDiscovery true if EndpointDiscovery is enabled.
     * @return the ConnectionPolicy.
     */
    public ConnectionPolicy enableEndpointDiscovery(boolean enableEndpointDiscovery) {
        this.enableEndpointDiscovery = enableEndpointDiscovery;
        return this;
    }

    /**
     * Gets the flag to enable writes on any locations (regions) for geo-replicated database accounts in the Azure Cosmos DB service.
     *
     * When the value of this property is true, the SDK will direct write operations to
     * available writable locations of geo-replicated database account. Writable locations
     * are ordered by PreferredLocations property. Setting the property value
     * to true has no effect until EnableMultipleWriteLocations in DatabaseAccount
     * is also set to true.
     *
     * DEFAULT value is false indicating that writes are only directed to
     * first region in PreferredLocations property.
     *
     * @return flag to enable writes on any locations (regions) for geo-replicated database accounts.
     */
    public boolean usingMultipleWriteLocations() {
        return this.usingMultipleWriteLocations;
    }

    /**
     * Gets whether to allow for reads to go to multiple regions configured on an account of Azure Cosmos DB service.
     *
     * DEFAULT value is null.
     *
     * If this property is not set, the default is true for all Consistency Levels other than Bounded Staleness,
     * The default is false for Bounded Staleness.
     * 1. {@link #enableEndpointDiscovery} is true
     * 2. the Azure Cosmos DB account has more than one region
     *
     * @return flag to allow for reads to go to multiple regions configured on an account of Azure Cosmos DB service.
     */
    public Boolean enableReadRequestsFallback() {
        return this.enableReadRequestsFallback;
    }

    /**
     * Sets the flag to enable writes on any locations (regions) for geo-replicated database accounts in the Azure Cosmos DB service.
     *
     * When the value of this property is true, the SDK will direct write operations to
     * available writable locations of geo-replicated database account. Writable locations
     * are ordered by PreferredLocations property. Setting the property value
     * to true has no effect until EnableMultipleWriteLocations in DatabaseAccount
     * is also set to true.
     *
     * DEFAULT value is false indicating that writes are only directed to
     * first region in PreferredLocations property.
     *
     * @param usingMultipleWriteLocations flag to enable writes on any locations (regions) for geo-replicated database accounts.
     * @return the ConnectionPolicy.
     */
    public ConnectionPolicy usingMultipleWriteLocations(boolean usingMultipleWriteLocations) {
        this.usingMultipleWriteLocations = usingMultipleWriteLocations;
        return this;
    }

    /**
     * Sets whether to allow for reads to go to multiple regions configured on an account of Azure Cosmos DB service.
     *
     * DEFAULT value is null.
     *
     * If this property is not set, the default is true for all Consistency Levels other than Bounded Staleness,
     * The default is false for Bounded Staleness.
     * 1. {@link #enableEndpointDiscovery} is true
     * 2. the Azure Cosmos DB account has more than one region
     *
     * @param enableReadRequestsFallback flag to enable reads to go to multiple regions configured on an account of Azure Cosmos DB service.
     * @return the ConnectionPolicy.
     */
    public ConnectionPolicy enableReadRequestsFallback(Boolean enableReadRequestsFallback) {
        this.enableReadRequestsFallback = enableReadRequestsFallback;
        return this;
    }

    /**
     * Gets the preferred locations for geo-replicated database accounts
     *
     * @return the list of preferred location.
     */
    public List<String> preferredLocations() {
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
    public ConnectionPolicy preferredLocations(List<String> preferredLocations) {
        this.preferredLocations = preferredLocations;
        return this;
    }

    /**
     * Gets the InetSocketAddress of proxy server.
     *
     * @return the value of proxyHost.
     */
    public InetSocketAddress proxy() {
        return this.inetSocketProxyAddress;
    }

    /**
     * This will create the InetSocketAddress for proxy server,
     * all the requests to cosmoDB will route from this address.
     * @param proxyHost The proxy server host.
     * @param proxyPort The proxy server port.
     * @return the ConnectionPolicy.
     */
    public ConnectionPolicy proxy(String proxyHost, int proxyPort) {
        this.inetSocketProxyAddress = new InetSocketAddress(proxyHost, proxyPort);
        return this;
    }

    @Override
    public String toString() {
        return "ConnectionPolicy{" +
                "requestTimeoutInMillis=" + requestTimeoutInMillis +
                ", mediaRequestTimeoutInMillis=" + mediaRequestTimeoutInMillis +
                ", connectionMode=" + connectionMode +
                ", maxPoolSize=" + maxPoolSize +
                ", idleConnectionTimeoutInMillis=" + idleConnectionTimeoutInMillis +
                ", userAgentSuffix='" + userAgentSuffix + '\'' +
                ", retryOptions=" + retryOptions +
                ", enableEndpointDiscovery=" + enableEndpointDiscovery +
                ", preferredLocations=" + preferredLocations +
                ", usingMultipleWriteLocations=" + usingMultipleWriteLocations +
                ", inetSocketProxyAddress=" + inetSocketProxyAddress +
                '}';
    }
}
