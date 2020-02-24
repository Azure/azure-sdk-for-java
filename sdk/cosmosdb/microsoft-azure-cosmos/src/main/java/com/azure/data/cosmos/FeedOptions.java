// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.cosmos;

import java.util.Map;

/**
 * Specifies the options associated with feed methods (enumeration operations)
 * in the Azure Cosmos DB database service.
 */
public final class FeedOptions {
    private String sessionToken;
    private String partitionKeyRangeId;
    private Boolean enableScanInQuery;
    private Boolean emitVerboseTracesInQuery;
    private Boolean enableCrossPartitionQuery;
    private int maxDegreeOfParallelism;
    private int maxBufferedItemCount;
    private int responseContinuationTokenLimitInKb;
    private Integer maxItemCount;
    private String requestContinuation;
    private PartitionKey partitionkey;
    private boolean populateQueryMetrics;
    private Map<String, Object> properties;
    private boolean allowEmptyPages;

    public FeedOptions() {
    }

    public FeedOptions(FeedOptions options) {
        this.sessionToken = options.sessionToken;
        this.partitionKeyRangeId = options.partitionKeyRangeId;
        this.enableScanInQuery = options.enableScanInQuery;
        this.emitVerboseTracesInQuery = options.emitVerboseTracesInQuery;
        this.enableCrossPartitionQuery = options.enableCrossPartitionQuery;
        this.maxDegreeOfParallelism = options.maxDegreeOfParallelism;
        this.maxBufferedItemCount = options.maxBufferedItemCount;
        this.responseContinuationTokenLimitInKb = options.responseContinuationTokenLimitInKb;
        this.maxItemCount = options.maxItemCount;
        this.requestContinuation = options.requestContinuation;
        this.partitionkey = options.partitionkey;
        this.populateQueryMetrics = options.populateQueryMetrics;
        this.allowEmptyPages = options.allowEmptyPages;
    }

    /**
     * Gets the partitionKeyRangeId.
     *
     * @return the partitionKeyRangeId.
     */
    String partitionKeyRangeIdInternal() {
        return this.partitionKeyRangeId;
    }

    /**
     * Sets the partitionKeyRangeId.
     *
     * @param partitionKeyRangeId the partitionKeyRangeId.
     * @return the FeedOptions.
     */
    FeedOptions partitionKeyRangeIdInternal(String partitionKeyRangeId) {
        this.partitionKeyRangeId = partitionKeyRangeId;
        return this;
    }

    /**
     * Gets the session token for use with session consistency.
     *
     * @return the session token.
     */
    public String sessionToken() {
        return this.sessionToken;
    }

    /**
     * Sets the session token for use with session consistency.
     *
     * @param sessionToken the session token.
     * @return the FeedOptions.
     */
    public FeedOptions sessionToken(String sessionToken) {
        this.sessionToken = sessionToken;
        return this;
    }

    /**
     * Gets the option to allow scan on the queries which couldn't be served as
     * indexing was opted out on the requested paths.
     *
     * @return the option of enable scan in query.
     */
    public Boolean enableScanInQuery() {
        return this.enableScanInQuery;
    }

    /**
     * Sets the option to allow scan on the queries which couldn't be served as
     * indexing was opted out on the requested paths.
     *
     * @param enableScanInQuery the option of enable scan in query.
     * @return the FeedOptions.
     */
    public FeedOptions enableScanInQuery(Boolean enableScanInQuery) {
        this.enableScanInQuery = enableScanInQuery;
        return this;
    }

    /**
     * Gets the option to allow queries to emit out verbose traces for
     * investigation.
     *
     * @return the emit verbose traces in query.
     */
    public Boolean emitVerboseTracesInQuery() {
        return this.emitVerboseTracesInQuery;
    }

    /**
     * Sets the option to allow queries to emit out verbose traces for
     * investigation.
     *
     * @param emitVerboseTracesInQuery the emit verbose traces in query.
     * @return the FeedOptions.
     */
    public FeedOptions emitVerboseTracesInQuery(Boolean emitVerboseTracesInQuery) {
        this.emitVerboseTracesInQuery = emitVerboseTracesInQuery;
        return this;
    }

