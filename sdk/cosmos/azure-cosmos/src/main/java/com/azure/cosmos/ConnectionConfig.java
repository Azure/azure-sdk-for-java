// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

import java.util.Collections;
import java.util.List;

/**
 * Represents the common connection configuration associated with a Cosmos Client in the Azure Cosmos DB database service.
 */
public abstract class ConnectionConfig {

    protected final ConnectionMode connectionMode;

    protected String userAgentSuffix;
    protected  ThrottlingRetryOptions throttlingRetryOptions;
    protected List<String> preferredRegions;
    protected boolean endpointDiscoveryEnabled = true;
    protected boolean usingMultipleWriteRegions = true;
    protected Boolean readRequestsFallbackEnabled;

    /**
     * Constructor
     * @param connectionMode connection mode
     */
    protected ConnectionConfig(ConnectionMode connectionMode) {
        this.connectionMode = connectionMode;
        this.throttlingRetryOptions = new ThrottlingRetryOptions();
        this.readRequestsFallbackEnabled = null;
        this.userAgentSuffix = "";
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
     * Gets the value of user-agent suffix.
     *
     * @return the value of user-agent suffix.
     */
    public String getUserAgentSuffix() {
        return this.userAgentSuffix;
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
     * Gets the flag to enable endpoint discovery for geo-replicated database accounts.
     *
     * @return whether endpoint discovery is enabled.
     */
    public boolean isEndpointDiscoveryEnabled() {
        return this.endpointDiscoveryEnabled;
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
    public boolean isUsingMultipleWriteRegions() {
        return this.usingMultipleWriteRegions;
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
     * Gets the preferred regions for geo-replicated database accounts
     *
     * @return the list of preferred region.
     */
    public List<String> getPreferredRegions() {
        return this.preferredRegions != null ? this.preferredRegions : Collections.emptyList();
    }

    /**
     * sets the value of the user-agent suffix.
     *
     * @param userAgentSuffix The value to be appended to the user-agent header, this is
     * used for monitoring purposes.
     *
     * @return the {@link ConnectionConfig}
     */
    ConnectionConfig setUserAgentSuffix(String userAgentSuffix) {
        this.userAgentSuffix = userAgentSuffix;
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
     * @return the {@link ConnectionConfig}
     * @throws IllegalArgumentException thrown if an error occurs
     */
    ConnectionConfig setThrottlingRetryOptions(ThrottlingRetryOptions throttlingRetryOptions) {
        if (throttlingRetryOptions == null) {
            throw new IllegalArgumentException("retryOptions value must not be null.");
        }

        this.throttlingRetryOptions = throttlingRetryOptions;
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
     * @return the {@link ConnectionConfig}
     */
    ConnectionConfig setEndpointDiscoveryEnabled(boolean endpointDiscoveryEnabled) {
        this.endpointDiscoveryEnabled = endpointDiscoveryEnabled;
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
     * @return the {@link ConnectionConfig}
     */
    ConnectionConfig setUsingMultipleWriteRegions(boolean usingMultipleWriteRegions) {
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
     * @return the {@link ConnectionConfig}
     */
    ConnectionConfig setReadRequestsFallbackEnabled(Boolean readRequestsFallbackEnabled) {
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
     * @return the {@link ConnectionConfig}
     */
    ConnectionConfig setPreferredRegions(List<String> preferredRegions) {
        this.preferredRegions = preferredRegions;
        return this;
    }
}
