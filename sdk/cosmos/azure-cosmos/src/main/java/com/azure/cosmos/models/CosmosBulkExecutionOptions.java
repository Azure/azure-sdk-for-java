// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.models;

import com.azure.cosmos.CosmosEndToEndOperationLatencyPolicyConfig;
import com.azure.cosmos.CosmosItemSerializer;
import com.azure.cosmos.implementation.CosmosBulkExecutionOptionsImpl;
import com.azure.cosmos.implementation.ImplementationBridgeHelpers;
import com.azure.cosmos.implementation.batch.BatchRequestResponseConstants;
import com.azure.cosmos.implementation.batch.BulkExecutorDiagnosticsTracker;
import com.azure.cosmos.implementation.spark.OperationContextAndListenerTuple;
import reactor.core.scheduler.Scheduler;

import java.time.Duration;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * Encapsulates options that can be specified for operations used in Bulk execution.
 * It can be passed while processing bulk operations.
 */
public final class CosmosBulkExecutionOptions {
    private final CosmosBulkExecutionOptionsImpl actualRequestOptions;
    private static final Set<String> EMPTY_KEYWORD_IDENTIFIERS = Collections.unmodifiableSet(new HashSet<>());

    CosmosBulkExecutionOptions(CosmosBulkExecutionOptions toBeCloned) {
        this.actualRequestOptions = new CosmosBulkExecutionOptionsImpl(toBeCloned.actualRequestOptions);
    }

    /**
     * Constructor
     * @param thresholdsState thresholds
     */
    CosmosBulkExecutionOptions(Object legacyBatchScopedContext, CosmosBulkExecutionThresholdsState thresholdsState, Map<String, String> customOptions) {
       this.actualRequestOptions = new CosmosBulkExecutionOptionsImpl(legacyBatchScopedContext, thresholdsState, customOptions);
    }

    /**
     * Constructor
     * @param thresholdsState thresholds
     */
    public CosmosBulkExecutionOptions(CosmosBulkExecutionThresholdsState thresholdsState) {
        this(null, thresholdsState, null);
    }

    /**
     * Constructor
     */
    public CosmosBulkExecutionOptions() {
        this(null, null, null);
    }

    /**
     * Gets the initial size of micro batches that will be sent to the backend. The size of micro batches will
     * be dynamically adjusted based on the throttling rate. The default value is 100 - so, it starts with relatively
     * large micro batches and when the throttling rate is too high, it will reduce the batch size. When the
     * short spikes of throttling before dynamically reducing the initial batch size results in side effects for other
     * workloads the initial micro batch size can be reduced - for example set to 1 - at which point it would
     * start with small micro batches and then increase the batch size over time.
     * @return the initial micro batch size
     */
    public int getInitialMicroBatchSize() {
        return actualRequestOptions.getInitialMicroBatchSize();
    }

    /**
     * Sets the initial size of micro batches that will be sent to the backend. The size of micro batches will
     * be dynamically adjusted based on the throttling rate. The default value is 100 - so, it starts with relatively
     * large micro batches and when the throttling rate is too high, it will reduce the batch size. When the
     * short spikes of throttling before dynamically reducing the initial batch size results in side effects for other
     * workloads the initial micro batch size can be reduced - for example set to 1 - at which point it would
     * start with small micro batches and then increase the batch size over time.
     * @param initialMicroBatchSize the initial micro batch size to be used. Must be a positive integer.
     * @return the bulk execution options.
     */
    public CosmosBulkExecutionOptions setInitialMicroBatchSize(int initialMicroBatchSize) {
        this.actualRequestOptions.setInitialMicroBatchSize(initialMicroBatchSize);
        return this;
    }

    /**
     * The maximum batching request payload size in bytes for bulk operations.
     *
     * @return maximum micro batch payload size in bytes
     */
    int getMaxMicroBatchPayloadSizeInBytes() {
        return this.actualRequestOptions.getMaxMicroBatchPayloadSizeInBytes();
    }

    /**
     * The maximum batching payload size in bytes for bulk operations. Once queued docs exceed this values the micro
     * batch will be flushed to the wire.
     *
     * @param maxMicroBatchPayloadSizeInBytes maximum payload size of a micro batch in bytes.
     *
     * @return the bulk processing options.
     */
    CosmosBulkExecutionOptions setMaxMicroBatchPayloadSizeInBytes(int maxMicroBatchPayloadSizeInBytes) {
        this.actualRequestOptions.setMaxMicroBatchPayloadSizeInBytes(maxMicroBatchPayloadSizeInBytes);
        return this;
    }

    /**
     * The maximum batch size for bulk operations. Once queued docs exceed this value, the micro
     * batch will be flushed to the wire.
     *
     * @return the max micro batch size.
     */
    public int getMaxMicroBatchSize() {
        return this.actualRequestOptions.getMaxMicroBatchSize();
    }