    /**
     * Gets the option to allow queries to run across all partitions of the
     * collection.
     *
     * @return whether to allow queries to run across all partitions of the
     *         collection.
     */
    public Boolean enableCrossPartitionQuery() {
        return this.enableCrossPartitionQuery;
    }

    /**
     * Sets the option to allow queries to run across all partitions of the
     * collection.
     *
     * @param enableCrossPartitionQuery whether to allow queries to run across all
     *                                  partitions of the collection.
     * @return the FeedOptions.
     */
    public FeedOptions enableCrossPartitionQuery(Boolean enableCrossPartitionQuery) {
        this.enableCrossPartitionQuery = enableCrossPartitionQuery;
        return this;
    }

    /**
     * Gets the number of concurrent operations run client side during parallel
     * query execution.
     *
     * @return number of concurrent operations run client side during parallel query
     *         execution.
     */
    public int maxDegreeOfParallelism() {
        return maxDegreeOfParallelism;
    }

    /**
     * Sets the number of concurrent operations run client side during parallel
     * query execution.
     *
     * @param maxDegreeOfParallelism number of concurrent operations.
     * @return the FeedOptions.
     */
    public FeedOptions maxDegreeOfParallelism(int maxDegreeOfParallelism) {
        this.maxDegreeOfParallelism = maxDegreeOfParallelism;
        return this;
    }

    /**
     * Gets the maximum number of items that can be buffered client side during
     * parallel query execution.
     *
     * @return maximum number of items that can be buffered client side during
     *         parallel query execution.
     */
    public int maxBufferedItemCount() {
        return maxBufferedItemCount;
    }

    /**
     * Sets the maximum number of items that can be buffered client side during
     * parallel query execution.
     *
     * @param maxBufferedItemCount maximum number of items.
     * @return the FeedOptions.
     */
    public FeedOptions maxBufferedItemCount(int maxBufferedItemCount) {
        this.maxBufferedItemCount = maxBufferedItemCount;
        return this;
    }

    /**
     * Sets the ResponseContinuationTokenLimitInKb request option for document query
     * requests in the Azure Cosmos DB service.
     *
     * ResponseContinuationTokenLimitInKb is used to limit the length of
     * continuation token in the query response. Valid values are &gt;= 1.
     *
     * The continuation token contains both required and optional fields. The
     * required fields are necessary for resuming the execution from where it was
     * stooped. The optional fields may contain serialized index lookup work that
     * was done but not yet utilized. This avoids redoing the work again in
     * subsequent continuations and hence improve the query performance. Setting the
     * maximum continuation size to 1KB, the Azure Cosmos DB service will only
     * serialize required fields. Starting from 2KB, the Azure Cosmos DB service
     * would serialize as much as it could fit till it reaches the maximum specified
     * size.
     *
     * @param limitInKb continuation token size limit.
     * @return the FeedOptions.
     */
    public FeedOptions responseContinuationTokenLimitInKb(int limitInKb) {
        this.responseContinuationTokenLimitInKb = limitInKb;
        return this;
    }

    /**
     * Gets the ResponseContinuationTokenLimitInKb request option for document query
     * requests in the Azure Cosmos DB service. If not already set returns 0.
     *
     * ResponseContinuationTokenLimitInKb is used to limit the length of
     * continuation token in the query response. Valid values are &gt;= 1.
     *
     * @return return set ResponseContinuationTokenLimitInKb, or 0 if not set
     */
    public int responseContinuationTokenLimitInKb() {
        return responseContinuationTokenLimitInKb;
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
    public FeedOptions maxItemCount(Integer maxItemCount) {
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
    public FeedOptions requestContinuation(String requestContinuation) {
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
    public FeedOptions partitionKey(PartitionKey partitionkey) {
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
    public FeedOptions populateQueryMetrics(boolean populateQueryMetrics) {
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
    public FeedOptions properties(Map<String, Object> properties) {
        this.properties = properties;
        return this;
    }

    /**
     * Gets the option to allow empty result pages in feed response.
     */
    public boolean allowEmptyPages() {
        return allowEmptyPages;
    }

    /**
     * Sets the option to allow empty result pages in feed response. Defaults to false
     * @param allowEmptyPages whether to allow empty pages in feed response
     */
    public void allowEmptyPages(boolean allowEmptyPages) {
        this.allowEmptyPages = allowEmptyPages;
    }
}
