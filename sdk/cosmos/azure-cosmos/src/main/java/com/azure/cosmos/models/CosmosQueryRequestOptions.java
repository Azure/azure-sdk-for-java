// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.models;

import java.util.Map;

/**
 * Specifies the options associated with query methods (enumeration operations)
 * in the Azure Cosmos DB database service.
 */
public class CosmosQueryRequestOptions {
    private String sessionToken;
    private String partitionKeyRangeId;
    private Boolean scanInQueryEnabled;
    private Boolean emitVerboseTracesInQuery;
    private int maxDegreeOfParallelism;
    private int maxBufferedItemCount;
    private int responseContinuationTokenLimitInKb;
    private Integer maxItemCount;
    private String requestContinuation;
    private PartitionKey partitionkey;
    private boolean queryMetricsEnabled;
    private Map<String, Object> properties;
    private boolean emptyPagesAllowed;

    /**
     * Instantiates a new query request options.
     */
    public CosmosQueryRequestOptions() {
        this.queryMetricsEnabled = true;
    }

    /**
     * Instantiates a new query request options.
     *
     * @param options the options
     */
    CosmosQueryRequestOptions(CosmosQueryRequestOptions options) {
        this.sessionToken = options.sessionToken;
        this.partitionKeyRangeId = options.partitionKeyRangeId;
        this.scanInQueryEnabled = options.scanInQueryEnabled;
        this.emitVerboseTracesInQuery = options.emitVerboseTracesInQuery;
        this.maxDegreeOfParallelism = options.maxDegreeOfParallelism;
        this.maxBufferedItemCount = options.maxBufferedItemCount;
        this.responseContinuationTokenLimitInKb = options.responseContinuationTokenLimitInKb;
        this.maxItemCount = options.maxItemCount;
        this.requestContinuation = options.requestContinuation;
        this.partitionkey = options.partitionkey;
        this.queryMetricsEnabled = options.queryMetricsEnabled;
        this.emptyPagesAllowed = options.emptyPagesAllowed;
    }

    /**
     * Gets the partitionKeyRangeId.
     *
     * @return the partitionKeyRangeId.
     */
    String getPartitionKeyRangeIdInternal() {
        return this.partitionKeyRangeId;
    }

    /**
     * Sets the partitionKeyRangeId.
     *
     * @param partitionKeyRangeId the partitionKeyRangeId.
     * @return the CosmosQueryRequestOptions.
     */
    CosmosQueryRequestOptions setPartitionKeyRangeIdInternal(String partitionKeyRangeId) {
        this.partitionKeyRangeId = partitionKeyRangeId;
        return this;
    }

    /**
     * Gets the session token for use with session consistency.
     *
     * @return the session token.
     */
    public String getSessionToken() {
        return this.sessionToken;
    }

    /**
     * Sets the session token for use with session consistency.
     *
     * @param sessionToken the session token.
     * @return the CosmosQueryRequestOptions.
     */
    public CosmosQueryRequestOptions setSessionToken(String sessionToken) {
        this.sessionToken = sessionToken;
        return this;
    }

    /**
     * Gets the option to allow scan on the queries which couldn't be served as
     * indexing was opted out on the requested paths.
     *
     * @return the option of enable scan in query.
     */
    public Boolean isScanInQueryEnabled() {
        return this.scanInQueryEnabled;
    }

    /**
     * Sets the option to allow scan on the queries which couldn't be served as
     * indexing was opted out on the requested paths.
     *
     * @param scanInQueryEnabled the option of enable scan in query.
     * @return the CosmosQueryRequestOptions.
     */
    public CosmosQueryRequestOptions setScanInQueryEnabled(Boolean scanInQueryEnabled) {
        this.scanInQueryEnabled = scanInQueryEnabled;
        return this;
    }

    /**
     * Gets the option to allow queries to emit out verbose traces for
     * investigation.
     *
     * @return the emit verbose traces in query.
     */
    Boolean isEmitVerboseTracesInQuery() {
        return this.emitVerboseTracesInQuery;
    }

    /**
     * Sets the option to allow queries to emit out verbose traces for
     * investigation.
     *
     * @param emitVerboseTracesInQuery the emit verbose traces in query.
     * @return the CosmosQueryRequestOptions.
     */
    CosmosQueryRequestOptions setEmitVerboseTracesInQuery(Boolean emitVerboseTracesInQuery) {
        this.emitVerboseTracesInQuery = emitVerboseTracesInQuery;
        return this;
    }

    /**
     * Gets the number of concurrent operations run client side during parallel
     * query execution.
     *
     * @return number of concurrent operations run client side during parallel query
     * execution.
     */
    public int getMaxDegreeOfParallelism() {
        return maxDegreeOfParallelism;
    }

    /**
     * Sets the number of concurrent operations run client side during parallel
     * query execution.
     *
     * @param maxDegreeOfParallelism number of concurrent operations.
     * @return the CosmosQueryRequestOptions.
     */
    public CosmosQueryRequestOptions setMaxDegreeOfParallelism(int maxDegreeOfParallelism) {
        this.maxDegreeOfParallelism = maxDegreeOfParallelism;
        return this;
    }

