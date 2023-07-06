// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.models;

import com.azure.cosmos.ConsistencyLevel;
import com.azure.cosmos.CosmosDiagnostics;
import com.azure.cosmos.CosmosDiagnosticsThresholds;
import com.azure.cosmos.CosmosEndToEndOperationLatencyPolicyConfig;
import com.azure.cosmos.implementation.Configs;
import com.azure.cosmos.implementation.CosmosPagedFluxOptions;
import com.azure.cosmos.implementation.ImplementationBridgeHelpers;
import com.azure.cosmos.implementation.RequestOptions;
import com.azure.cosmos.implementation.Strings;
import com.azure.cosmos.implementation.apachecommons.collections.list.UnmodifiableList;
import com.azure.cosmos.implementation.spark.OperationContextAndListenerTuple;
import com.fasterxml.jackson.databind.JsonNode;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

/**
 * Specifies the options associated with query methods (enumeration operations)
 * in the Azure Cosmos DB database service.
 */
public class CosmosQueryRequestOptions {
    private final static ImplementationBridgeHelpers.CosmosDiagnosticsThresholdsHelper.CosmosDiagnosticsThresholdsAccessor thresholdsAccessor =
        ImplementationBridgeHelpers.CosmosDiagnosticsThresholdsHelper.getCosmosAsyncClientAccessor();

    private ConsistencyLevel consistencyLevel;
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
    private FeedRange feedRange;
    private OperationContextAndListenerTuple operationContextAndListenerTuple;
    private String throughputControlGroupName;
    private DedicatedGatewayRequestOptions dedicatedGatewayRequestOptions;
    private CosmosDiagnosticsThresholds thresholds;
    private Map<String, String> customOptions;
    private boolean indexMetricsEnabled;
    private boolean queryPlanRetrievalDisallowed;
    private UUID correlationActivityId;
    private boolean emptyPageDiagnosticsEnabled;
    private Function<JsonNode, ?> itemFactoryMethod;
    private String queryName;
    private CosmosEndToEndOperationLatencyPolicyConfig cosmosEndToEndOperationLatencyPolicyConfig;
    private List<CosmosDiagnostics> cancelledRequestDiagnosticsTracker = new ArrayList<>();
    private List<String> excludeRegions;

    /**
     * Instantiates a new query request options.
     */
    public CosmosQueryRequestOptions() {

        this.thresholds = null;
        this.queryMetricsEnabled = true;
        this.emptyPageDiagnosticsEnabled = Configs.isEmptyPageDiagnosticsEnabled();
    }

    /**
     * Instantiates a new query request options.
     *
     * @param options the options
     */
    CosmosQueryRequestOptions(CosmosQueryRequestOptions options) {
        this.consistencyLevel = options.consistencyLevel;
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
        this.throughputControlGroupName = options.throughputControlGroupName;
        this.operationContextAndListenerTuple = options.operationContextAndListenerTuple;
        this.dedicatedGatewayRequestOptions = options.dedicatedGatewayRequestOptions;
        this.customOptions = options.customOptions;
        this.indexMetricsEnabled = options.indexMetricsEnabled;
        this.queryPlanRetrievalDisallowed = options.queryPlanRetrievalDisallowed;
        this.correlationActivityId = options.correlationActivityId;
        this.emptyPageDiagnosticsEnabled = options.emptyPageDiagnosticsEnabled;
        this.itemFactoryMethod = options.itemFactoryMethod;
        this.queryName = options.queryName;
        this.feedRange = options.feedRange;
        this.thresholds = options.thresholds;
        this.cosmosEndToEndOperationLatencyPolicyConfig = options.cosmosEndToEndOperationLatencyPolicyConfig;
        this.excludeRegions = options.excludeRegions;
        this.cancelledRequestDiagnosticsTracker = options.cancelledRequestDiagnosticsTracker;
    }

    void setOperationContextAndListenerTuple(OperationContextAndListenerTuple operationContextAndListenerTuple) {
        this.operationContextAndListenerTuple = operationContextAndListenerTuple;
    }

