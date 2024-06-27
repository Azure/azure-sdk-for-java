// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.models;

import com.azure.cosmos.ConsistencyLevel;
import com.azure.cosmos.CosmosDiagnostics;
import com.azure.cosmos.CosmosDiagnosticsThresholds;
import com.azure.cosmos.CosmosEndToEndOperationLatencyPolicyConfig;
import com.azure.cosmos.CosmosItemSerializer;
import com.azure.cosmos.implementation.CosmosQueryRequestOptionsBase;
import com.azure.cosmos.implementation.CosmosQueryRequestOptionsImpl;
import com.azure.cosmos.implementation.ImplementationBridgeHelpers;
import com.azure.cosmos.implementation.RequestOptions;

import java.time.Duration;
import java.util.List;

/**
 * Specifies the options associated with query methods (enumeration operations)
 * in the Azure Cosmos DB database service.
 */
public class CosmosQueryRequestOptions {
    private final CosmosQueryRequestOptionsImpl actualRequestOptions;

    /**
     * Instantiates a new query request options.
     */
    public CosmosQueryRequestOptions() {

        this.actualRequestOptions = new CosmosQueryRequestOptionsImpl();
    }

    /**
     * Instantiates a new query request options.
     *
     * @param options the options
     */
    CosmosQueryRequestOptions(CosmosQueryRequestOptions options) {
        this.actualRequestOptions = new CosmosQueryRequestOptionsImpl(options.actualRequestOptions);
    }

    /**
     * Instantiates a new query request options.
     *
     * @param options the options
     */
    CosmosQueryRequestOptions(CosmosQueryRequestOptionsBase<?> options) {
        this.actualRequestOptions = new CosmosQueryRequestOptionsImpl(options);
    }

    /**
     * Gets the consistency level required for the request.
     *
     * @return the consistency level.
     */

    public ConsistencyLevel getConsistencyLevel() {
        return this.actualRequestOptions.getConsistencyLevel();
    }

    /**
     * Sets the consistency level required for the request. The effective consistency level
     * can only be reduce for read/query requests. So when the Account's default consistency level
     * is for example Session you can specify on a request-by-request level for individual requests
     * that Eventual consistency is sufficient - which could reduce the latency and RU charges for this
     * request but will not guarantee session consistency (read-your-own-write) anymore
     *
     * @param consistencyLevel the consistency level.
     * @return the CosmosItemRequestOptions.
     */
    public CosmosQueryRequestOptions setConsistencyLevel(ConsistencyLevel consistencyLevel) {
        this.actualRequestOptions.setConsistencyLevel(consistencyLevel);
        return this;
    }

    /**
     * Gets the session token for use with session consistency.
     *
     * @return the session token.
     */
    public String getSessionToken() {
        return this.actualRequestOptions.getSessionToken();
    }

    /**
     * Sets the session token for use with session consistency.
     *
     * @param sessionToken the session token.
     * @return the CosmosQueryRequestOptions.
     */
    public CosmosQueryRequestOptions setSessionToken(String sessionToken) {
        this.actualRequestOptions.setSessionToken(sessionToken);
        return this;
    }

    /**
     * Gets the option to allow scan on the queries which couldn't be served as
     * indexing was opted out on the requested paths.
     *
     * @return the option of enable scan in query.
     */
    public Boolean isScanInQueryEnabled() {
        return this.actualRequestOptions.isScanInQueryEnabled();
    }

    /**
     * Sets the option to allow scan on the queries which couldn't be served as
     * indexing was opted out on the requested paths.
     *
     * @param scanInQueryEnabled the option of enable scan in query.
     * @return the CosmosQueryRequestOptions.
     */
    public CosmosQueryRequestOptions setScanInQueryEnabled(Boolean scanInQueryEnabled) {
        this.actualRequestOptions.setScanInQueryEnabled(scanInQueryEnabled);
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
        return this.actualRequestOptions.getMaxDegreeOfParallelism();
    }

