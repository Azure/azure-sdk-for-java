// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.models;

import com.azure.cosmos.CosmosDiagnosticsThresholds;
import com.azure.cosmos.CosmosEndToEndOperationLatencyPolicyConfig;
import com.azure.cosmos.CosmosItemSerializer;
import com.azure.cosmos.ReadConsistencyStrategy;
import com.azure.cosmos.implementation.CosmosQueryRequestOptionsBase;
import com.azure.cosmos.implementation.CosmosReadManyByPartitionKeysRequestOptionsImpl;
import com.azure.cosmos.implementation.ImplementationBridgeHelpers;
import com.azure.cosmos.util.Beta;

import java.time.Duration;
import java.util.List;
import java.util.Set;

/**
 * Specifies the options associated with the {@code readManyByPartitionKeys} operation
 * in the Azure Cosmos DB database service.
 * <p>
 * This is distinct from {@link CosmosReadManyRequestOptions} (used by the
 * {@code readMany(List&lt;CosmosItemIdentity&gt;)} API). It exposes only the knobs that are
 * applicable to {@code readManyByPartitionKeys} — for example, properties that influence
 * query parallelism inside a single physical partition or a feed range filter are
 * intentionally not exposed because the operation is fully managed by the SDK.
 */
public final class CosmosReadManyByPartitionKeysRequestOptions {
    private final CosmosReadManyByPartitionKeysRequestOptionsImpl actualRequestOptions;

    /**
     * Instantiates a new readManyByPartitionKeys request options.
     */
    public CosmosReadManyByPartitionKeysRequestOptions() {
        this.actualRequestOptions = new CosmosReadManyByPartitionKeysRequestOptionsImpl();
    }

    /**
     * Copy constructor.
     *
     * @param options the options to copy.
     */
    CosmosReadManyByPartitionKeysRequestOptions(CosmosReadManyByPartitionKeysRequestOptions options) {
        this.actualRequestOptions = new CosmosReadManyByPartitionKeysRequestOptionsImpl(options.actualRequestOptions);
    }

    /**
     * Gets the composite continuation token used to resume a previous
     * {@code readManyByPartitionKeys} invocation.
     *
     * @return the continuation token, or null if not set.
     */
    public String getContinuationToken() {
        return this.actualRequestOptions.getContinuationToken();
    }

    /**
     * Sets the composite continuation token used to resume a previous
     * {@code readManyByPartitionKeys} invocation. The token must have been returned by a prior
     * invocation of {@code readManyByPartitionKeys} on the same container, with the same
     * partition-key set and the same custom query.
     *
     * @param continuationToken the composite continuation token from a previous invocation.
     * @return the {@link CosmosReadManyByPartitionKeysRequestOptions} for fluent chaining.
     */
    public CosmosReadManyByPartitionKeysRequestOptions setContinuationToken(String continuationToken) {
        this.actualRequestOptions.setContinuationToken(continuationToken);
        return this;
    }

    /**
     * Gets the maximum number of per-physical-partition batches whose first page is prefetched
     * concurrently. This bounds the prefetch parallelism the SDK uses while sequentially
     * draining batches.
     *
     * @return the max concurrent batch prefetch, or null if the SDK default is in effect.
     */
    public Integer getMaxConcurrentBatchPrefetch() {
        return this.actualRequestOptions.getMaxConcurrentBatchPrefetch();
    }

    /**
     * Sets the maximum number of per-physical-partition batches whose first page is prefetched
     * concurrently. The default is {@code Math.max(1, Math.min(Runtime.getRuntime().availableProcessors(), 8))}.
     * <p>
     * Increase this to trade memory for lower end-to-end latency on wide containers; decrease it
     * (e.g. to {@code 1}) when running in environments where a single task already saturates the
     * network/CPU and additional prefetch only adds memory pressure.
     *
     * @param maxConcurrentBatchPrefetch the max concurrent batch prefetch (must be &gt;= 1).
     * @return the {@link CosmosReadManyByPartitionKeysRequestOptions} for fluent chaining.
     * @throws IllegalArgumentException if {@code maxConcurrentBatchPrefetch} is &lt; 1.
     */
    public CosmosReadManyByPartitionKeysRequestOptions setMaxConcurrentBatchPrefetch(int maxConcurrentBatchPrefetch) {
        if (maxConcurrentBatchPrefetch < 1) {
            throw new IllegalArgumentException(
                "Argument 'maxConcurrentBatchPrefetch' must be greater than or equal to 1.");
        }
        this.actualRequestOptions.setMaxConcurrentBatchPrefetch(maxConcurrentBatchPrefetch);
        return this;
    }

