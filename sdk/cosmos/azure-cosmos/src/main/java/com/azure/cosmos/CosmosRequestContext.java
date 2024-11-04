// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos;

import com.azure.cosmos.implementation.ImplementationBridgeHelpers;
import com.azure.cosmos.implementation.OverridableRequestOptions;
import com.azure.cosmos.models.DedicatedGatewayRequestOptions;

import java.util.List;
import java.util.Set;

/**
 * Getters for the common request context for operations in CosmosDB.
 */
public final class CosmosRequestContext {

    private OverridableRequestOptions requestOptions;

    CosmosRequestContext(OverridableRequestOptions requestOptions) {
        this.requestOptions = requestOptions;
    }

    /**
     * Gets the CosmosEndToEndLatencyPolicyConfig.
     *
     * @return the CosmosEndToEndLatencyPolicyConfig. It could be null if not defined or called on an irrelevant operation.
     */
    public CosmosEndToEndOperationLatencyPolicyConfig getCosmosEndToEndLatencyPolicyConfig() {
        return requestOptions.getCosmosEndToEndLatencyPolicyConfig();
    }

    /**
     * Gets the consistency level.
     *
     * @return the consistency level. It could be null if not defined or called on an irrelevant operation.
     */
    public ConsistencyLevel getConsistencyLevel() {
        return requestOptions.getConsistencyLevel();
    }

    /**
     * Gets the content response on write enabled.
     *
     * @return the content response on write enabled. It could be null if not defined or called on an irrelevant operation.
     */
    public Boolean isContentResponseOnWriteEnabled() {
        return requestOptions.isContentResponseOnWriteEnabled();
    }

    /**
     * Gets the non idempotent write retries enabled.
     *
     * @return the non idempotent write retries enabled. It could be null if not defined or called on an irrelevant operation.
     */
    public Boolean getNonIdempotentWriteRetriesEnabled() {
        return requestOptions.getNonIdempotentWriteRetriesEnabled();
    }

    /**
     * Gets the dedicated gateway request options.
     *
     * @return the dedicated gateway request options. It could be null if not defined or called on an irrelevant operation.
     */
    public DedicatedGatewayRequestOptions getDedicatedGatewayRequestOptions() {
        return requestOptions.getDedicatedGatewayRequestOptions();
    }

    /**
     * Gets the excluded regions.
     *
     * @return the excluded regions.
     */
    public List<String> getExcludedRegions() {
        return requestOptions.getExcludedRegions();
    }

    /**
     * Gets the resource token.
     *
     * @return the resource token.
     */
    public String getThroughputControlGroupName() {
        return requestOptions.getThroughputControlGroupName();
    }

    /**
     * Gets the diagnostics thresholds.
     *
     * @return the diagnostics thresholds. It could be null if not defined or called on an irrelevant operation.
     */
    public CosmosDiagnosticsThresholds getDiagnosticsThresholds() {
        return requestOptions.getDiagnosticsThresholds();
    }

    /**
     * Gets the scan in query enabled.
     *
     * @return the scan in query enabled. It could be null if not defined or called on an irrelevant operation.
     */
    public Boolean isScanInQueryEnabled() {
        return requestOptions.isScanInQueryEnabled();
    }

    /**
     * Gets the max degree of parallelism.
     *
     * @return the max degree of parallelism. It could be null if not defined or called on an irrelevant operation.
     */
    public Integer getMaxDegreeOfParallelism() {
        return requestOptions.getMaxDegreeOfParallelism();
    }

    /**
     * Gets the max buffered item count.
     *
     * @return the max buffered item count. It could be null if not defined or called on an irrelevant operation.
     */
    public Integer getMaxBufferedItemCount() {
        return requestOptions.getMaxBufferedItemCount();
    }

    /**
     * Gets the response continuation token limit in KB.
     *
     * @return the response continuation token limit in KB. It could be null if not defined or called on an irrelevant operation.
     */
    public Integer getResponseContinuationTokenLimitInKb() {
        return requestOptions.getResponseContinuationTokenLimitInKb();
    }

    /**
     * Gets the max item count.
     *
     * @return the max item count. It could be null if not defined or called on an irrelevant operation.
     */
    public Integer getMaxItemCount() {
        return requestOptions.getMaxItemCount();
    }

    /**
     * Gets the query metrics enabled.
     *
     * @return the query metrics enabled. It could be null if not defined or called on an irrelevant operation.
     */
    public Boolean isQueryMetricsEnabled() {
        return requestOptions.isQueryMetricsEnabled();
    }

    /**
     * Gets the index metrics enabled.
     *
     * @return the index metrics enabled. It could be null if not defined or called on an irrelevant operation.
     */
    public Boolean isIndexMetricsEnabled() {
        return requestOptions.isIndexMetricsEnabled();
    }

    /**
     * Gets the query name.
     *
     * @return the query name. It could be null if not defined or called on an irrelevant operation.
     */
    public Integer getMaxPrefetchPageCount() {
        return requestOptions.getMaxPrefetchPageCount();
    }

    /**
     * Gets the query name.
     * @param defaultQueryName the default query name.
     *
     * @return the query name. It could be null if not defined or called on an irrelevant operation.
     */
    public String getQueryNameOrDefault(String defaultQueryName) {
        return requestOptions.getQueryNameOrDefault(defaultQueryName);
    }

    /**
     * Gets the keyword identifiers.
     *
     * @return the keyword identifiers.
     */
    public Set<String> getKeywordIdentifiers() {
        return requestOptions.getKeywordIdentifiers();
    }

    ///////////////////////////////////////////////////////////////////////////////////////////
    // the following helper/accessor only helps to access this class outside of this package.//
    ///////////////////////////////////////////////////////////////////////////////////////////
    static void initialize() {
        ImplementationBridgeHelpers.CosmosRequestContextHelper
            .setCosmosRequestContextAccessor(
                new ImplementationBridgeHelpers.CosmosRequestContextHelper.CosmosRequestContextAccessor() {
                    @Override
                    public CosmosRequestContext create(OverridableRequestOptions requestOptions) {
                        return new CosmosRequestContext(requestOptions);
                    }
                }
                );
    }
}
