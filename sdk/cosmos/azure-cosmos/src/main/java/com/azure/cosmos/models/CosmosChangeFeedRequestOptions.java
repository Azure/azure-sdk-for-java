// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.models;

import com.azure.cosmos.implementation.RxDocumentServiceRequest;
import com.azure.cosmos.implementation.Strings;
import com.azure.cosmos.implementation.changefeed.implementation.ChangeFeedRequestOptionsImpl;
import com.azure.cosmos.implementation.changefeed.implementation.ChangeFeedStartFromInternal;
import com.azure.cosmos.implementation.feedranges.FeedRangeContinuation;
import com.azure.cosmos.implementation.feedranges.FeedRangeInternal;

import java.time.Instant;
import java.util.Map;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

public final class CosmosChangeFeedRequestOptions {
    private static final Integer DEFAULT_MAX_ITEM_COUNT = 1000;
    private final FeedRangeInternal feedRangeInternal;
    private Integer maxItemCount;
    private ChangeFeedStartFromInternal startFromInternal;
    private Map<String, Object> properties;

    private CosmosChangeFeedRequestOptions(
        FeedRangeInternal feedRange,
        ChangeFeedStartFromInternal startFromInternal) {

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
    }

    public FeedRange getFeedRange() {
        return this.feedRangeInternal;
    }

    /**
     * Gets the maximum number of items to be returned in the enumeration
     * operation.
     *
     * @return the max number of items.
     */
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
    public CosmosChangeFeedRequestOptions setMaxItemCount(Integer maxItemCount) {
        this.maxItemCount = maxItemCount;
        return this;
    }

    public CosmosChangeFeedRequestOptions setRequestContinuation(String continuation) {

        checkNotNull(continuation, "expected non-null continuation");

        final FeedRangeContinuation feedRangeContinuation =
            toFeedRangeContinuation(continuation);

        final FeedRangeInternal feedRangeFromContinuation = feedRangeContinuation.getFeedRange();
        if (!feedRangeFromContinuation.toJsonString().equals(feedRangeInternal.toJsonString())) {

            final String message = String.format(
                "The feed range used to construct the request options '%s' isn't the same " +
                    "as the feed range '%s' of this continuation '%s'.",
                this.feedRangeInternal.toJsonString(),
                feedRangeFromContinuation.toJsonString(),
                continuation);

            throw new IllegalArgumentException(message);
        }

        String continuationToken = feedRangeContinuation.getContinuation();
        if (Strings.isNullOrWhiteSpace(continuationToken)) {
            final String message = String.format(
                "No continuation available in the provided feed range continuation '%s'.",
                continuation);

            throw new IllegalArgumentException(message);
        }

        this.startFromInternal = ChangeFeedStartFromInternal.createFromContinuation(continuationToken);

        return this;
    }

    public static CosmosChangeFeedRequestOptions createForProcessingFromBeginning(FeedRange feedRange) {
        if (feedRange == null) {
            throw new NullPointerException("feedRange");
        }

        return new CosmosChangeFeedRequestOptions(
            FeedRangeInternal.convert(feedRange),
            ChangeFeedStartFromInternal.createFromBeginning());
    }

    public static CosmosChangeFeedRequestOptions createForProcessingFromContinuation(
        String continuation) {

        final FeedRangeContinuation feedRangeContinuation =
            toFeedRangeContinuation(continuation);

        final FeedRangeInternal feedRange = feedRangeContinuation.getFeedRange();
        final String continuationToken = feedRangeContinuation.getContinuation();
        if (continuationToken != null) {
            return new CosmosChangeFeedRequestOptions(
                feedRange,
                ChangeFeedStartFromInternal.createFromContinuation(continuationToken));
        }

        return new CosmosChangeFeedRequestOptions(
            feedRange,
            ChangeFeedStartFromInternal.createFromBeginning());
    }

    public static CosmosChangeFeedRequestOptions createForProcessingFromNow(FeedRange feedRange) {
        if (feedRange == null) {
            throw new NullPointerException("feedRange");
        }

        return new CosmosChangeFeedRequestOptions(
            FeedRangeInternal.convert(feedRange),
            ChangeFeedStartFromInternal.createFromNow());
    }

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
            ChangeFeedStartFromInternal.createFromPointInTime(pointInTime));
    }

    void populateRequestOptions(RxDocumentServiceRequest request, String continuation) {
        ChangeFeedRequestOptionsImpl.populateRequestOptions(
            this,
            request,
            this.startFromInternal,
            this.feedRangeInternal,
            continuation
        );
    }

    /**
     * Gets the properties
     *
     * @return Map of request options properties
     */
    public Map<String, Object> getProperties() {
        return properties;
    }

    /**
     * Sets the properties used to identify the request token.
     *
     * @param properties the properties.
     * @return the FeedOptionsBase.
     */
    public CosmosChangeFeedRequestOptions setProperties(Map<String, Object> properties) {
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