// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.models;

import com.azure.cosmos.CosmosItemSerializer;
import com.azure.cosmos.implementation.ImplementationBridgeHelpers;
import com.azure.cosmos.implementation.apachecommons.collections.list.UnmodifiableList;
import com.azure.cosmos.implementation.batch.BatchRequestResponseConstants;
import com.azure.cosmos.implementation.batch.BulkExecutorDiagnosticsTracker;
import com.azure.cosmos.implementation.spark.OperationContextAndListenerTuple;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkArgument;

/**
 * Encapsulates options that can be specified for operations used in Bulk execution.
 * It can be passed while processing bulk operations.
 */
public final class CosmosBulkExecutionOptions {
    private int initialMicroBatchSize = BatchRequestResponseConstants.MAX_OPERATIONS_IN_DIRECT_MODE_BATCH_REQUEST;
    private int maxMicroBatchConcurrency = BatchRequestResponseConstants.DEFAULT_MAX_MICRO_BATCH_CONCURRENCY;

    private int maxMicroBatchSize = BatchRequestResponseConstants.MAX_OPERATIONS_IN_DIRECT_MODE_BATCH_REQUEST;
    private double maxMicroBatchRetryRate = BatchRequestResponseConstants.DEFAULT_MAX_MICRO_BATCH_RETRY_RATE;
    private double minMicroBatchRetryRate = BatchRequestResponseConstants.DEFAULT_MIN_MICRO_BATCH_RETRY_RATE;

    private int maxMicroBatchPayloadSizeInBytes = BatchRequestResponseConstants.DEFAULT_MAX_DIRECT_MODE_BATCH_REQUEST_BODY_SIZE_IN_BYTES;
    private final Duration maxMicroBatchInterval = Duration.ofMillis(
        BatchRequestResponseConstants.DEFAULT_MAX_MICRO_BATCH_INTERVAL_IN_MILLISECONDS);
    private final Object legacyBatchScopedContext;
    private final CosmosBulkExecutionThresholdsState thresholds;
    private Integer maxConcurrentCosmosPartitions = null;
    private OperationContextAndListenerTuple operationContextAndListenerTuple;
    private Map<String, String> customOptions;
    private String throughputControlGroupName;
    private List<String> excludeRegions;
    private BulkExecutorDiagnosticsTracker diagnosticsTracker = null;
    private CosmosItemSerializer customSerializer;


    CosmosBulkExecutionOptions(CosmosBulkExecutionOptions toBeCloned) {
        this.initialMicroBatchSize = toBeCloned.initialMicroBatchSize;
        this.maxMicroBatchConcurrency = toBeCloned.maxMicroBatchConcurrency;
        this.maxMicroBatchSize = toBeCloned.maxMicroBatchSize;
        this.maxMicroBatchRetryRate = toBeCloned.maxMicroBatchRetryRate;
        this.minMicroBatchRetryRate = toBeCloned.minMicroBatchRetryRate;
        this.maxMicroBatchPayloadSizeInBytes = toBeCloned.maxMicroBatchPayloadSizeInBytes;
        this.legacyBatchScopedContext = toBeCloned.legacyBatchScopedContext;
        this.thresholds = toBeCloned.thresholds;
        this.maxConcurrentCosmosPartitions = toBeCloned.maxConcurrentCosmosPartitions;
        this.throughputControlGroupName = toBeCloned.throughputControlGroupName;
        this.operationContextAndListenerTuple = toBeCloned.operationContextAndListenerTuple;
        this.diagnosticsTracker = toBeCloned.diagnosticsTracker;
        this.customSerializer = toBeCloned.customSerializer;
        this.customOptions = toBeCloned.customOptions;

        if (toBeCloned.excludeRegions != null) {
            this.excludeRegions = new ArrayList<>(toBeCloned.excludeRegions);
        }
    }

