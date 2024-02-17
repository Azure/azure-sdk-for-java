// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.models;

import com.azure.cosmos.CosmosDiagnostics;
import com.azure.cosmos.implementation.Configs;
import com.azure.cosmos.implementation.ImplementationBridgeHelpers;
import com.azure.cosmos.implementation.RequestOptions;
import com.azure.cosmos.implementation.Strings;

import java.util.ArrayList;
import java.util.List;

/**
 * Specifies the options associated with query methods (enumeration operations)
 * in the Azure Cosmos DB database service.
 */
public class CosmosQueryRequestOptions extends CosmosQueryRequestOptionsBase<CosmosQueryRequestOptions> {
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
    private boolean emptyPagesAllowed;
    private FeedRange feedRange;
    private boolean queryPlanRetrievalDisallowed;
    private boolean emptyPageDiagnosticsEnabled;
    private String queryName;
    private List<CosmosDiagnostics> cancelledRequestDiagnosticsTracker = new ArrayList<>();

    /**
     * Instantiates a new query request options.
     */
    public CosmosQueryRequestOptions() {
        super();

        this.emptyPageDiagnosticsEnabled = Configs.isEmptyPageDiagnosticsEnabled();
    }

    CosmosQueryRequestOptions(CosmosQueryRequestOptionsBase<?> options) {
        super(options);
    }

    /**
     * Instantiates a new query request options.
     *
     * @param options the options
     */
    CosmosQueryRequestOptions(CosmosQueryRequestOptions options) {
        super(options);

        this.partitionKeyRangeId = options.partitionKeyRangeId;
        this.scanInQueryEnabled = options.scanInQueryEnabled;
        this.emitVerboseTracesInQuery = options.emitVerboseTracesInQuery;
        this.maxDegreeOfParallelism = options.maxDegreeOfParallelism;
        this.maxBufferedItemCount = options.maxBufferedItemCount;
        this.maxItemCount = options.maxItemCount;
        this.requestContinuation = options.requestContinuation;
        this.partitionkey = options.partitionkey;
        this.emptyPagesAllowed = options.emptyPagesAllowed;
        this.queryPlanRetrievalDisallowed = options.queryPlanRetrievalDisallowed;
        this.emptyPageDiagnosticsEnabled = options.emptyPageDiagnosticsEnabled;
        this.queryName = options.queryName;
        this.feedRange = options.feedRange;
        this.cancelledRequestDiagnosticsTracker = options.cancelledRequestDiagnosticsTracker;
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
    public CosmosQueryRequestOptions setFeedRange(FeedRange feedRange) {
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
    String getQueryNameOrDefault(String defaultQueryName) {
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
    public CosmosQueryRequestOptions setQueryName(String queryName) {
        this.queryName = queryName;

        return this;
    }

    CosmosQueryRequestOptions disallowQueryPlanRetrieval() {
        this.queryPlanRetrievalDisallowed = true;

        return this;
    }

    boolean isQueryPlanRetrievalDisallowed() {
        return this.queryPlanRetrievalDisallowed;
    }

    boolean isEmptyPageDiagnosticsEnabled() { return this.emptyPageDiagnosticsEnabled; }

    List<CosmosDiagnostics> getCancelledRequestDiagnosticsTracker() {
        return this.cancelledRequestDiagnosticsTracker;
    }

    void setCancelledRequestDiagnosticsTracker(List<CosmosDiagnostics> cancelledRequestDiagnosticsTracker) {
        this.cancelledRequestDiagnosticsTracker = cancelledRequestDiagnosticsTracker;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////
    // the following helper/accessor only helps to access this class outside of this package.//
    ///////////////////////////////////////////////////////////////////////////////////////////
    static void initialize() {
        ImplementationBridgeHelpers.CosmosQueryRequestOptionsHelper.setCosmosQueryRequestOptionsAccessor(
            new ImplementationBridgeHelpers.CosmosQueryRequestOptionsHelper.CosmosQueryRequestOptionsAccessor() {

                @Override
                public CosmosQueryRequestOptions clone(CosmosQueryRequestOptions toBeCloned) {
                    return new CosmosQueryRequestOptions(toBeCloned);
                }

                @Override
                public CosmosQueryRequestOptions clone(CosmosQueryRequestOptionsBase<?> toBeCloned) {
                    return new CosmosQueryRequestOptions(toBeCloned);
                }

                @Override
                public CosmosQueryRequestOptions disallowQueryPlanRetrieval(
                    CosmosQueryRequestOptions queryRequestOptions) {

                    return queryRequestOptions.disallowQueryPlanRetrieval();
                }

                @Override
                public boolean isQueryPlanRetrievalDisallowed(CosmosQueryRequestOptions queryRequestOptions) {
                    return queryRequestOptions.isQueryPlanRetrievalDisallowed();
                }

                @Override
                public boolean isEmptyPageDiagnosticsEnabled(CosmosQueryRequestOptions queryRequestOptions) {
                    return queryRequestOptions.isEmptyPageDiagnosticsEnabled();
                }

                @Override
                public String getQueryNameOrDefault(CosmosQueryRequestOptions queryRequestOptions,
                                                    String defaultQueryName) {

                    return queryRequestOptions.getQueryNameOrDefault(defaultQueryName);
                }

                @Override
                public RequestOptions toRequestOptions(CosmosQueryRequestOptions queryRequestOptions) {
                    RequestOptions requestOptions = queryRequestOptions.applyToRequestOptions(new RequestOptions());
                    requestOptions.setPartitionKey(queryRequestOptions.getPartitionKey());

                    return requestOptions;
                }

                @Override
                public List<CosmosDiagnostics> getCancelledRequestDiagnosticsTracker(CosmosQueryRequestOptions options) {
                    return options.getCancelledRequestDiagnosticsTracker();
                }

                public void setCancelledRequestDiagnosticsTracker(
                    CosmosQueryRequestOptions options,
                    List<CosmosDiagnostics> cancelledRequestDiagnosticsTracker) {

                    options.setCancelledRequestDiagnosticsTracker(cancelledRequestDiagnosticsTracker);
                }

                @Override
                public void setAllowEmptyPages(CosmosQueryRequestOptions options, boolean emptyPagesAllowed) {
                    options.setEmptyPagesAllowed(emptyPagesAllowed);
                }

                @Override
                public boolean getAllowEmptyPages(CosmosQueryRequestOptions options) {
                    return options.isEmptyPagesAllowed();
                }

                @Override
                public Integer getMaxItemCount(CosmosQueryRequestOptions options) {
                    return options.getMaxItemCount();
                }

                @Override
                public String getRequestContinuation(CosmosQueryRequestOptions options) {
                    return options.getRequestContinuation();
                }
            });
    }

    static { initialize(); }
}
