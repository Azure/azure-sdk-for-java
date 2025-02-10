// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.models;

import com.azure.cosmos.ConsistencyLevel;
import com.azure.cosmos.CosmosDiagnosticsThresholds;
import com.azure.cosmos.CosmosEndToEndOperationLatencyPolicyConfig;
import com.azure.cosmos.CosmosItemSerializer;
import com.azure.cosmos.implementation.CosmosQueryRequestOptionsBase;
import com.azure.cosmos.implementation.CosmosReadManyRequestOptionsImpl;
import com.azure.cosmos.implementation.ImplementationBridgeHelpers;

import java.time.Duration;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Specifies the options associated with read many operation
 * in the Azure Cosmos DB database service.
 */
public final class CosmosReadManyRequestOptions {
    private final CosmosReadManyRequestOptionsImpl actualRequestOptions;

    /**
     * Instantiates a new read many request options.
     */
    public CosmosReadManyRequestOptions() {

        this.actualRequestOptions = new CosmosReadManyRequestOptionsImpl();
    }

    /**
     * Instantiates a new read many request options.
     *
     * @param options the options
     */
    CosmosReadManyRequestOptions(CosmosReadManyRequestOptions options) {
        this.actualRequestOptions = new CosmosReadManyRequestOptionsImpl(options.actualRequestOptions);
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
     * @return the CosmosReadManyRequestOptions.
     */
    public CosmosReadManyRequestOptions setConsistencyLevel(ConsistencyLevel consistencyLevel) {
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
     * @return the CosmosReadManyRequestOptions.
     */
    public CosmosReadManyRequestOptions setSessionToken(String sessionToken) {
        this.actualRequestOptions.setSessionToken(sessionToken);
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
     * @return the CosmosReadManyRequestOptions.
     */
    public CosmosReadManyRequestOptions setResponseContinuationTokenLimitInKb(int limitInKb) {
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
     * @return the CosmosReadManyRequestOptions
     */
    public CosmosReadManyRequestOptions setCosmosEndToEndOperationLatencyPolicyConfig(CosmosEndToEndOperationLatencyPolicyConfig cosmosEndToEndOperationLatencyPolicyConfig) {
        this.actualRequestOptions.setCosmosEndToEndOperationLatencyPolicyConfig(cosmosEndToEndOperationLatencyPolicyConfig);
        return this;
    }

    /**
     * List of regions to be excluded for the request/retries. Example "East US" or "East US, West US"
     * These regions will be excluded from the preferred regions list
     *
     * @param excludeRegions the regions to exclude
     * @return the {@link CosmosReadManyRequestOptions}
     */
    public CosmosReadManyRequestOptions setExcludedRegions(List<String> excludeRegions) {
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
     * @return the CosmosReadManyRequestOptions.
     */
    public CosmosReadManyRequestOptions setQueryMetricsEnabled(boolean queryMetricsEnabled) {
        this.actualRequestOptions.setQueryMetricsEnabled(queryMetricsEnabled);
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
     * @return A {@link CosmosReadManyRequestOptions}.
     */
    public CosmosReadManyRequestOptions setThroughputControlGroupName(String throughputControlGroupName) {
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
     * @return the CosmosReadManyRequestOptions
     */
    public CosmosReadManyRequestOptions setDedicatedGatewayRequestOptions(DedicatedGatewayRequestOptions dedicatedGatewayRequestOptions) {
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
     * @return the CosmosReadManyRequestOptions
     */
    public CosmosReadManyRequestOptions setThresholdForDiagnosticsOnTracer(Duration thresholdForDiagnosticsOnTracer) {
        this.actualRequestOptions.setThresholdForDiagnosticsOnTracer(thresholdForDiagnosticsOnTracer);

        return this;
    }

    /**
     * Allows overriding the diagnostic thresholds for a specific operation.
     * @param operationSpecificThresholds the diagnostic threshold override for this operation
     * @return the CosmosReadManyRequestOptions.
     */
    public CosmosReadManyRequestOptions setDiagnosticsThresholds(
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
    public CosmosReadManyRequestOptions setIndexMetricsEnabled(boolean indexMetricsEnabled) {
        this.actualRequestOptions.setIndexMetricsEnabled(indexMetricsEnabled);
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
    public CosmosReadManyRequestOptions setCustomItemSerializer(CosmosItemSerializer customItemSerializer) {
        this.actualRequestOptions.setCustomItemSerializer(customItemSerializer);

        return this;
    }

    /**
     * Sets the custom ids.
     *
     * @param keywordIdentifiers the custom ids.
     * @return the current request options.
     */
    public CosmosReadManyRequestOptions setKeywordIdentifiers(Set<String> keywordIdentifiers) {
        this.actualRequestOptions.setKeywordIdentifiers(keywordIdentifiers);
        return this;
    }

    /**
     * Gets the custom ids.
     *
     * @return the custom ids.
     */
    public Set<String> getKeywordIdentifiers() {
        return this.actualRequestOptions.getKeywordIdentifiers();
    }

    CosmosQueryRequestOptionsBase<?> getImpl() {
        return this.actualRequestOptions;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////
    // the following helper/accessor only helps to access this class outside of this package.//
    ///////////////////////////////////////////////////////////////////////////////////////////
    static void initialize() {
        ImplementationBridgeHelpers.CosmosReadManyRequestOptionsHelper.setCosmosReadManyRequestOptionsAccessor(
            new ImplementationBridgeHelpers.CosmosReadManyRequestOptionsHelper.CosmosReadManyRequestOptionsAccessor() {
                @Override
                public CosmosQueryRequestOptionsBase<?> getImpl(CosmosReadManyRequestOptions options) {
                    return options.actualRequestOptions;
                }
            });
    }

    static { initialize(); }
}
