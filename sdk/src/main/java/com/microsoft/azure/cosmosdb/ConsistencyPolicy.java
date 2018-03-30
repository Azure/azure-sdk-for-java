/*
 * The MIT License (MIT)
 * Copyright (c) 2018 Microsoft Corporation
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.microsoft.azure.cosmosdb;


import org.apache.commons.lang3.text.WordUtils;
import org.json.JSONObject;

import com.microsoft.azure.cosmosdb.internal.Constants;

/**
 * Encapsulates the settings for consistency policy in the Azure Cosmos DB database service.
 */
@SuppressWarnings("serial")
public final class ConsistencyPolicy extends JsonSerializable {
    private static final ConsistencyLevel DEFAULT_DEFAULT_CONSISTENCY_LEVEL =
            ConsistencyLevel.Session;

    private static final int DEFAULT_MAX_STALENESS_INTERVAL = 5;
    private static final int DEFAULT_MAX_STALENESS_PREFIX = 100;


    /**
     * Constructor.
     */
    ConsistencyPolicy() {
    }

    /**
     * Constructor.
     *
     * @param jsonString the json string that represents the consistency policy.
     */
    public ConsistencyPolicy(String jsonString) {
        super(jsonString);
    }

    /**
     * Constructor.
     *
     * @param jsonObject the json object that represents the consistency policy.
     */
    public ConsistencyPolicy(JSONObject jsonObject) {
        super(jsonObject);
    }

    /**
     * Get the name of the resource.
     *
     * @return the default consistency level.
     */
    public ConsistencyLevel getDefaultConsistencyLevel() {

        ConsistencyLevel result = ConsistencyPolicy.DEFAULT_DEFAULT_CONSISTENCY_LEVEL;
        try {
            result = ConsistencyLevel.valueOf(
                    WordUtils.capitalize(super.getString(Constants.Properties.DEFAULT_CONSISTENCY_LEVEL)));
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
     */
    public void setDefaultConsistencyLevel(ConsistencyLevel level) {
        super.set(Constants.Properties.DEFAULT_CONSISTENCY_LEVEL, level.name());
    }

    /**
     * Gets the bounded staleness consistency, the maximum allowed staleness in terms difference in sequence numbers
     * (aka version).
     *
     * @return the max staleness prefix.
     */
    public int getMaxStalenessPrefix() {
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
     */
    public void setMaxStalenessPrefix(int maxStalenessPrefix) {
        super.set(Constants.Properties.MAX_STALENESS_PREFIX, maxStalenessPrefix);
    }

    /**
     * Gets the in bounded staleness consistency, the maximum allowed staleness in terms time interval.
     *
     * @return the max staleness prefix.
     */
    public int getMaxStalenessIntervalInSeconds() {
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
     */
    public void setMaxStalenessIntervalInSeconds(int maxStalenessIntervalInSeconds) {
        super.set(Constants.Properties.MAX_STALENESS_INTERVAL_IN_SECONDS, maxStalenessIntervalInSeconds);
    }
}
