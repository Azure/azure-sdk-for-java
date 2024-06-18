// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation;

import com.azure.cosmos.CosmosItemSerializer;
import com.azure.cosmos.implementation.apachecommons.collections.list.UnmodifiableList;
import com.azure.cosmos.implementation.batch.BatchRequestResponseConstants;
import com.azure.cosmos.implementation.batch.BulkExecutorDiagnosticsTracker;
import com.azure.cosmos.implementation.spark.OperationContextAndListenerTuple;
import com.azure.cosmos.models.CosmosBatchPatchItemRequestOptions;
import com.azure.cosmos.models.CosmosBulkExecutionThresholdsState;
import com.azure.cosmos.models.CosmosCommonRequestOptions;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkArgument;

/**
 * Encapsulates options that can be specified for operations used in Bulk execution.
 * It can be passed while processing bulk operations.
 */
public class CosmosBulkExecutionOptionsImpl implements OverridableRequestOptions {
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
    private Set<String> customCorrelatedIds;


    public CosmosBulkExecutionOptionsImpl(CosmosBulkExecutionOptionsImpl toBeCloned) {
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
    public CosmosBulkExecutionOptionsImpl(Object legacyBatchScopedContext, CosmosBulkExecutionThresholdsState thresholdsState, Map<String, String> customOptions) {
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

    public int getInitialMicroBatchSize() {
        return initialMicroBatchSize;
    }

    public void setInitialMicroBatchSize(int initialMicroBatchSize) {
        checkArgument(
            initialMicroBatchSize > 0,
            "The argument 'initialMicroBatchSize' must be a positive integer.");
        this.initialMicroBatchSize = initialMicroBatchSize;
    }

    public int getMaxMicroBatchPayloadSizeInBytes() {
        return maxMicroBatchPayloadSizeInBytes;
    }

    public void setMaxMicroBatchPayloadSizeInBytes(int maxMicroBatchPayloadSizeInBytes) {
        this.maxMicroBatchPayloadSizeInBytes = maxMicroBatchPayloadSizeInBytes;
    }

    public int getMaxMicroBatchSize() {
        return maxMicroBatchSize;
    }

    public void setMaxMicroBatchSize(int maxMicroBatchSize) {
        this.maxMicroBatchSize = maxMicroBatchSize;
    }

    public CosmosItemSerializer getCustomItemSerializer() {
        return this.customSerializer;
    }

    public void setCustomItemSerializer(CosmosItemSerializer customItemSerializer) {
        this.customSerializer = customItemSerializer;
    }

    public Integer getMaxConcurrentCosmosPartitions() {
        return this.maxConcurrentCosmosPartitions;
    }

    public void setMaxConcurrentCosmosPartitions(int maxConcurrentCosmosPartitions) {
        this.maxConcurrentCosmosPartitions = maxConcurrentCosmosPartitions;
    }

    public int getMaxMicroBatchConcurrency() {
        return maxMicroBatchConcurrency;
    }

    public void setMaxMicroBatchConcurrency(int maxMicroBatchConcurrency) {
        checkArgument(
            maxMicroBatchConcurrency >= 1 && maxMicroBatchConcurrency <= 5,
            "maxMicroBatchConcurrency should be between [1, 5]");
        this.maxMicroBatchConcurrency = maxMicroBatchConcurrency;
    }

    public Duration getMaxMicroBatchInterval() {
        return maxMicroBatchInterval;
    }

    public double getMaxTargetedMicroBatchRetryRate() {
        return this.maxMicroBatchRetryRate;
    }

    public void setTargetedMicroBatchRetryRate(double minRetryRate, double maxRetryRate) {
        if (minRetryRate < 0) {
            throw new IllegalArgumentException("The maxRetryRate must not be a negative value");
        }

        if (minRetryRate > maxRetryRate) {
            throw new IllegalArgumentException("The minRetryRate must not exceed the maxRetryRate");
        }

        this.maxMicroBatchRetryRate = maxRetryRate;
        this.minMicroBatchRetryRate = minRetryRate;
    }

    public double getMinTargetedMicroBatchRetryRate() {
        return this.minMicroBatchRetryRate;
    }

    public Object getLegacyBatchScopedContext() {
        return this.legacyBatchScopedContext;
    }

    public CosmosBulkExecutionThresholdsState getThresholdsState() {
        return this.thresholds;
    }

    public OperationContextAndListenerTuple getOperationContextAndListenerTuple() {
        return this.operationContextAndListenerTuple;
    }

    public void setOperationContextAndListenerTuple(OperationContextAndListenerTuple operationContextAndListenerTuple) {
        this.operationContextAndListenerTuple = operationContextAndListenerTuple;
    }

    public void setHeader(String name, String value) {
        if (this.customOptions == null) {
            this.customOptions = new HashMap<>();
        }
        this.customOptions.put(name, value);
    }

    public Map<String, String> getHeaders() {
        return this.customOptions;
    }

    @Override
    public String getThroughputControlGroupName() {
        return this.throughputControlGroupName;
    }

    public void setThroughputControlGroupName(String throughputControlGroupName) {
        this.throughputControlGroupName = throughputControlGroupName;
    }

    public void setExcludedRegions(List<String> excludeRegions) {
        this.excludeRegions = excludeRegions;
    }

    @Override
    public List<String> getExcludedRegions() {
        if (this.excludeRegions == null) {
            return null;
        }
        return UnmodifiableList.unmodifiableList(this.excludeRegions);
    }

    public void setDiagnosticsTracker(BulkExecutorDiagnosticsTracker tracker) {
        this.diagnosticsTracker = tracker;
    }

    public BulkExecutorDiagnosticsTracker getDiagnosticsTracker() {
        return this.diagnosticsTracker;
    }

    public void setCustomCorrelatedIds(Set<String> customCorrelatedIds) {
        this.customCorrelatedIds = customCorrelatedIds;
    }

    @Override
    public Set<String> getCustomCorrelatedIds() {
        return this.customCorrelatedIds;
    }

    @Override
    public void override(CosmosCommonRequestOptions cosmosCommonRequestOptions) {
        this.excludeRegions = overrideOption(cosmosCommonRequestOptions.getExcludedRegions(), this.excludeRegions);
        this.throughputControlGroupName = overrideOption(cosmosCommonRequestOptions.getThroughputControlGroupName(), this.throughputControlGroupName);
        this.customCorrelatedIds = overrideOption(cosmosCommonRequestOptions.getCustomCorrelatedIds(), this.customCorrelatedIds);
    }

}