    /**
     * Sets the number of concurrent operations run client side during parallel
     * query execution.
     *
     * @param maxDegreeOfParallelism number of concurrent operations.
     * @return the CosmosQueryRequestOptions.
     */
    public CosmosQueryRequestOptions setMaxDegreeOfParallelism(int maxDegreeOfParallelism) {
        this.actualRequestOptions.setMaxDegreeOfParallelism(maxDegreeOfParallelism);
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
        return this.actualRequestOptions.getMaxBufferedItemCount();
    }

    /**
     * Sets the maximum number of items that can be buffered client side during
     * parallel query execution.
     *
     * @param maxBufferedItemCount maximum number of items.
     * @return the CosmosQueryRequestOptions.
     */
    public CosmosQueryRequestOptions setMaxBufferedItemCount(int maxBufferedItemCount) {
        this.actualRequestOptions.setMaxBufferedItemCount(maxBufferedItemCount);
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
        this.actualRequestOptions.setResponseContinuationTokenLimitInKb(limitInKb);
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
        return this.actualRequestOptions.getResponseContinuationTokenLimitInKb();
    }

    /**
     * Sets the {@link CosmosEndToEndOperationLatencyPolicyConfig} to be used for the request. If the config is already set
     *      * on the client, then this will override the client level config for this request
     *
     * @param cosmosEndToEndOperationLatencyPolicyConfig the {@link CosmosEndToEndOperationLatencyPolicyConfig}
     * @return the CosmosQueryRequestOptions
     */
    public CosmosQueryRequestOptions setCosmosEndToEndOperationLatencyPolicyConfig(CosmosEndToEndOperationLatencyPolicyConfig cosmosEndToEndOperationLatencyPolicyConfig) {
        this.actualRequestOptions.setCosmosEndToEndOperationLatencyPolicyConfig(cosmosEndToEndOperationLatencyPolicyConfig);
        return this;
    }

    /**
     * List of regions to be excluded for the request/retries. Example "East US" or "East US, West US"
     * These regions will be excluded from the preferred regions list
     *
     * @param excludeRegions the regions to exclude
     * @return the {@link CosmosQueryRequestOptions}
     */
    public CosmosQueryRequestOptions setExcludedRegions(List<String> excludeRegions) {
        this.actualRequestOptions.setExcludedRegions(excludeRegions);
        return this;
    }

    /**
     * Gets the list of regions to exclude for the request/retries. These regions are excluded
     * from the preferred region list.
     *
     * @return a list of excluded regions
     * */
    public List<String> getExcludedRegions() {
        return this.actualRequestOptions.getExcludedRegions();
    }

    /**
     * Gets the maximum number of items to be returned in the enumeration
     * operation.
     *
     * @return the max number of items.
     */
    Integer getMaxItemCount() {
        return this.actualRequestOptions.getMaxItemCount();
    }

    /**
     * Sets the maximum number of items to be returned in the enumeration
     * operation.
     *
     * @param maxItemCount the max number of items.
     * @return the CosmosQueryRequestOptions.
     */
    CosmosQueryRequestOptions setMaxItemCount(Integer maxItemCount) {
        this.actualRequestOptions.setMaxItemCount(maxItemCount);
        return this;
    }

    /**
     * Gets the maximum item size to fetch during non-streaming order by queries.
     *
     * @return the max number of items for vector search.
     */
    Integer getMaxItemCountForVectorSearch() {
        return this.actualRequestOptions.getMaxItemCountForVectorSearch();
    }

    /**
     * Gets the request continuation token.
     *
     * @return the request continuation.
     */
    String getRequestContinuation() {
        return this.actualRequestOptions.getRequestContinuation();
    }

    /**
     * Sets the request continuation token.
     *
     * @param requestContinuation the request continuation.
     * @return the CosmosQueryRequestOptions.
     */
    CosmosQueryRequestOptions setRequestContinuation(String requestContinuation) {
        this.actualRequestOptions.setRequestContinuation(requestContinuation);
        return this;
    }

    /**
     * Gets the partition key used to identify the current request's target
     * partition.
     *
     * @return the partition key.
     */
    public PartitionKey getPartitionKey() {
        return this.actualRequestOptions.getPartitionKey();
    }