    OperationContextAndListenerTuple getOperationContextAndListenerTuple() {
        return this.operationContextAndListenerTuple;
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
     * Gets the consistency level required for the request.
     *
     * @return the consistency level.
     */

    public ConsistencyLevel getConsistencyLevel() {
        return consistencyLevel;
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
        this.consistencyLevel = consistencyLevel;
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
     * Gets the correlation activityId which is used across requests/responses sent in the
     * scope of this query execution. If no correlation activityId is specified (`null`) a
     * random UUID will be generated for each query
     *
     * @return the correlation activityId
     */
    UUID getCorrelationActivityId() {
        return this.correlationActivityId;
    }

    /**
     * Sets the option to allow scan on the queries which couldn't be served as
     * indexing was opted out on the requested paths.
     *
     * @param correlationActivityId the correlation activityId.
     * @return the CosmosQueryRequestOptions.
     */
    CosmosQueryRequestOptions setCorrelationActivityId(UUID correlationActivityId) {
        this.correlationActivityId = correlationActivityId;
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
     * Sets the {@link CosmosEndToEndOperationLatencyPolicyConfig} to be used for the request. If the config is already set
     *      * on the client, then this will override the client level config for this request
     *
     * @param cosmosEndToEndOperationLatencyPolicyConfig the {@link CosmosEndToEndOperationLatencyPolicyConfig}
     * @return the CosmosQueryRequestOptions
     */
    public CosmosQueryRequestOptions setCosmosEndToEndOperationLatencyPolicyConfig(CosmosEndToEndOperationLatencyPolicyConfig cosmosEndToEndOperationLatencyPolicyConfig) {
        this.cosmosEndToEndOperationLatencyPolicyConfig = cosmosEndToEndOperationLatencyPolicyConfig;
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
        this.excludeRegions = excludeRegions;
        return this;
    }

    /**
     * Gets the list of regions to exclude for the request/retries. These regions are excluded
     * from the preferred region list.
     *
     * @return a list of excluded regions
     * */
    public List<String> getExcludedRegions() {
        if (this.excludeRegions == null) {
            return null;
        }
        return UnmodifiableList.unmodifiableList(this.excludeRegions);
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
     * Get throughput control group name.
     * @return The throughput control group name.
     */
    public String getThroughputControlGroupName() {
        return this.throughputControlGroupName;
    }

    /**
     * Set the throughput control group name.
     *
     * @param throughputControlGroupName The throughput control group name.
     * @return A {@link CosmosQueryRequestOptions}.
     */
    public CosmosQueryRequestOptions setThroughputControlGroupName(String throughputControlGroupName) {
        this.throughputControlGroupName = throughputControlGroupName;
        return this;
    }

    /**
     * Gets the Dedicated Gateway Request Options
     * @return the Dedicated Gateway Request Options
     */
    public DedicatedGatewayRequestOptions getDedicatedGatewayRequestOptions() {
        return this.dedicatedGatewayRequestOptions;
    }

    /**
     * Sets the Dedicated Gateway Request Options
     * @param dedicatedGatewayRequestOptions Dedicated Gateway Request Options
     * @return the CosmosQueryRequestOptions
     */
    public CosmosQueryRequestOptions setDedicatedGatewayRequestOptions(DedicatedGatewayRequestOptions dedicatedGatewayRequestOptions) {
        this.dedicatedGatewayRequestOptions = dedicatedGatewayRequestOptions;
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
        if (this.thresholds == null) {
            return CosmosDiagnosticsThresholds.DEFAULT_NON_POINT_OPERATION_LATENCY_THRESHOLD;
        }

        return thresholdsAccessor.getNonPointReadLatencyThreshold(this.thresholds);
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
        if (this.thresholds == null) {
            this.thresholds = new CosmosDiagnosticsThresholds();
        }

        this.thresholds.setNonPointOperationLatencyThreshold(
            thresholdForDiagnosticsOnTracer
        );

        return this;
    }

    /**
     * Allows overriding the diagnostic thresholds for a specific operation.
     * @param operationSpecificThresholds the diagnostic threshold override for this operation
     * @return the CosmosQueryRequestOptions.
     */
    public CosmosQueryRequestOptions setDiagnosticsThresholds(
        CosmosDiagnosticsThresholds operationSpecificThresholds) {

        this.thresholds = operationSpecificThresholds;
        return this;
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
        return indexMetricsEnabled;
    }

    /**
     * Sets indexMetricsEnabled, which is used to obtain the index metrics to understand how the query engine used existing
     * indexes and could use potential new indexes.
     * The results will be displayed in QueryMetrics. Please note that this options will incurs overhead, so it should be
     * enabled when debuging slow queries.
     *
     * By default the indexMetrics are disabled.
     *
     * @param indexMetricsEnabled a boolean used to obtain the index metrics
     * @return indexMetricsEnabled
     */
    public CosmosQueryRequestOptions setIndexMetricsEnabled(boolean indexMetricsEnabled) {
        this.indexMetricsEnabled = indexMetricsEnabled;
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

    /**
     * Sets the custom query request option value by key
     *
     * @param name  a string representing the custom option's name
     * @param value a string representing the custom option's value
     *
     * @return the CosmosQueryRequestOptions.
     */
    CosmosQueryRequestOptions setHeader(String name, String value) {
        if (this.customOptions == null) {
            this.customOptions = new HashMap<>();
        }
        this.customOptions.put(name, value);
        return this;
    }

    /**
     * Gets the custom query request options
     *
     * @return Map of custom request options
     */
    Map<String, String> getHeaders() {
        return this.customOptions;
    }

    CosmosQueryRequestOptions disallowQueryPlanRetrieval() {
        this.queryPlanRetrievalDisallowed = true;

        return this;
    }

    boolean isQueryPlanRetrievalDisallowed() {
        return this.queryPlanRetrievalDisallowed;
    }

    boolean isEmptyPageDiagnosticsEnabled() { return this.emptyPageDiagnosticsEnabled; }

    CosmosQueryRequestOptions setEmptyPageDiagnosticsEnabled(boolean emptyPageDiagnosticsEnabled) {
        this.emptyPageDiagnosticsEnabled = emptyPageDiagnosticsEnabled;
        return this;
    }

    CosmosQueryRequestOptions withEmptyPageDiagnosticsEnabled(boolean emptyPageDiagnosticsEnabled) {
        if (this.emptyPageDiagnosticsEnabled == emptyPageDiagnosticsEnabled)
        {
            return this;
        }

        return new CosmosQueryRequestOptions(this).setEmptyPageDiagnosticsEnabled(emptyPageDiagnosticsEnabled);
    }

    Function<JsonNode, ?> getItemFactoryMethod() { return this.itemFactoryMethod; }

    CosmosQueryRequestOptions setItemFactoryMethod(Function<JsonNode, ?> factoryMethod) {
        this.itemFactoryMethod = factoryMethod;

        return this;
    }

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
                public void setOperationContext(CosmosQueryRequestOptions queryRequestOptions,
                                                OperationContextAndListenerTuple operationContextAndListenerTuple) {
                    queryRequestOptions.setOperationContextAndListenerTuple(operationContextAndListenerTuple);
                }

                @Override
                public OperationContextAndListenerTuple getOperationContext(CosmosQueryRequestOptions queryRequestOptions) {
                    if (queryRequestOptions == null) {
                        return null;
                    }

                    return queryRequestOptions.getOperationContextAndListenerTuple();
                }

                @Override
                public CosmosQueryRequestOptions setHeader(CosmosQueryRequestOptions queryRequestOptions, String name
                    , String value) {
                    return queryRequestOptions.setHeader(name, value);
                }

                @Override
                public Map<String, String> getHeader(CosmosQueryRequestOptions queryRequestOptions) {
                    return queryRequestOptions.getHeaders();
                }

                @Override
                public CosmosQueryRequestOptions disallowQueryPlanRetrieval(
                    CosmosQueryRequestOptions queryRequestOptions) {

                    return queryRequestOptions.disallowQueryPlanRetrieval();
                }

                @Override
                public UUID getCorrelationActivityId(CosmosQueryRequestOptions queryRequestOptions) {
                    if (queryRequestOptions == null) {
                        return null;
                    }

                    return queryRequestOptions.getCorrelationActivityId();
                }

                @Override
                public CosmosQueryRequestOptions setCorrelationActivityId(
                    CosmosQueryRequestOptions queryRequestOptions, UUID correlationActivityId) {

                    return queryRequestOptions.setCorrelationActivityId(correlationActivityId);
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
                public CosmosQueryRequestOptions setEmptyPageDiagnosticsEnabled(CosmosQueryRequestOptions queryRequestOptions, boolean emptyPageDiagnosticsEnabled) {
                    return queryRequestOptions.setEmptyPageDiagnosticsEnabled(emptyPageDiagnosticsEnabled);
                }

                @Override
                public CosmosQueryRequestOptions withEmptyPageDiagnosticsEnabled(CosmosQueryRequestOptions queryRequestOptions, boolean emptyPageDiagnosticsEnabled) {
                    return queryRequestOptions.withEmptyPageDiagnosticsEnabled(emptyPageDiagnosticsEnabled);
                }

                @Override
                @SuppressWarnings("unchecked")
                public <T> Function<JsonNode, T> getItemFactoryMethod(
                    CosmosQueryRequestOptions queryRequestOptions, Class<T> classOfT) {

                    return (Function<JsonNode, T>)queryRequestOptions.getItemFactoryMethod();
                }

                @Override
                public CosmosQueryRequestOptions setItemFactoryMethod(
                    CosmosQueryRequestOptions queryRequestOptions,
                    Function<JsonNode, ?> factoryMethod) {

                    return queryRequestOptions.setItemFactoryMethod(factoryMethod);
                }

                @Override
                public String getQueryNameOrDefault(CosmosQueryRequestOptions queryRequestOptions,
                                                    String defaultQueryName) {

                    return queryRequestOptions.getQueryNameOrDefault(defaultQueryName);
                }

                @Override
                public RequestOptions toRequestOptions(CosmosQueryRequestOptions queryRequestOptions) {
                    RequestOptions requestOptions = new RequestOptions();
                    requestOptions.setConsistencyLevel(queryRequestOptions.getConsistencyLevel());
                    requestOptions.setSessionToken(queryRequestOptions.getSessionToken());
                    requestOptions.setPartitionKey(queryRequestOptions.getPartitionKey());
                    requestOptions.setThroughputControlGroupName(queryRequestOptions.getThroughputControlGroupName());
                    requestOptions.setOperationContextAndListenerTuple(queryRequestOptions.getOperationContextAndListenerTuple());
                    requestOptions.setDedicatedGatewayRequestOptions(queryRequestOptions.getDedicatedGatewayRequestOptions());
                    if (queryRequestOptions.thresholds != null) {
                        requestOptions.setDiagnosticsThresholds(queryRequestOptions.thresholds);
                    }
                    requestOptions.setCosmosEndToEndLatencyPolicyConfig(queryRequestOptions.cosmosEndToEndOperationLatencyPolicyConfig);
                    requestOptions.setExcludeRegions(queryRequestOptions.excludeRegions);

                    if (queryRequestOptions.customOptions != null) {
                        for(Map.Entry<String, String> entry : queryRequestOptions.customOptions.entrySet()) {
                            requestOptions.setHeader(entry.getKey(), entry.getValue());
                        }
                    }

                    return requestOptions;
                }

                @Override
                public CosmosDiagnosticsThresholds getDiagnosticsThresholds(CosmosQueryRequestOptions options) {
                    return options.thresholds;
                }

                @Override
                public void applyMaxItemCount(
                    CosmosQueryRequestOptions requestOptions,
                    CosmosPagedFluxOptions fluxOptions) {

                    if (requestOptions == null || requestOptions.getMaxItemCount() == null || fluxOptions == null) {
                        return;
                    }

                    if (fluxOptions.getMaxItemCount() != null) {
                        return;
                    }

                    fluxOptions.setMaxItemCount(requestOptions.getMaxItemCount());
                }

                @Override
                public CosmosEndToEndOperationLatencyPolicyConfig getEndToEndOperationLatencyPolicyConfig(CosmosQueryRequestOptions options) {
                    return options.getEndToEndOperationLatencyConfig();
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
                public List<String> getExcludeRegions(CosmosQueryRequestOptions options) {
                    return options.getExcludedRegions();
                }
            });
    }

    private CosmosEndToEndOperationLatencyPolicyConfig getEndToEndOperationLatencyConfig() {
        return cosmosEndToEndOperationLatencyPolicyConfig;
    }

    static { initialize(); }
}