    /**
     * Gets the maximum number of partition key values per batch query sent to a single
     * physical partition. Returns {@code null} if not set, in which case the SDK default
     * is used (currently {@code 100}, configurable globally via the system property or
     * environment variable {@code COSMOS.READ_MANY_BY_PK_MAX_BATCH_SIZE}).
     *
     * @return the max batch size, or null if the SDK default is in effect.
     */
    public Integer getMaxBatchSize() {
        return this.actualRequestOptions.getMaxBatchSize();
    }

    /**
     * Sets the maximum number of partition key values per batch query sent to a single
     * physical partition. The default is 100 (overridable globally via the system property
     * {@code COSMOS.READ_MANY_BY_PK_MAX_BATCH_SIZE}). This per-request setting takes
     * precedence over the global default.
     * <p>
     * Increasing this value reduces the number of batches (and round-trips) but produces
     * larger IN-clause queries that consume more RUs per request. Decreasing it increases
     * the number of batches but keeps individual requests lighter.
     *
     * @param maxBatchSize the maximum number of PKs per batch (must be &gt;= 1).
     * @return the {@link CosmosReadManyByPartitionKeysRequestOptions} for fluent chaining.
     * @throws IllegalArgumentException if {@code maxBatchSize} is &lt; 1.
     */
    public CosmosReadManyByPartitionKeysRequestOptions setMaxBatchSize(int maxBatchSize) {
        if (maxBatchSize < 1) {
            throw new IllegalArgumentException(
                "Argument 'maxBatchSize' must be greater than or equal to 1.");
        }
        this.actualRequestOptions.setMaxBatchSize(maxBatchSize);
        return this;
    }