    /**
     * Sets the partition key used to identify the current request's target
     * partition.
     *
     * @param partitionkey the partition key value.
     * @return the CosmosQueryRequestOptions.
     */
    public CosmosQueryRequestOptions setPartitionKey(PartitionKey partitionkey) {
        this.actualRequestOptions.setPartitionKey(partitionkey);
        return this;
    }

    /**
     * Gets the option to enable populate query metrics. By default query metrics are enabled.
     *
     * @return whether to enable populate query metrics (default: true)
     */
    public boolean isQueryMetricsEnabled() {
        return this.actualRequestOptions.isQueryMetricsEnabled();
    }

    /**
     * Sets the option to enable/disable getting metrics relating to query execution on item query requests.
     * By default query metrics are enabled.
     *
     * @param queryMetricsEnabled whether to enable or disable query metrics
     * @return the CosmosQueryRequestOptions.
     */
    public CosmosQueryRequestOptions setQueryMetricsEnabled(boolean queryMetricsEnabled) {
        this.actualRequestOptions.setQueryMetricsEnabled(queryMetricsEnabled);
        return this;
    }

    /**
     * Gets the {@link FeedRange}
     * @return the {@link FeedRange}
     */
    public FeedRange getFeedRange() {
        return this.actualRequestOptions.getFeedRange();
    }

    /**
     * Sets the {@link FeedRange} that we want to query
     * @param feedRange the {@link FeedRange}
     * @return the CosmosQueryRequestOptions.
     */
    public CosmosQueryRequestOptions setFeedRange(FeedRange feedRange) {
        this.actualRequestOptions.setFeedRange(feedRange);
        return this;
    }

    /**
     * Get throughput control group name.
     * @return The throughput control group name.
     */
    public String getThroughputControlGroupName() {
        return this.actualRequestOptions.getThroughputControlGroupName();
    }

    /**
     * Set the throughput control group name.
     *
     * @param throughputControlGroupName The throughput control group name.
     * @return A {@link CosmosQueryRequestOptions}.
     */
    public CosmosQueryRequestOptions setThroughputControlGroupName(String throughputControlGroupName) {
        this.actualRequestOptions.setThroughputControlGroupName(throughputControlGroupName);
        return this;
    }

    /**
     * Gets the Dedicated Gateway Request Options
     * @return the Dedicated Gateway Request Options
     */
    public DedicatedGatewayRequestOptions getDedicatedGatewayRequestOptions() {
        return this.actualRequestOptions.getDedicatedGatewayRequestOptions();
    }

    /**
     * Sets the Dedicated Gateway Request Options
     * @param dedicatedGatewayRequestOptions Dedicated Gateway Request Options
     * @return the CosmosQueryRequestOptions
     */
    public CosmosQueryRequestOptions setDedicatedGatewayRequestOptions(DedicatedGatewayRequestOptions dedicatedGatewayRequestOptions) {
        this.actualRequestOptions.setDedicatedGatewayRequestOptions(dedicatedGatewayRequestOptions);
        return this;
    }

    /**
     * Gets the thresholdForDiagnosticsOnTracer, if latency on query operation is greater than this
     * diagnostics will be send to open telemetry exporter as events in tracer span of end to end CRUD api.
     *
     * Default is 500 ms.
     *
     * @return  thresholdForDiagnosticsOnTracer the latency threshold for diagnostics on tracer.
     */
    public Duration getThresholdForDiagnosticsOnTracer() {
        return this.actualRequestOptions.getThresholdForDiagnosticsOnTracer();
    }

    /**
     * Sets the thresholdForDiagnosticsOnTracer, if latency on query operation is greater than this
     * diagnostics will be send to open telemetry exporter as events in tracer span of end to end CRUD api.
     *
     * Default is 500 ms
     *
     * @param thresholdForDiagnosticsOnTracer the latency threshold for diagnostics on tracer.
     * @return the CosmosQueryRequestOptions
     */
    public CosmosQueryRequestOptions setThresholdForDiagnosticsOnTracer(Duration thresholdForDiagnosticsOnTracer) {
        this.actualRequestOptions.setThresholdForDiagnosticsOnTracer(thresholdForDiagnosticsOnTracer);

        return this;
    }

