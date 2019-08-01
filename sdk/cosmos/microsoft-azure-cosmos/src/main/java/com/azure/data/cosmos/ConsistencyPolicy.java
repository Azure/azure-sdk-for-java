// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.cosmos;


import com.azure.data.cosmos.internal.Constants;
import org.apache.commons.lang3.StringUtils;

/**
 * Encapsulates the properties for consistency policy in the Azure Cosmos DB database service.
 */
public final class ConsistencyPolicy extends JsonSerializable {
    private static final ConsistencyLevel DEFAULT_DEFAULT_CONSISTENCY_LEVEL =
            ConsistencyLevel.SESSION;

    private static final int DEFAULT_MAX_STALENESS_INTERVAL = 5;
    private static final int DEFAULT_MAX_STALENESS_PREFIX = 100;


    /**
     * Constructor.
     */
    public ConsistencyPolicy() {
    }

    /**
     * Constructor.
     *
     * @param jsonString the json string that represents the consistency policy.
     */
    ConsistencyPolicy(String jsonString) {
        super(jsonString);
    }

    /**
     * Get the name of the resource.
     *
     * @return the default consistency level.
     */
    public ConsistencyLevel defaultConsistencyLevel() {

        ConsistencyLevel result = ConsistencyPolicy.DEFAULT_DEFAULT_CONSISTENCY_LEVEL;
        try {
            result = ConsistencyLevel.valueOf(
                    StringUtils.upperCase(super.getString(Constants.Properties.DEFAULT_CONSISTENCY_LEVEL)));
        } catch (IllegalArgumentException e) {
            // ignore the exception and return the default
            this.getLogger().warn("Unknown consistency level {}, value ignored.", super.getString(Constants.Properties.DEFAULT_CONSISTENCY_LEVEL));
        }
        return result;
    }

    /**
     * Set the name of the resource.
     *
     * @param level the consistency level.
     * @return the ConsistenctPolicy.
     */
    public ConsistencyPolicy defaultConsistencyLevel(ConsistencyLevel level) {
        super.set(Constants.Properties.DEFAULT_CONSISTENCY_LEVEL, level.toString());
        return this;
    }

    /**
     * Gets the bounded staleness consistency, the maximum allowed staleness in terms difference in sequence numbers
     * (aka version).
     *
     * @return the max staleness prefix.
     */
    public int maxStalenessPrefix() {
        Integer value = super.getInt(Constants.Properties.MAX_STALENESS_PREFIX);
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
     * @return the ConsistenctPolicy.
     */
    public ConsistencyPolicy maxStalenessPrefix(int maxStalenessPrefix) {
        super.set(Constants.Properties.MAX_STALENESS_PREFIX, maxStalenessPrefix);
        return this;
    }

    /**
     * Gets the in bounded staleness consistency, the maximum allowed staleness in terms time interval.
     *
     * @return the max staleness prefix.
     */
    public int maxStalenessIntervalInSeconds() {
        Integer value = super.getInt(Constants.Properties.MAX_STALENESS_INTERVAL_IN_SECONDS);
        if (value == null) {
            return ConsistencyPolicy.DEFAULT_MAX_STALENESS_INTERVAL;
        }
        return value;
    }

    /**
     * Sets the in bounded staleness consistency, the maximum allowed staleness in terms time interval.
     *
     * @param maxStalenessIntervalInSeconds the max staleness interval in seconds.
     * @return the ConsistenctPolicy.
     */
    public ConsistencyPolicy maxStalenessIntervalInSeconds(int maxStalenessIntervalInSeconds) {
        super.set(Constants.Properties.MAX_STALENESS_INTERVAL_IN_SECONDS, maxStalenessIntervalInSeconds);
        return this;
    }
}
