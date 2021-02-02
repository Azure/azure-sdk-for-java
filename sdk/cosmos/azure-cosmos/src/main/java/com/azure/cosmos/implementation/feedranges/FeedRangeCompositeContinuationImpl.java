// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.feedranges;

import com.azure.cosmos.implementation.Constants;
import com.azure.cosmos.implementation.GoneException;
import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.PartitionKeyRange;
import com.azure.cosmos.implementation.Resource;
import com.azure.cosmos.implementation.RxDocumentClientImpl;
import com.azure.cosmos.implementation.ShouldRetryResult;
import com.azure.cosmos.implementation.Strings;
import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.implementation.caches.RxPartitionKeyRangeCache;
import com.azure.cosmos.implementation.query.CompositeContinuationToken;
import com.azure.cosmos.implementation.routing.Range;
import com.azure.cosmos.models.FeedResponse;
import com.azure.cosmos.models.ModelBridgeInternal;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Queue;

import static com.azure.cosmos.BridgeInternal.setProperty;
import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

/**
 * FeedRangeContinuation using Composite Continuation Tokens and split proof.
 * It uses a breath-first approach to transverse Composite Continuation Tokens.
 */
final class FeedRangeCompositeContinuationImpl extends FeedRangeContinuation {

    private final static Logger LOGGER = LoggerFactory.getLogger(FeedRangeCompositeContinuationImpl.class);
    private final Queue<CompositeContinuationToken> compositeContinuationTokens;
    private CompositeContinuationToken currentToken;
    private String initialNoResultsRange;

    public FeedRangeCompositeContinuationImpl(
        String containerRid,
        FeedRangeInternal feedRange,
        List<Range<String>> ranges) {

        this(containerRid, feedRange, ranges, null);
    }

    public FeedRangeCompositeContinuationImpl(
        String containerRid,
        FeedRangeInternal feedRange,
        List<Range<String>> ranges,
        String continuation) {

        this(containerRid, feedRange);

        checkNotNull(ranges, "'ranges' must not be null");

        if (ranges.size() == 0) {
            throw new IllegalArgumentException("'ranges' must not be empty");
        }

        for (Range<String> range : ranges) {
            this.compositeContinuationTokens.add(
                FeedRangeCompositeContinuationImpl.createCompositeContinuationTokenForRange(
                    range.getMin(),
                    range.getMax(),
                    continuation)
            );
        }

        this.currentToken = this.getCompositeContinuationTokens().peek();
    }

    public void populatePropertyBag() {
        super.populatePropertyBag();

        setProperty(
            this,
            Constants.Properties.FEED_RANGE_COMPOSITE_CONTINUATION_VERSION,
            FeedRangeContinuationVersions.V1);

        setProperty(
            this,
            Constants.Properties.FEED_RANGE_COMPOSITE_CONTINUATION_RESOURCE_ID,
            this.getContainerRid());

        if (this.compositeContinuationTokens.size() > 0) {
            for (CompositeContinuationToken token : this.compositeContinuationTokens) {
                ModelBridgeInternal.populatePropertyBag(token);
            }

            setProperty(
                this,
                Constants.Properties.FEED_RANGE_COMPOSITE_CONTINUATION_CONTINUATION,
                this.compositeContinuationTokens);
        }

        if (this.feedRange != null) {
            this.feedRange.setProperties(this, true);
        }
    }

    private FeedRangeCompositeContinuationImpl(String containerRid, FeedRangeInternal feedRange) {
        super(containerRid, feedRange);

        this.compositeContinuationTokens = new LinkedList<>();
    }

    public Queue<CompositeContinuationToken> getCompositeContinuationTokens() {
        return compositeContinuationTokens;
    }

    public CompositeContinuationToken getCurrentToken() {
        return this.currentToken;
    }

    @Override
    public FeedRangeInternal getFeedRange() {
        if (!(this.feedRange instanceof FeedRangeEpkImpl)) {
            return this.feedRange;
        }

        if (this.currentToken != null) {
            return new FeedRangeEpkImpl(this.currentToken.getRange());
        }

        return null;
    }

    @Override
    public CompositeContinuationToken getCurrentContinuationToken() {
        CompositeContinuationToken tokenSnapshot = this.currentToken;
        if (tokenSnapshot == null) {
            return null;
        }

        return tokenSnapshot;
    }

    @Override
    public int getContinuationTokenCount() {
        return this.compositeContinuationTokens.size();
    }