    /**
     * Allows overriding the diagnostic thresholds for a specific operation.
     * @param operationSpecificThresholds the diagnostic threshold override for this operation
     * @return the CosmosQueryRequestOptions.
     */
    public CosmosQueryRequestOptions setDiagnosticsThresholds(
        CosmosDiagnosticsThresholds operationSpecificThresholds) {

        this.actualRequestOptions.setDiagnosticsThresholds(operationSpecificThresholds);
        return this;
    }

    /**
     * Gets the diagnostic thresholds used as an override for a specific operation. If no operation specific
     * diagnostic threshold has been specified, this method will return null, although at runtime the default
     * thresholds specified at the client-level will be used.
     * @return the diagnostic thresholds used as an override for a specific operation.
     */
    public CosmosDiagnosticsThresholds getDiagnosticsThresholds() {
        return this.actualRequestOptions.getDiagnosticsThresholds();
    }

    /**
     * Gets indexMetricsEnabled, which is used to obtain the index metrics to understand how the query engine used existing
     * indexes and could use potential new indexes.
     * The results will be displayed in QueryMetrics. Please note that this options will incurs overhead, so it should be
     * enabled when debuging slow queries.
     *
     * @return indexMetricsEnabled (default: false)
     */
    public boolean isIndexMetricsEnabled() {
        return this.actualRequestOptions.isIndexMetricsEnabled();
    }

    /**
     * Sets indexMetricsEnabled, which is used to obtain the index metrics to understand how the query engine used existing
     * indexes and could use potential new indexes.
     * The results will be displayed in QueryMetrics. Please note that this options will incurs overhead, so it should be
     * enabled when debugging slow queries.
     *
     * By default the indexMetrics are disabled.
     *
     * @param indexMetricsEnabled a boolean used to obtain the index metrics
     * @return indexMetricsEnabled
     */
    public CosmosQueryRequestOptions setIndexMetricsEnabled(boolean indexMetricsEnabled) {
        this.actualRequestOptions.setIndexMetricsEnabled(indexMetricsEnabled);
        return this;
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
        this.actualRequestOptions.setQueryName(queryName);

        return this;
    }

    /**
     * Gets the custom item serializer defined for this instance of request options
     * @return the custom item serializer
     */
    public CosmosItemSerializer getCustomItemSerializer() {
        return this.actualRequestOptions.getCustomItemSerializer();
    }

    /**
     * Allows specifying a custom item serializer to be used for this operation. If the serializer
     * on the request options is null, the serializer on CosmosClientBuilder is used. If both serializers
     * are null (the default), an internal Jackson ObjectMapper is ued for serialization/deserialization.
     * @param customItemSerializer the custom item serializer for this operation
     * @return  the CosmosItemRequestOptions.
     */
    public CosmosQueryRequestOptions setCustomItemSerializer(CosmosItemSerializer customItemSerializer) {
        this.actualRequestOptions.setCustomItemSerializer(customItemSerializer);

        return this;
    }

    CosmosQueryRequestOptionsBase<?> getImpl() {
        return this.actualRequestOptions;
    }

    /**
     * Gets the partitionKeyRangeId.
     *
     * @return the partitionKeyRangeId.
     */
    String getPartitionKeyRangeIdInternal() {
        return this.actualRequestOptions.getPartitionKeyRangeIdInternal();
    }

    /**
     * Sets the partitionKeyRangeId.
     *
     * @param partitionKeyRangeId the partitionKeyRangeId.
     * @return the CosmosQueryRequestOptions.
     */
    CosmosQueryRequestOptions setPartitionKeyRangeIdInternal(String partitionKeyRangeId) {
        this.actualRequestOptions.setPartitionKeyRangeIdInternal(partitionKeyRangeId);
        return this;
    }

