// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation;

import com.azure.cosmos.CosmosDiagnostics;
import com.azure.cosmos.models.CosmosRequestOptions;
import com.azure.cosmos.models.FeedRange;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.models.PartitionKeyDefinition;

import java.util.ArrayList;
import java.util.List;

public final class CosmosQueryRequestOptionsImpl extends CosmosQueryRequestOptionsBase<CosmosQueryRequestOptionsImpl> {
    private final static ImplementationBridgeHelpers.CosmosDiagnosticsThresholdsHelper.CosmosDiagnosticsThresholdsAccessor thresholdsAccessor =
        ImplementationBridgeHelpers.CosmosDiagnosticsThresholdsHelper.getCosmosAsyncClientAccessor();
    private String partitionKeyRangeId;
    private Boolean scanInQueryEnabled;
    private Boolean emitVerboseTracesInQuery;
    private int maxDegreeOfParallelism;
    private int maxBufferedItemCount;
    private Integer maxItemCount;
    private String requestContinuation;
    private PartitionKey partitionkey;
    private PartitionKeyDefinition partitionKeyDefinition;
    private boolean emptyPagesAllowed;
    private FeedRange feedRange;
    private boolean queryPlanRetrievalDisallowed;
    private boolean emptyPageDiagnosticsEnabled;
    private String queryName;
    private Integer maxItemCountForVectorSearch;
    private Integer maxItemCountForHybridSearch;
    private List<CosmosDiagnostics> cancelledRequestDiagnosticsTracker = new ArrayList<>();
    private String collectionRid;
    private String operationId;

    /**
     * Instantiates a new query request options.
     */
    public CosmosQueryRequestOptionsImpl() {
        super();

        this.emptyPageDiagnosticsEnabled = Configs.isEmptyPageDiagnosticsEnabled();
    }

    public CosmosQueryRequestOptionsImpl(CosmosQueryRequestOptionsBase<?> options) {
        super(options);
    }

    /**
     * Instantiates a new query request options.
     *
     * @param options the options
     */
    public CosmosQueryRequestOptionsImpl(CosmosQueryRequestOptionsImpl options) {
        super(options);

        this.partitionKeyRangeId = options.partitionKeyRangeId;
        this.scanInQueryEnabled = options.scanInQueryEnabled;
        this.emitVerboseTracesInQuery = options.emitVerboseTracesInQuery;
        this.maxDegreeOfParallelism = options.maxDegreeOfParallelism;
        this.maxBufferedItemCount = options.maxBufferedItemCount;
        this.maxItemCount = options.maxItemCount;
        this.requestContinuation = options.requestContinuation;
        this.partitionkey = options.partitionkey;
        this.partitionKeyDefinition = options.partitionKeyDefinition;
        this.emptyPagesAllowed = options.emptyPagesAllowed;
        this.queryPlanRetrievalDisallowed = options.queryPlanRetrievalDisallowed;
        this.emptyPageDiagnosticsEnabled = options.emptyPageDiagnosticsEnabled;
        this.queryName = options.queryName;
        this.feedRange = options.feedRange;
        this.cancelledRequestDiagnosticsTracker = options.cancelledRequestDiagnosticsTracker;
        this.maxItemCountForVectorSearch = options.maxItemCountForVectorSearch;
        this.maxItemCountForHybridSearch = options.maxItemCountForHybridSearch;
        this.collectionRid = options.collectionRid;
        this.operationId = options.operationId;
    }

    /**
     * Gets the partitionKeyRangeId.
     *
     * @return the partitionKeyRangeId.
     */
    public String getPartitionKeyRangeIdInternal() {
        return this.partitionKeyRangeId;
    }

