package com.azure.cosmos.models;

import com.azure.cosmos.ConsistencyLevel;
import com.azure.cosmos.CosmosDiagnosticsThresholds;
import com.azure.cosmos.CosmosEndToEndOperationLatencyPolicyConfig;

import java.util.List;

public final class CosmosCommonRequestOptions implements ICosmosCommonRequestOptions {
// ------------------------------- Remember validation -------------------------
    private CosmosEndToEndOperationLatencyPolicyConfig endToEndOperationLatencyConfig;

    private ConsistencyLevel consistencyLevel;
    private String sessionToken;
    private Boolean contentResponseOnWriteEnabled;
    private boolean nonIdempotentWriteRetriesEnabled = false;
    private DedicatedGatewayRequestOptions dedicatedGatewayRequestOptions;
    private List<String> excludeRegions;
    private String throughputControlGroupName;
    private CosmosDiagnosticsThresholds thresholds;
    private Boolean scanInQueryEnabled;

    public CosmosCommonRequestOptions setCosmosEndToEndLatencyPolicyConfig(CosmosEndToEndOperationLatencyPolicyConfig endToEndOperationLatencyPolicyConfig) {
        this.endToEndOperationLatencyConfig = endToEndOperationLatencyPolicyConfig;
        return this;
    }
    public CosmosCommonRequestOptions setConsistencyLevel(ConsistencyLevel consistencyLevel) {
        this.consistencyLevel = consistencyLevel;
        return this;
    }

    public CosmosCommonRequestOptions setSessionToken(String sessionToken) {
        this.sessionToken = sessionToken;
        return this;
    }

    public CosmosCommonRequestOptions setContentResponseOnWriteEnabled(Boolean contentResponseOnWriteEnabled) {
        this.contentResponseOnWriteEnabled = contentResponseOnWriteEnabled;
        return this;
    }

    public CosmosCommonRequestOptions setNonIdempotentWriteRetriesEnabled(boolean nonIdempotentWriteRetriesEnabled) {
        this.nonIdempotentWriteRetriesEnabled = nonIdempotentWriteRetriesEnabled;
        return this;
    }

    public CosmosCommonRequestOptions setDedicatedGatewayRequestOptions(DedicatedGatewayRequestOptions dedicatedGatewayRequestOptions) {
        this.dedicatedGatewayRequestOptions = dedicatedGatewayRequestOptions;
        return this;
    }

    public CosmosCommonRequestOptions setExcludeRegions(List<String> excludeRegions) {
        this.excludeRegions = excludeRegions;
        return this;
    }

    public CosmosCommonRequestOptions setThroughputControlGroupName(String throughputControlGroupName) {
        this.throughputControlGroupName = throughputControlGroupName;
        return this;
    }

    public CosmosCommonRequestOptions setThresholds(CosmosDiagnosticsThresholds thresholds) {
        this.thresholds = thresholds;
        return this;
    }

    public CosmosCommonRequestOptions setScanInQueryEnabled(Boolean scanInQueryEnabled) {
        this.scanInQueryEnabled = scanInQueryEnabled;
        return this;
    }

    @Override
    public Boolean isScanInQueryEnabled() {
        return scanInQueryEnabled;
    }

    @Override
    public CosmosDiagnosticsThresholds getDiagnosticsThresholds() {
        return thresholds;
    }

    @Override
    public String getThroughputControlGroupName() {
        return throughputControlGroupName;
    }

    @Override
    public List<String> getExcludedRegions() {
        return excludeRegions;
    }

    @Override
    public DedicatedGatewayRequestOptions getDedicatedGatewayRequestOptions() {
        return dedicatedGatewayRequestOptions;
    }

    @Override
    public boolean getNonIdempotentWriteRetriesEnabled() {
        return nonIdempotentWriteRetriesEnabled;
    }

    @Override
    public Boolean isContentResponseOnWriteEnabled() {
        return contentResponseOnWriteEnabled;
    }

    @Override
    public String getSessionToken() {
        return sessionToken;
    }

    @Override
    public ConsistencyLevel getConsistencyLevel() {
        return consistencyLevel;
    }

    @Override
    public CosmosEndToEndOperationLatencyPolicyConfig getCosmosEndToEndLatencyPolicyConfig(){
        return this.endToEndOperationLatencyConfig;
    }
}