    @Override
    public void replaceContinuation(final String continuationToken) {
        final CompositeContinuationToken continuationTokenSnapshot = this.currentToken;

        if (continuationTokenSnapshot == null) {
            return;
        }

        continuationTokenSnapshot.setToken(continuationToken);
        this.moveToNextToken();
    }

    @Override
    public boolean isDone() {
        return this.compositeContinuationTokens.size() == 0;
    }

    @Override
    public void validateContainer(final String containerRid) throws IllegalArgumentException {
        if (Strings.isNullOrEmpty(containerRid) || !containerRid.equals(this.getContainerRid())) {

            final String message = String.format(
                "The continuation was generated for container %s but current container is %s.",
                this.getContainerRid(), containerRid);
            throw new IllegalArgumentException(message);
        }
    }

    @Override
    public <T extends Resource> ShouldRetryResult handleChangeFeedNotModified(final FeedResponse<T> response) {
        checkNotNull(response, "Argument 'response' must not be null");

        if (!ModelBridgeInternal.<T>noChanges(response)) {
            this.initialNoResultsRange = null;
        } else if (this.compositeContinuationTokens.size() > 1) {
            final String eTag = this.currentToken.getToken();
            if (this.initialNoResultsRange == null) {

                this.initialNoResultsRange = this.currentToken.getRange().getMin();
                this.replaceContinuation(eTag);
                this.moveToNextToken();
                return ShouldRetryResult.RETRY_NOW;
            }

            if (!this.initialNoResultsRange.equalsIgnoreCase(this.currentToken.getRange().getMin())) {
                this.replaceContinuation(eTag);
                this.moveToNextToken();
                return ShouldRetryResult.RETRY_NOW;
            }
        }

        return ShouldRetryResult.NO_RETRY;
    }

    @Override
    public Mono<ShouldRetryResult> handleSplit(final RxDocumentClientImpl client,
                                               final GoneException goneException) {

        checkNotNull(client, "Argument 'client' must not be null");
        checkNotNull(goneException, "Argument 'goeException' must not be null");

        Integer nSubStatus = goneException.getSubStatusCode();

        final boolean partitionSplit =
            goneException.getStatusCode() == HttpConstants.StatusCodes.GONE &&
                nSubStatus != null &&
                (nSubStatus == HttpConstants.SubStatusCodes.PARTITION_KEY_RANGE_GONE
                || nSubStatus == HttpConstants.SubStatusCodes.COMPLETING_SPLIT);

        if (!partitionSplit) {
            return Mono.just(ShouldRetryResult.NO_RETRY);
        }

        final RxPartitionKeyRangeCache partitionKeyRangeCache = client.getPartitionKeyRangeCache();
        final Mono<Utils.ValueHolder<List<PartitionKeyRange>>> resolvedRangesTask =
            this.tryGetOverlappingRanges(
                partitionKeyRangeCache,
                this.currentToken.getRange().getMin(),
                this.currentToken.getRange().getMax(),
                true);

        return resolvedRangesTask.flatMap(resolvedRanges -> {
            if (resolvedRanges.v != null && resolvedRanges.v.size() > 0) {
                this.createChildRanges(resolvedRanges.v);
            }

            return Mono.just(ShouldRetryResult.RETRY_NOW);
        });
    }

    /**
     * Used for deserializtion only
     */
    public static FeedRangeCompositeContinuationImpl createFromDeserializedTokens(
        String containerRid,
        FeedRangeInternal feedRange,
        List<CompositeContinuationToken> deserializedTokens) {

        FeedRangeCompositeContinuationImpl thisPtr =
            new FeedRangeCompositeContinuationImpl(containerRid, feedRange);

        checkNotNull(deserializedTokens, "'deserializedTokens' must not be null");

        if (deserializedTokens.size() == 0) {
            throw new IllegalArgumentException("'deserializedTokens' must not be empty");
        }

        thisPtr.compositeContinuationTokens.addAll(deserializedTokens);

        thisPtr.currentToken = thisPtr.getCompositeContinuationTokens().peek();

        return thisPtr;
    }

    public static FeedRangeContinuation parse(final String jsonString) throws IOException {
        checkNotNull(jsonString, "Argument 'jsonString' must not be null");
        final ObjectMapper mapper = Utils.getSimpleObjectMapper();
        return mapper.readValue(jsonString, FeedRangeContinuation.class);
    }