    /**
     * Gets the read consistency strategy for the request.
     *
     * @return the read consistency strategy.
     */
    @Beta(value = Beta.SinceVersion.V4_69_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    public ReadConsistencyStrategy getReadConsistencyStrategy() {
        return this.actualRequestOptions.getReadConsistencyStrategy();
    }

    /**
     * Sets the read consistency strategy required for the request.
     *
     * @param readConsistencyStrategy the read consistency strategy.
     * @return the {@link CosmosReadManyByPartitionKeysRequestOptions} for fluent chaining.
     */
    @Beta(value = Beta.SinceVersion.V4_69_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    public CosmosReadManyByPartitionKeysRequestOptions setReadConsistencyStrategy(
        ReadConsistencyStrategy readConsistencyStrategy) {
        this.actualRequestOptions.setReadConsistencyStrategy(readConsistencyStrategy);
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
     * @return the {@link CosmosReadManyByPartitionKeysRequestOptions} for fluent chaining.
     */
    public CosmosReadManyByPartitionKeysRequestOptions setSessionToken(String sessionToken) {
        this.actualRequestOptions.setSessionToken(sessionToken);
        return this;
    }

    /**
     * Sets the maximum size (in kilobytes) of the backend continuation token embedded inside the
     * composite {@code readManyByPartitionKeys} continuation token.
     * <p>
     * Note: this only constrains the per-batch backend continuation that the SDK wraps inside
     * the public composite token; the public composite token itself is always larger because it
     * also carries the remaining batch definitions, query hash, and partition-key-set hash.
     *
     * @param limitInKb backend continuation token size limit (must be &gt;= 1).
     * @return the {@link CosmosReadManyByPartitionKeysRequestOptions} for fluent chaining.
     */
    public CosmosReadManyByPartitionKeysRequestOptions setMaxBackendContinuationTokenSizeInKb(int limitInKb) {
        this.actualRequestOptions.setResponseContinuationTokenLimitInKb(limitInKb);
        return this;
    }

    /**
     * Gets the maximum size (in kilobytes) of the backend continuation token embedded inside the
     * composite {@code readManyByPartitionKeys} continuation token. Returns 0 if not set.
     *
     * @return the configured backend continuation token size limit, or 0 if not set.
     */
    public int getMaxBackendContinuationTokenSizeInKb() {
        return this.actualRequestOptions.getResponseContinuationTokenLimitInKb();
    }

    /**
     * Gets the maximum number of items returned in a single page.
     *
     * @return the max item count, or null if not set (the SDK default applies).
     */
    public Integer getMaxItemCount() {
        return this.actualRequestOptions.getMaxItemCount();
    }

    /**
     * Sets the maximum number of items returned in a single page.
     *
     * @param maxItemCount the maximum number of items per page.
     * @return the {@link CosmosReadManyByPartitionKeysRequestOptions} for fluent chaining.
     */
    public CosmosReadManyByPartitionKeysRequestOptions setMaxItemCount(int maxItemCount) {
        this.actualRequestOptions.setMaxItemCount(maxItemCount);
        return this;
    }

    /**
     * Sets the {@link CosmosEndToEndOperationLatencyPolicyConfig} to be used for the request.
     *
     * @param cosmosEndToEndOperationLatencyPolicyConfig the {@link CosmosEndToEndOperationLatencyPolicyConfig}
     * @return the {@link CosmosReadManyByPartitionKeysRequestOptions} for fluent chaining.
     */
    public CosmosReadManyByPartitionKeysRequestOptions setCosmosEndToEndOperationLatencyPolicyConfig(
        CosmosEndToEndOperationLatencyPolicyConfig cosmosEndToEndOperationLatencyPolicyConfig) {
        this.actualRequestOptions
            .setCosmosEndToEndOperationLatencyPolicyConfig(cosmosEndToEndOperationLatencyPolicyConfig);
        return this;
    }

    /**
     * List of regions to be excluded for the request/retries.
     *
     * @param excludeRegions the regions to exclude
     * @return the {@link CosmosReadManyByPartitionKeysRequestOptions}
     */
    public CosmosReadManyByPartitionKeysRequestOptions setExcludedRegions(List<String> excludeRegions) {
        this.actualRequestOptions.setExcludedRegions(excludeRegions);
        return this;
    }

    /**
     * Gets the list of regions to exclude for the request/retries.
     *
     * @return the list of excluded regions
     */
    public List<String> getExcludedRegions() {
        return this.actualRequestOptions.getExcludedRegions();
    }

    /**
     * Gets the option to enable populate query metrics. By default query metrics are enabled.
     *
     * @return whether query metrics are enabled
     */
    public boolean isQueryMetricsEnabled() {
        return this.actualRequestOptions.isQueryMetricsEnabled();
    }

    /**
     * Sets the option to enable/disable query metrics.
     *
     * @param queryMetricsEnabled whether to enable or disable query metrics
     * @return the {@link CosmosReadManyByPartitionKeysRequestOptions} for fluent chaining.
     */
    public CosmosReadManyByPartitionKeysRequestOptions setQueryMetricsEnabled(boolean queryMetricsEnabled) {
        this.actualRequestOptions.setQueryMetricsEnabled(queryMetricsEnabled);
        return this;
    }

    /**
     * Gets the throughput control group name.
     *
     * @return the throughput control group name.
     */
    public String getThroughputControlGroupName() {
        return this.actualRequestOptions.getThroughputControlGroupName();
    }

    /**
     * Sets the throughput control group name.
     *
     * @param throughputControlGroupName the throughput control group name.
     * @return the {@link CosmosReadManyByPartitionKeysRequestOptions} for fluent chaining.
     */
    public CosmosReadManyByPartitionKeysRequestOptions setThroughputControlGroupName(String throughputControlGroupName) {
        this.actualRequestOptions.setThroughputControlGroupName(throughputControlGroupName);
        return this;
    }

    /**
     * Gets the dedicated gateway request options.
     *
     * @return the dedicated gateway request options.
     */
    public DedicatedGatewayRequestOptions getDedicatedGatewayRequestOptions() {
        return this.actualRequestOptions.getDedicatedGatewayRequestOptions();
    }

    /**
     * Sets the dedicated gateway request options.
     *
     * @param dedicatedGatewayRequestOptions the dedicated gateway request options.
     * @return the {@link CosmosReadManyByPartitionKeysRequestOptions} for fluent chaining.
     */
    public CosmosReadManyByPartitionKeysRequestOptions setDedicatedGatewayRequestOptions(
        DedicatedGatewayRequestOptions dedicatedGatewayRequestOptions) {
        this.actualRequestOptions.setDedicatedGatewayRequestOptions(dedicatedGatewayRequestOptions);
        return this;
    }

    /**
     * Gets the latency threshold for diagnostics on tracer.
     *
     * @return the latency threshold for diagnostics on tracer.
     */
    public Duration getThresholdForDiagnosticsOnTracer() {
        return this.actualRequestOptions.getThresholdForDiagnosticsOnTracer();
    }

    /**
     * Sets the latency threshold for diagnostics on tracer.
     *
     * @param thresholdForDiagnosticsOnTracer the latency threshold for diagnostics on tracer.
     * @return the {@link CosmosReadManyByPartitionKeysRequestOptions} for fluent chaining.
     */
    public CosmosReadManyByPartitionKeysRequestOptions setThresholdForDiagnosticsOnTracer(
        Duration thresholdForDiagnosticsOnTracer) {
        this.actualRequestOptions.setThresholdForDiagnosticsOnTracer(thresholdForDiagnosticsOnTracer);
        return this;
    }

    /**
     * Allows overriding the diagnostic thresholds for a specific operation.
     *
     * @param operationSpecificThresholds the diagnostic threshold override for this operation
     * @return the {@link CosmosReadManyByPartitionKeysRequestOptions} for fluent chaining.
     */
    public CosmosReadManyByPartitionKeysRequestOptions setDiagnosticsThresholds(
        CosmosDiagnosticsThresholds operationSpecificThresholds) {
        this.actualRequestOptions.setDiagnosticsThresholds(operationSpecificThresholds);
        return this;
    }

    /**
     * Gets the diagnostic thresholds used as an override for a specific operation.
     *
     * @return the diagnostic thresholds for this operation.
     */
    public CosmosDiagnosticsThresholds getDiagnosticsThresholds() {
        return this.actualRequestOptions.getDiagnosticsThresholds();
    }

    /**
     * Gets the custom item serializer defined for this instance of request options.
     *
     * @return the custom item serializer.
     */
    public CosmosItemSerializer getCustomItemSerializer() {
        return this.actualRequestOptions.getCustomItemSerializer();
    }

    /**
     * Sets a custom item serializer to be used for this operation.
     *
     * @param customItemSerializer the custom item serializer for this operation.
     * @return the {@link CosmosReadManyByPartitionKeysRequestOptions} for fluent chaining.
     */
    public CosmosReadManyByPartitionKeysRequestOptions setCustomItemSerializer(
        CosmosItemSerializer customItemSerializer) {
        this.actualRequestOptions.setCustomItemSerializer(customItemSerializer);
        return this;
    }

    /**
     * Sets the custom keyword identifiers.
     *
     * @param keywordIdentifiers the custom keyword identifiers.
     * @return the {@link CosmosReadManyByPartitionKeysRequestOptions} for fluent chaining.
     */
    public CosmosReadManyByPartitionKeysRequestOptions setKeywordIdentifiers(Set<String> keywordIdentifiers) {
        this.actualRequestOptions.setKeywordIdentifiers(keywordIdentifiers);
        return this;
    }

    /**
     * Gets the custom keyword identifiers.
     *
     * @return the custom keyword identifiers.
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
        ImplementationBridgeHelpers.CosmosReadManyByPartitionKeysRequestOptionsHelper
            .setCosmosReadManyByPartitionKeysRequestOptionsAccessor(
                new ImplementationBridgeHelpers.CosmosReadManyByPartitionKeysRequestOptionsHelper
                    .CosmosReadManyByPartitionKeysRequestOptionsAccessor() {
                    @Override
                    public CosmosQueryRequestOptionsBase<?> getImpl(
                        CosmosReadManyByPartitionKeysRequestOptions options) {
                        return options.actualRequestOptions;
                    }

                    @Override
                    public String getContinuationToken(CosmosReadManyByPartitionKeysRequestOptions options) {
                        return options.actualRequestOptions.getContinuationToken();
                    }

                    @Override
                    public Integer getMaxConcurrentBatchPrefetch(
                        CosmosReadManyByPartitionKeysRequestOptions options) {
                        return options.actualRequestOptions.getMaxConcurrentBatchPrefetch();
                    }

                    @Override
                    public Integer getMaxBatchSize(
                        CosmosReadManyByPartitionKeysRequestOptions options) {
                        return options.actualRequestOptions.getMaxBatchSize();
                    }
                });
    }

    static { initialize(); }
}