    /**
     * Gets the maximum number of items that can be buffered client side during
     * parallel query execution.
     *
     * @return maximum number of items that can be buffered client side during
     * parallel query execution.
     */
    public int getMaxBufferedItemCount() {
        return maxBufferedItemCount;
    }

    /**
     * Sets the maximum number of items that can be buffered client side during
     * parallel query execution.
     *
     * @param maxBufferedItemCount maximum number of items.
     * @return the CosmosQueryRequestOptions.
     */
    public CosmosQueryRequestOptions setMaxBufferedItemCount(int maxBufferedItemCount) {
        this.maxBufferedItemCount = maxBufferedItemCount;
        return this;
    }

    /**
     * Sets the ResponseContinuationTokenLimitInKb request option for item query
     * requests in the Azure Cosmos DB service.
     * <p>
     * ResponseContinuationTokenLimitInKb is used to limit the length of
     * continuation token in the query response. Valid values are &gt;= 1.
     * <p>
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
     * @return the CosmosQueryRequestOptions.
     */
    public CosmosQueryRequestOptions setResponseContinuationTokenLimitInKb(int limitInKb) {
        this.responseContinuationTokenLimitInKb = limitInKb;
        return this;
    }

    /**
     * Gets the ResponseContinuationTokenLimitInKb request option for item query
     * requests in the Azure Cosmos DB service. If not already set returns 0.
     * <p>
     * ResponseContinuationTokenLimitInKb is used to limit the length of
     * continuation token in the query response. Valid values are &gt;= 1.
     *
     * @return return set ResponseContinuationTokenLimitInKb, or 0 if not set
     */
    public int getResponseContinuationTokenLimitInKb() {
        return responseContinuationTokenLimitInKb;
    }


    /**
     * Gets the maximum number of items to be returned in the enumeration
     * operation.
     *
     * @return the max number of items.
     */
    Integer getMaxItemCount() {
        return this.maxItemCount;
    }

    /**
     * Sets the maximum number of items to be returned in the enumeration
     * operation.
     *
     * @param maxItemCount the max number of items.
     * @return the CosmosQueryRequestOptions.
     */
    CosmosQueryRequestOptions setMaxItemCount(Integer maxItemCount) {
        this.maxItemCount = maxItemCount;
        return this;
    }

    /**
     * Gets the request continuation token.
     *
     * @return the request continuation.
     */
    String getRequestContinuation() {
        return this.requestContinuation;
    }

    /**
     * Sets the request continuation token.
     *
     * @param requestContinuation the request continuation.
     * @return the CosmosQueryRequestOptions.
     */
    CosmosQueryRequestOptions setRequestContinuation(String requestContinuation) {
        this.requestContinuation = requestContinuation;
        return this;
    }

    /**
     * Gets the partition key used to identify the current request's target
     * partition.
     *
     * @return the partition key.
     */
    public PartitionKey getPartitionKey() {
        return this.partitionkey;
    }

    /**
     * Sets the partition key used to identify the current request's target
     * partition.
     *
     * @param partitionkey the partition key value.
     * @return the CosmosQueryRequestOptions.
     */
    public CosmosQueryRequestOptions setPartitionKey(PartitionKey partitionkey) {
        this.partitionkey = partitionkey;
        return this;
    }

    /**
     * Gets the option to enable populate query metrics. By default query metrics are enabled.
     *
     * @return whether to enable populate query metrics (default: true)
     */
    public boolean isQueryMetricsEnabled() {
        return queryMetricsEnabled;
    }

    /**
     * Sets the option to enable/disable getting metrics relating to query execution on item query requests.
     * By default query metrics are enabled.
     *
     * @param queryMetricsEnabled whether to enable or disable query metrics
     * @return the CosmosQueryRequestOptions.
     */
    public CosmosQueryRequestOptions setQueryMetricsEnabled(boolean queryMetricsEnabled) {
        this.queryMetricsEnabled = queryMetricsEnabled;
        return this;
    }

    /**
     * Gets the properties
     *
     * @return Map of request options properties
     */
    Map<String, Object> getProperties() {
        return properties;
    }

    /**
     * Sets the properties used to identify the request token.
     *
     * @param properties the properties.
     * @return the CosmosQueryRequestOptions.
     */
    CosmosQueryRequestOptions setProperties(Map<String, Object> properties) {
        this.properties = properties;
        return this;
    }

    /**
     * Gets the option to allow empty result pages in feed response.
     *
     * @return whether to enable allow empty pages or not
     */
    boolean isEmptyPagesAllowed() {
        return emptyPagesAllowed;
    }

    /**
     * Sets the option to allow empty result pages in feed response. Defaults to false
     * @param emptyPagesAllowed whether to allow empty pages in feed response
     * @return the CosmosQueryRequestOptions.
     */
    CosmosQueryRequestOptions setEmptyPagesAllowed(boolean emptyPagesAllowed) {
        this.emptyPagesAllowed = emptyPagesAllowed;
        return this;
    }
}