    @Override
    public String toString() {
        try {
            return Utils.getSimpleObjectMapper().writeValueAsString(this);
        } catch (final IOException e) {
            throw new IllegalArgumentException(
                "Unable serialize the composite FeedRange continuation token into a JSON string",
                e);
        }
    }

    private void createChildRanges(final List<PartitionKeyRange> keyRanges) {
        final PartitionKeyRange firstRange = keyRanges.get(0);
        this.currentToken
            .setRange(new Range<>(firstRange.getMinInclusive(),
                firstRange.getMaxExclusive(), true, false));

        final CompositeContinuationToken continuationAsComposite =
            tryParseAsCompositeContinuationToken(
                this.currentToken.getToken());

        if (continuationAsComposite != null) {
            // Update the internal composite continuation
            continuationAsComposite.setRange(this.currentToken.getRange());
            this.currentToken.setToken(continuationAsComposite.toJson());
            // Add children
            final int size = keyRanges.size();
            for (int i = 1; i < size; i++) {
                final PartitionKeyRange keyRange = keyRanges.get(i);
                continuationAsComposite.setRange(keyRange.toRange());
                this.compositeContinuationTokens.add(createCompositeContinuationTokenForRange(
                    keyRange.getMinInclusive(), keyRange.getMaxExclusive(),
                    continuationAsComposite.toJson()));
            }
        } else {
            // Add children
            final int size = keyRanges.size();
            for (int i = 1; i < size; i++) {
                final PartitionKeyRange keyRange = keyRanges.get(i);
                this.compositeContinuationTokens.add(createCompositeContinuationTokenForRange(
                    keyRange.getMinInclusive(), keyRange.getMaxExclusive(),
                    this.currentToken.getToken()));
            }
        }
    }

    private static CompositeContinuationToken createCompositeContinuationTokenForRange(
        String minInclusive,
        String maxExclusive,
        String token) {
        return new CompositeContinuationToken(
            token,
            new Range<>(minInclusive, maxExclusive, true, false));
    }

    private void moveToNextToken() {
        final CompositeContinuationToken recentToken = this.compositeContinuationTokens.poll();
        this.compositeContinuationTokens.add(recentToken);

        if (this.compositeContinuationTokens.size() > 0) {
            this.currentToken = this.compositeContinuationTokens.peek();
        } else {
            this.currentToken = null;
        }
    }

    private Mono<Utils.ValueHolder<List<PartitionKeyRange>>> tryGetOverlappingRanges(
        final RxPartitionKeyRangeCache partitionKeyRangeCache, final String min, final String max,
        final Boolean forceRefresh) {

        return partitionKeyRangeCache.tryGetOverlappingRangesAsync(null, this.getContainerRid(),
            new Range<>(min, max, false, true), forceRefresh, null);
    }

    private static CompositeContinuationToken tryParseAsCompositeContinuationToken(
        final String providedContinuation) {

        try {
            final ObjectMapper mapper = Utils.getSimpleObjectMapper();

            if (providedContinuation == null) {
                return null;
            }

            if (providedContinuation.trim().startsWith("[")) {
                final List<CompositeContinuationToken> compositeContinuationTokens = Arrays
                    .asList(mapper.readValue(providedContinuation,
                        CompositeContinuationToken[].class));

                if (compositeContinuationTokens.size() > 0) {
                    return compositeContinuationTokens.get(0);
                }

                return null;
            } else if (providedContinuation.trim().startsWith("{")) {
                return mapper.readValue(providedContinuation, CompositeContinuationToken.class);
            }

            return null;
        } catch (final IOException ioError) {
            LOGGER.debug(
                "Failed to parse as composite continuation token JSON {}",
                providedContinuation,
                ioError);
            return null;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof FeedRangeCompositeContinuationImpl)) {
            return false;
        }

        FeedRangeCompositeContinuationImpl other = (FeedRangeCompositeContinuationImpl)o;
        return Objects.equals(this.feedRange, other.feedRange) &&
            Objects.equals(this.getContainerRid(), other.getContainerRid()) &&
            Objects.equals(this.initialNoResultsRange, other.initialNoResultsRange) &&
            Objects.equals(this.currentToken, other.currentToken) &&
            Objects.equals(this.compositeContinuationTokens, other.compositeContinuationTokens);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
            this.feedRange,
            this.getContainerRid(),
            this.initialNoResultsRange,
            this.currentToken,
            this.compositeContinuationTokens);
    }
}