    PartitionKeyDefinition getPartitionKeyDefinition() {
        return this.actualRequestOptions.getPartitionKeyDefinition();
    }

    CosmosQueryRequestOptions setPartitionKeyDefinition(PartitionKeyDefinition partitionKeyDefinition) {
        this.actualRequestOptions.setPartitionKeyDefinition(partitionKeyDefinition);
        return this;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////
    // the following helper/accessor only helps to access this class outside of this package.//
    ///////////////////////////////////////////////////////////////////////////////////////////
    static void initialize() {
        ImplementationBridgeHelpers.CosmosQueryRequestOptionsHelper.setCosmosQueryRequestOptionsAccessor(
            new ImplementationBridgeHelpers.CosmosQueryRequestOptionsHelper.CosmosQueryRequestOptionsAccessor() {

                @Override
                public CosmosQueryRequestOptionsBase<?> getImpl(CosmosQueryRequestOptions options) {
                    return options.actualRequestOptions;
                }

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

                    queryRequestOptions.actualRequestOptions.disallowQueryPlanRetrieval();
                    return queryRequestOptions;
                }

                @Override
                public boolean isQueryPlanRetrievalDisallowed(CosmosQueryRequestOptions queryRequestOptions) {
                    return queryRequestOptions.actualRequestOptions.isQueryPlanRetrievalDisallowed();
                }

                @Override
                public boolean isEmptyPageDiagnosticsEnabled(CosmosQueryRequestOptions queryRequestOptions) {
                    return queryRequestOptions.actualRequestOptions.isEmptyPageDiagnosticsEnabled();
                }

                @Override
                public String getQueryNameOrDefault(CosmosQueryRequestOptions queryRequestOptions,
                                                    String defaultQueryName) {

                    return queryRequestOptions.actualRequestOptions.getQueryNameOrDefault(defaultQueryName);
                }

                @Override
                public RequestOptions toRequestOptions(CosmosQueryRequestOptions queryRequestOptions) {
                    RequestOptions requestOptions = queryRequestOptions
                        .actualRequestOptions
                        .applyToRequestOptions(new RequestOptions());
                    requestOptions.setPartitionKey(queryRequestOptions.getPartitionKey());

                    return requestOptions;
                }

                @Override
                public List<CosmosDiagnostics> getCancelledRequestDiagnosticsTracker(CosmosQueryRequestOptions options) {
                    return options.actualRequestOptions.getCancelledRequestDiagnosticsTracker();
                }

                public void setCancelledRequestDiagnosticsTracker(
                    CosmosQueryRequestOptions options,
                    List<CosmosDiagnostics> cancelledRequestDiagnosticsTracker) {

                    options
                        .actualRequestOptions
                        .setCancelledRequestDiagnosticsTracker(cancelledRequestDiagnosticsTracker);
                }

                @Override
                public void setAllowEmptyPages(CosmosQueryRequestOptions options, boolean emptyPagesAllowed) {
                    options
                        .actualRequestOptions
                        .setEmptyPagesAllowed(emptyPagesAllowed);
                }

                @Override
                public boolean getAllowEmptyPages(CosmosQueryRequestOptions options) {
                    return options.actualRequestOptions.isEmptyPagesAllowed();
                }

                @Override
                public Integer getMaxItemCount(CosmosQueryRequestOptions options) {
                    return options.getMaxItemCount();
                }

                @Override
                public String getRequestContinuation(CosmosQueryRequestOptions options) {
                    return options.getRequestContinuation();
                }

                @Override
                public Integer getMaxItemCountForVectorSearch(CosmosQueryRequestOptions options) {
                    return options.getMaxItemCountForVectorSearch();
                }

                @Override
                public void setPartitionKeyDefinition(CosmosQueryRequestOptions options, PartitionKeyDefinition partitionKeyDefinition) {
                    options.setPartitionKeyDefinition(partitionKeyDefinition);
                }

                @Override
                public PartitionKeyDefinition getPartitionKeyDefinition(CosmosQueryRequestOptions options) {
                    return options.getPartitionKeyDefinition();

                }
            });
    }

    static { initialize(); }
}
