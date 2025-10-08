// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation;

import com.azure.cosmos.ConsistencyLevel;
import com.azure.cosmos.CosmosDiagnosticsThresholds;
import com.azure.cosmos.CosmosEndToEndOperationLatencyPolicyConfig;
import com.azure.cosmos.CosmosItemSerializer;
import com.azure.cosmos.ReadConsistencyStrategy;
import com.azure.cosmos.implementation.apachecommons.collections.list.UnmodifiableList;
import com.azure.cosmos.implementation.changefeed.common.ChangeFeedMode;
import com.azure.cosmos.implementation.changefeed.common.ChangeFeedStartFromInternal;
import com.azure.cosmos.implementation.changefeed.common.ChangeFeedState;
import com.azure.cosmos.implementation.changefeed.common.ChangeFeedStateV1;
import com.azure.cosmos.implementation.feedranges.FeedRangeInternal;
import com.azure.cosmos.implementation.spark.OperationContextAndListenerTuple;
import com.azure.cosmos.models.CosmosRequestOptions;
import com.azure.cosmos.models.DedicatedGatewayRequestOptions;
import com.azure.cosmos.models.FeedRange;
import com.azure.cosmos.models.PartitionKeyDefinition;
import com.azure.cosmos.util.Beta;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkArgument;

public final class CosmosChangeFeedRequestOptionsImpl implements OverridableRequestOptions {
    private static final int DEFAULT_MAX_ITEM_COUNT = 100;
    private static final int DEFAULT_MAX_PREFETCH_PAGE_COUNT = 1;
    private static final boolean DEFAULT_COMPLETE_AFTER_ALL_CURRENT_CHANGES_RETRIEVED = false;
    private final ChangeFeedState continuationState;
    private final FeedRangeInternal feedRangeInternal;
    private final Map<String, Object> properties;
    private int maxItemCount;
    private int maxPrefetchPageCount;
    private ChangeFeedMode mode;
    private ChangeFeedStartFromInternal startFromInternal;
    private boolean isSplitHandlingDisabled;
    private boolean quotaInfoEnabled;
    private String throughputControlGroupName;
    private Map<String, String> customOptions;
    private OperationContextAndListenerTuple operationContextAndListenerTuple;
    private CosmosDiagnosticsThresholds thresholds;
    private List<String> excludeRegions;
    private CosmosItemSerializer customSerializer;
    private PartitionKeyDefinition partitionKeyDefinition;
    private String collectionRid;
    private Set<String> keywordIdentifiers;
    private boolean completeAfterAllCurrentChangesRetrieved;
    private Long endLSN;
    private ReadConsistencyStrategy readConsistencyStrategy;
    private Callable<Void> responseInterceptor;

    public CosmosChangeFeedRequestOptionsImpl(CosmosChangeFeedRequestOptionsImpl toBeCloned) {
        if (toBeCloned.continuationState != null) {
            this.continuationState = new ChangeFeedStateV1((ChangeFeedStateV1) toBeCloned.continuationState);
        } else {
            this.continuationState = null;
        }
        this.feedRangeInternal = toBeCloned.feedRangeInternal;
        this.properties = toBeCloned.properties;
        this.maxItemCount = toBeCloned.maxItemCount;
        this.readConsistencyStrategy = toBeCloned.readConsistencyStrategy;
        this.maxPrefetchPageCount = toBeCloned.maxPrefetchPageCount;
        this.mode = toBeCloned.mode;
        this.startFromInternal = toBeCloned.startFromInternal;
        this.isSplitHandlingDisabled = toBeCloned.isSplitHandlingDisabled;
        this.quotaInfoEnabled = toBeCloned.quotaInfoEnabled;
        this.throughputControlGroupName = toBeCloned.throughputControlGroupName;
        this.customOptions = toBeCloned.customOptions;
        this.operationContextAndListenerTuple = toBeCloned.operationContextAndListenerTuple;
        this.thresholds = toBeCloned.thresholds;
        this.excludeRegions = toBeCloned.excludeRegions;
        this.customSerializer = toBeCloned.customSerializer;
        this.collectionRid = toBeCloned.collectionRid;
        this.partitionKeyDefinition = toBeCloned.partitionKeyDefinition;
        this.keywordIdentifiers = toBeCloned.keywordIdentifiers;
        this.completeAfterAllCurrentChangesRetrieved = toBeCloned.completeAfterAllCurrentChangesRetrieved;
        this.endLSN = toBeCloned.endLSN;
        this.responseInterceptor = toBeCloned.responseInterceptor;
    }

