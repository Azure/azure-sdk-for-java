// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation;

import com.azure.cosmos.ConsistencyLevel;
import com.azure.cosmos.CosmosDiagnosticsThresholds;
import com.azure.cosmos.CosmosEndToEndOperationLatencyPolicyConfig;
import com.azure.cosmos.CosmosItemSerializer;
import com.azure.cosmos.implementation.apachecommons.collections.list.UnmodifiableList;
import com.azure.cosmos.implementation.spark.OperationContextAndListenerTuple;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.CosmosRequestOptions;
import com.azure.cosmos.models.DedicatedGatewayRequestOptions;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Specifies the options associated with readMany methods
 * in the Azure Cosmos DB database service.
 */
public abstract class CosmosQueryRequestOptionsBase<T extends CosmosQueryRequestOptionsBase<?>> implements OverridableRequestOptions {
    private final static ImplementationBridgeHelpers.CosmosDiagnosticsThresholdsHelper.CosmosDiagnosticsThresholdsAccessor thresholdsAccessor =
        ImplementationBridgeHelpers.CosmosDiagnosticsThresholdsHelper.getCosmosAsyncClientAccessor();

    private ConsistencyLevel consistencyLevel;
    private String sessionToken;
    private int responseContinuationTokenLimitInKb;
    private boolean queryMetricsEnabled;
    private Map<String, Object> properties;
    private OperationContextAndListenerTuple operationContextAndListenerTuple;
    private String throughputControlGroupName;
    private DedicatedGatewayRequestOptions dedicatedGatewayRequestOptions;
    private CosmosDiagnosticsThresholds thresholds;
    private Map<String, String> customOptions;
    private boolean indexMetricsEnabled;
    private UUID correlationActivityId;
    private CosmosEndToEndOperationLatencyPolicyConfig cosmosEndToEndOperationLatencyPolicyConfig;
    private List<String> excludeRegions;
    private CosmosItemSerializer customSerializer;

    /**
     * Instantiates a new query request options.
     */
    protected CosmosQueryRequestOptionsBase() {

        this.thresholds = null;
        this.queryMetricsEnabled = true;
    }

    /**
     * Instantiates a new query request options.
     *
     * @param options the options
     */
    protected CosmosQueryRequestOptionsBase(CosmosQueryRequestOptionsBase<?> options) {
        this.consistencyLevel = options.consistencyLevel;
        this.sessionToken = options.sessionToken;
        this.responseContinuationTokenLimitInKb = options.responseContinuationTokenLimitInKb;
        this.queryMetricsEnabled = options.queryMetricsEnabled;
        this.throughputControlGroupName = options.throughputControlGroupName;
        this.operationContextAndListenerTuple = options.operationContextAndListenerTuple;
        this.dedicatedGatewayRequestOptions = options.dedicatedGatewayRequestOptions;
        this.customOptions = options.customOptions;
        this.indexMetricsEnabled = options.indexMetricsEnabled;
        this.correlationActivityId = options.correlationActivityId;
        this.thresholds = options.thresholds;
        this.cosmosEndToEndOperationLatencyPolicyConfig = options.cosmosEndToEndOperationLatencyPolicyConfig;
        this.excludeRegions = options.excludeRegions;
        this.properties = options.properties;
        this.customSerializer = options.customSerializer;
    }

    public void setOperationContextAndListenerTuple(OperationContextAndListenerTuple operationContextAndListenerTuple) {
        this.operationContextAndListenerTuple = operationContextAndListenerTuple;
    }