    /**
     * The maximum batch size for bulk operations. Once queued docs exceed this value, the micro
     * batch will be flushed to the wire.
     *
     * @param maxMicroBatchSize maximum batching size.
     * @return the bulk processing options.
     */
    public CosmosBulkExecutionOptions setMaxMicroBatchSize(int maxMicroBatchSize) {
        this.actualRequestOptions.setMaxMicroBatchSize(maxMicroBatchSize);
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
    public CosmosBulkExecutionOptions setCustomItemSerializer(CosmosItemSerializer customItemSerializer) {
        this.actualRequestOptions.setCustomItemSerializer(customItemSerializer);
        return this;
    }

    Integer getMaxConcurrentCosmosPartitions() {
        return this.actualRequestOptions.getMaxConcurrentCosmosPartitions();
    }

    CosmosBulkExecutionOptions setMaxConcurrentCosmosPartitions(int maxConcurrentCosmosPartitions) {
        this.actualRequestOptions.setMaxConcurrentCosmosPartitions(maxConcurrentCosmosPartitions);
        return this;
    }

    /**
     * The maximum concurrency for executing requests for a partition key range.
     * By default, the maxMicroBatchConcurrency is 1.
     *
     * @return max micro batch concurrency
     */
    public int getMaxMicroBatchConcurrency() {
        return this.actualRequestOptions.getMaxMicroBatchConcurrency();
    }

    /**
     * Set the maximum concurrency for executing requests for a partition key range.
     * By default, the maxMicroBatchConcurrency is 1.
     * It only allows values &ge;1 and &le;5.
     *
     * Attention! Please adjust this value with caution.
     * By increasing this value, more concurrent requests will be allowed to be sent to the server,
     * in which case may cause 429 or request timed out due to saturate local resources, which could degrade the performance.
     *
     * @param maxMicroBatchConcurrency the micro batch concurrency.
     *
     * @return the bulk processing options.
     */
    public CosmosBulkExecutionOptions setMaxMicroBatchConcurrency(int maxMicroBatchConcurrency) {
        this.actualRequestOptions.setMaxMicroBatchConcurrency(maxMicroBatchConcurrency);
        return this;
    }

    /**
     * The flush interval for bulk operations.
     *
     * @return max micro batch interval
     */
    Duration getMaxMicroBatchInterval() {
        return this.actualRequestOptions.getMaxMicroBatchInterval();
    }

    /**
     * The maximum acceptable retry rate bandwidth. This value determines how aggressively the actual micro batch size
     * gets reduced or increased if the number of retries (for example due to 429 - Throttling or because the total
     * request size exceeds the payload limit) is higher or lower that the targeted range.
     *
     * @return max targeted micro batch retry rate
     */
    double getMaxTargetedMicroBatchRetryRate() {
        return this.actualRequestOptions.getMaxTargetedMicroBatchRetryRate();
    }

    /**
     * The acceptable retry rate bandwidth. This value determines how aggressively the actual micro batch size
     * gets reduced or increased if the number of retries (for example due to 429 - Throttling or because the total
     * request size exceeds the payload limit) is higher or lower that the targeted range.
     *
     * @param minRetryRate minimum targeted retry rate of batch requests. If the retry rate is
     *                     lower than this threshold the micro batch size will be dynamically increased over time
     * @param maxRetryRate maximum retry rate of batch requests that is treated as acceptable. If the retry rate is
     *                     higher than this threshold the micro batch size will be dynamically reduced over time
     *
     * @return the bulk processing options.
     */
    CosmosBulkExecutionOptions setTargetedMicroBatchRetryRate(double minRetryRate, double maxRetryRate) {
        this.actualRequestOptions.setTargetedMicroBatchRetryRate(minRetryRate, maxRetryRate);
        return this;
    }

    /**
     * The minimum acceptable retry rate bandwidth. This value determines how aggressively the actual micro batch size
     * gets reduced or increased if the number of retries (for example due to 429 - Throttling or because the total
     * request size exceeds the payload limit) is higher or lower that the targeted range.
     *
     * @return min targeted micro batch retry rate
     */
    double getMinTargetedMicroBatchRetryRate() {
        return this.actualRequestOptions.getMinTargetedMicroBatchRetryRate();
    }

    /**
     * Returns batch context
     * @return batch context
     */
    Object getLegacyBatchScopedContext() {
        return this.actualRequestOptions.getLegacyBatchScopedContext();
    }

    /**
     * Returns threshold state that can be passed to other CosmosBulkExecutionOptions in the future
     * @return thresholds
     */
    public CosmosBulkExecutionThresholdsState getThresholdsState() {
        return this.actualRequestOptions.getThresholdsState();
    }

    OperationContextAndListenerTuple getOperationContextAndListenerTuple() {
        return this.actualRequestOptions.getOperationContextAndListenerTuple();
    }

    void setOperationContextAndListenerTuple(OperationContextAndListenerTuple operationContextAndListenerTuple) {
        this.actualRequestOptions.setOperationContextAndListenerTuple(operationContextAndListenerTuple);
    }

    /**
     * Sets the custom bulk request option value by key
     *
     * @param name  a string representing the custom option's name
     * @param value a string representing the custom option's value
     * @return the CosmosBulkExecutionOptions.
     */
    CosmosBulkExecutionOptions setHeader(String name, String value) {
        this.actualRequestOptions.setHeader(name, value);
        return this;
    }

    /**
     * Gets the custom batch request options
     *
     * @return Map of custom request options
     */
    Map<String, String> getHeaders() {
        return this.actualRequestOptions.getHeaders();
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
     * @return the CosmosBulkExecutionOptions.
     */
    public CosmosBulkExecutionOptions setThroughputControlGroupName(String throughputControlGroupName) {
        this.actualRequestOptions.setThroughputControlGroupName(throughputControlGroupName);
        return this;
    }

    /**
     * List of regions to exclude for the request/retries. Example "East US" or "East US, West US"
     * These regions will be excluded from the preferred regions list
     *
     * @param excludeRegions list of regions
     * @return the {@link CosmosBulkExecutionOptions}
     */
    public CosmosBulkExecutionOptions setExcludedRegions(List<String> excludeRegions) {
        this.actualRequestOptions.setExcludedRegions(excludeRegions);
        return this;
    }

    /**
     * Gets the list of regions to be excluded for the request/retries. These regions are excluded
     * from the preferred region list.
     *
     * @return a list of excluded regions
     * */
    public List<String> getExcludedRegions() {
        return this.actualRequestOptions.getExcludedRegions();
    }

    void setDiagnosticsTracker(BulkExecutorDiagnosticsTracker tracker) {
        this.actualRequestOptions.setDiagnosticsTracker(tracker);
    }

    BulkExecutorDiagnosticsTracker getDiagnosticsTracker() {
        return this.actualRequestOptions.getDiagnosticsTracker();
    }

    /**
     * Sets the custom ids.
     *
     * @param keywordIdentifiers the custom ids.
     * @return the current request options.
     */
    public CosmosBulkExecutionOptions setKeywordIdentifiers(Set<String> keywordIdentifiers) {
        if (keywordIdentifiers != null) {
            this.actualRequestOptions.setKeywordIdentifiers(Collections.unmodifiableSet(keywordIdentifiers));
        } else {
            this.actualRequestOptions.setKeywordIdentifiers(EMPTY_KEYWORD_IDENTIFIERS);
        }
        return this;
    }

    /**
     * Gets the custom ids.
     *
     * @return set of custom ids.
     */
    public Set<String> getKeywordIdentifiers() {
        return this.actualRequestOptions.getKeywordIdentifiers();
    }

    CosmosBulkExecutionOptionsImpl getImpl() {
        return this.actualRequestOptions;
    }

    CosmosBulkExecutionOptions setSchedulerOverride(Scheduler customScheduler) {
        this.actualRequestOptions.setSchedulerOverride(customScheduler);
        return this;
    }

    CosmosEndToEndOperationLatencyPolicyConfig getEndToEndOperationLatencyPolicyConfig() {
        return this.actualRequestOptions.getCosmosEndToEndLatencyPolicyConfig();
    }

    CosmosBulkExecutionOptions setEndToEndOperationLatencyPolicyConfig(CosmosEndToEndOperationLatencyPolicyConfig cfg) {
        this.actualRequestOptions.setCosmosEndToEndLatencyPolicyConfig(cfg);

        return this;
    }


    ///////////////////////////////////////////////////////////////////////////////////////////
    // the following helper/accessor only helps to access this class outside of this package.//
    ///////////////////////////////////////////////////////////////////////////////////////////
    static void initialize() {
        ImplementationBridgeHelpers.CosmosBulkExecutionOptionsHelper.setCosmosBulkExecutionOptionsAccessor(
            new ImplementationBridgeHelpers.CosmosBulkExecutionOptionsHelper.CosmosBulkExecutionOptionsAccessor() {
                public CosmosBulkExecutionOptions clone(CosmosBulkExecutionOptions toBeCloned) {
                    return new CosmosBulkExecutionOptions(toBeCloned);
                }

                @Override
                public CosmosBulkExecutionOptionsImpl getImpl(CosmosBulkExecutionOptions options) {
                    return options.getImpl();
                }
            });
    }

    static { initialize(); }
}
