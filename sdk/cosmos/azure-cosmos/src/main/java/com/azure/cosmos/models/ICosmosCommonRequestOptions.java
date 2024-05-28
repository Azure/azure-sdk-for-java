package com.azure.cosmos.models;

import com.azure.cosmos.ConsistencyLevel;
import com.azure.cosmos.CosmosDiagnosticsThresholds;
import com.azure.cosmos.CosmosEndToEndOperationLatencyPolicyConfig;

import java.util.List;

public interface ICosmosCommonRequestOptions {
    // ------------------------------- Maybe here log that it's not the correct one -------------------------
    default CosmosEndToEndOperationLatencyPolicyConfig getCosmosEndToEndLatencyPolicyConfig() {
        return null;
    }

    default ConsistencyLevel getConsistencyLevel() {
        return null;
    }

    default String getSessionToken() {
        return null;
    }

    default Boolean isContentResponseOnWriteEnabled() {
        return null;
    }

    default boolean getNonIdempotentWriteRetriesEnabled() {
        return false;
    }

    default DedicatedGatewayRequestOptions getDedicatedGatewayRequestOptions() {
        return null;
    }

    List<String> getExcludedRegions();

    String getThroughputControlGroupName();

    default CosmosDiagnosticsThresholds getDiagnosticsThresholds() {
        return null;
    }

    default Boolean isScanInQueryEnabled() {
        return null;
    }

    default int getMaxDegreeOfParallelism() {
        return 0;
    }

    default int getMaxBufferedItemCount() {
        return 0;
    }

    default int getResponseContinuationTokenLimitInKb() {
        return 0;
    }

    default int getMaxItemCount() {
        return 0;
    }

    default boolean isQueryMetricsEnabled() {
        return false;
    }

    default boolean isIndexMetricsEnabled() {
        return false;
    }

    default int getMaxPrefetchPageCount() {
        return 0;
    }

    default String getQueryNameOrDefault(String defaultQueryName) {
        return null;
    }
}
