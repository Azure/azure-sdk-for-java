// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.models;

import com.azure.cosmos.implementation.Strings;
import com.azure.cosmos.implementation.changefeed.implementation.ChangeFeedMode;
import com.azure.cosmos.implementation.changefeed.implementation.ChangeFeedStartFromInternal;
import com.azure.cosmos.implementation.changefeed.implementation.ChangeFeedState;
import com.azure.cosmos.implementation.feedranges.FeedRangeContinuation;
import com.azure.cosmos.implementation.feedranges.FeedRangeInternal;
import com.azure.cosmos.implementation.query.CompositeContinuationToken;
import com.azure.cosmos.util.Beta;

import java.time.Instant;
import java.util.Map;

@Beta(Beta.SinceVersion.WHATEVER_NEW_VERSION)
public final class CosmosChangeFeedRequestOptions {
    private static final Integer DEFAULT_MAX_ITEM_COUNT = 1000;
    private final FeedRangeInternal feedRangeInternal;
    private Integer maxItemCount;
    private ChangeFeedStartFromInternal startFromInternal;
    private Map<String, Object> properties;
    private ChangeFeedMode mode;
    private final ChangeFeedState continuationState;

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

        if (mode != ChangeFeedMode.INCREMENTAL) {
            throw new IllegalArgumentException(
                String.format(
                    "Argument 'mode' has unsupported change feed mode %s",
                    mode.toString()));
        }

        this.mode = mode;
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

    ChangeFeedStartFromInternal getStartFromSettings() {
        return this.startFromInternal;
    }

    // TODO fabianm remove or at least make internal
    public void setRequestContinuation(String etag) {
        this.startFromInternal = ChangeFeedStartFromInternal.createFromEtagAndFeedRange(
            etag,
            this.feedRangeInternal);
    }

    @Beta(Beta.SinceVersion.WHATEVER_NEW_VERSION)
    public CosmosChangeFeedRequestOptions withFullFidelity() {
        this.mode = ChangeFeedMode.FULL_FIDELITY;
        return this;
    }

    @Beta(Beta.SinceVersion.WHATEVER_NEW_VERSION)
    public static CosmosChangeFeedRequestOptions createForProcessingFromBeginning(FeedRange feedRange) {
        if (feedRange == null) {
            throw new NullPointerException("feedRange");
        }

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
        ChangeFeedState changeFeedState ) {

        FeedRangeInternal feedRange = changeFeedState.getFeedRange();
        FeedRangeContinuation continuation = changeFeedState.getContinuation();
        ChangeFeedMode mode = changeFeedState.getMode();

        if (continuation != null) {
            CompositeContinuationToken continuationToken = continuation.getCurrentContinuationToken();
            if (continuationToken != null) {
                String etag = continuationToken.getToken();
                return new CosmosChangeFeedRequestOptions(
                    feedRange,
                    ChangeFeedStartFromInternal.createFromEtagAndFeedRange(etag, feedRange),
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
                ChangeFeedStartFromInternal.createFromEtagAndFeedRange(etag, FeedRangeInternal.convert(feedRange)),
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

    /**
     * Gets the properties
     *
     * @return Map of request options properties
     */
    Map<String, Object> getProperties() {
        return properties;
    }

    /**
     * Sets the properties used to identify the request token.
     *
     * @param properties the properties.
     * @return the FeedOptionsBase.
     */
    CosmosChangeFeedRequestOptions setProperties(Map<String, Object> properties) {
        this.properties = properties;
        return this;
    }

    private static FeedRangeContinuation toFeedRangeContinuation(String continuation) {
        final FeedRangeContinuation feedRangeContinuation =
            FeedRangeContinuation.tryParse(continuation);

        if (Strings.isNullOrWhiteSpace(continuation)) {
            throw new NullPointerException("continuation");
        }

        if (feedRangeContinuation == null) {
            final String message = String.format(
                "The provided string '%s' does not represent any known format.",
                continuation);
            throw new IllegalArgumentException(message);
        }

        return feedRangeContinuation;
    }
}