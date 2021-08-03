// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.feedranges;

import com.azure.cosmos.implementation.GoneException;
import com.azure.cosmos.implementation.JsonSerializable;
import com.azure.cosmos.implementation.Resource;
import com.azure.cosmos.implementation.RxDocumentClientImpl;
import com.azure.cosmos.implementation.ShouldRetryResult;
import com.azure.cosmos.implementation.Strings;
import com.azure.cosmos.implementation.query.CompositeContinuationToken;
import com.azure.cosmos.implementation.routing.PartitionKeyInternalHelper;
import com.azure.cosmos.implementation.routing.Range;
import com.azure.cosmos.models.FeedResponse;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

@JsonDeserialize(using = FeedRangeContinuationDeserializer.class)
public abstract class FeedRangeContinuation extends JsonSerializable {
    private final static Logger LOGGER = LoggerFactory.getLogger(FeedRangeContinuation.class);

    protected final  FeedRangeInternal feedRange;
    private final String containerRid;

    // for mocking
    protected FeedRangeContinuation() {
        this.feedRange = null;
        this.containerRid = null;
    }

    public FeedRangeContinuation(String containerRid, FeedRangeInternal feedRange) {
        checkNotNull(feedRange, "expected non-null feedRange");
        this.feedRange = feedRange;
        this.containerRid = containerRid;
    }

    public String getContainerRid() {
        return this.containerRid;
    }

    public FeedRangeInternal getFeedRange() {
        return this.feedRange;
    }

    public abstract CompositeContinuationToken getCurrentContinuationToken();

    public abstract int getContinuationTokenCount();

    public abstract void replaceContinuation(String continuationToken);

    public abstract boolean isDone();

    public abstract void validateContainer(String containerRid);

    public abstract CompositeContinuationToken[] getCurrentContinuationTokens();

    public void populatePropertyBag() {
        super.populatePropertyBag();
    }

    private static FeedRangeContinuation tryParse(String jsonString) {
        if (jsonString == null) {
            return null;
        }

        try
        {
            return FeedRangeCompositeContinuationImpl.parse(jsonString);
        }
        catch (final IOException ioError) {
            LOGGER.debug(
                "Failed to parse feed range continuation JSON {}",
                jsonString,
                ioError);
            return null;
        }
    }

    public static FeedRangeContinuation convert(final String continuationToken) {
        if (Strings.isNullOrWhiteSpace(continuationToken)) {
            throw new NullPointerException("continuationToken");
        }

        FeedRangeContinuation returnValue = tryParse(continuationToken);
        if (returnValue != null) {
            return returnValue;
        }

        throw new IllegalArgumentException(
            String.format(
                "Invalid Feed range continuation token '%s'",
                continuationToken));
    }

    public static FeedRangeContinuation create(
        String containerRid,
        FeedRangeInternal feedRange,
        Range<String> effectiveRange) {

        checkNotNull(containerRid, "Argument 'collectionLink' must not be null");
        checkNotNull(feedRange, "Argument 'feedRange' must not be null");
        checkNotNull(effectiveRange, "Argument 'effectiveRange' must not be null");

        List<Range<String>> ranges = new ArrayList<>();
        ranges.add(
            effectiveRange);

        return new FeedRangeCompositeContinuationImpl(
            containerRid,
            feedRange,
            ranges,
            null);
    }

    public static FeedRangeContinuation create(
        String containerRid,
        FeedRangeInternal feedRange,
        List<CompositeContinuationToken> continuationTokens) {

        return new FeedRangeCompositeContinuationImpl(
            containerRid,
            feedRange,
            continuationTokens);
    }

    public abstract <T extends Resource> ShouldRetryResult handleChangeFeedNotModified(
        FeedResponse<T> responseMessage);

    public abstract Mono<ShouldRetryResult> handleSplit(
        RxDocumentClientImpl client,
        GoneException goneException);
}
