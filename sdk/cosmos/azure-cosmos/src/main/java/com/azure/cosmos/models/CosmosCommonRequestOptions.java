package com.azure.cosmos.models;

import com.azure.cosmos.ConsistencyLevel;
import com.azure.cosmos.CosmosDiagnosticsThresholds;
import com.azure.cosmos.CosmosEndToEndOperationLatencyPolicyConfig;
import com.azure.cosmos.implementation.apachecommons.collections.list.UnmodifiableList;

import java.util.List;

public final class CosmosCommonRequestOptions implements ICosmosCommonRequestOptions {
    private CosmosEndToEndOperationLatencyPolicyConfig endToEndOperationLatencyConfig;

    private ConsistencyLevel consistencyLevel;
    private String sessionToken;
    private Boolean contentResponseOnWriteEnabled;
    private boolean nonIdempotentWriteRetriesEnabled = false;
    private DedicatedGatewayRequestOptions dedicatedGatewayRequestOptions;
    private String throughputControlGroupName;
    private CosmosDiagnosticsThresholds thresholds;
    private Boolean scanInQueryEnabled;
    private List<String> excludeRegions;
    private int maxDegreeOfParallelism;
    private int maxBufferedItemCount;
    private int responseContinuationTokenLimitInKb;
    private int maxItemCount;
    private boolean queryMetricsEnabled;
    private boolean indexMetricsEnabled;
    private int maxPrefetchPageCount;
    private String queryName;

    /**
     * Sets the CosmosEndToEndLatencyPolicyConfig.
     *
     * @param endToEndOperationLatencyPolicyConfig the CosmosEndToEndLatencyPolicyConfig.
     * @return current CosmosCommonRequestOptions.
     */
    public CosmosCommonRequestOptions setCosmosEndToEndLatencyPolicyConfig(CosmosEndToEndOperationLatencyPolicyConfig endToEndOperationLatencyPolicyConfig) {
        this.endToEndOperationLatencyConfig = endToEndOperationLatencyPolicyConfig;
        return this;
    }

    /**
     * Sets the consistency level.
     *
     * @param consistencyLevel the consistency level.
     * @return current CosmosCommonRequestOptions.
     */
    public CosmosCommonRequestOptions setConsistencyLevel(ConsistencyLevel consistencyLevel) {
        this.consistencyLevel = consistencyLevel;
        return this;
    }

    /**
     * Sets the session token.
     *
     * @param sessionToken the session token.
     * @return current CosmosCommonRequestOptions.
     */
    public CosmosCommonRequestOptions setSessionToken(String sessionToken) {
        this.sessionToken = sessionToken;
        return this;
    }

    /**
     * Sets the content response on write enabled.
     *
     * @param contentResponseOnWriteEnabled the content response on write enabled.
     * @return current CosmosCommonRequestOptions.
     */
    public CosmosCommonRequestOptions setContentResponseOnWriteEnabled(Boolean contentResponseOnWriteEnabled) {
        this.contentResponseOnWriteEnabled = contentResponseOnWriteEnabled;
        return this;
    }

    /**
     * Sets the NonIdempotentWriteRetriesEnabled.
     *
     * @param nonIdempotentWriteRetriesEnabled the NonIdempotentWriteRetriesEnabled.
     * @return current CosmosCommonRequestOptions.
     */
    public CosmosCommonRequestOptions setNonIdempotentWriteRetriesEnabled(boolean nonIdempotentWriteRetriesEnabled) {
        this.nonIdempotentWriteRetriesEnabled = nonIdempotentWriteRetriesEnabled;
        return this;
    }

    /**
     * Sets the DedicatedGatewayRequestOptions.
     *
     * @param dedicatedGatewayRequestOptions the DedicatedGatewayRequestOptions.
     * @return current CosmosCommonRequestOptions.
     */
    public CosmosCommonRequestOptions setDedicatedGatewayRequestOptions(DedicatedGatewayRequestOptions dedicatedGatewayRequestOptions) {
        this.dedicatedGatewayRequestOptions = dedicatedGatewayRequestOptions;
        return this;
    }

    /**
     * Sets the exclude regions.
     * @param excludeRegions the ExcludeRegions.
     * @return current CosmosCommonRequestOptions.
     */
    public CosmosCommonRequestOptions setExcludeRegions(List<String> excludeRegions) {
        this.excludeRegions = excludeRegions;
        return this;
    }

    /**
     * Sets the throughput control group name.
     * @param throughputControlGroupName the ThroughputControlGroupName.
     * @return current CosmosCommonRequestOptions.
     */
    public CosmosCommonRequestOptions setThroughputControlGroupName(String throughputControlGroupName) {
        this.throughputControlGroupName = throughputControlGroupName;
        return this;
    }

    /**
     * Sets the CosmosDiagnosticsThresholds.
     * @param thresholds the CosmosDiagnosticsThresholds.
     * @return current CosmosCommonRequestOptions.
     */
    public CosmosCommonRequestOptions setThresholds(CosmosDiagnosticsThresholds thresholds) {
        this.thresholds = thresholds;
        return this;
    }

    /**
     * Sets the ScanInQueryEnabled.
     * @param scanInQueryEnabled the ScanInQueryEnabled.
     * @return current CosmosCommonRequestOptions.
     */
    public CosmosCommonRequestOptions setScanInQueryEnabled(Boolean scanInQueryEnabled) {
        this.scanInQueryEnabled = scanInQueryEnabled;
        return this;
    }

