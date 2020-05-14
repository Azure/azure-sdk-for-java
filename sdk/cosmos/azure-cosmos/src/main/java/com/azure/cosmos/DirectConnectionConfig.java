// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

import java.time.Duration;

/**
 * Represents the connection config with {@link ConnectionMode#DIRECT} associated with Cosmos Client in the Azure Cosmos DB database service.
 */
public final class DirectConnectionConfig {
    //  Constants
    private static final Duration DEFAULT_IDLE_CHANNEL_TIMEOUT = Duration.ZERO;
    private static final Duration DEFAULT_IDLE_ENDPOINT_TIMEOUT = Duration.ofSeconds(70L);
    private static final int DEFAULT_MAX_CHANNELS_PER_ENDPOINT = 30;
    private static final int DEFAULT_MAX_REQUESTS_PER_ENDPOINT = 10;

    private Duration connectionTimeout;
    private Duration idleChannelTimeout;
    private Duration idleEndpointTimeout;
    private int maxChannelsPerEndpoint;
    private int maxRequestsPerChannel;

    /**
     * Constructor.
     */
    public DirectConnectionConfig() {
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
        return new DirectConnectionConfig();
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

    @Override
    public String toString() {
        return "DirectConnectionConfig{" +
            "connectionTimeout=" + connectionTimeout +
            ", idleChannelTimeout=" + idleChannelTimeout +
            ", idleEndpointTimeout=" + idleEndpointTimeout +
            ", maxChannelsPerEndpoint=" + maxChannelsPerEndpoint +
            ", maxRequestsPerChannel=" + maxRequestsPerChannel +
            '}';
    }
}
