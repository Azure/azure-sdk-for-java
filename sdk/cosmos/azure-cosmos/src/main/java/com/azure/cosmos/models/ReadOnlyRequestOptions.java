// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.models;

import com.azure.cosmos.ConsistencyLevel;
import com.azure.cosmos.CosmosDiagnosticsThresholds;
import com.azure.cosmos.CosmosEndToEndOperationLatencyPolicyConfig;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Set;

/**
 * Getters for the common request options for operations in CosmosDB.
 */
public interface ReadOnlyRequestOptions {

    /**
     * Gets the CosmosEndToEndLatencyPolicyConfig.
     *
     * @return the CosmosEndToEndLatencyPolicyConfig.
     */
    default CosmosEndToEndOperationLatencyPolicyConfig getCosmosEndToEndLatencyPolicyConfig() {
        LoggerFactory.getLogger(ReadOnlyRequestOptions.class).info("This is not the correct class to get the CosmosEndToEndLatencyPolicyConfig.");
        return null;
    }

    /**
     * Gets the consistency level.
     *
     * @return the consistency level.
     */
    default ConsistencyLevel getConsistencyLevel() {
        LoggerFactory.getLogger(ReadOnlyRequestOptions.class).info("This is not the correct class to get the consistency level.");
        return null;
    }

    /**
     * Gets the content response on write enabled.
     *
     * @return the content response on write enabled.
     */
    default Boolean isContentResponseOnWriteEnabled() {
        LoggerFactory.getLogger(ReadOnlyRequestOptions.class).info("This is not the correct class to get the content response on write enabled.");
        return null;
    }

    /**
     * Gets the non idempotent write retries enabled.
     *
     * @return the non idempotent write retries enabled.
     */
    default Boolean getNonIdempotentWriteRetriesEnabled() {
        LoggerFactory.getLogger(ReadOnlyRequestOptions.class).info("This is not the correct class to get the non idempotent write retries enabled.");
        return null;
    }

    /**
     * Gets the dedicated gateway request options.
     *
     * @return the dedicated gateway request options.
     */
    default DedicatedGatewayRequestOptions getDedicatedGatewayRequestOptions() {
        LoggerFactory.getLogger(ReadOnlyRequestOptions.class).info("This is not the correct class to get the dedicated gateway request options.");
        return null;
    }

    /**
     * Gets the excluded regions.
     *
     * @return the excluded regions.
     */
    List<String> getExcludedRegions();

    /**
     * Gets the resource token.
     *
     * @return the resource token.
     */
    String getThroughputControlGroupName();

    /**
     * Gets the diagnostics thresholds.
     *
     * @return the diagnostics thresholds.
     */
    default CosmosDiagnosticsThresholds getDiagnosticsThresholds() {
        LoggerFactory.getLogger(ReadOnlyRequestOptions.class).info("This is not the correct class to get the diagnostics thresholds.");
        return null;
    }

    /**
     * Gets the scan in query enabled.
     *
     * @return the scan in query enabled.
     */
    default Boolean isScanInQueryEnabled() {
        LoggerFactory.getLogger(ReadOnlyRequestOptions.class).info("This is not the correct class to get the scan in query enabled.");
        return null;
    }

    /**
     * Gets the max degree of parallelism.
     *
     * @return the max degree of parallelism.
     */
    default Integer getMaxDegreeOfParallelism() {
        LoggerFactory.getLogger(ReadOnlyRequestOptions.class).info("This is not the correct class to get the max degree of parallelism.");
        return null;
    }

    /**
     * Gets the max buffered item count.
     *
     * @return the max buffered item count.
     */
    default Integer getMaxBufferedItemCount() {
        LoggerFactory.getLogger(ReadOnlyRequestOptions.class).info("This is not the correct class to get the max buffered item count.");
        return null;
    }

    /**
     * Gets the response continuation token limit in KB.
     *
     * @return the response continuation token limit in KB.
     */
    default Integer getResponseContinuationTokenLimitInKb() {
        LoggerFactory.getLogger(ReadOnlyRequestOptions.class).info("This is not the correct class to get the response continuation token limit in KB.");
        return null;
    }

    /**
     * Gets the max item count.
     *
     * @return the max item count.
     */
    default Integer getMaxItemCount() {
        LoggerFactory.getLogger(ReadOnlyRequestOptions.class).info("This is not the correct class to get the max item count.");
        return null;
    }

    /**
     * Gets the query metrics enabled.
     *
     * @return the query metrics enabled.
     */
    default Boolean isQueryMetricsEnabled() {
        LoggerFactory.getLogger(ReadOnlyRequestOptions.class).info("This is not the correct class to get the query metrics enabled.");
        return null;
    }

    /**
     * Gets the index metrics enabled.
     *
     * @return the index metrics enabled.
     */
    default Boolean isIndexMetricsEnabled() {
        LoggerFactory.getLogger(ReadOnlyRequestOptions.class).info("This is not the correct class to get the index metrics enabled.");
        return null;
    }

    /**
     * Gets the query name.
     *
     * @return the query name.
     */
    default Integer getMaxPrefetchPageCount() {
        LoggerFactory.getLogger(ReadOnlyRequestOptions.class).info("This is not the correct class to get the max prefetch page count.");
        return null;
    }

    /**
     * Gets the query name.
     * @param defaultQueryName the default query name.
     *
     * @return the query name.
     */
    default String getQueryNameOrDefault(String defaultQueryName) {
        LoggerFactory.getLogger(ReadOnlyRequestOptions.class).info("This is not the correct class to get the query name.");
        return null;
    }

    /**
     * Gets the custom correlated ids.
     *
     * @return the custom correlated ids.
     */
    Set<String> getCustomCorrelatedIds();
}
