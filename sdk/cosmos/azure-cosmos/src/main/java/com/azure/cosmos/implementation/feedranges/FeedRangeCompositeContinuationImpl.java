// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.feedranges;

import com.azure.cosmos.CosmosItemSerializer;
import com.azure.cosmos.implementation.Constants;
import com.azure.cosmos.implementation.GoneException;
import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.PartitionKeyRange;
import com.azure.cosmos.implementation.RxDocumentClientImpl;
import com.azure.cosmos.implementation.ShouldRetryResult;
import com.azure.cosmos.implementation.Strings;
import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;
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
import java.io.Serializable;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

/**
 * FeedRangeContinuation using Composite Continuation Tokens and split proof.
 * It uses a breath-first approach to transverse Composite Continuation Tokens.
 */
final class FeedRangeCompositeContinuationImpl extends FeedRangeContinuation {

    private final static Logger LOGGER = LoggerFactory.getLogger(FeedRangeCompositeContinuationImpl.class);
    private static final String PK_RANGE_ID_SEPARATOR = ":";
    private static final String SEGMENT_SEPARATOR = "#";

    private final Queue<CompositeContinuationToken> compositeContinuationTokens;
    private CompositeContinuationToken currentToken;
    private String initialNoResultsRange;
    private final AtomicLong continuousNotModifiedSinceInitialNoResultsRangeCaptured = new AtomicLong(0);
    private final Map<Range<String>, FeedRangeLSNContext> feedRangeLSNContextMap = new ConcurrentHashMap<>();


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

    public FeedRangeCompositeContinuationImpl(
        String containerRid,
        FeedRangeInternal feedRange,
        List<CompositeContinuationToken> continuationTokens) {

        this(containerRid, feedRange);

        checkNotNull(continuationTokens, "'continuationTokens' must not be null");

        if (continuationTokens.size() == 0) {
            throw new IllegalArgumentException("'continuationTokens' must not be empty");
        }

        for (CompositeContinuationToken continuationToken : continuationTokens) {
            this.compositeContinuationTokens.add(
                // add a copy
                FeedRangeCompositeContinuationImpl.createCompositeContinuationTokenForRange(
                    continuationToken.getRange().getMin(),
                    continuationToken.getRange().getMax(),
                    continuationToken.getToken())
            );
        }

        this.currentToken = this.getCompositeContinuationTokens().peek();
    }

    public void populatePropertyBag() {
        super.populatePropertyBag();

        this.set(
            Constants.Properties.FEED_RANGE_COMPOSITE_CONTINUATION_VERSION,
            FeedRangeContinuationVersions.V1,
            CosmosItemSerializer.DEFAULT_SERIALIZER);

        this.set(
            Constants.Properties.FEED_RANGE_COMPOSITE_CONTINUATION_RESOURCE_ID,
            this.getContainerRid(),
            CosmosItemSerializer.DEFAULT_SERIALIZER);

        if (this.compositeContinuationTokens.size() > 0) {
            for (CompositeContinuationToken token : this.compositeContinuationTokens) {
                ModelBridgeInternal.populatePropertyBag(token);
            }

            this.set(
                Constants.Properties.FEED_RANGE_COMPOSITE_CONTINUATION_CONTINUATION,
                this.compositeContinuationTokens,
                CosmosItemSerializer.DEFAULT_SERIALIZER);
        }

        if (this.feedRange != null) {
            this.feedRange.setProperties(this, true);
        }
    }

    private FeedRangeCompositeContinuationImpl(String containerRid, FeedRangeInternal feedRange) {
        super(containerRid, feedRange);

        this.compositeContinuationTokens = new LinkedList<>();
    }

    @Override
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
    public CompositeContinuationToken[] getCurrentContinuationTokens() {
        CompositeContinuationToken[] snapshot = new CompositeContinuationToken[this.compositeContinuationTokens.size()];
        this.compositeContinuationTokens.toArray(snapshot);

        return snapshot;
    }

    @Override
    public int getContinuationTokenCount() {
        return this.compositeContinuationTokens.size();
    }

