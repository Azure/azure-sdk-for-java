// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

import java.time.Duration;
import java.util.List;

/**
 * Represents the connection config with {@link ConnectionMode#DIRECT} associated with Cosmos Client in the Azure Cosmos DB database service.
 */
public final class DirectConnectionConfig extends ConnectionConfig {
    //  Constants
    private static final Duration DEFAULT_IDLE_CHANNEL_TIMEOUT = Duration.ZERO;
    private static final Duration DEFAULT_IDLE_ENDPOINT_TIMEOUT = Duration.ofSeconds(70L);
    private static final int DEFAULT_MAX_CHANNELS_PER_ENDPOINT = 30;
    private static final int DEFAULT_MAX_REQUESTS_PER_ENDPOINT = 10;

    private static final DirectConnectionConfig defaultConfig = new DirectConnectionConfig();

    private Duration connectionTimeout;
    private Duration idleChannelTimeout;
    private Duration idleEndpointTimeout;
    private int maxChannelsPerEndpoint;
    private int maxRequestsPerChannel;

    /**
     * Constructor.
     */
    public DirectConnectionConfig() {
        super(ConnectionMode.DIRECT);
        this.connectionTimeout = null;
        this.idleChannelTimeout = DEFAULT_IDLE_CHANNEL_TIMEOUT;
        this.idleEndpointTimeout = DEFAULT_IDLE_ENDPOINT_TIMEOUT;
        this.maxChannelsPerEndpoint = DEFAULT_MAX_CHANNELS_PER_ENDPOINT;
        this.maxRequestsPerChannel = DEFAULT_MAX_REQUESTS_PER_ENDPOINT;
    }

    /**
     * Gets the default DIRECT connection configuration.
     *
     * @return the default direct connection configuration.
     */
    public static DirectConnectionConfig getDefaultConfig() {
        return DirectConnectionConfig.defaultConfig;
    }

    //  TODO: (DANOBLE) - To update these docs.

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
     * @return the {@link DirectConnectionConfig}
     */
    public DirectConnectionConfig setConnectionTimeout(Duration connectionTimeout) {
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
     * @return the {@link DirectConnectionConfig}
     */
    public DirectConnectionConfig setIdleChannelTimeout(Duration idleChannelTimeout) {
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
     * @return the {@link DirectConnectionConfig}
     */
    public DirectConnectionConfig setIdleEndpointTimeout(Duration idleEndpointTimeout) {
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
     * @return the {@link DirectConnectionConfig}
     */
    public DirectConnectionConfig setMaxChannelsPerEndpoint(int maxChannelsPerEndpoint) {
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
     * @return the {@link DirectConnectionConfig}
     */
    public DirectConnectionConfig setMaxRequestsPerChannel(int maxRequestsPerChannel) {
        this.maxRequestsPerChannel = maxRequestsPerChannel;
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
     * @return the {@link DirectConnectionConfig}.
     * @throws IllegalArgumentException thrown if an error occurs
     */
    @Override
    public DirectConnectionConfig setThrottlingRetryOptions(ThrottlingRetryOptions throttlingRetryOptions) {
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
     * @return the {@link DirectConnectionConfig}.
     */
    @Override
    public DirectConnectionConfig setEndpointDiscoveryEnabled(boolean endpointDiscoveryEnabled) {
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
     * @return the {@link DirectConnectionConfig}.
     */
    @Override
    public DirectConnectionConfig setUsingMultipleWriteRegions(boolean usingMultipleWriteRegions) {
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
     * @return the {@link DirectConnectionConfig}.
     */
    @Override
    public DirectConnectionConfig setReadRequestsFallbackEnabled(Boolean readRequestsFallbackEnabled) {
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
     * @return the {@link DirectConnectionConfig}.
     */
    @Override
    public DirectConnectionConfig setPreferredRegions(List<String> preferredRegions) {
        super.setPreferredRegions(preferredRegions);
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
        return "DirectConnectionConfig{" +
            "connectionTimeout=" + connectionTimeout +
            ", idleChannelTimeout=" + idleChannelTimeout +
            ", idleEndpointTimeout=" + idleEndpointTimeout +
            ", maxChannelsPerEndpoint=" + maxChannelsPerEndpoint +
            ", maxRequestsPerChannel=" + maxRequestsPerChannel +
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
