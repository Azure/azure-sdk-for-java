// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.models;

import com.azure.cosmos.ConsistencyLevel;
import com.azure.cosmos.CosmosDiagnosticsThresholds;
import com.azure.cosmos.CosmosEndToEndOperationLatencyPolicyConfig;
import com.azure.cosmos.implementation.apachecommons.collections.list.UnmodifiableList;

import java.util.List;

/**
 * The common request options for operations. This class should be used with the addPolicy method in the {@link com.azure.cosmos.CosmosClientBuilder}
 *  to change request options without restarting the application.
 *
 */
public final class CosmosRequestOptions {
    private CosmosEndToEndOperationLatencyPolicyConfig endToEndOperationLatencyConfig;
    private ConsistencyLevel consistencyLevel;
    private Boolean contentResponseOnWriteEnabled;
    private Boolean nonIdempotentWriteRetriesEnabled;
    private DedicatedGatewayRequestOptions dedicatedGatewayRequestOptions;
    private String throughputControlGroupName;
    private CosmosDiagnosticsThresholds thresholds;
    private Boolean scanInQueryEnabled;
    private List<String> excludeRegions;
    private Integer maxDegreeOfParallelism;
    private Integer maxBufferedItemCount;
    private Integer responseContinuationTokenLimitInKb;
    private Integer maxItemCount;
    private Boolean queryMetricsEnabled;
    private Boolean indexMetricsEnabled;
    private Integer maxPrefetchPageCount;
    private String queryName;

    /**
     * Sets the CosmosEndToEndLatencyPolicyConfig.
     *
     * @param endToEndOperationLatencyPolicyConfig the CosmosEndToEndLatencyPolicyConfig.
     * @return current CosmosRequestOptions.
     */
    public CosmosRequestOptions setCosmosEndToEndLatencyPolicyConfig(CosmosEndToEndOperationLatencyPolicyConfig endToEndOperationLatencyPolicyConfig) {
        this.endToEndOperationLatencyConfig = endToEndOperationLatencyPolicyConfig;
        return this;
    }

    /**
     * Sets the consistency level.
     *
     * @param consistencyLevel the consistency level.
     * @return current CosmosRequestOptions.
     */
    public CosmosRequestOptions setConsistencyLevel(ConsistencyLevel consistencyLevel) {
        this.consistencyLevel = consistencyLevel;
        return this;
    }

    /**
     * Sets the content response on write enabled.
     *
     * @param contentResponseOnWriteEnabled the content response on write enabled.
     * @return current CosmosRequestOptions.
     */
    public CosmosRequestOptions setContentResponseOnWriteEnabled(Boolean contentResponseOnWriteEnabled) {
        this.contentResponseOnWriteEnabled = contentResponseOnWriteEnabled;
        return this;
    }

    /**
     * Sets the NonIdempotentWriteRetriesEnabled.
     *
     * @param nonIdempotentWriteRetriesEnabled the NonIdempotentWriteRetriesEnabled.
     * @return current CosmosRequestOptions.
     */
    public CosmosRequestOptions setNonIdempotentWriteRetriesEnabled(Boolean nonIdempotentWriteRetriesEnabled) {
        this.nonIdempotentWriteRetriesEnabled = nonIdempotentWriteRetriesEnabled;
        return this;
    }

    /**
     * Sets the DedicatedGatewayRequestOptions.
     *
     * @param dedicatedGatewayRequestOptions the DedicatedGatewayRequestOptions.
     * @return current CosmosRequestOptions.
     */
    public CosmosRequestOptions setDedicatedGatewayRequestOptions(DedicatedGatewayRequestOptions dedicatedGatewayRequestOptions) {
        this.dedicatedGatewayRequestOptions = dedicatedGatewayRequestOptions;
        return this;
    }

    /**
     * Sets the exclude regions.
     *
     * @param excludeRegions the ExcludeRegions.
     * @return current CosmosRequestOptions.
     */
    public CosmosRequestOptions setExcludeRegions(List<String> excludeRegions) {
        this.excludeRegions = excludeRegions;
        return this;
    }