    /**
     * Sets the MaxDegreeOfParallelism.
     * @param maxDegreeOfParallelism the MaxDegreeOfParallelism.
     * @return current CosmosCommonRequestOptions.
     */
    public CosmosCommonRequestOptions setMaxDegreeOfParallelism(int maxDegreeOfParallelism) {
        this.maxDegreeOfParallelism = maxDegreeOfParallelism;
        return this;
    }

    /**
     * Sets the MaxBufferedItemCount.
     * @param maxBufferedItemCount the MaxBufferedItemCount.
     * @return current CosmosCommonRequestOptions.
     */
    public CosmosCommonRequestOptions setMaxBufferedItemCount(int maxBufferedItemCount) {
        this.maxBufferedItemCount = maxBufferedItemCount;
        return this;
    }

    /**
     * Sets the ResponseContinuationTokenLimitInKb.
     * @param responseContinuationTokenLimitInKb the ResponseContinuationTokenLimitInKb.
     * @return current CosmosCommonRequestOptions.
     */
    public CosmosCommonRequestOptions setResponseContinuationTokenLimitInKb(int responseContinuationTokenLimitInKb) {
        this.responseContinuationTokenLimitInKb = responseContinuationTokenLimitInKb;
        return this;
    }

    /**
     * Sets the MaxItemCount.
     * @param maxItemCount the MaxItemCount.
     * @return current CosmosCommonRequestOptions.
     */
    public CosmosCommonRequestOptions setMaxItemCount(int maxItemCount) {
        this.maxItemCount = maxItemCount;
        return this;
    }

    /**
     * Sets the QueryMetricsEnabled.
     * @param queryMetricsEnabled the QueryMetricsEnabled.
     * @return current CosmosCommonRequestOptions.
     */
    public CosmosCommonRequestOptions setQueryMetricsEnabled(boolean queryMetricsEnabled) {
        this.queryMetricsEnabled = queryMetricsEnabled;
        return this;
    }

    /**
     * Sets the IndexMetricsEnabled.
     * @param indexMetricsEnabled the IndexMetricsEnabled.
     * @return current CosmosCommonRequestOptions.
     */
    public CosmosCommonRequestOptions setIndexMetricsEnabled(boolean indexMetricsEnabled) {
        this.indexMetricsEnabled = indexMetricsEnabled;
        return this;
    }

    /**
     * Sets the MaxPrefetchPageCount.
     * @param maxPrefetchPageCount the MaxPrefetchPageCount.
     * @return current CosmosCommonRequestOptions.
     */
    public CosmosCommonRequestOptions setMaxPrefetchPageCount(int maxPrefetchPageCount) {
        this.maxPrefetchPageCount = maxPrefetchPageCount;
        return this;
    }

    /**
     * Sets the QueryName.
     * @param queryName the QueryName.
     * @return current CosmosCommonRequestOptions.
     */
    public CosmosCommonRequestOptions setQueryName(String queryName) {
        this.queryName = queryName;
        return this;
    }

    @Override
    public Integer getMaxItemCount() {
        return this.maxItemCount;
    }

    @Override
    public Boolean isQueryMetricsEnabled() {
        return this.queryMetricsEnabled;
    }

    @Override
    public Boolean isIndexMetricsEnabled() {
        return this.indexMetricsEnabled;
    }

    @Override
    public String getQueryNameOrDefault(String defaultQueryName) {
        return this.queryName;
    }

    @Override
    public Integer getMaxPrefetchPageCount() {
        return this.maxPrefetchPageCount;
    }

    @Override
    public Integer getResponseContinuationTokenLimitInKb() {
        return this.responseContinuationTokenLimitInKb;
    }

    @Override
    public Integer getMaxBufferedItemCount() {
        return this.maxBufferedItemCount;
    }

    @Override
    public Integer getMaxDegreeOfParallelism() {
        return this.maxDegreeOfParallelism;
    }

    @Override
    public List<String> getExcludedRegions() {
         if (this.excludeRegions == null) {
            return null;
        }
        return UnmodifiableList.unmodifiableList(this.excludeRegions);
    }

    @Override
    public Boolean isScanInQueryEnabled() {
        return this.scanInQueryEnabled;
    }

    @Override
    public CosmosDiagnosticsThresholds getDiagnosticsThresholds() {
        return this.thresholds;
    }

    @Override
    public String getThroughputControlGroupName() {
        return this.throughputControlGroupName;
    }


    @Override
    public DedicatedGatewayRequestOptions getDedicatedGatewayRequestOptions() {
        return this.dedicatedGatewayRequestOptions;
    }

    @Override
    public boolean getNonIdempotentWriteRetriesEnabled() {
        return this.nonIdempotentWriteRetriesEnabled;
    }

    @Override
    public Boolean isContentResponseOnWriteEnabled() {
        return this.contentResponseOnWriteEnabled;
    }

    @Override
    public String getSessionToken() {
        return this.sessionToken;
    }

    @Override
    public ConsistencyLevel getConsistencyLevel() {
        return this.consistencyLevel;
    }

    @Override
    public CosmosEndToEndOperationLatencyPolicyConfig getCosmosEndToEndLatencyPolicyConfig(){
        return this.endToEndOperationLatencyConfig;
    }
}
