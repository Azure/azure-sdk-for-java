// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.models;

import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;

import java.io.Serializable;
import java.time.Duration;
import java.util.regex.Pattern;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkArgument;

/**
 * Dedicated Gateway Request Options
 */
public final class DedicatedGatewayRequestOptions implements Serializable {

    private static final long serialVersionUID = -2579875939911623561L;

    private static final Pattern SHARD_KEY_PATTERN = Pattern.compile("^[a-zA-Z0-9-]+$");
    private static final int MAX_SHARD_KEY_LENGTH = 36;

    /**
     * The staleness value associated with the request in the Azure CosmosDB service.
     */
    private Duration maxIntegratedCacheStaleness;

    /**
     * A flag indicating whether the integrated cache is enabled or bypassed with the request in Azure CosmosDB service.
     */
    private boolean integratedCacheBypassed;

    /**
     * The shard key value associated with the request in the Azure CosmosDB service to optionally specify a shard key
     * to use the new sharding feature in dedicated gateway. Sharding works only for dedicated gateway clients with http2
     * enabled.
     */
    private String shardKey;

    /**
     * Constructor
     */
    public DedicatedGatewayRequestOptions() {

    }

    /**
     * Gets the staleness value associated with the request in the Azure CosmosDB service. For requests where the {@link
     * com.azure.cosmos.ConsistencyLevel} is {@link com.azure.cosmos.ConsistencyLevel#EVENTUAL} or {@link com.azure.cosmos.ConsistencyLevel#SESSION}, responses from the
     * integrated cache are guaranteed to be no staler than value indicated by this maxIntegratedCacheStaleness.
     *
     * <p>Default value is null</p>
     *
     * <p>Cache Staleness is supported in milliseconds granularity. Anything smaller than milliseconds will be ignored.</p>
     *
     * @return Duration of maxIntegratedCacheStaleness
     */
    public Duration getMaxIntegratedCacheStaleness() {
        return maxIntegratedCacheStaleness;
    }

    /**
     * Sets the staleness value associated with the request in the Azure CosmosDB service. For requests where the {@link
     * com.azure.cosmos.ConsistencyLevel} is {@link com.azure.cosmos.ConsistencyLevel#EVENTUAL} or {@link com.azure.cosmos.ConsistencyLevel#SESSION}, responses from the
     * integrated cache are guaranteed to be no staler than value indicated by this maxIntegratedCacheStaleness.
     *
     * <p>Default value is null</p>
     *
     * <p>Cache Staleness is supported in milliseconds granularity. Anything smaller than milliseconds will be ignored.</p>
     *
     * @param maxIntegratedCacheStaleness Max Integrated Cache Staleness duration
     * @return this DedicatedGatewayRequestOptions
     */
    public DedicatedGatewayRequestOptions setMaxIntegratedCacheStaleness(Duration maxIntegratedCacheStaleness) {
        this.maxIntegratedCacheStaleness = maxIntegratedCacheStaleness;
        return this;
    }

    /**
     * Gets if the integrated cache is enabled or bypassed with the request in Azure CosmosDB service.
     *
     * <p>Default value is false</p>
     *
     * @return bypassIntegratedCache boolean value
     */
    public boolean isIntegratedCacheBypassed() {
        return integratedCacheBypassed;
    }

    /**
     * Sets if integrated cache should be enabled or bypassed for the request in Azure CosmosDB service.
     *
     * <p>Default value is false</p>
     *
     * @param bypassIntegratedCache boolean value
     * @return this DedicatedGatewayRequestOptions
     */
    public DedicatedGatewayRequestOptions setIntegratedCacheBypassed(boolean bypassIntegratedCache) {
        this.integratedCacheBypassed = bypassIntegratedCache;
        return this;
    }

    /**
     * Gets the shard key value associated with the request in the Azure CosmosDB service to optionally specify a shard key
     * to use the new sharding feature in dedicated gateway.
     * <p>Default value is null</p>
     *
     * @return shard key value
     */
    public String getShardKey() {
        return shardKey;
    }

    /**
     * Sets the shard key value associated with the request in the Azure CosmosDB service to optionally specify a shard key
     * to use the new sharding feature in dedicated gateway.
     * <p>Default value is null</p>
     *
     * @param shardKey shard key value
     * @return this DedicatedGatewayRequestOptions
     */
    public DedicatedGatewayRequestOptions setShardKey(String shardKey) {
        checkArgument(StringUtils.isNotEmpty(shardKey), "shardKey must not be null or empty");
        checkArgument(validateShardKey(shardKey), "shardKey contains invalid characters. Only alphanumeric and hyphen (-) are allowed. Max length is %s characters.",
            MAX_SHARD_KEY_LENGTH);
        this.shardKey = shardKey;
        return this;
    }

    private boolean validateShardKey(String shardKey) {
        // Since the pattern only allows ASCII characters (a-z, A-Z, 0-9, hyphen),
        // each character is 1 byte in UTF-8, so length check is sufficient.
        if (shardKey.length() > MAX_SHARD_KEY_LENGTH) {
            return false;
        }
        return SHARD_KEY_PATTERN.matcher(shardKey).matches();
    }
}