    /**
     * Constructor
     * @param thresholdsState thresholds
     */
    CosmosBulkExecutionOptions(Object legacyBatchScopedContext, CosmosBulkExecutionThresholdsState thresholdsState, Map<String, String> customOptions) {
        this.legacyBatchScopedContext = legacyBatchScopedContext;
        if (thresholdsState == null) {
            this.thresholds = new CosmosBulkExecutionThresholdsState();
        } else {
            this.thresholds = thresholdsState;
        }
        if (customOptions == null) {
            this.customOptions = new HashMap<>();
        } else {
            this.customOptions = customOptions;
        }
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
        return initialMicroBatchSize;
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
        checkArgument(
            initialMicroBatchSize > 0,
            "The argument 'initialMicroBatchSize' must be a positive integer.");
        this.initialMicroBatchSize = initialMicroBatchSize;
        return this;
    }

    /**
     * The maximum batching request payload size in bytes for bulk operations.
     *
     * @return maximum micro batch payload size in bytes
     */
    int getMaxMicroBatchPayloadSizeInBytes() {
        return maxMicroBatchPayloadSizeInBytes;
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
        this.maxMicroBatchPayloadSizeInBytes = maxMicroBatchPayloadSizeInBytes;
        return this;
    }

    /**
     * The maximum batch size for bulk operations. Once queued docs exceed this value, the micro
     * batch will be flushed to the wire.
     *
     * @return the max micro batch size.
     */
    public int getMaxMicroBatchSize() {
        return maxMicroBatchSize;
    }

    /**
     * The maximum batch size for bulk operations. Once queued docs exceed this value, the micro
     * batch will be flushed to the wire.
     *
     * @param maxMicroBatchSize maximum batching size.
     * @return the bulk processing options.
     */
    public CosmosBulkExecutionOptions setMaxMicroBatchSize(int maxMicroBatchSize) {
        this.maxMicroBatchSize = maxMicroBatchSize;
        return this;
    }

    /**
     * Gets the custom item serializer defined for this instance of request options
     * @return the custom item serializer
     */
    public CosmosItemSerializer getCustomItemSerializer() {
        return this.customSerializer;
    }

    /**
     * Allows specifying a custom item serializer to be used for this operation. If the serializer
     * on the request options is null, the serializer on CosmosClientBuilder is used. If both serializers
     * are null (the default), an internal Jackson ObjectMapper is ued for serialization/deserialization.
     * @param customItemSerializer the custom item serializer for this operation
     * @return  the CosmosItemRequestOptions.
     */
    public CosmosBulkExecutionOptions setCustomItemSerializer(CosmosItemSerializer customItemSerializer) {
        this.customSerializer = customItemSerializer;

        return this;
    }

    Integer getMaxConcurrentCosmosPartitions() {
        return this.maxConcurrentCosmosPartitions;
    }

    CosmosBulkExecutionOptions setMaxConcurrentCosmosPartitions(int maxConcurrentCosmosPartitions) {
        this.maxConcurrentCosmosPartitions = maxConcurrentCosmosPartitions;
        return this;
    }

    /**
     * The maximum concurrency for executing requests for a partition key range.
     * By default, the maxMicroBatchConcurrency is 1.
     *
     * @return max micro batch concurrency
     */
    public int getMaxMicroBatchConcurrency() {
        return maxMicroBatchConcurrency;
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
        checkArgument(
            maxMicroBatchConcurrency >= 1 && maxMicroBatchConcurrency <= 5,
            "maxMicroBatchConcurrency should be between [1, 5]");
        this.maxMicroBatchConcurrency = maxMicroBatchConcurrency;
        return this;
    }

    /**
     * The flush interval for bulk operations.
     *
     * @return max micro batch interval
     */
    Duration getMaxMicroBatchInterval() {
        return maxMicroBatchInterval;
    }

