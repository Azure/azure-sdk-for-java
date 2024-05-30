package com.azure.cosmos.models;

import com.azure.cosmos.ConsistencyLevel;
import com.azure.cosmos.CosmosDiagnosticsThresholds;
import com.azure.cosmos.CosmosEndToEndOperationLatencyPolicyConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Getters for the common request options for operations in CosmosDB.
 */
public interface ICosmosCommonRequestOptions {
    Logger logger = LoggerFactory.getLogger(ICosmosCommonRequestOptions.class);

    /**
     * Gets the CosmosEndToEndLatencyPolicyConfig.
     *
     * @return the CosmosEndToEndLatencyPolicyConfig.
     */
    default CosmosEndToEndOperationLatencyPolicyConfig getCosmosEndToEndLatencyPolicyConfig() {
        logger.info("This is not the correct class to get the CosmosEndToEndLatencyPolicyConfig.");
        return null;
    }

    /**
     * Gets the consistency level.
     *
     * @return the consistency level.
     */
    default ConsistencyLevel getConsistencyLevel() {
        logger.info("This is not the correct class to get the consistency level.");
        return null;
    }

    /**
     * Gets the session token.
     *
     * @return the session token.
     */
    default String getSessionToken() {
        logger.info("This is not the correct class to get the session token.");
        return null;
    }

    /**
     * Gets the content response on write enabled.
     *
     * @return the content response on write enabled.
     */
    default Boolean isContentResponseOnWriteEnabled() {
        logger.info("This is not the correct class to get the content response on write enabled.");
        return null;
    }

    /**
     * Gets the non idempotent write retries enabled.
     *
     * @return the non idempotent write retries enabled.
     */
    default boolean getNonIdempotentWriteRetriesEnabled() {
        logger.info("This is not the correct class to get the non idempotent write retries enabled.");
        return false;
    }

    /**
     * Gets the dedicated gateway request options.
     *
     * @return the dedicated gateway request options.
     */
    default DedicatedGatewayRequestOptions getDedicatedGatewayRequestOptions() {
        logger.info("This is not the correct class to get the dedicated gateway request options.");
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
        logger.info("This is not the correct class to get the diagnostics thresholds.");
        return null;
    }

    /**
     * Gets the scan in query enabled.
     *
     * @return the scan in query enabled.
     */
    default Boolean isScanInQueryEnabled() {
        logger.info("This is not the correct class to get the scan in query enabled.");
        return null;
    }

    /**
     * Gets the max degree of parallelism.
     *
     * @return the max degree of parallelism.
     */
    default Integer getMaxDegreeOfParallelism() {
        logger.info("This is not the correct class to get the max degree of parallelism.");
        return null;
    }

    /**
     * Gets the max buffered item count.
     *
     * @return the max buffered item count.
     */
    default Integer getMaxBufferedItemCount() {
        logger.info("This is not the correct class to get the max buffered item count.");
        return null;
    }

    /**
     * Gets the response continuation token limit in KB.
     *
     * @return the response continuation token limit in KB.
     */
    default Integer getResponseContinuationTokenLimitInKb() {
        logger.info("This is not the correct class to get the response continuation token limit in KB.");
        return null;
    }

    /**
     * Gets the max item count.
     *
     * @return the max item count.
     */
    default Integer getMaxItemCount() {
        logger.info("This is not the correct class to get the max item count.");
        return null;
    }

    /**
     * Gets the query metrics enabled.
     *
     * @return the query metrics enabled.
     */
    default Boolean isQueryMetricsEnabled() {
        logger.info("This is not the correct class to get the query metrics enabled.");
        return null;
    }

    /**
     * Gets the index metrics enabled.
     *
     * @return the index metrics enabled.
     */
    default Boolean isIndexMetricsEnabled() {
        logger.info("This is not the correct class to get the index metrics enabled.");
        return null;
    }

    /**
     * Gets the query name.
     *
     * @return the query name.
     */
    default Integer getMaxPrefetchPageCount() {
        logger.info("This is not the correct class to get the max prefetch page count.");
        return null;
    }

    /**
     * Gets the query name.
     *
     * @return the query name.
     */
    default String getQueryNameOrDefault(String defaultQueryName) {
        logger.info("This is not the correct class to get the query name.");
        return null;
    }
}
