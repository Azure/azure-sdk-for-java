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

package com.azure.data.cosmos;

import com.azure.data.cosmos.internal.PartitionKey;
import com.azure.data.cosmos.internal.PartitionKeyRange;

import java.time.OffsetDateTime;
import java.util.Map;

/**
 * Specifies the options associated with change feed methods (enumeration
 * operations) in the Azure Cosmos DB database service.
 */
public final class ChangeFeedOptions {
    private String partitionKeyRangeId;
    private boolean startFromBeginning;
    private OffsetDateTime startDateTime;
    private Integer maxItemCount;
    private String requestContinuation;
    private PartitionKey partitionkey;
    private boolean populateQueryMetrics;
    private Map<String, Object> properties;

    public ChangeFeedOptions() {
    }

    public ChangeFeedOptions(ChangeFeedOptions options) {
        this.partitionKeyRangeId = options.partitionKeyRangeId;
        this.startFromBeginning = options.startFromBeginning;
        this.startDateTime = options.startDateTime;
        this.maxItemCount = options.maxItemCount;
        this.requestContinuation = options.requestContinuation;
        this.partitionkey = options.partitionkey;
        this.populateQueryMetrics = options.populateQueryMetrics;
    }

    /**
     * Get the partition key range id for the current request
     * <p>
     * ChangeFeed requests can be executed against specific partition key ranges.
     * This is used to process the change feed in parallel across multiple
     * consumers.
     * </p>
     *
     * @return a string indicating the partition key range ID
     * @see PartitionKeyRange
     */
    String partitionKeyRangeId() {
        return partitionKeyRangeId;
    }

    /**
     * Set the partition key range id for the current request
     * <p>
     * ChangeFeed requests can be executed against specific partition key ranges.
     * This is used to process the change feed in parallel across multiple
     * consumers.
     * </p>
     *
     * @param partitionKeyRangeId a string indicating the partition key range ID
     * @see PartitionKeyRange
     * @return the ChangeFeedOptions.
     */
    ChangeFeedOptions partitionKeyRangeId(String partitionKeyRangeId) {
        this.partitionKeyRangeId = partitionKeyRangeId;
        return this;
    }

    /**
     * Get whether change feed should start from beginning (true) or from current
     * (false). By default it's start from current (false).
     *
     * @return a boolean value indicating change feed should start from beginning or
     *         not
     */
    public boolean startFromBeginning() {
        return startFromBeginning;
    }

    /**
     * Set whether change feed should start from beginning (true) or from current
     * (false). By default it's start from current (false).
     *
     * @param startFromBeginning a boolean value indicating change feed should start
     *                           from beginning or not
     * @return the ChangeFeedOptions.
     */
    public ChangeFeedOptions startFromBeginning(boolean startFromBeginning) {
        this.startFromBeginning = startFromBeginning;
        return this;
    }

    /**
     * Gets the zoned date time to start looking for changes after.
     * 
     * @return a zoned date time to start looking for changes after, if set or null
     *         otherwise
     */
    public OffsetDateTime startDateTime() {
        return startDateTime;
    }

    /**
     * Sets the zoned date time (exclusive) to start looking for changes after. If
     * this is specified, startFromBeginning is ignored.
     * 
     * @param startDateTime a zoned date time to start looking for changes after.
     * @return the ChangeFeedOptions.
     */
    public ChangeFeedOptions startDateTime(OffsetDateTime startDateTime) {
        this.startDateTime = startDateTime;
        return this;
    }

    /**
     * Gets the maximum number of items to be returned in the enumeration
     * operation.
     *
     * @return the max number of items.
     */
    public Integer maxItemCount() {
        return this.maxItemCount;
    }

    /**
     * Sets the maximum number of items to be returned in the enumeration
     * operation.
     *
     * @param maxItemCount the max number of items.
     * @return the FeedOptionsBase.
     */
    public ChangeFeedOptions maxItemCount(Integer maxItemCount) {
        this.maxItemCount = maxItemCount;
        return this;
    }

    /**
     * Gets the request continuation token.
     *
     * @return the request continuation.
     */
    public String requestContinuation() {
        return this.requestContinuation;
    }

    /**
     * Sets the request continuation token.
     *
     * @param requestContinuation
     *            the request continuation.
     * @return the FeedOptionsBase.
     */
    public ChangeFeedOptions requestContinuation(String requestContinuation) {
        this.requestContinuation = requestContinuation;
        return this;
    }

    /**
     * Gets the partition key used to identify the current request's target
     * partition.
     *
     * @return the partition key.
     */
    public PartitionKey partitionKey() {
        return this.partitionkey;
    }

    /**
     * Sets the partition key used to identify the current request's target
     * partition.
     *
     * @param partitionkey
     *            the partition key value.
     * @return the FeedOptionsBase.
     */
    public ChangeFeedOptions partitionKey(PartitionKey partitionkey) {
        this.partitionkey = partitionkey;
        return this;
    }

    /**
     * Gets the option to enable populate query metrics
     * @return whether to enable populate query metrics
     */
    public boolean populateQueryMetrics() {
        return populateQueryMetrics;
    }

    /**
     * Sets the option to enable/disable getting metrics relating to query execution on document query requests
     * @param populateQueryMetrics whether to enable or disable query metrics
     * @return the FeedOptionsBase.
     */
    public ChangeFeedOptions populateQueryMetrics(boolean populateQueryMetrics) {
        this.populateQueryMetrics = populateQueryMetrics;
        return this;
    }

    /**
     * Gets the properties
     *
     * @return Map of request options properties
     */
    public Map<String, Object> properties() {
        return properties;
    }

    /**
     * Sets the properties used to identify the request token.
     *
     * @param properties the properties.
     * @return the FeedOptionsBase.
     */
    public ChangeFeedOptions properties(Map<String, Object> properties) {
        this.properties = properties;
        return this;
    }
}
