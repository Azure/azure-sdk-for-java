// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation;

import com.azure.cosmos.ConsistencyLevel;
import com.azure.cosmos.CosmosDiagnosticsThresholds;
import com.azure.cosmos.CosmosEndToEndOperationLatencyPolicyConfig;
import com.azure.cosmos.models.DedicatedGatewayRequestOptions;

import java.util.List;

/**
 * Getters for the common request options for operations in CosmosDB.
 */
public interface ReadOnlyRequestOptions {

    /**
     * Gets the CosmosEndToEndLatencyPolicyConfig.
     *
     * @return the CosmosEndToEndLatencyPolicyConfig. It could be null if not defined or called on an irrelevant operation.
     */
    CosmosEndToEndOperationLatencyPolicyConfig getCosmosEndToEndLatencyPolicyConfig();

    /**
     * Gets the consistency level.
     *
     * @return the consistency level. It could be null if not defined or called on an irrelevant operation.
     */
    ConsistencyLevel getConsistencyLevel();

    /**
     * Gets the content response on write enabled.
     *
     * @return the content response on write enabled. It could be null if not defined or called on an irrelevant operation.
     */
    Boolean isContentResponseOnWriteEnabled();

    /**
     * Gets the non idempotent write retries enabled.
     *
     * @return the non idempotent write retries enabled. It could be null if not defined or called on an irrelevant operation.
     */
    Boolean getNonIdempotentWriteRetriesEnabled();

    /**
     * Gets the dedicated gateway request options.
     *
     * @return the dedicated gateway request options. It could be null if not defined or called on an irrelevant operation.
     */
    DedicatedGatewayRequestOptions getDedicatedGatewayRequestOptions();

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
     * @return the diagnostics thresholds. It could be null if not defined or called on an irrelevant operation.
     */
    CosmosDiagnosticsThresholds getDiagnosticsThresholds();

    /**
     * Gets the scan in query enabled.
     *
     * @return the scan in query enabled. It could be null if not defined or called on an irrelevant operation.
     */
    Boolean isScanInQueryEnabled();

    /**
     * Gets the max degree of parallelism.
     *
     * @return the max degree of parallelism. It could be null if not defined or called on an irrelevant operation.
     */
    Integer getMaxDegreeOfParallelism();

    /**
     * Gets the max buffered item count.
     *
     * @return the max buffered item count. It could be null if not defined or called on an irrelevant operation.
     */
    Integer getMaxBufferedItemCount();

    /**
     * Gets the response continuation token limit in KB.
     *
     * @return the response continuation token limit in KB. It could be null if not defined or called on an irrelevant operation.
     */
    Integer getResponseContinuationTokenLimitInKb();

    /**
     * Gets the max item count.
     *
     * @return the max item count. It could be null if not defined or called on an irrelevant operation.
     */
    Integer getMaxItemCount();

    /**
     * Gets the query metrics enabled.
     *
     * @return the query metrics enabled. It could be null if not defined or called on an irrelevant operation.
     */
    Boolean isQueryMetricsEnabled();

    /**
     * Gets the index metrics enabled.
     *
     * @return the index metrics enabled. It could be null if not defined or called on an irrelevant operation.
     */
    Boolean isIndexMetricsEnabled();

    /**
     * Gets the query name.
     *
     * @return the query name. It could be null if not defined or called on an irrelevant operation.
     */
    Integer getMaxPrefetchPageCount();

    /**
     * Gets the query name.
     * @param defaultQueryName the default query name.
     *
     * @return the query name. It could be null if not defined or called on an irrelevant operation.
     */
    String getQueryNameOrDefault(String defaultQueryName);
}
