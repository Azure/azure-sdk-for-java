// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.models;

import com.azure.cosmos.implementation.changefeed.implementation.ChangeFeedMode;
import com.azure.cosmos.implementation.changefeed.implementation.ChangeFeedStartFromInternal;
import com.azure.cosmos.implementation.changefeed.implementation.ChangeFeedState;
import com.azure.cosmos.implementation.feedranges.FeedRangeContinuation;
import com.azure.cosmos.implementation.feedranges.FeedRangeInternal;
import com.azure.cosmos.implementation.query.CompositeContinuationToken;
import com.azure.cosmos.util.Beta;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

@Beta(Beta.SinceVersion.WHATEVER_NEW_VERSION)
public final class CosmosChangeFeedRequestOptions {
    private static final Integer DEFAULT_MAX_ITEM_COUNT = 1000;
    private final ChangeFeedState continuationState;
    private final FeedRangeInternal feedRangeInternal;
    private final Map<String, Object> properties;
    private Integer maxItemCount;
    private ChangeFeedMode mode;
    private ChangeFeedStartFromInternal startFromInternal;

    private CosmosChangeFeedRequestOptions(
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
        this.feedRangeInternal = feedRange;
        this.startFromInternal = startFromInternal;
        this.continuationState = continuationState;

        if (mode != ChangeFeedMode.INCREMENTAL && mode != ChangeFeedMode.FULL_FIDELITY) {
            throw new IllegalArgumentException(
                String.format(
                    "Argument 'mode' has unsupported change feed mode %s",
                    mode.toString()));
        }

        this.mode = mode;
        this.properties = new HashMap<>();
    }

    ChangeFeedState getContinuation() {
        return this.continuationState;
    }

    @Beta(Beta.SinceVersion.WHATEVER_NEW_VERSION)
    public FeedRange getFeedRange() {
        return this.feedRangeInternal;
    }

    /**
     * Gets the maximum number of items to be returned in the enumeration
     * operation.
     *
     * @return the max number of items.
     */
    @Beta(Beta.SinceVersion.WHATEVER_NEW_VERSION)
    public Integer getMaxItemCount() {
        return this.maxItemCount;
    }

    /**
     * Sets the maximum number of items to be returned in the enumeration
     * operation.
     *
     * @param maxItemCount the max number of items.
     * @return the FeedOptionsBase.
     */
    @Beta(Beta.SinceVersion.WHATEVER_NEW_VERSION)
    public CosmosChangeFeedRequestOptions setMaxItemCount(Integer maxItemCount) {
        this.maxItemCount = maxItemCount;
        return this;
    }

    ChangeFeedMode getMode() {
        return this.mode;
    }

    /**
     * Gets the properties
     *
     * @return Map of request options properties
     */
    Map<String, Object> getProperties() {
        return properties;
    }

    ChangeFeedStartFromInternal getStartFromSettings() {
        return this.startFromInternal;
    }

    @Beta(Beta.SinceVersion.WHATEVER_NEW_VERSION)
    public static CosmosChangeFeedRequestOptions createForProcessingFromBeginning(FeedRange feedRange) {
        checkNotNull(feedRange, "Argument 'feedRange' must not be null.");

        return new CosmosChangeFeedRequestOptions(
            FeedRangeInternal.convert(feedRange),
            ChangeFeedStartFromInternal.createFromBeginning(),
            ChangeFeedMode.INCREMENTAL,
            null);
    }

    @Beta(Beta.SinceVersion.WHATEVER_NEW_VERSION)
    public static CosmosChangeFeedRequestOptions createForProcessingFromContinuation(
        String continuation) {

        final ChangeFeedState changeFeedState = ChangeFeedState.fromJson(continuation);

        return createForProcessingFromContinuation(changeFeedState);
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

    static CosmosChangeFeedRequestOptions createForProcessingFromEtagAndFeedRange(
        String etag,
        FeedRange feedRange) {

        if (etag != null) {
            return new CosmosChangeFeedRequestOptions(
                FeedRangeInternal.convert(feedRange),
                ChangeFeedStartFromInternal.createFromETagAndFeedRange(etag,
                    FeedRangeInternal.convert(feedRange)),
                ChangeFeedMode.INCREMENTAL,
                null);
        }

        return new CosmosChangeFeedRequestOptions(
            FeedRangeInternal.convert(feedRange),
            ChangeFeedStartFromInternal.createFromBeginning(),
            ChangeFeedMode.INCREMENTAL,
            null);
    }

    @Beta(Beta.SinceVersion.WHATEVER_NEW_VERSION)
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

    @Beta(Beta.SinceVersion.WHATEVER_NEW_VERSION)
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
        this.startFromInternal = ChangeFeedStartFromInternal.createFromETagAndFeedRange(
            etag,
            this.feedRangeInternal);
    }

    @Beta(Beta.SinceVersion.WHATEVER_NEW_VERSION)
    public CosmosChangeFeedRequestOptions withFullFidelity() {

        if (!this.startFromInternal.supportsFullFidelityRetention()) {
            throw new IllegalStateException(
                "Full fidelity retention is not supported for the chosen change feed start from " +
                    "option. Use CosmosChangeFeedRequestOptions.createForProcessingFromNow or " +
                    "CosmosChangeFeedRequestOptions.createFromContinuation instead."
            );
        }

        this.mode = ChangeFeedMode.FULL_FIDELITY;
        return this;
    }
}