    /**
     * Sets the throughput control group name.
     *
     * @param throughputControlGroupName the ThroughputControlGroupName.
     * @return current CosmosRequestOptions.
     */
    public CosmosRequestOptions setThroughputControlGroupName(String throughputControlGroupName) {
        this.throughputControlGroupName = throughputControlGroupName;
        return this;
    }

    /**
     * Sets the CosmosDiagnosticsThresholds.
     *
     * @param thresholds the CosmosDiagnosticsThresholds.
     * @return current CosmosRequestOptions.
     */
    public CosmosRequestOptions setDiagnosticsThresholds(CosmosDiagnosticsThresholds thresholds) {
        this.thresholds = thresholds;
        return this;
    }

    /**
     * Sets the ScanInQueryEnabled.
     *
     * @param scanInQueryEnabled the ScanInQueryEnabled.
     * @return current CosmosRequestOptions.
     */
    public CosmosRequestOptions setScanInQueryEnabled(Boolean scanInQueryEnabled) {
        this.scanInQueryEnabled = scanInQueryEnabled;
        return this;
    }

    /**
     * Sets the MaxDegreeOfParallelism.
     *
     * @param maxDegreeOfParallelism the MaxDegreeOfParallelism.
     * @return current CosmosRequestOptions.
     */
    public CosmosRequestOptions setMaxDegreeOfParallelism(Integer maxDegreeOfParallelism) {
        this.maxDegreeOfParallelism = maxDegreeOfParallelism;
        return this;
    }

    /**
     * Sets the MaxBufferedItemCount.
     *
     * @param maxBufferedItemCount the MaxBufferedItemCount.
     * @return current CosmosRequestOptions.
     */
    public CosmosRequestOptions setMaxBufferedItemCount(Integer maxBufferedItemCount) {
        this.maxBufferedItemCount = maxBufferedItemCount;
        return this;
    }

    /**
     * Sets the ResponseContinuationTokenLimitInKb.
     *
     * @param responseContinuationTokenLimitInKb the ResponseContinuationTokenLimitInKb.
     * @return current CosmosRequestOptions.
     */
    public CosmosRequestOptions setResponseContinuationTokenLimitInKb(Integer responseContinuationTokenLimitInKb) {
        this.responseContinuationTokenLimitInKb = responseContinuationTokenLimitInKb;
        return this;
    }

    /**
     * Sets the MaxItemCount.
     *
     * @param maxItemCount the MaxItemCount.
     * @return current CosmosRequestOptions.
     */
    public CosmosRequestOptions setMaxItemCount(Integer maxItemCount) {
        this.maxItemCount = maxItemCount;
        return this;
    }

    /**
     * Sets the QueryMetricsEnabled.
     *
     * @param queryMetricsEnabled the QueryMetricsEnabled.
     * @return current CosmosRequestOptions.
     */
    public CosmosRequestOptions setQueryMetricsEnabled(Boolean queryMetricsEnabled) {
        this.queryMetricsEnabled = queryMetricsEnabled;
        return this;
    }

    /**
     * Sets the IndexMetricsEnabled.
     *
     * @param indexMetricsEnabled the IndexMetricsEnabled.
     * @return current CosmosRequestOptions.
     */
    public CosmosRequestOptions setIndexMetricsEnabled(Boolean indexMetricsEnabled) {
        this.indexMetricsEnabled = indexMetricsEnabled;
        return this;
    }

    /**
     * Sets the MaxPrefetchPageCount.
     *
     * @param maxPrefetchPageCount the MaxPrefetchPageCount.
     * @return current CosmosRequestOptions.
     */
    public CosmosRequestOptions setMaxPrefetchPageCount(Integer maxPrefetchPageCount) {
        this.maxPrefetchPageCount = maxPrefetchPageCount;
        return this;
    }

