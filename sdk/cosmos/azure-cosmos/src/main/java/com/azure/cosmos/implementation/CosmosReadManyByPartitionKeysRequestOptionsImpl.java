// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation;

/**
 * Internal implementation backing the public {@code CosmosReadManyByPartitionKeysRequestOptions}
 * facade. Holds state specific to the {@code readManyByPartitionKeys} operation.
 */
public class CosmosReadManyByPartitionKeysRequestOptionsImpl
    extends CosmosQueryRequestOptionsBase<CosmosReadManyByPartitionKeysRequestOptionsImpl> {

    private String continuationToken;
    private Integer maxConcurrentBatchPrefetch;
    private Integer maxItemCount;
    private Integer maxBatchSize;

    public CosmosReadManyByPartitionKeysRequestOptionsImpl() {
        super();
    }

    public CosmosReadManyByPartitionKeysRequestOptionsImpl(CosmosReadManyByPartitionKeysRequestOptionsImpl options) {
        super(options);
        this.continuationToken = options.continuationToken;
        this.maxConcurrentBatchPrefetch = options.maxConcurrentBatchPrefetch;
        this.maxItemCount = options.maxItemCount;
        this.maxBatchSize = options.maxBatchSize;
    }

    /**
     * Gets the composite continuation token for readManyByPartitionKeys.
     *
     * @return the continuation token, or null if not set.
     */
    public String getContinuationToken() {
        return this.continuationToken;
    }

    /**
     * Sets the composite continuation token for readManyByPartitionKeys.
     *
     * @param continuationToken the continuation token from a previous invocation.
     * @return this instance.
     */
    public CosmosReadManyByPartitionKeysRequestOptionsImpl setContinuationToken(String continuationToken) {
        this.continuationToken = continuationToken;
        return this;
    }

    /**
     * Gets the maximum number of per-physical-partition batches whose first page is
     * prefetched concurrently. {@code null} means the SDK default applies.
     *
     * @return the max concurrent batch prefetch, or null if not set.
     */
    public Integer getMaxConcurrentBatchPrefetch() {
        return this.maxConcurrentBatchPrefetch;
    }

    /**
     * Sets the maximum number of per-physical-partition batches whose first page is
     * prefetched concurrently.
     *
     * @param maxConcurrentBatchPrefetch the max concurrent batch prefetch (must be &gt;= 1).
     * @return this instance.
     */
    public CosmosReadManyByPartitionKeysRequestOptionsImpl setMaxConcurrentBatchPrefetch(int maxConcurrentBatchPrefetch) {
        this.maxConcurrentBatchPrefetch = maxConcurrentBatchPrefetch;
        return this;
    }

    @Override
    public Integer getMaxItemCount() {
        return this.maxItemCount;
    }

    public CosmosReadManyByPartitionKeysRequestOptionsImpl setMaxItemCount(Integer maxItemCount) {
        this.maxItemCount = maxItemCount;
        return this;
    }

    public Integer getMaxBatchSize() {
        return this.maxBatchSize;
    }

    public CosmosReadManyByPartitionKeysRequestOptionsImpl setMaxBatchSize(int maxBatchSize) {
        this.maxBatchSize = maxBatchSize;
        return this;
    }

    @Override
    public Boolean isContentResponseOnWriteEnabled() {
        return null;
    }

    @Override
    public Boolean getNonIdempotentWriteRetriesEnabled() {
        return null;
    }

    @Override
    public Boolean isScanInQueryEnabled() {
        return null;
    }

    @Override
    public Integer getMaxDegreeOfParallelism() {
        return null;
    }

    @Override
    public Integer getMaxBufferedItemCount() {
        return null;
    }

    @Override
    public Integer getMaxPrefetchPageCount() {
        return null;
    }

    @Override
    public String getQueryNameOrDefault(String defaultQueryName) {
        return defaultQueryName;
    }
}
