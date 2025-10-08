// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.models;

import com.azure.cosmos.CosmosDiagnosticsThresholds;
import com.azure.cosmos.CosmosItemSerializer;
import com.azure.cosmos.ReadConsistencyStrategy;
import com.azure.cosmos.implementation.CosmosChangeFeedRequestOptionsImpl;
import com.azure.cosmos.implementation.CosmosPagedFluxOptions;
import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.ImplementationBridgeHelpers;
import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;
import com.azure.cosmos.implementation.changefeed.common.ChangeFeedMode;
import com.azure.cosmos.implementation.changefeed.common.ChangeFeedStartFromInternal;
import com.azure.cosmos.implementation.changefeed.common.ChangeFeedState;
import com.azure.cosmos.implementation.changefeed.common.ChangeFeedStateV1;
import com.azure.cosmos.implementation.feedranges.FeedRangeContinuation;
import com.azure.cosmos.implementation.feedranges.FeedRangeEpkImpl;
import com.azure.cosmos.implementation.feedranges.FeedRangeInternal;
import com.azure.cosmos.implementation.query.CompositeContinuationToken;
import com.azure.cosmos.implementation.routing.Range;
import com.azure.cosmos.implementation.spark.OperationContextAndListenerTuple;
import com.azure.cosmos.util.Beta;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

/**
 * Encapsulates options that can be specified for an operation within a change feed request.
 */
public final class CosmosChangeFeedRequestOptions {
   private final CosmosChangeFeedRequestOptionsImpl actualRequestOptions;
   private static final Set<String> EMPTY_KEYWORD_IDENTIFIERS = Collections.unmodifiableSet(new HashSet<>());

    CosmosChangeFeedRequestOptions(CosmosChangeFeedRequestOptions topBeCloned) {
       this.actualRequestOptions  = new CosmosChangeFeedRequestOptionsImpl(topBeCloned.actualRequestOptions);
    }

    private CosmosChangeFeedRequestOptions(
        FeedRangeInternal feedRange,
        ChangeFeedStartFromInternal startFromInternal,
        ChangeFeedMode mode,
        ChangeFeedState continuationState) {
       this.actualRequestOptions = new CosmosChangeFeedRequestOptionsImpl(feedRange, startFromInternal, mode, continuationState);
    }

    ChangeFeedState getContinuation() {
        return this.actualRequestOptions.getContinuation();
    }

    /**
     * Gets the feed range.
     *
     * @return the feed range.
     */
    public FeedRange getFeedRange() {
        return this.actualRequestOptions.getFeedRange();
    }

    /**
     * Gets the maximum number of items to be returned in the enumeration
     * operation.
     *
     * @return the max number of items.
     */
    public int getMaxItemCount() {
        return this.actualRequestOptions.getMaxItemCount();
    }

