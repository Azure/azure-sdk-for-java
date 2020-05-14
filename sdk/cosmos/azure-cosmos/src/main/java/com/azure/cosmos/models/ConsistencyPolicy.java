// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.models;


import com.azure.cosmos.ConsistencyLevel;
import com.azure.cosmos.implementation.Constants;
import com.azure.cosmos.implementation.JsonSerializable;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.azure.cosmos.implementation.guava25.base.CaseFormat;

import java.time.Duration;

/**
 * Encapsulates the properties for consistency policy in the Azure Cosmos DB database service.
 */
public final class ConsistencyPolicy extends JsonSerializableWrapper{
    private static final ConsistencyLevel DEFAULT_DEFAULT_CONSISTENCY_LEVEL =
        ConsistencyLevel.SESSION;

    private static final int DEFAULT_MAX_STALENESS_INTERVAL = 5;
    private static final int DEFAULT_MAX_STALENESS_PREFIX = 100;

    /**
     * Constructor.
     */
    public ConsistencyPolicy() {
        this.jsonSerializable = new JsonSerializable();
    }

    /**
     * Constructor.
     *
     * @param objectNode the {@link ObjectNode} that represent the
     * {@link JsonSerializable}
     */
    ConsistencyPolicy(ObjectNode objectNode) {
        this.jsonSerializable = new JsonSerializable(objectNode);
    }

    /**
     * Constructor.
     *
     * @param jsonString the json string that represents the consistency policy.
     */
    ConsistencyPolicy(String jsonString) {
        this.jsonSerializable = new JsonSerializable(jsonString);
    }

    /**
     * Get the name of the resource.
     *
     * @return the default consistency level.
     */
    public ConsistencyLevel getDefaultConsistencyLevel() {

        ConsistencyLevel result = ConsistencyPolicy.DEFAULT_DEFAULT_CONSISTENCY_LEVEL;
        String consistencyLevelString = this.jsonSerializable.getString(Constants.Properties.DEFAULT_CONSISTENCY_LEVEL);
        try {
            result = ConsistencyLevel
                         .valueOf(CaseFormat.UPPER_CAMEL.to(CaseFormat.UPPER_UNDERSCORE, consistencyLevelString));
        } catch (IllegalArgumentException e) {
            // ignore the exception and return the default
            this.jsonSerializable.getLogger().warn("Unknown consistency level {}, value ignored.", consistencyLevelString);
        }
        return result;
    }

    /**
     * Set the name of the resource.
     *
     * @param level the consistency level.
     * @return the ConsistencyPolicy.
     */
    public ConsistencyPolicy setDefaultConsistencyLevel(ConsistencyLevel level) {
        this.jsonSerializable.set(Constants.Properties.DEFAULT_CONSISTENCY_LEVEL, level.toString());
        return this;
    }

    /**
     * Gets the bounded staleness consistency, the maximum allowed staleness in terms difference in sequence numbers
     * (aka version).
     *
     * @return the max staleness prefix.
     */
    public int getMaxStalenessPrefix() {
        Integer value = this.jsonSerializable.getInt(Constants.Properties.MAX_STALENESS_PREFIX);
        if (value == null) {
            return ConsistencyPolicy.DEFAULT_MAX_STALENESS_PREFIX;
        }
        return value;
    }

    /**
     * Sets the bounded staleness consistency, the maximum allowed staleness in terms difference in sequence numbers
     * (aka version).
     *
     * @param maxStalenessPrefix the max staleness prefix.
     * @return the ConsistencyPolicy.
     */
    public ConsistencyPolicy setMaxStalenessPrefix(int maxStalenessPrefix) {
        this.jsonSerializable.set(Constants.Properties.MAX_STALENESS_PREFIX, maxStalenessPrefix);
        return this;
    }

    /**
     * Gets the in bounded staleness consistency, the maximum allowed staleness in terms time interval.
     * Resolution is in seconds.
     *
     * @return the max staleness prefix.
     */
    public Duration getMaxStalenessInterval() {
        Integer value = this.jsonSerializable.getInt(Constants.Properties.MAX_STALENESS_INTERVAL_IN_SECONDS);
        if (value == null) {
            return Duration.ofSeconds(ConsistencyPolicy.DEFAULT_MAX_STALENESS_INTERVAL);
        }
        return Duration.ofSeconds(value);
    }

    /**
     * Sets the in bounded staleness consistency, the maximum allowed staleness in terms time interval.
     * Resolution is in seconds.
     *
     * @param maxStalenessInterval the max staleness interval.
     * @return the ConsistencyPolicy.
     */
    public ConsistencyPolicy setMaxStalenessInterval(Duration maxStalenessInterval) {
        if (maxStalenessInterval == null) {
            throw new IllegalArgumentException("maxStalenessInterval should not be null");
        }
        this.jsonSerializable.set(Constants.Properties.MAX_STALENESS_INTERVAL_IN_SECONDS, maxStalenessInterval.getSeconds());
        return this;
    }
}