    /**
     * The maximum acceptable retry rate bandwidth. This value determines how aggressively the actual micro batch size
     * gets reduced or increased if the number of retries (for example due to 429 - Throttling or because the total
     * request size exceeds the payload limit) is higher or lower that the targeted range.
     *
     * @return max targeted micro batch retry rate
     */
    double getMaxTargetedMicroBatchRetryRate() {
        return this.maxMicroBatchRetryRate;
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
        if (minRetryRate < 0) {
            throw new IllegalArgumentException("The maxRetryRate must not be a negative value");
        }

        if (minRetryRate > maxRetryRate) {
            throw new IllegalArgumentException("The minRetryRate must not exceed the maxRetryRate");
        }

        this.maxMicroBatchRetryRate = maxRetryRate;
        this.minMicroBatchRetryRate = minRetryRate;
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
        return this.minMicroBatchRetryRate;
    }

    /**
     * Returns batch context
     * @return batch context
     */
    Object getLegacyBatchScopedContext() {
        return this.legacyBatchScopedContext;
    }

    /**
     * Returns threshold state that can be passed to other CosmosBulkExecutionOptions in the future
     * @return thresholds
     */
    public CosmosBulkExecutionThresholdsState getThresholdsState() {
        return this.thresholds;
    }

    OperationContextAndListenerTuple getOperationContextAndListenerTuple() {
        return this.operationContextAndListenerTuple;
    }

    void setOperationContextAndListenerTuple(OperationContextAndListenerTuple operationContextAndListenerTuple) {
        this.operationContextAndListenerTuple = operationContextAndListenerTuple;
    }

    /**
     * Sets the custom bulk request option value by key
     *
     * @param name  a string representing the custom option's name
     * @param value a string representing the custom option's value
     * @return the CosmosBulkExecutionOptions.
     */
    CosmosBulkExecutionOptions setHeader(String name, String value) {
        if (this.customOptions == null) {
            this.customOptions = new HashMap<>();
        }
        this.customOptions.put(name, value);
        return this;
    }