    /**
     * Sets the maximum number of items to be returned in the enumeration
     * operation.
     *
     * @param maxItemCount the max number of items.
     * @return the FeedOptionsBase.
     */
    public CosmosChangeFeedRequestOptions setMaxItemCount(int maxItemCount) {
        this.actualRequestOptions.setMaxItemCount(maxItemCount);
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
     * Sets the read consistency strategy required for the request. This allows specifying the effective consistency
     * strategy for read/query and change feed operations and even request stronger consistency (`LOCAL_COMMITTED` for example) for
     * accounts with lower default consistency level
     * NOTE: If the read consistency strategy set on a request level here is `SESSION` and the default consistency
     * level specified when constructing the CosmosClient instance via CosmosClientBuilder.consistencyLevel
     * is not SESSION then session token capturing also needs to be enabled by calling
     * CosmosClientBuilder:sessionCapturingOverrideEnabled(true) explicitly.
     *
     * @param readConsistencyStrategy the consistency level.
     * @return the request options.
     */
    @Beta(value = Beta.SinceVersion.V4_69_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    public CosmosChangeFeedRequestOptions setReadConsistencyStrategy(ReadConsistencyStrategy readConsistencyStrategy) {
        this.actualRequestOptions.setReadConsistencyStrategy(readConsistencyStrategy);
        return this;
    }

    /**
     * Gets the maximum number of pages that will be prefetched from the backend asynchronously
     * in the background. By pre-fetching these changes the throughput of processing the
     * change feed records can be increased because the processing doesn't have to stop while
     * waiting for the IO operations to retrieve a new page form the backend to complete. The
     * only scenario where it can be useful to disable prefetching pages (with
     * setMaxPrefetchPageCount(0))
     * would be when the caller only plans to retrieve just one page - so any prefetched pages
     * would not be used anyway.
     *
     * @return the modified change feed request options.
     */
    public int getMaxPrefetchPageCount() {
        return this.actualRequestOptions.getMaxPrefetchPageCount();
    }

    /**
     * Sets the maximum number of pages that will be prefetched from the backend asynchronously
     * in the background. By pre-fetching these changes the throughput of processing the
     * change feed records can be increased because the processing doesn't have to stop while
     * waiting for the IO operations to retrieve a new page form the backend to complete. The
     * only scenario where it can be useful to disable prefetching pages (with
     * setMaxPrefetchPageCount(0))
     * would be when the caller only plans to retrieve just one page - so any prefetched pages
     * would not be used anyway.
     *
     * @param maxPrefetchPageCount the max number of pages that will be prefetched from the backend
     *                             asynchronously in the background
     * @return the modified change feed request options.
     */
    public CosmosChangeFeedRequestOptions setMaxPrefetchPageCount(int maxPrefetchPageCount) {
       this.actualRequestOptions.setMaxPrefetchPageCount(maxPrefetchPageCount);

        return this;
    }

    /**
     * Gets the quotaInfoEnabled setting for change feed request in the Azure Cosmos DB database service.
     * quotaInfoEnabled is used to enable/disable getting quota related stats
     *
     * @return true if quotaInfoEnabled is enabled
     */
    public boolean isQuotaInfoEnabled() {
        return this.actualRequestOptions.isQuotaInfoEnabled();
    }

    /**
     * Gets the quotaInfoEnabled setting for change feed request in the Azure Cosmos DB database service.
     * quotaInfoEnabled is used to enable/disable getting quota related stats
     *
     * @param quotaInfoEnabled a boolean value indicating whether quotaInfoEnabled is enabled or not
     */
    public void setQuotaInfoEnabled(boolean quotaInfoEnabled) {
        this.actualRequestOptions.setQuotaInfoEnabled(quotaInfoEnabled);
    }

    /**
     * Allows overriding the diagnostic thresholds for a specific operation.
     * @param operationSpecificThresholds the diagnostic threshold override for this operation
     * @return the CosmosQueryRequestOptions.
     */
    public CosmosChangeFeedRequestOptions setDiagnosticsThresholds(
        CosmosDiagnosticsThresholds operationSpecificThresholds) {

        this.actualRequestOptions.setDiagnosticsThresholds(operationSpecificThresholds);
        return this;
    }

    /**
     * Gets the diagnostic thresholds used as an override for a specific operation. If no operation specific
     * diagnostic threshold has been specified, this method will return null, although at runtime the default
     * thresholds specified at the client-level will be used.
     * @return the diagnostic thresholds used as an override for a specific operation.
     */
    public CosmosDiagnosticsThresholds getDiagnosticsThresholds() {
        return this.actualRequestOptions.getDiagnosticsThresholds();
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
     * @return  the CosmosChangeFeedRequestOptions.
     */
    public CosmosChangeFeedRequestOptions setCustomItemSerializer(CosmosItemSerializer customItemSerializer) {
        this.actualRequestOptions.setCustomItemSerializer(customItemSerializer);
        return this;
    }

    /***
     * Whether the query should be completed when all available changes when the query starts have been fetched.
     *
     * @param completeAfterAllCurrentChangesRetrieved flag to indicate whether to complete the query when all changes up to current moment have been fetched.
     * @return the CosmosChangeFeedRequestOptions.
     */
    public CosmosChangeFeedRequestOptions setCompleteAfterAllCurrentChangesRetrieved(
        boolean completeAfterAllCurrentChangesRetrieved) {
        this.actualRequestOptions.setCompleteAfterAllCurrentChangesRetrieved(completeAfterAllCurrentChangesRetrieved);
        return this;
    }

    /***
     * Whether the query should be completed when all available changes when the query starts have been fetched.
     *
     * @return true if complete the query when all changes up to the current moment have been fetched.
     */
    public boolean isCompleteAfterAllCurrentChangesRetrieved() {
        return this.actualRequestOptions.isCompleteAfterAllCurrentChangesRetrieved();
    }

    // This will override setCompleteAfterAllCurrentChangesRetrieved if both used together
    CosmosChangeFeedRequestOptions setEndLSN(Long endLsn) {
        this.actualRequestOptions.setEndLSN(endLsn);
        return this;
    }

    Long getEndLSN() {
        return this.actualRequestOptions.getEndLSN();
    }

    boolean isSplitHandlingDisabled() {
        return this.actualRequestOptions.isSplitHandlingDisabled();
    }

    CosmosChangeFeedRequestOptions disableSplitHandling() {
        this.actualRequestOptions.disableSplitHandling();
        return this;
    }

    ChangeFeedMode getMode() {
        return this.actualRequestOptions.getMode();
    }

    /**
     * Gets the properties
     *
     * @return Map of request options properties
     */
    Map<String, Object> getProperties() {
        return this.actualRequestOptions.getProperties();
    }

    ChangeFeedStartFromInternal getStartFromSettings() {
        return this.actualRequestOptions.getStartFromSettings();
    }

    /**
     * Creates a new {@link CosmosChangeFeedRequestOptions} instance to start processing
     * change feed items from the beginning of the change feed
     *
     * @param feedRange The {@link FeedRange} that is used to define the scope (entire container,
     *                  logical partition or subset of a container)
     * @return a new {@link CosmosChangeFeedRequestOptions} instance
     */
    public static CosmosChangeFeedRequestOptions createForProcessingFromBeginning(FeedRange feedRange) {
        checkNotNull(feedRange, "Argument 'feedRange' must not be null.");

        return new CosmosChangeFeedRequestOptions(
            FeedRangeInternal.convert(feedRange),
            ChangeFeedStartFromInternal.createFromBeginning(),
            ChangeFeedMode.INCREMENTAL,
            null);
    }

    /**
     * Creates a new {@link CosmosChangeFeedRequestOptions} instance to start processing
     * change feed items from a previous continuation
     *
     * @param continuation The continuation that was retrieved from a previously retrieved
     *                     FeedResponse
     * @return a new {@link CosmosChangeFeedRequestOptions} instance
     */
    public static CosmosChangeFeedRequestOptions createForProcessingFromContinuation(
        String continuation) {

        final ChangeFeedState changeFeedState = ChangeFeedState.fromString(continuation);

        return createForProcessingFromContinuation(changeFeedState);
    }

    /***
     * Creates a new {@link CosmosChangeFeedRequestOptions} instance to start processing
     * change feed items based on a previous continuation.
     * ONLY used by Kafka connector.
     *
     * @param continuation The continuation that was retrieved from a previously retrieved FeedResponse
     * @param targetRange the new target range
     * @param continuationLsn the new continuation lsn
     * @return a new {@link CosmosChangeFeedRequestOptions} instance
     */
    static CosmosChangeFeedRequestOptions createForProcessingFromContinuation(
        String continuation, FeedRange targetRange, String continuationLsn) {
        if (targetRange instanceof FeedRangeEpkImpl) {
            Range<String> normalizedRange =
                FeedRangeInternal.normalizeRange(((FeedRangeEpkImpl) targetRange).getRange());

            final ChangeFeedState changeFeedState = ChangeFeedState.fromString(continuation);

            if (StringUtils.isEmpty(continuationLsn)) {
                continuationLsn = changeFeedState.getContinuation().getCurrentContinuationToken().getToken();
            }

            ChangeFeedState targetChangeFeedState =
                new ChangeFeedStateV1(
                    changeFeedState.getContainerRid(),
                    (FeedRangeEpkImpl) targetRange,
                    changeFeedState.getMode(),
                    changeFeedState.getStartFromSettings(),
                    FeedRangeContinuation.create(
                        changeFeedState.getContainerRid(),
                        (FeedRangeEpkImpl) targetRange,
                        Arrays.asList(new CompositeContinuationToken(continuationLsn, normalizedRange))
                    )
                );

            return createForProcessingFromContinuation(targetChangeFeedState);
        }

        throw new IllegalStateException("createForProcessingFromContinuation does not support feedRange type " + targetRange.getClass());
    }

    static CosmosChangeFeedRequestOptions createForProcessingFromContinuation(
        ChangeFeedState changeFeedState) {

        FeedRangeInternal feedRange = changeFeedState.getFeedRange();
        FeedRangeContinuation continuation = changeFeedState.getContinuation();
        ChangeFeedMode mode = changeFeedState.getMode();

        if (continuation != null) {
            CompositeContinuationToken continuationToken =
                continuation.getCurrentContinuationToken();
            if (continuationToken != null) {
                String etag = continuationToken.getToken();
                return new CosmosChangeFeedRequestOptions(
                    feedRange,
                    ChangeFeedStartFromInternal.createFromETagAndFeedRange(etag, feedRange),
                    mode,
                    changeFeedState);
            }

            return new CosmosChangeFeedRequestOptions(
                feedRange,
                ChangeFeedStartFromInternal.createFromBeginning(),
                mode,
                changeFeedState);
        }

        return new CosmosChangeFeedRequestOptions(
            feedRange,
            changeFeedState.getStartFromSettings(),
            mode,
            changeFeedState);
    }

    /**
     * Creates a new {@link CosmosChangeFeedRequestOptions} instance to start processing
     * change feed items from the current time - so only events for all future changes will be
     * retrieved
     *
     * @param feedRange The {@link FeedRange} that is used to define the scope (entire container,
     *                  logical partition or subset of a container)
     * @return a new {@link CosmosChangeFeedRequestOptions} instance
     */
    public static CosmosChangeFeedRequestOptions createForProcessingFromNow(FeedRange feedRange) {
        if (feedRange == null) {
            throw new NullPointerException("feedRange");
        }

        return new CosmosChangeFeedRequestOptions(
            FeedRangeInternal.convert(feedRange),
            ChangeFeedStartFromInternal.createFromNow(),
            ChangeFeedMode.INCREMENTAL,
            null);
    }

    /**
     * Creates a new {@link CosmosChangeFeedRequestOptions} instance to start processing
     * change feed items from a certain point in time
     *
     * @param pointInTime The point in time from which processing of change feed events should start
     * @param feedRange   The {@link FeedRange} that is used to define the scope (entire container,
     *                    logical partition or subset of a container)
     * @return a new {@link CosmosChangeFeedRequestOptions} instance
     */
    public static CosmosChangeFeedRequestOptions createForProcessingFromPointInTime(
        Instant pointInTime,
        FeedRange feedRange) {

        if (pointInTime == null) {
            throw new NullPointerException("pointInTime");
        }

        if (feedRange == null) {
            throw new NullPointerException("feedRange");
        }

        return new CosmosChangeFeedRequestOptions(
            FeedRangeInternal.convert(feedRange),
            ChangeFeedStartFromInternal.createFromPointInTime(pointInTime),
            ChangeFeedMode.INCREMENTAL,
            null);
    }

    void setRequestContinuation(String etag) {
        this.actualRequestOptions.setRequestContinuation(etag);
    }

    CosmosChangeFeedRequestOptions withCosmosPagedFluxOptions(
        CosmosPagedFluxOptions pagedFluxOptions) {

        if (pagedFluxOptions == null) {
            return this;
        }

        CosmosChangeFeedRequestOptions effectiveRequestOptions = this;

        if (pagedFluxOptions.getRequestContinuation() != null) {
            effectiveRequestOptions =
                CosmosChangeFeedRequestOptions.createForProcessingFromContinuation(
                    pagedFluxOptions.getRequestContinuation());
            effectiveRequestOptions.setMaxPrefetchPageCount(this.getMaxPrefetchPageCount());
            effectiveRequestOptions.setThroughputControlGroupName(this.getThroughputControlGroupName());
        }

        if (pagedFluxOptions.getMaxItemCount() != null) {
            effectiveRequestOptions.setMaxItemCount(pagedFluxOptions.getMaxItemCount());
        }

        return effectiveRequestOptions;
    }

    /**
     * Changes the change feed mode so that the change feed will contain events for creations,
     * deletes as well as all intermediary snapshots for updates. Enabling AllVersionsAndDeletes
     * change feed mode requires configuring a retention duration in the change feed policy of the
     * container. {@link ChangeFeedPolicy}
     * <p>
     * Intermediary snapshots of changes as well as deleted documents would be
     * available for processing for retention window before they vanish.
     * When enabling AllVersionsAndDeletes mode you will only be able to process change feed events
     * within the retention window configured in the change feed policy of the container.
     * If you attempt to process a change feed after more than the retention window
     * an error (Status Code 400) will be returned because the events for intermediary
     * updates and deletes have vanished.
     * It would still be possible to process changes using LatestVersion mode even when
     * configuring a AllVersionsAndDeletes change feed policy with retention window on the container
     * and when using LatestVersion mode it doesn't matter whether your are out of the retention
     * window or not - but no events for deletes or intermediary updates would be included.
     * When events are not getting processed within the retention window it is also possible
     * to continue processing future events in AllVersionsAndDeletes mode by querying the change feed
     * with a new CosmosChangeFeedRequestOptions instance.
     * </p>
     *
     * @return a {@link CosmosChangeFeedRequestOptions} instance with AllVersionsAndDeletes mode enabled
     * @deprecated use {@link CosmosChangeFeedRequestOptions#allVersionsAndDeletes()} instead.
     */
    @Beta(value = Beta.SinceVersion.V4_12_0, warningText =
        Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    @Deprecated //since = "V4_37_0", forRemoval = true
    public CosmosChangeFeedRequestOptions fullFidelity() {
        this.actualRequestOptions.fullFidelity();
        return this;
    }

    /**
     * Changes the change feed mode so that the change feed will contain events for creations,
     * deletes as well as all intermediary snapshots for updates. Enabling AllVersionsAndDeletes
     * change feed mode requires configuring a retention duration in the change feed policy of the
     * container. {@link ChangeFeedPolicy}
     * <p>
     * Intermediary snapshots of changes as well as deleted documents would be
     * available for processing for 8 minutes before they vanish.
     * When enabling AllVersionsAndDeletes mode you will only be able to process change feed events
     * within the retention window configured in the change feed policy of the container.
     * If you attempt to process a change feed after more than the retention window
     * an error (Status Code 400) will be returned because the events for intermediary
     * updates and deletes have vanished.
     * It would still be possible to process changes using LatestVersion mode even when
     * configuring a AllVersionsAndDeletes change feed policy with retention window on the container
     * and when using LatestVersion mode it doesn't matter whether your are out of the retention
     * window or not - but no events for deletes or intermediary updates would be included.
     * When events are not getting processed within the retention window it is also possible
     * to continue processing future events in AllVersionsAndDeletes mode by querying the change feed
     * with a new CosmosChangeFeedRequestOptions instance.
     * </p>
     *
     * @return a {@link CosmosChangeFeedRequestOptions} instance with AllVersionsAndDeletes mode enabled
     */
    @Beta(value = Beta.SinceVersion.V4_37_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    public CosmosChangeFeedRequestOptions allVersionsAndDeletes() {
        this.actualRequestOptions.allVersionsAndDeletes();
        return this;
    }

    /**
     * Get the throughput control group name.
     *
     * @return The throughput control group name.
     */
    public String getThroughputControlGroupName() {
        return this.actualRequestOptions.getThroughputControlGroupName();
    }

    /**
     * Set the throughput control group name.
     *
     * @param throughputControlGroupName The throughput control group name.
     * @return A {@link CosmosChangeFeedRequestOptions}.
     */
    public CosmosChangeFeedRequestOptions setThroughputControlGroupName(String throughputControlGroupName) {
        this.actualRequestOptions.setThroughputControlGroupName(throughputControlGroupName);
        return this;
    }

    /**
     * List of regions to be excluded for the request/retries. Example "East US" or "East US, West US"
     * These regions will be excluded from the preferred regions list. If all the regions are excluded,
     * the request will be sent to the primary region for the account. The primary region is the write region in a
     * single master account and the hub region in a multi-master account.
     *
     * @param excludeRegions list of regions
     * @return the {@link CosmosChangeFeedRequestOptions}
     */
    public CosmosChangeFeedRequestOptions setExcludedRegions(List<String> excludeRegions) {
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

    /**
     * Sets the custom change feed request option value by key
     *
     * @param name  a string representing the custom option's name
     * @param value a string representing the custom option's value
     *
     * @return the CosmosChangeFeedRequestOptions.
     */
    CosmosChangeFeedRequestOptions setHeader(String name, String value) {
        this.actualRequestOptions.setHeader(name, value);
        return this;
    }

    /**
     * Gets the custom change feed request options
     *
     * @return Map of custom request options
     */
    Map<String, String> getHeaders() {
        return this.actualRequestOptions.getHeaders();
    }

    /**
     * Sets the custom ids.
     *
     * @param keywordIdentifiers the custom ids.
     * @return the current request options.
     */
    public CosmosChangeFeedRequestOptions setKeywordIdentifiers(Set<String> keywordIdentifiers) {
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
     * @return the custom ids.
     */
    public Set<String> getKeywordIdentifiers() {
        return this.actualRequestOptions.getKeywordIdentifiers();
    }

    /**
     * Sets the response interceptor to be called after receiving the response
     * for the request.
     *
     * @param responseInterceptor the response interceptor.
     */
    public void setResponseInterceptor(Callable<Void> responseInterceptor) {
        this.actualRequestOptions.setResponseInterceptor(responseInterceptor);
    }

    /**
     * Gets the response interceptor to be called after receiving the response
     * for the request.
     *
     * @return the response interceptor.
     */
    public Callable<Void> getResponseInterceptor() {
        return this.actualRequestOptions.getResponseInterceptor();
    }

    void setOperationContextAndListenerTuple(OperationContextAndListenerTuple operationContextAndListenerTuple) {
        this.actualRequestOptions.setOperationContextAndListenerTuple(operationContextAndListenerTuple);
    }

    OperationContextAndListenerTuple getOperationContextAndListenerTuple() {
        return this.actualRequestOptions.getOperationContextAndListenerTuple();
    }

    private void addCustomOptionsForFullFidelityMode() {
        this.setHeader(
            HttpConstants.HttpHeaders.CHANGE_FEED_WIRE_FORMAT_VERSION,
            HttpConstants.ChangeFeedWireFormatVersions.SEPARATE_METADATA_WITH_CRTS);
    }

    CosmosChangeFeedRequestOptionsImpl getImpl() {
        return this.actualRequestOptions;
    }

    String getCollectionRid() {
        return this.actualRequestOptions.getCollectionRid();
    }

    void setCollectionRid(String collectionRid) {
        this.actualRequestOptions.setCollectionRid(collectionRid);
    }

    PartitionKeyDefinition getPartitionKeyDefinition() {
        return this.actualRequestOptions.getPartitionKeyDefinition();
    }

    void setPartitionKeyDefinition(PartitionKeyDefinition partitionKeyDefinition) {
        this.actualRequestOptions.setPartitionKeyDefinition(partitionKeyDefinition);
    }

    ///////////////////////////////////////////////////////////////////////////////////////////
    // the following helper/accessor only helps to access this class outside of this package.//
    ///////////////////////////////////////////////////////////////////////////////////////////
    static void initialize() {
        ImplementationBridgeHelpers.CosmosChangeFeedRequestOptionsHelper.setCosmosChangeFeedRequestOptionsAccessor(
            new ImplementationBridgeHelpers.CosmosChangeFeedRequestOptionsHelper.CosmosChangeFeedRequestOptionsAccessor() {

                @Override
                public CosmosChangeFeedRequestOptions setHeader(CosmosChangeFeedRequestOptions changeFeedRequestOptions, String name, String value) {
                    return changeFeedRequestOptions.setHeader(name, value);
                }

                @Override
                public Map<String, String> getHeaders(CosmosChangeFeedRequestOptions changeFeedRequestOptions) {
                    return changeFeedRequestOptions.getHeaders();
                }

                @Override
                public CosmosChangeFeedRequestOptionsImpl getImpl(CosmosChangeFeedRequestOptions changeFeedRequestOptions) {
                    return changeFeedRequestOptions.getImpl();
                }

                @Override
                public CosmosChangeFeedRequestOptions setEndLSN(CosmosChangeFeedRequestOptions changeFeedRequestOptions, Long endLsn) {
                    return changeFeedRequestOptions.setEndLSN(endLsn);
                }

                @Override
                public Long getEndLSN(CosmosChangeFeedRequestOptions changeFeedRequestOptions) {
                    return changeFeedRequestOptions.getEndLSN();
                }

                @Override
                public void setOperationContext
                    (
                        CosmosChangeFeedRequestOptions changeFeedRequestOptions,
                        OperationContextAndListenerTuple operationContextAndListenerTuple
                    ) {

                    changeFeedRequestOptions.setOperationContextAndListenerTuple(operationContextAndListenerTuple);
                }

                @Override
                public OperationContextAndListenerTuple getOperationContext
                    (
                        CosmosChangeFeedRequestOptions changeFeedRequestOptions
                    ) {

                    return changeFeedRequestOptions.getOperationContextAndListenerTuple();
                }

                @Override
                public CosmosDiagnosticsThresholds getDiagnosticsThresholds(CosmosChangeFeedRequestOptions options) {
                    return options.getDiagnosticsThresholds();
                }

                @Override
                public CosmosChangeFeedRequestOptions createForProcessingFromContinuation(
                    String continuation,
                    FeedRange targetRange,
                    String continuationLsn) {

                    return CosmosChangeFeedRequestOptions.createForProcessingFromContinuation(continuation, targetRange, continuationLsn);
                }

                @Override
                public CosmosChangeFeedRequestOptions clone(CosmosChangeFeedRequestOptions toBeCloned) {
                    return new CosmosChangeFeedRequestOptions(toBeCloned);
                }

                @Override
                public String getCollectionRid(CosmosChangeFeedRequestOptions changeFeedRequestOptions) {
                    return changeFeedRequestOptions.getCollectionRid();
                }

                @Override
                public void setCollectionRid(CosmosChangeFeedRequestOptions changeFeedRequestOptions, String collectionRid) {
                    changeFeedRequestOptions.setCollectionRid(collectionRid);
                }

                @Override
                public PartitionKeyDefinition getPartitionKeyDefinition(CosmosChangeFeedRequestOptions changeFeedRequestOptions) {
                    return changeFeedRequestOptions.getPartitionKeyDefinition();
                }

                @Override
                public void setPartitionKeyDefinition(CosmosChangeFeedRequestOptions changeFeedRequestOptions, PartitionKeyDefinition partitionKeyDefinition) {
                    changeFeedRequestOptions.setPartitionKeyDefinition(partitionKeyDefinition);
                }

                @Override
                public Map<String, Object> getProperties(CosmosChangeFeedRequestOptions changeFeedRequestOptions) {
                    if (changeFeedRequestOptions == null) {
                        return null;
                    }

                    return changeFeedRequestOptions.getImpl().getProperties();
                }
            });
    }

    static { initialize(); }
}