    /**
     * Sets the partitionKeyRangeId.
     *
     * @param partitionKeyRangeId the partitionKeyRangeId.
     * @return the CosmosQueryRequestOptions.
     */
    public CosmosQueryRequestOptionsImpl setPartitionKeyRangeIdInternal(String partitionKeyRangeId) {
        this.partitionKeyRangeId = partitionKeyRangeId;
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

    /**
     * Gets the option to allow scan on the queries which couldn't be served as
     * indexing was opted out on the requested paths.
     *
     * @return the option of enable scan in query.
     */
    @Override
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
    public CosmosQueryRequestOptionsImpl setScanInQueryEnabled(Boolean scanInQueryEnabled) {
        this.scanInQueryEnabled = scanInQueryEnabled;
        return this;
    }

    /**
     * Gets the option to allow queries to emit out verbose traces for
     * investigation.
     *
     * @return the emit verbose traces in query.
     */
    public Boolean isEmitVerboseTracesInQuery() {
        return this.emitVerboseTracesInQuery;
    }

    /**
     * Sets the option to allow queries to emit out verbose traces for
     * investigation.
     *
     * @param emitVerboseTracesInQuery the emit verbose traces in query.
     * @return the CosmosQueryRequestOptions.
     */
    public CosmosQueryRequestOptionsImpl setEmitVerboseTracesInQuery(Boolean emitVerboseTracesInQuery) {
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
    @Override
    public Integer getMaxDegreeOfParallelism() {
        return maxDegreeOfParallelism;
    }

    /**
     * Sets the number of concurrent operations run client side during parallel
     * query execution.
     *
     * @param maxDegreeOfParallelism number of concurrent operations.
     * @return the CosmosQueryRequestOptions.
     */
    public CosmosQueryRequestOptionsImpl setMaxDegreeOfParallelism(int maxDegreeOfParallelism) {
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
    @Override
    public Integer getMaxBufferedItemCount() {
        return maxBufferedItemCount;
    }

    /**
     * Sets the maximum number of items that can be buffered client side during
     * parallel query execution.
     *
     * @param maxBufferedItemCount maximum number of items.
     * @return the CosmosQueryRequestOptions.
     */
    public CosmosQueryRequestOptionsImpl setMaxBufferedItemCount(int maxBufferedItemCount) {
        this.maxBufferedItemCount = maxBufferedItemCount;
        return this;
    }

    /**
     * Gets the maximum number of items to be returned in the enumeration
     * operation.
     *
     * @return the max number of items.
     */
    @Override
    public Integer getMaxItemCount() {
        return this.maxItemCount;
    }

    @Override
    public Integer getMaxPrefetchPageCount() {
        return null;
    }

    /**
     * Sets the maximum number of items to be returned in the enumeration
     * operation.
     *
     * @param maxItemCount the max number of items.
     * @return the CosmosQueryRequestOptions.
     */
    public CosmosQueryRequestOptionsImpl setMaxItemCount(Integer maxItemCount) {
        this.maxItemCount = maxItemCount;
        return this;
    }

    /**
     * Gets the maximum item size to fetch during non-streaming order by queries.
     *
     * @return the max number of items for vector search.
     */
    public Integer getMaxItemCountForVectorSearch() {
        if (this.maxItemCountForVectorSearch == null) {
            this.maxItemCountForVectorSearch = Configs.DEFAULT_MAX_ITEM_COUNT_FOR_VECTOR_SEARCH;
        }
        return this.maxItemCountForVectorSearch;
    }

    /**
     * Gets the maximum item size to fetch during hybrid search queries.
     *
     * @return the max number of items for hybrid search.
     */
    public Integer getMaxItemCountForHybridSearch() {
        return this.maxItemCountForHybridSearch != null ? this.maxItemCountForHybridSearch : Configs.getMaxItemCountForHybridSearchSearch();
    }

    /**
     * Sets the maximum item size to fetch during non-streaming order by queries.
     *
     * @param maxItemCountForVectorSearch the max number of items for vector search.
     * return the CosmosQueryRequestOptions.
     */
    public CosmosQueryRequestOptionsImpl setMaxItemCountForVectorSearch(Integer maxItemCountForVectorSearch) {
        this.maxItemCountForVectorSearch = maxItemCountForVectorSearch;
        return this;
    }

    /**
     * Gets the request continuation token.
     *
     * @return the request continuation.
     */
    public String getRequestContinuation() {
        return this.requestContinuation;
    }

    /**
     * Sets the request continuation token.
     *
     * @param requestContinuation the request continuation.
     * @return the CosmosQueryRequestOptions.
     */
    public CosmosQueryRequestOptionsImpl setRequestContinuation(String requestContinuation) {
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
    public CosmosQueryRequestOptionsImpl setPartitionKey(PartitionKey partitionkey) {
        this.partitionkey = partitionkey;
        return this;
    }

    /**
     * Gets the option to allow empty result pages in feed response.
     *
     * @return whether to enable allow empty pages or not
     */
    public boolean isEmptyPagesAllowed() {
        return emptyPagesAllowed;
    }

    /**
     * Sets the option to allow empty result pages in feed response. Defaults to false
     * @param emptyPagesAllowed whether to allow empty pages in feed response
     * @return the CosmosQueryRequestOptions.
     */
    public CosmosQueryRequestOptionsImpl setEmptyPagesAllowed(boolean emptyPagesAllowed) {
        this.emptyPagesAllowed = emptyPagesAllowed;
        return this;
    }

    /**
     * Gets the {@link FeedRange}
     * @return the {@link FeedRange}
     */
    public FeedRange getFeedRange() {
        return feedRange;
    }

    /**
     * Sets the {@link FeedRange} that we want to query
     * @param feedRange the {@link FeedRange}
     * @return the CosmosQueryRequestOptions.
     */
    public CosmosQueryRequestOptionsImpl setFeedRange(FeedRange feedRange) {
        this.feedRange = feedRange;
        return this;
    }

    /**
     * Gets the logical query name - this identifier is only used for metrics and logs
     * to distinguish different queries in telemetry. Cardinality of unique  values for queryName should be
     * reasonably low - like significantly smaller than 100.
     *
     * @param defaultQueryName the default query name that should be used if none is specified on request options
     * @return the logical query name
     */
    @Override
    public String getQueryNameOrDefault(String defaultQueryName) {
        return !Strings.isNullOrWhiteSpace(queryName) ? queryName : defaultQueryName;
    }

    /**
     * Sets the logical query name - this identifier is only used for metrics and logs
     * to distinguish different queries in telemetry. Cardinality of unique  values for queryName should be
     * reasonably low - like significantly smaller than 100.
     *
     * @param queryName a logical query name to distinguish this query pattern from others
     * @return the logical query name
     */
    public CosmosQueryRequestOptionsImpl setQueryName(String queryName) {
        this.queryName = queryName;

        return this;
    }

    public CosmosQueryRequestOptionsImpl disallowQueryPlanRetrieval() {
        this.queryPlanRetrievalDisallowed = true;

        return this;
    }

    public boolean isQueryPlanRetrievalDisallowed() {
        return this.queryPlanRetrievalDisallowed;
    }

    public boolean isEmptyPageDiagnosticsEnabled() { return this.emptyPageDiagnosticsEnabled; }

    public List<CosmosDiagnostics> getCancelledRequestDiagnosticsTracker() {
        return this.cancelledRequestDiagnosticsTracker;
    }

    public void setCancelledRequestDiagnosticsTracker(List<CosmosDiagnostics> cancelledRequestDiagnosticsTracker) {
        this.cancelledRequestDiagnosticsTracker = cancelledRequestDiagnosticsTracker;
    }

    public void setPartitionKeyDefinition(PartitionKeyDefinition partitionKeyDefinition) {
        this.partitionKeyDefinition = partitionKeyDefinition;
    }

    public PartitionKeyDefinition getPartitionKeyDefinition() {
        return this.partitionKeyDefinition;
    }

    @Override
    public void override(CosmosRequestOptions cosmosRequestOptions) {
        super.override(cosmosRequestOptions);
        this.scanInQueryEnabled = overrideOption(cosmosRequestOptions.isScanInQueryEnabled(), this.scanInQueryEnabled);
        this.maxDegreeOfParallelism = overrideOption(cosmosRequestOptions.getMaxDegreeOfParallelism(), this.maxDegreeOfParallelism);
        this.maxBufferedItemCount = overrideOption(cosmosRequestOptions.getMaxBufferedItemCount(), this.maxBufferedItemCount);
        this.maxItemCount = overrideOption(cosmosRequestOptions.getMaxItemCount(), this.maxItemCount);
        this.queryName = overrideOption(cosmosRequestOptions.getQueryName(), this.queryName);
    }

    public String getCollectionRid() {
        return collectionRid;
    }

    public void setCollectionRid(String collectionRid) {
        this.collectionRid = collectionRid;
    }

    public String getOperationId() {
        return operationId;
    }

    public void setOperationId(String operationId) {
        this.operationId = operationId;
    }
}