    @Override
    public void replaceContinuation(final String continuationToken, boolean shouldMoveToNextTokenOnETagReplace) {
        final CompositeContinuationToken continuationTokenSnapshot = this.currentToken;

        if (continuationTokenSnapshot == null) {
            return;
        }

        continuationTokenSnapshot.setToken(continuationToken);

        if (shouldMoveToNextTokenOnETagReplace) {
            this.moveToNextToken();
        }
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
    public <T> ShouldRetryResult handleChangeFeedNotModified(final FeedResponse<T> response) {
        checkNotNull(response, "Argument 'response' must not be null");

        if (!ModelBridgeInternal.<T>noChanges(response)) {
            this.initialNoResultsRange = null;
            this.continuousNotModifiedSinceInitialNoResultsRangeCaptured.set(0);
        } else if (this.compositeContinuationTokens.size() > 1) {
            if (this.initialNoResultsRange == null) {

                this.initialNoResultsRange = this.currentToken.getRange().getMin();
                this.continuousNotModifiedSinceInitialNoResultsRangeCaptured.set(0);

                // Done already in ChangeFeedFetcher.applyServerContinuation
                // this.replaceContinuation(eTag);

                this.moveToNextToken();

                return ShouldRetryResult.RETRY_NOW;
            }

            if (!this.initialNoResultsRange.equalsIgnoreCase(this.currentToken.getRange().getMin())) {
                this.continuousNotModifiedSinceInitialNoResultsRangeCaptured.incrementAndGet();

                // Done already in ChangeFeedFetcher.applyServerContinuation
                // this.replaceContinuation(eTag);

                this.moveToNextToken();

                long consecutiveNotModifiedResponsesSnapshot =
                    this.continuousNotModifiedSinceInitialNoResultsRangeCaptured.get();
                if (consecutiveNotModifiedResponsesSnapshot > 4L * (this.compositeContinuationTokens.size() + 1)) {

                    // This is just a defense in-depth - if we see subsequent 304s all the time, avoid similar hangs
                    // just bail out - the threshold allows for two-level splits of all sub-ranges which is
                    // safe enough - with more than two-level splits we have other design gaps (service, not SDK)
                    // due to problems identifying child  ranges anyway.
                    LOGGER.warn(
                        "Preempting change feed query early due to {} consecutive 304.",
                        consecutiveNotModifiedResponsesSnapshot);

                    return ShouldRetryResult.NO_RETRY;
                } else {
                    return ShouldRetryResult.RETRY_NOW;
                }
            }
        }

        return ShouldRetryResult.NO_RETRY;
    }

    @Override
    public <T> boolean hasFetchedAllChangesAvailableNow(FeedResponse<T> response) {
        FeedRangeLSNContext feedRangeLSNContext =
            this.updateFeedRangeEndLSNIfAbsent(
                this.currentToken.getRange(),
                response.getSessionToken());
        feedRangeLSNContext.handleLSNFromContinuation(this.currentToken);

        // find next token which can fetch more
        Range<String> initialToken = this.currentToken.getRange();
        do {
            this.moveToNextToken();
        } while (
            !this.currentToken.getRange().equals(initialToken) &&
                this.hasFetchAllChangesAvailableNowForFeedRange(this.currentToken.getRange()));

        return this.hasFetchAllChangesAvailableNowForFeedRange(this.currentToken.getRange());
    }

    @Override
    public Mono<ShouldRetryResult> handleFeedRangeGone(final RxDocumentClientImpl client,
                                                       final GoneException goneException) {

        checkNotNull(client, "Argument 'client' must not be null");
        checkNotNull(goneException, "Argument 'goeException' must not be null");

        Integer nSubStatus = goneException.getSubStatusCode();

        final boolean partitionSplitOrMerge =
            goneException.getStatusCode() == HttpConstants.StatusCodes.GONE &&
                nSubStatus != null &&
                (nSubStatus == HttpConstants.SubStatusCodes.PARTITION_KEY_RANGE_GONE
                || nSubStatus == HttpConstants.SubStatusCodes.COMPLETING_SPLIT_OR_MERGE);

        if (!partitionSplitOrMerge) {
            return Mono.just(ShouldRetryResult.NO_RETRY);
        }

        final RxPartitionKeyRangeCache partitionKeyRangeCache = client.getPartitionKeyRangeCache();
        Range<String> effectiveTokenRange = this.currentToken.getRange();
        final Mono<Utils.ValueHolder<List<PartitionKeyRange>>> resolvedRangesTask =
            this.tryGetOverlappingRanges(partitionKeyRangeCache, effectiveTokenRange, true);

        return resolvedRangesTask.flatMap(resolvedRanges -> {
            if (resolvedRanges.v != null) {
                if (resolvedRanges.v.size() == 1) {
                    // Merge happen, will continue draining from the current range
                    LOGGER.debug("ChangeFeedFetcher detected feed range gone due to merge for range [{}]", effectiveTokenRange);
                } else {
                    this.createChildRanges(resolvedRanges.v, effectiveTokenRange);
                    LOGGER.debug("ChangeFeedFetcher detected feed range gone due to split for range [{}]", effectiveTokenRange);
                }
            }
            return Mono.just(ShouldRetryResult.RETRY_NOW);
        });
    }

    private Long getLatestLsnFromSessionToken(String sessionToken) {
        String parsedSessionToken = sessionToken.substring(
            sessionToken.indexOf(PK_RANGE_ID_SEPARATOR));
        String[] segments = StringUtils.split(parsedSessionToken, SEGMENT_SEPARATOR);
        String latestLsn = segments[0];
        if (segments.length >= 2) {
            // default to Global LSN
            latestLsn = segments[1];
        }

        return Long.parseLong(latestLsn);
    }

    private FeedRangeLSNContext updateFeedRangeEndLSNIfAbsent(
        Range<String> targetedRange,
        String sessionToken) {
        return this.feedRangeLSNContextMap.computeIfAbsent(
            targetedRange,
            (range) -> {
                return new FeedRangeLSNContext(
                    targetedRange,
                    this.getLatestLsnFromSessionToken(sessionToken)
                );
            });
    }

    private boolean hasFetchAllChangesAvailableNowForFeedRange(Range<String> range) {
        return this.feedRangeLSNContextMap.containsKey(range) &&
            this.feedRangeLSNContextMap.get(range).hasCompleted;
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

    private static String getMinString(String left, String right) {
        checkNotNull(left, "Argument 'left' must not be null.");
        checkNotNull(right, "Argument 'right' must not be null.");

        if (left.compareTo(right) < 0) {
            return left;
        }

        return right;
    }

    private static String getMaxString(String left, String right) {
        checkNotNull(left, "Argument 'left' must not be null.");
        checkNotNull(right, "Argument 'right' must not be null.");

        if (left.compareTo(right) > 0) {
            return left;
        }

        return right;
    }

    private void createChildRanges(
        final List<PartitionKeyRange> keyRanges,
        final Range<String> effectiveTokenRange) {

        keyRanges.sort(PartitionKeyRangeMinInclusiveComparator.SingletonInstance);

        final PartitionKeyRange firstRange = keyRanges.get(0);
        this.currentToken
            .setRange(
                new Range<>(
                    getMaxString(effectiveTokenRange.getMin(), firstRange.getMinInclusive()),
                    getMinString(effectiveTokenRange.getMax(), firstRange.getMaxExclusive()),
                    true,
                    false));

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
                Range<String> newRange = new Range<>(
                    getMaxString(effectiveTokenRange.getMin(), keyRange.getMinInclusive()),
                    getMinString(effectiveTokenRange.getMax(), keyRange.getMaxExclusive()),
                    true,
                    false
                );

                continuationAsComposite.setRange(newRange);
                this.compositeContinuationTokens.add(createCompositeContinuationTokenForRange(
                    newRange.getMin(),
                    newRange.getMax(),
                    continuationAsComposite.toJson()));
            }
        } else {
            // Add children
            final int size = keyRanges.size();
            for (int i = 1; i < size; i++) {
                final PartitionKeyRange keyRange = keyRanges.get(i);
                Range<String> newRange = new Range<>(
                    getMaxString(effectiveTokenRange.getMin(), keyRange.getMinInclusive()),
                    getMinString(effectiveTokenRange.getMax(), keyRange.getMaxExclusive()),
                    true,
                    false
                );

                this.compositeContinuationTokens.add(createCompositeContinuationTokenForRange(
                    newRange.getMin(),
                    newRange.getMax(),
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
        final RxPartitionKeyRangeCache partitionKeyRangeCache,
        Range<String> effectiveRange,
        final Boolean forceRefresh) {

        return partitionKeyRangeCache.tryGetOverlappingRangesAsync(
                null,
                this.getContainerRid(),
                effectiveRange,
                forceRefresh,
                null);
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

    static class PartitionKeyRangeMinInclusiveComparator implements Comparator<PartitionKeyRange>, Serializable {
        private static final long serialVersionUID = 1L;
        final static Comparator<PartitionKeyRange> SingletonInstance = new PartitionKeyRangeMinInclusiveComparator();

        private PartitionKeyRangeMinInclusiveComparator() {
        }

        @Override
        public int compare(PartitionKeyRange o1, PartitionKeyRange o2) {
            return o1.getMinInclusive().compareTo(o2.getMinInclusive());
        }
    }

    final static class FeedRangeLSNContext {
        private Range<String> range;
        private Long endLSN;
        private boolean hasCompleted;

        public FeedRangeLSNContext(Range<String> range, Long endLSN) {
            this.range = range;
            this.endLSN = endLSN;
            this.hasCompleted = false;
        }

        public void handleLSNFromContinuation(CompositeContinuationToken compositeContinuationToken) {
            if (!compositeContinuationToken.getRange().equals(this.range)) {
                throw new IllegalStateException(
                    "Range in FeedRangeAvailableNowContext is different than the range in the continuationToken");
            }

            String lsnFromContinuationToken = compositeContinuationToken.getToken();
            if (lsnFromContinuationToken.startsWith("\"")) {
                lsnFromContinuationToken = lsnFromContinuationToken.substring(1, lsnFromContinuationToken.length() - 1);
            }

            if (Long.parseLong(lsnFromContinuationToken) >= endLSN) {
                this.hasCompleted = true;
            }
        }
    }
}