    public CosmosChangeFeedRequestOptionsImpl(
        FeedRangeInternal feedRange,
        ChangeFeedStartFromInternal startFromInternal,
        ChangeFeedMode mode,
        ChangeFeedState continuationState) {
        super();

        if (feedRange == null) {
            throw new NullPointerException("feedRange");
        }

        if (startFromInternal == null) {
            throw new NullPointerException("startFromInternal");
        }

        this.maxItemCount = DEFAULT_MAX_ITEM_COUNT;
        this.readConsistencyStrategy = ReadConsistencyStrategy.DEFAULT;
        this.maxPrefetchPageCount = DEFAULT_MAX_PREFETCH_PAGE_COUNT;
        this.feedRangeInternal = feedRange;
        this.startFromInternal = startFromInternal;
        if (continuationState != null) {
            this.continuationState = new ChangeFeedStateV1((ChangeFeedStateV1) continuationState);
        } else {
            this.continuationState = null;
        }


        if (mode != ChangeFeedMode.INCREMENTAL && mode != ChangeFeedMode.FULL_FIDELITY) {
            throw new IllegalArgumentException(
                String.format(
                    "Argument 'mode' has unsupported change feed mode %s",
                    mode.toString()));
        }

        this.mode = mode;
        if (this.mode == ChangeFeedMode.FULL_FIDELITY) {
            this.addCustomOptionsForFullFidelityMode();
        }

        this.properties = new HashMap<>();
        this.isSplitHandlingDisabled = false;
        this.completeAfterAllCurrentChangesRetrieved = DEFAULT_COMPLETE_AFTER_ALL_CURRENT_CHANGES_RETRIEVED;
    }

    public ChangeFeedState getContinuation() {
        return this.continuationState;
    }

    public FeedRange getFeedRange() {
        return this.feedRangeInternal;
    }

    @Override
    public Integer getMaxItemCount() {
        return this.maxItemCount;
    }

    @Override
    public Boolean isQueryMetricsEnabled() {
        return null;
    }

    @Override
    public Boolean isIndexMetricsEnabled() {
        return null;
    }

    public void setMaxItemCount(int maxItemCount) {
        this.maxItemCount = maxItemCount;
    }

    @Override
    public Integer getMaxPrefetchPageCount() {
        return this.maxPrefetchPageCount;
    }

    @Override
    public String getQueryNameOrDefault(String defaultQueryName) {
        return null;
    }

    public void setMaxPrefetchPageCount(int maxPrefetchPageCount) {
        checkArgument(
            maxPrefetchPageCount > 0,
            "Argument 'maxPrefetchCount' must be larger than 0.");
        this.maxPrefetchPageCount = maxPrefetchPageCount;


    }

    public boolean isQuotaInfoEnabled() {
        return quotaInfoEnabled;
    }

    public void setQuotaInfoEnabled(boolean quotaInfoEnabled) {
        this.quotaInfoEnabled = quotaInfoEnabled;
    }

    public void setDiagnosticsThresholds(
        CosmosDiagnosticsThresholds operationSpecificThresholds) {

        this.thresholds = operationSpecificThresholds;
    }

