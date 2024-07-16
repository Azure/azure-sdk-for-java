// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation;

import com.azure.cosmos.ConsistencyLevel;
import com.azure.cosmos.CosmosDiagnosticsThresholds;
import com.azure.cosmos.CosmosEndToEndOperationLatencyPolicyConfig;
import com.azure.cosmos.CosmosItemSerializer;
import com.azure.cosmos.implementation.apachecommons.collections.list.UnmodifiableList;
import com.azure.cosmos.implementation.batch.BatchRequestResponseConstants;
import com.azure.cosmos.implementation.batch.BulkExecutorDiagnosticsTracker;
import com.azure.cosmos.implementation.spark.OperationContextAndListenerTuple;
import com.azure.cosmos.models.CosmosBulkExecutionThresholdsState;
import com.azure.cosmos.models.CosmosRequestOptions;
import com.azure.cosmos.models.DedicatedGatewayRequestOptions;
import reactor.core.scheduler.Scheduler;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
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
    private Set<String> keywordIdentifiers;
    private Scheduler schedulerOverride = null;

    public CosmosBulkExecutionOptionsImpl(CosmosBulkExecutionOptionsImpl toBeCloned) {
        this.schedulerOverride = toBeCloned.schedulerOverride;
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
        if (toBeCloned.keywordIdentifiers != null) {
            this.keywordIdentifiers = new HashSet<>(toBeCloned.keywordIdentifiers);
        }
    }

    public CosmosBulkExecutionOptionsImpl() {
        this(null, null, null);
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

    public void setSchedulerOverride(Scheduler customScheduler) {
        this.schedulerOverride = customScheduler;
    }

    public Scheduler getSchedulerOverride() { return this.schedulerOverride; }

    @Override
    public String getThroughputControlGroupName() {
        return this.throughputControlGroupName;
    }

    @Override
    public CosmosDiagnosticsThresholds getDiagnosticsThresholds() {
        // @Todo: Implement this method and the relevant ones below
        return null;
    }

    @Override
    public Boolean isScanInQueryEnabled() {
        return null;
    }

    @Override
    public Integer getMaxDegreeOfParallelism() {
        return null;
    }

    @Override
    public Integer getMaxBufferedItemCount() {
        return null;
    }

    @Override
    public Integer getResponseContinuationTokenLimitInKb() {
        return null;
    }

    @Override
    public Integer getMaxItemCount() {
        return null;
    }

    @Override
    public Boolean isQueryMetricsEnabled() {
        return null;
    }

    @Override
    public Boolean isIndexMetricsEnabled() {
        return null;
    }

    @Override
    public Integer getMaxPrefetchPageCount() {
        return null;
    }

    @Override
    public String getQueryNameOrDefault(String defaultQueryName) {
        return null;
    }

    public void setThroughputControlGroupName(String throughputControlGroupName) {
        this.throughputControlGroupName = throughputControlGroupName;
    }

    public void setExcludedRegions(List<String> excludeRegions) {
        this.excludeRegions = excludeRegions;
    }

    @Override
    public CosmosEndToEndOperationLatencyPolicyConfig getCosmosEndToEndLatencyPolicyConfig() {
        return null;
    }

    @Override
    public ConsistencyLevel getConsistencyLevel() {
        return null;
    }

    @Override
    public Boolean isContentResponseOnWriteEnabled() {
        return null;
    }

    @Override
    public Boolean getNonIdempotentWriteRetriesEnabled() {
        return null;
    }

    @Override
    public DedicatedGatewayRequestOptions getDedicatedGatewayRequestOptions() {
        return null;
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

    public void setKeywordIdentifiers(Set<String> keywordIdentifiers) {
        this.keywordIdentifiers = keywordIdentifiers;
    }

    @Override
    public Set<String> getKeywordIdentifiers() {
        return this.keywordIdentifiers;
    }

    @Override
    public void override(CosmosRequestOptions cosmosRequestOptions) {
        this.excludeRegions = overrideOption(cosmosRequestOptions.getExcludedRegions(), this.excludeRegions);
        this.throughputControlGroupName = overrideOption(cosmosRequestOptions.getThroughputControlGroupName(), this.throughputControlGroupName);
        this.keywordIdentifiers = overrideOption(cosmosRequestOptions.getKeywordIdentifiers(), this.keywordIdentifiers);
    }

}
