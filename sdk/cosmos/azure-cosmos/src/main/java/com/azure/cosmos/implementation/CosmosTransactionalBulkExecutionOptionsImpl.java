// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation;

import com.azure.cosmos.ConsistencyLevel;
import com.azure.cosmos.CosmosDiagnosticsThresholds;
import com.azure.cosmos.CosmosEndToEndOperationLatencyPolicyConfig;
import com.azure.cosmos.CosmosItemSerializer;
import com.azure.cosmos.ReadConsistencyStrategy;
import com.azure.cosmos.implementation.apachecommons.collections.list.UnmodifiableList;
import com.azure.cosmos.implementation.batch.BulkExecutorDiagnosticsTracker;
import com.azure.cosmos.implementation.spark.OperationContextAndListenerTuple;
import com.azure.cosmos.models.CosmosRequestOptions;
import com.azure.cosmos.models.DedicatedGatewayRequestOptions;
import reactor.core.scheduler.Scheduler;

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
public class CosmosTransactionalBulkExecutionOptionsImpl implements OverridableRequestOptions {
    private int maxMicroBatchConcurrency = Configs.getMaxBulkMicroBatchConcurrency();

    private Integer maxConcurrentCosmosPartitions = null;
    private OperationContextAndListenerTuple operationContextAndListenerTuple;
    private Map<String, String> customOptions;
    private String throughputControlGroupName;
    private List<String> excludeRegions;
    private BulkExecutorDiagnosticsTracker diagnosticsTracker = null;
    private CosmosItemSerializer customSerializer;
    private Set<String> keywordIdentifiers;
    private Scheduler schedulerOverride = null;

    private CosmosEndToEndOperationLatencyPolicyConfig e2ePolicy = null;

    public CosmosTransactionalBulkExecutionOptionsImpl(CosmosTransactionalBulkExecutionOptionsImpl toBeCloned) {
        this.schedulerOverride = toBeCloned.schedulerOverride;
        this.maxMicroBatchConcurrency = toBeCloned.maxMicroBatchConcurrency;
        this.maxConcurrentCosmosPartitions = toBeCloned.maxConcurrentCosmosPartitions;
        this.throughputControlGroupName = toBeCloned.throughputControlGroupName;
        this.operationContextAndListenerTuple = toBeCloned.operationContextAndListenerTuple;
        this.diagnosticsTracker = toBeCloned.diagnosticsTracker;
        this.customSerializer = toBeCloned.customSerializer;
        this.customOptions = toBeCloned.customOptions;
        this.e2ePolicy = toBeCloned.e2ePolicy;

        if (toBeCloned.excludeRegions != null) {
            this.excludeRegions = new ArrayList<>(toBeCloned.excludeRegions);
        }
        if (toBeCloned.keywordIdentifiers != null) {
            this.keywordIdentifiers = new HashSet<>(toBeCloned.keywordIdentifiers);
        }
    }

    public CosmosTransactionalBulkExecutionOptionsImpl(Map<String, String> customOptions) {
        if (customOptions == null) {
            this.customOptions = new HashMap<>();
        } else {
            this.customOptions = customOptions;
        }
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
        return this.e2ePolicy;
    }

    public void setCosmosEndToEndLatencyPolicyConfig(CosmosEndToEndOperationLatencyPolicyConfig cfg) {
        this.e2ePolicy = cfg;
    }

    @Override
    public ConsistencyLevel getConsistencyLevel() {
        return null;
    }

    @Override
    public ReadConsistencyStrategy getReadConsistencyStrategy() {
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