    public OperationContextAndListenerTuple getOperationContextAndListenerTuple() {
        return this.operationContextAndListenerTuple;
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
     * can only be reduced for read/query requests. So when the Account's default consistency level
     * is for example Session you can specify on a request-by-request level for individual requests
     * that Eventual consistency is sufficient - which could reduce the latency and RU charges for this
     * request but will not guarantee session consistency (read-your-own-write) anymore
     *
     * @param consistencyLevel the consistency level.
     * @return the CosmosItemRequestOptions.
     */
    @SuppressWarnings("unchecked")
    public T setConsistencyLevel(ConsistencyLevel consistencyLevel) {
        this.consistencyLevel = consistencyLevel;
        return (T)this;
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
    @SuppressWarnings("unchecked")
    public T setSessionToken(String sessionToken) {
        this.sessionToken = sessionToken;
        return (T)this;
    }

    /**
     * Gets the correlation activityId which is used across requests/responses sent in the
     * scope of this query execution. If no correlation activityId is specified (`null`) a
     * random UUID will be generated for each query
     *
     * @return the correlation activityId
     */
    public UUID getCorrelationActivityId() {
        return this.correlationActivityId;
    }

    /**
     * Sets the option to allow scan on the queries which couldn't be served as
     * indexing was opted out on the requested paths.
     *
     * @param correlationActivityId the correlation activityId.
     * @return the CosmosQueryRequestOptions.
     */
    @SuppressWarnings("unchecked")
    public T setCorrelationActivityId(UUID correlationActivityId) {
        this.correlationActivityId = correlationActivityId;
        return (T)this;
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
    @SuppressWarnings("unchecked")
    public T setResponseContinuationTokenLimitInKb(int limitInKb) {
        this.responseContinuationTokenLimitInKb = limitInKb;
        return (T)this;
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
    @Override
    public Integer getResponseContinuationTokenLimitInKb() {
        return responseContinuationTokenLimitInKb;
    }

    /**
     * Sets the {@link CosmosEndToEndOperationLatencyPolicyConfig} to be used for the request. If the config is already set
     *      * on the client, then this will override the client level config for this request
     *
     * @param cosmosEndToEndOperationLatencyPolicyConfig the {@link CosmosEndToEndOperationLatencyPolicyConfig}
     * @return the CosmosQueryRequestOptions
     */
    @SuppressWarnings("unchecked")
    public T setCosmosEndToEndOperationLatencyPolicyConfig(CosmosEndToEndOperationLatencyPolicyConfig cosmosEndToEndOperationLatencyPolicyConfig) {
        this.cosmosEndToEndOperationLatencyPolicyConfig = cosmosEndToEndOperationLatencyPolicyConfig;
        return (T)this;
    }

    /**
     * List of regions to be excluded for the request/retries. Example "East US" or "East US, West US"
     * These regions will be excluded from the preferred regions list
     *
     * @param excludeRegions the regions to exclude
     * @return the {@link CosmosQueryRequestOptions}
     */
    @SuppressWarnings("unchecked")
    public T setExcludedRegions(List<String> excludeRegions) {
        this.excludeRegions = excludeRegions;
        return (T)this;
    }

    /**
     * Gets the list of regions to exclude for the request/retries. These regions are excluded
     * from the preferred region list.
     *
     * @return a list of excluded regions
     * */
    @Override
    public List<String> getExcludedRegions() {
        if (this.excludeRegions == null) {
            return null;
        }
        return UnmodifiableList.unmodifiableList(this.excludeRegions);
    }

    /**
     * Gets the option to enable populate query metrics. By default query metrics are enabled.
     *
     * @return whether to enable populate query metrics (default: true)
     */
    @Override
    public Boolean isQueryMetricsEnabled() {
        return queryMetricsEnabled;
    }

    /**
     * Sets the option to enable/disable getting metrics relating to query execution on item query requests.
     * By default query metrics are enabled.
     *
     * @param queryMetricsEnabled whether to enable or disable query metrics
     * @return the CosmosQueryRequestOptions.
     */
    @SuppressWarnings("unchecked")
    public T setQueryMetricsEnabled(boolean queryMetricsEnabled) {
        this.queryMetricsEnabled = queryMetricsEnabled;
        return (T)this;
    }

    /**
     * Gets the properties
     *
     * @return Map of request options properties
     */
    public Map<String, Object> getProperties() {
        return properties;
    }

    /**
     * Sets the properties used to identify the request token.
     *
     * @param properties the properties.
     * @return the CosmosQueryRequestOptions.
     */
    @SuppressWarnings("unchecked")
    public T setProperties(Map<String, Object> properties) {
        this.properties = properties;
        return (T)this;
    }

    /**
     * Get throughput control group name.
     * @return The throughput control group name.
     */
    @Override
    public String getThroughputControlGroupName() {
        return this.throughputControlGroupName;
    }

    /**
     * Set the throughput control group name.
     *
     * @param throughputControlGroupName The throughput control group name.
     * @return A {@link CosmosQueryRequestOptions}.
     */
    @SuppressWarnings("unchecked")
    public T setThroughputControlGroupName(String throughputControlGroupName) {
        this.throughputControlGroupName = throughputControlGroupName;
        return (T)this;
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
    @SuppressWarnings("unchecked")
    public T setDedicatedGatewayRequestOptions(DedicatedGatewayRequestOptions dedicatedGatewayRequestOptions) {
        this.dedicatedGatewayRequestOptions = dedicatedGatewayRequestOptions;
        return (T)this;
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
    @SuppressWarnings("unchecked")
    public T setThresholdForDiagnosticsOnTracer(Duration thresholdForDiagnosticsOnTracer) {
        if (this.thresholds == null) {
            this.thresholds = new CosmosDiagnosticsThresholds();
        }

        this.thresholds.setNonPointOperationLatencyThreshold(
            thresholdForDiagnosticsOnTracer
        );

        return (T)this;
    }

    /**
     * Allows overriding the diagnostic thresholds for a specific operation.
     * @param operationSpecificThresholds the diagnostic threshold override for this operation
     * @return the CosmosQueryRequestOptions.
     */
    @SuppressWarnings("unchecked")
    public T setDiagnosticsThresholds(
        CosmosDiagnosticsThresholds operationSpecificThresholds) {

        this.thresholds = operationSpecificThresholds;
        return (T)this;
    }

    /**
     * Gets indexMetricsEnabled, which is used to obtain the index metrics to understand how the query engine used existing
     * indexes and could use potential new indexes.
     * The results will be displayed in QueryMetrics. Please note that this options will incurs overhead, so it should be
     * enabled when debuging slow queries.
     *
     * @return indexMetricsEnabled (default: false)
     */
    @Override
    public Boolean isIndexMetricsEnabled() {
        return indexMetricsEnabled;
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
    @SuppressWarnings("unchecked")
    public T setIndexMetricsEnabled(boolean indexMetricsEnabled) {
        this.indexMetricsEnabled = indexMetricsEnabled;
        return (T)this;
    }

    /**
     * Sets the custom query request option value by key
     *
     * @param name  a string representing the custom option's name
     * @param value a string representing the custom option's value
     *
     * @return the CosmosQueryRequestOptions.
     */
    @SuppressWarnings("unchecked")
    public T setHeader(String name, String value) {
        if (this.customOptions == null) {
            this.customOptions = new HashMap<>();
        }
        this.customOptions.put(name, value);
        return (T)this;
    }

    /**
     * Gets the custom query request options
     *
     * @return Map of custom request options
     */
    public Map<String, String> getHeaders() {
        return this.customOptions;
    }

    @Override
    public CosmosDiagnosticsThresholds getDiagnosticsThresholds() {
        return this.thresholds;
    }

    @Override
    public CosmosEndToEndOperationLatencyPolicyConfig getCosmosEndToEndLatencyPolicyConfig() {
        return cosmosEndToEndOperationLatencyPolicyConfig;
    }

    /**
     * Gets the custom item serializer defined for this instance of request options
     * @return the custom item serializer
     */
    public CosmosItemSerializer getCustomItemSerializer() {
        return this.customSerializer;
    }

    /**
     * Allows specifying a custom item serializer to be used for this operation. If the serializer
     * on the request options is null, the serializer on CosmosClientBuilder is used. If both serializers
     * are null (the default), an internal Jackson ObjectMapper is ued for serialization/deserialization.
     * @param customItemSerializer the custom item serializer for this operation
     * @return  the CosmosItemRequestOptions.
     */
    @SuppressWarnings("unchecked")
    public T setCustomItemSerializer(CosmosItemSerializer customItemSerializer) {
        this.customSerializer = customItemSerializer;

        return (T)this;
    }

    @Override
    public void override(CosmosRequestOptions cosmosRequestOptions) {
        this.consistencyLevel = overrideOption(cosmosRequestOptions.getConsistencyLevel(), this.consistencyLevel);
        this.throughputControlGroupName = overrideOption(cosmosRequestOptions.getThroughputControlGroupName(), this.throughputControlGroupName);
        this.dedicatedGatewayRequestOptions = overrideOption(cosmosRequestOptions.getDedicatedGatewayRequestOptions(), this.dedicatedGatewayRequestOptions);
        this.cosmosEndToEndOperationLatencyPolicyConfig = overrideOption(cosmosRequestOptions.getCosmosEndToEndLatencyPolicyConfig(), this.cosmosEndToEndOperationLatencyPolicyConfig);
        this.excludeRegions = overrideOption(cosmosRequestOptions.getExcludedRegions(), this.excludeRegions);
        this.thresholds = overrideOption(cosmosRequestOptions.getDiagnosticsThresholds(), this.thresholds);
        this.indexMetricsEnabled = overrideOption(cosmosRequestOptions.isIndexMetricsEnabled(), this.indexMetricsEnabled);
        this.queryMetricsEnabled = overrideOption(cosmosRequestOptions.isQueryMetricsEnabled(), this.queryMetricsEnabled);
        this.responseContinuationTokenLimitInKb = overrideOption(cosmosRequestOptions.getResponseContinuationTokenLimitInKb(), this.responseContinuationTokenLimitInKb);
    }

    public RequestOptions applyToRequestOptions(RequestOptions requestOptions) {
        requestOptions.setConsistencyLevel(this.getConsistencyLevel());
        requestOptions.setSessionToken(this.getSessionToken());
        requestOptions.setThroughputControlGroupName(this.getThroughputControlGroupName());
        requestOptions.setOperationContextAndListenerTuple(this.getOperationContextAndListenerTuple());
        requestOptions.setDedicatedGatewayRequestOptions(this.getDedicatedGatewayRequestOptions());
        if (this.thresholds != null) {
            requestOptions.setDiagnosticsThresholds(this.thresholds);
        }
        requestOptions.setCosmosEndToEndLatencyPolicyConfig(this.cosmosEndToEndOperationLatencyPolicyConfig);
        requestOptions.setExcludedRegions(this.excludeRegions);

        if (this.customOptions != null) {
            for(Map.Entry<String, String> entry : this.customOptions.entrySet()) {
                requestOptions.setHeader(entry.getKey(), entry.getValue());
            }
        }

        return requestOptions;
    }
}