    @Override
    public CosmosDiagnosticsThresholds getDiagnosticsThresholds() {
        return this.thresholds;
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

    public CosmosItemSerializer getCustomItemSerializer() {
        return this.customSerializer;
    }

    public void setCustomItemSerializer(CosmosItemSerializer customItemSerializer) {
        this.customSerializer = customItemSerializer;
    }

    public boolean isSplitHandlingDisabled() {
        return this.isSplitHandlingDisabled;
    }

    public void disableSplitHandling() {
        this.isSplitHandlingDisabled = true;
    }

    public ChangeFeedMode getMode() {
        return this.mode;
    }

    public Map<String, Object> getProperties() {
        return properties;
    }

    public ChangeFeedStartFromInternal getStartFromSettings() {
        return this.startFromInternal;
    }

    public void setRequestContinuation(String etag) {
        this.startFromInternal = ChangeFeedStartFromInternal.createFromETagAndFeedRange(
            etag,
            this.feedRangeInternal);
    }

    public Callable<Void> getResponseInterceptor() {
        return this.responseInterceptor;
    }

    public void setResponseInterceptor(Callable<Void> responseInterceptor) {
        this.responseInterceptor = responseInterceptor;
    }

    @Beta(value = Beta.SinceVersion.V4_12_0, warningText =
        Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    @Deprecated //since = "V4_37_0", forRemoval = true
    public CosmosChangeFeedRequestOptionsImpl fullFidelity() {

        if (!this.startFromInternal.supportsFullFidelityRetention()) {
            throw new IllegalStateException(
                "Full fidelity retention is not supported for the chosen change feed start from " +
                    "option. Use CosmosChangeFeedRequestOptions.createForProcessingFromNow or " +
                    "CosmosChangeFeedRequestOptions.createFromContinuation instead."
            );
        }

        this.mode = ChangeFeedMode.FULL_FIDELITY;
        this.addCustomOptionsForFullFidelityMode();
        return this;
    }

    @Beta(value = Beta.SinceVersion.V4_37_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    public CosmosChangeFeedRequestOptionsImpl allVersionsAndDeletes() {

        if (!this.startFromInternal.supportsFullFidelityRetention()) {
            throw new IllegalStateException(
                "All Versions and Deletes mode is not supported for the chosen change feed start from " +
                    "option. Use CosmosChangeFeedRequestOptions.createForProcessingFromNow or " +
                    "CosmosChangeFeedRequestOptions.createFromContinuation instead."
            );
        }

        this.mode = ChangeFeedMode.FULL_FIDELITY;
        this.addCustomOptionsForFullFidelityMode();
        return this;
    }

    @Override
    public String getThroughputControlGroupName() {
        return this.throughputControlGroupName;
    }

    public CosmosChangeFeedRequestOptionsImpl setThroughputControlGroupName(String throughputControlGroupName) {
        this.throughputControlGroupName = throughputControlGroupName;
        return this;
    }

    public CosmosChangeFeedRequestOptionsImpl setExcludedRegions(List<String> excludeRegions) {
        this.excludeRegions = excludeRegions;
        return this;
    }

    @Override
    public CosmosEndToEndOperationLatencyPolicyConfig getCosmosEndToEndLatencyPolicyConfig() {
        // @TODO: Implement this and some of the others below
        return null;
    }

    @Override
    public ConsistencyLevel getConsistencyLevel() {
        return null;
    }

    @Override
    public ReadConsistencyStrategy getReadConsistencyStrategy() {
        return this.readConsistencyStrategy;
    }

    public CosmosChangeFeedRequestOptionsImpl setReadConsistencyStrategy(
        ReadConsistencyStrategy readConsistencyStrategy) {

        this.readConsistencyStrategy = readConsistencyStrategy;
        return this;
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

    public CosmosChangeFeedRequestOptionsImpl setHeader(String name, String value) {
        if (this.customOptions == null) {
            this.customOptions = new HashMap<>();
        }
        this.customOptions.put(name, value);
        return this;
    }

    public Map<String, String> getHeaders() {
        return this.customOptions;
    }

    public void setOperationContextAndListenerTuple(OperationContextAndListenerTuple operationContextAndListenerTuple) {
        this.operationContextAndListenerTuple = operationContextAndListenerTuple;
    }

    public OperationContextAndListenerTuple getOperationContextAndListenerTuple() {
        return this.operationContextAndListenerTuple;
    }

    private void addCustomOptionsForFullFidelityMode() {
        this.setHeader(
            HttpConstants.HttpHeaders.CHANGE_FEED_WIRE_FORMAT_VERSION,
            HttpConstants.ChangeFeedWireFormatVersions.SEPARATE_METADATA_WITH_CRTS);
    }

    public PartitionKeyDefinition getPartitionKeyDefinition() {
        return partitionKeyDefinition;
    }

    public void setPartitionKeyDefinition(PartitionKeyDefinition partitionKeyDefinition) {
        this.partitionKeyDefinition = partitionKeyDefinition;
    }

    public String getCollectionRid() {
        return collectionRid;
    }

    public void setCollectionRid(String collectionRid) {
        this.collectionRid = collectionRid;
    }

    public void setKeywordIdentifiers(Set<String> keywordIdentifiers) {
        this.keywordIdentifiers = keywordIdentifiers;
    }

    @Override
    public Set<String> getKeywordIdentifiers() {
        return this.keywordIdentifiers;
    }

    public void setEndLSN(Long endLSN) {
        this.endLSN = endLSN;
    }

    public Long getEndLSN() {
        return endLSN;
    }

    public boolean isCompleteAfterAllCurrentChangesRetrieved() {
        return this.completeAfterAllCurrentChangesRetrieved;
    }

    public void setCompleteAfterAllCurrentChangesRetrieved(boolean queryAvailableNow) {
        this.completeAfterAllCurrentChangesRetrieved = queryAvailableNow;
    }

    @Override
    public void override(CosmosRequestOptions cosmosRequestOptions) {
        this.maxItemCount = overrideOption(cosmosRequestOptions.getMaxItemCount(), this.maxItemCount);
        this.readConsistencyStrategy = overrideOption(cosmosRequestOptions.getReadConsistencyStrategy(), this.readConsistencyStrategy);
        this.maxPrefetchPageCount = overrideOption(cosmosRequestOptions.getMaxPrefetchPageCount(), this.maxPrefetchPageCount);
        this.excludeRegions = overrideOption(cosmosRequestOptions.getExcludedRegions(), this.excludeRegions);
        this.throughputControlGroupName = overrideOption(cosmosRequestOptions.getThroughputControlGroupName(), this.throughputControlGroupName);
        this.thresholds = overrideOption(cosmosRequestOptions.getDiagnosticsThresholds(), this.thresholds);
        this.keywordIdentifiers = overrideOption(cosmosRequestOptions.getKeywordIdentifiers(), this.keywordIdentifiers);
    }

}