    /**
     * Gets the custom batch request options
     *
     * @return Map of custom request options
     */
    Map<String, String> getHeaders() {
        return this.customOptions;
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
     * Sets the throughput control group name.
     *
     * @param throughputControlGroupName the throughput control group name.
     * @return the CosmosBulkExecutionOptions.
     */
    public CosmosBulkExecutionOptions setThroughputControlGroupName(String throughputControlGroupName) {
        this.throughputControlGroupName = throughputControlGroupName;

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
        this.excludeRegions = excludeRegions;
        return this;
    }

    /**
     * Gets the list of regions to be excluded for the request/retries. These regions are excluded
     * from the preferred region list.
     *
     * @return a list of excluded regions
     * */
    public List<String> getExcludedRegions() {
        if (this.excludeRegions == null) {
            return null;
        }
        return UnmodifiableList.unmodifiableList(this.excludeRegions);
    }

    void setDiagnosticsTracker(BulkExecutorDiagnosticsTracker tracker) {
        this.diagnosticsTracker = tracker;
    }

    BulkExecutorDiagnosticsTracker getDiagnosticsTracker() {
        return this.diagnosticsTracker;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////
    // the following helper/accessor only helps to access this class outside of this package.//
    ///////////////////////////////////////////////////////////////////////////////////////////
    static void initialize() {
        ImplementationBridgeHelpers.CosmosBulkExecutionOptionsHelper.setCosmosBulkExecutionOptionsAccessor(
            new ImplementationBridgeHelpers.CosmosBulkExecutionOptionsHelper.CosmosBulkExecutionOptionsAccessor() {

                @Override
                public void setOperationContext(CosmosBulkExecutionOptions options,
                                                OperationContextAndListenerTuple operationContextAndListenerTuple) {
                    options.setOperationContextAndListenerTuple(operationContextAndListenerTuple);
                }

                @Override
                public OperationContextAndListenerTuple getOperationContext(CosmosBulkExecutionOptions options) {
                    return options.getOperationContextAndListenerTuple();
                }

                @Override
                @SuppressWarnings({"unchecked"})
                public <T> T getLegacyBatchScopedContext(CosmosBulkExecutionOptions options) {
                    return (T)options.getLegacyBatchScopedContext();
                }

                @Override
                public double getMinTargetedMicroBatchRetryRate(CosmosBulkExecutionOptions options) {
                    return options.getMinTargetedMicroBatchRetryRate();
                }

                @Override
                public double getMaxTargetedMicroBatchRetryRate(CosmosBulkExecutionOptions options) {
                    return options.getMaxTargetedMicroBatchRetryRate();
                }

                @Override
                public int getMaxMicroBatchPayloadSizeInBytes(CosmosBulkExecutionOptions options) {
                    return options.getMaxMicroBatchPayloadSizeInBytes();
                }

                @Override
                public CosmosBulkExecutionOptions setMaxMicroBatchPayloadSizeInBytes(
                    CosmosBulkExecutionOptions options,
                    int maxMicroBatchPayloadSizeInBytes) {

                    return options.setMaxMicroBatchPayloadSizeInBytes(maxMicroBatchPayloadSizeInBytes);
                }

                @Override
                public int getMaxMicroBatchConcurrency(CosmosBulkExecutionOptions options) {
                    return options.getMaxMicroBatchConcurrency();
                }

                @Override
                public Integer getMaxConcurrentCosmosPartitions(CosmosBulkExecutionOptions options) {
                    return options.getMaxConcurrentCosmosPartitions();
                }

                @Override
                public CosmosBulkExecutionOptions setMaxConcurrentCosmosPartitions(
                    CosmosBulkExecutionOptions options, int maxConcurrentCosmosPartitions) {
                    return options.setMaxConcurrentCosmosPartitions(maxConcurrentCosmosPartitions);
                }

                @Override
                public Duration getMaxMicroBatchInterval(CosmosBulkExecutionOptions options) {
                    return options.getMaxMicroBatchInterval();
                }

                @Override
                public CosmosBulkExecutionOptions setTargetedMicroBatchRetryRate(
                    CosmosBulkExecutionOptions options,
                    double minRetryRate,
                    double maxRetryRate) {

                    return options.setTargetedMicroBatchRetryRate(minRetryRate, maxRetryRate);
                }

                @Override
                public CosmosBulkExecutionOptions setHeader(CosmosBulkExecutionOptions cosmosBulkExecutionOptions,
                                                            String name, String value) {
                    return cosmosBulkExecutionOptions.setHeader(name, value);
                }

                @Override
                public Map<String, String> getHeader(CosmosBulkExecutionOptions cosmosBulkExecutionOptions) {
                    return cosmosBulkExecutionOptions.getHeaders();
                }

                @Override
                public Map<String, String> getCustomOptions(CosmosBulkExecutionOptions cosmosBulkExecutionOptions) {
                    return cosmosBulkExecutionOptions.customOptions;
                }

                @Override
                public List<String> getExcludeRegions(CosmosBulkExecutionOptions cosmosBulkExecutionOptions) {
                    return cosmosBulkExecutionOptions.excludeRegions;
                }

                @Override
                public int getMaxMicroBatchSize(CosmosBulkExecutionOptions cosmosBulkExecutionOptions) {
                    if (cosmosBulkExecutionOptions == null) {
                        return BatchRequestResponseConstants.MAX_OPERATIONS_IN_DIRECT_MODE_BATCH_REQUEST;
                    }

                    return cosmosBulkExecutionOptions.getMaxMicroBatchSize();
                }

                @Override
                public void setDiagnosticsTracker(CosmosBulkExecutionOptions cosmosBulkExecutionOptions, BulkExecutorDiagnosticsTracker tracker) {
                    cosmosBulkExecutionOptions.setDiagnosticsTracker(tracker);
                }

                @Override
                public BulkExecutorDiagnosticsTracker getDiagnosticsTracker(CosmosBulkExecutionOptions cosmosBulkExecutionOptions) {
                    return cosmosBulkExecutionOptions.getDiagnosticsTracker();
                }

                @Override
                public CosmosBulkExecutionOptions clone(CosmosBulkExecutionOptions toBeCloned) {
                    return new CosmosBulkExecutionOptions(toBeCloned);
                }
            });
    }

    static { initialize(); }
}