    /**
     * Sets the QueryName.
     *
     * @param queryName the QueryName.
     * @return current CosmosRequestOptions.
     */
    public CosmosRequestOptions setQueryName(String queryName) {
        this.queryName = queryName;
        return this;
    }

    /**
     * Gets the max item count.
     *
     * @return the max item count.
     */
    public Integer getMaxItemCount() {
        return this.maxItemCount;
    }

    /**
     * Gets the query metrics enabled.
     *
     * @return the query metrics enabled.
     */
    public Boolean isQueryMetricsEnabled() {
        return this.queryMetricsEnabled;
    }

    /**
     * Gets the index metrics enabled.
     *
     * @return the index metrics enabled.
     */
    public Boolean isIndexMetricsEnabled() {
        return this.indexMetricsEnabled;
    }

    /**
     * Gets the query name.
     *
     * @return the query name.
     */
    public String getQueryName() {
        return this.queryName;
    }

    /**
     * Gets the max prefetch page count.
     *
     * @return the max prefetch page count.
     */
    public Integer getMaxPrefetchPageCount() {
        return this.maxPrefetchPageCount;
    }

    /**
     * Gets the response continuation token limit in KB.
     *
     * @return the response continuation token limit in KB.
     */
    public Integer getResponseContinuationTokenLimitInKb() {
        return this.responseContinuationTokenLimitInKb;
    }

    /**
     * Gets the max buffered item count.
     *
     * @return the max buffered item count.
     */
    public Integer getMaxBufferedItemCount() {
        return this.maxBufferedItemCount;
    }

    /**
     * Gets the max degree of parallelism.
     *
     * @return the max degree of parallelism.
     */
    public Integer getMaxDegreeOfParallelism() {
        return this.maxDegreeOfParallelism;
    }

    /**
     * Gets the exclude regions.
     *
     * @return the exclude regions.
     */
    public List<String> getExcludedRegions() {
         if (this.excludeRegions == null) {
            return null;
        }
        return UnmodifiableList.unmodifiableList(this.excludeRegions);
    }

    /**
     * Gets the scan in query enabled.
     *
     * @return the scan in query enabled.
     */
    public Boolean isScanInQueryEnabled() {
        return this.scanInQueryEnabled;
    }

    /**
     * Gets the diagnostics thresholds.
     *
     * @return the diagnostics thresholds.
     */
    public CosmosDiagnosticsThresholds getDiagnosticsThresholds() {
        return this.thresholds;
    }

    /**
     * Gets the throughput control group name.
     *
     * @return the throughput control group name.
     */
    public String getThroughputControlGroupName() {
        return this.throughputControlGroupName;
    }

    /**
     * Gets the dedicated gateway request options.
     *
     * @return the dedicated gateway request options.
     */
    public DedicatedGatewayRequestOptions getDedicatedGatewayRequestOptions() {
        return this.dedicatedGatewayRequestOptions;
    }

    /**
     * Gets the non idempotent write retries enabled.
     *
     * @return the non idempotent write retries enabled.
     */
    public Boolean getNonIdempotentWriteRetriesEnabled() {
        return this.nonIdempotentWriteRetriesEnabled;
    }

    /**
     * Gets the content response on write enabled.
     *
     * @return the content response on write enabled.
     */
    public Boolean isContentResponseOnWriteEnabled() {
        return this.contentResponseOnWriteEnabled;
    }

    /**
     * Gets the consistency level.
     *
     * @return the consistency level.
     */
    public ConsistencyLevel getConsistencyLevel() {
        return this.consistencyLevel;
    }

    /**
     * Gets the CosmosEndToEndLatencyPolicyConfig.
     *
     * @return the CosmosEndToEndLatencyPolicyConfig.
     */
    public CosmosEndToEndOperationLatencyPolicyConfig getCosmosEndToEndLatencyPolicyConfig(){
        return this.endToEndOperationLatencyConfig;
    }
}
