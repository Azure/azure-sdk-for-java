// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.changefeed.common;

import com.azure.cosmos.implementation.JsonSerializable;
import com.azure.cosmos.implementation.RxDocumentServiceRequest;
import com.azure.cosmos.implementation.Strings;
import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.implementation.apachecommons.lang.tuple.Pair;
import com.azure.cosmos.implementation.feedranges.FeedRangeContinuation;
import com.azure.cosmos.implementation.feedranges.FeedRangeEpkImpl;
import com.azure.cosmos.implementation.feedranges.FeedRangeInternal;
import com.azure.cosmos.implementation.query.CompositeContinuationToken;
import com.azure.cosmos.implementation.routing.PartitionKeyInternalHelper;
import com.azure.cosmos.implementation.routing.Range;
import com.azure.cosmos.models.FeedRange;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkArgument;
import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

@JsonDeserialize(using = ChangeFeedStateDeserializer.class)
public abstract class ChangeFeedState extends JsonSerializable {
    private static final Comparator<Range<String>> MIN_RANGE_COMPARATOR = new Range.MinComparator<>();
    private static final Comparator<Range<String>> MAX_RANGE_COMPARATOR = new Range.MaxComparator<>();

    // Lazily-initialized cache holding a pre-sorted snapshot of continuation tokens.
    // Reused across multiple extractForEffectiveRange calls on the same instance to
    // avoid redundant O(T log T) copy+sort per partition during Spark planning.
    // Benign race by design: concurrent callers may both create a snapshot,
    // but the snapshot is immutable and volatile ensures safe publication.
    // This class is NOT thread-safe for concurrent setContinuation() calls.
    private transient volatile SortedTokensSnapshot cachedSortedTokensSnapshot;

    ChangeFeedState() {
    }

    public abstract FeedRangeContinuation getContinuation();

    /**
     * Sets the continuation for this change feed state.
     * <p>
     * Implementations must assign a new {@link FeedRangeContinuation} reference rather than
     * mutating the existing one in-place. The base class uses reference-equality detection
     * to invalidate a lazily-cached sorted-token snapshot. If the same reference is reused
     * with modified contents, the cache will serve stale data.
     *
     * @param continuation the new continuation to set
     * @return this {@link ChangeFeedState} instance
     */
    public abstract ChangeFeedState setContinuation(FeedRangeContinuation continuation);

    public abstract FeedRangeInternal getFeedRange();

    public abstract ChangeFeedMode getMode();

    public abstract ChangeFeedStartFromInternal getStartFromSettings();

    public abstract String applyServerResponseContinuation(
        String serverContinuationToken,
        RxDocumentServiceRequest request,
        boolean shouldMoveToNextTokenOnETagReplace);

    public abstract String getContainerRid();

    public static ChangeFeedState fromString(String base64EncodedJson) {
        checkNotNull(base64EncodedJson, "Argument 'base64EncodedJson' must not be null");

        String json = new String(
            Base64.getUrlDecoder().decode(base64EncodedJson),
            StandardCharsets.UTF_8);

        final ObjectMapper mapper = Utils.getSimpleObjectMapper();

        try {
            return mapper.readValue(json, ChangeFeedState.class);
        } catch (IOException ioException) {
            throw new IllegalArgumentException(
                String.format("The change feed state continuation contains invalid or unsupported" +
                    " json: %s", json),
                ioException);
        }
    }

    @Override
    public void populatePropertyBag() {
        super.populatePropertyBag();
    }

    @Override
    public String toString() {
        String json = this.toJson();

        if (json == null) {
            return "";
        }

        return Base64.getUrlEncoder().encodeToString(json.getBytes(StandardCharsets.UTF_8));
    }

    public abstract void populateRequest(RxDocumentServiceRequest request, int maxItemCount);

    public List<CompositeContinuationToken> extractContinuationTokens() {
        return extractContinuationTokens(PartitionKeyInternalHelper.FullRange).getLeft();
    }

    private List<CompositeContinuationToken> getOrCreateSortedContinuationTokens() {
        FeedRangeContinuation continuation = this.getContinuation();
        if (continuation == null) {
            return Collections.emptyList();
        }

        SortedTokensSnapshot snapshot = this.cachedSortedTokensSnapshot;
        // Intentional reference equality (==): setContinuation() replaces the reference,
        // invalidating the cache. In-place mutations via applyServerResponseContinuation()
        // do not change the reference and do not affect token range order, so the cache
        // remains valid.
        if (snapshot != null && snapshot.continuationRef == continuation) {
            return snapshot.sortedTokens;
        }

        List<CompositeContinuationToken> sorted = new ArrayList<>();
        Collections.addAll(sorted, continuation.getCurrentContinuationTokens());
        sorted.sort(ContinuationTokenRangeComparator.SINGLETON_INSTANCE);

        this.cachedSortedTokensSnapshot = new SortedTokensSnapshot(continuation, Collections.unmodifiableList(sorted));
        return sorted;
    }

    private Pair<List<CompositeContinuationToken>, Range<String>> extractContinuationTokens(
        Range<String> effectiveRange) {

        checkNotNull(effectiveRange);

        List<CompositeContinuationToken> extractedContinuationTokens = new ArrayList<>();
        String min = null;
        String max = null;

        List<CompositeContinuationToken> sortedTokens = getOrCreateSortedContinuationTokens();

        if (!sortedTokens.isEmpty()) {
            int startIndex = findFirstPotentialOverlapIndex(sortedTokens, effectiveRange);

            // Primary scan from binary search starting position
            MinMaxAccumulator primaryMinMax = new MinMaxAccumulator();
            collectOverlapping(sortedTokens, effectiveRange, startIndex, sortedTokens.size(),
                extractedContinuationTokens, primaryMinMax);
            min = primaryMinMax.min;
            max = primaryMinMax.max;

            // Fallback: if binary search started past index 0, scan earlier indices for any
            // overlapping tokens that the binary search may have skipped. This handles both
            // complete misses (no overlaps found in primary scan) and partial misses (some
            // overlaps missed due to non-contiguous token ranges). Note: the early-break
            // optimization in collectOverlapping still applies, so this does not handle
            // arbitrary non-contiguous overlapping ranges — it preserves the original
            // linear scan behavior which assumes contiguous overlaps (Cosmos DB contract).
            if (startIndex > 0) {
                List<CompositeContinuationToken> missedTokens = new ArrayList<>();
                MinMaxAccumulator fallbackMinMax = new MinMaxAccumulator();
                collectOverlapping(sortedTokens, effectiveRange, 0, startIndex,
                    missedTokens, fallbackMinMax);
                if (!missedTokens.isEmpty()) {
                    // Prepend missed tokens (they precede startIndex in sorted order)
                    missedTokens.addAll(extractedContinuationTokens);
                    extractedContinuationTokens = missedTokens;
                    // Missed tokens come first in sorted order, so their min is the overall min
                    min = fallbackMinMax.min;
                    // Take the larger max between fallback and primary results
                    if (max == null || (fallbackMinMax.max != null
                        && fallbackMinMax.max.compareTo(max) > 0)) {
                        max = fallbackMinMax.max;
                    }
                }
            }
        }

        Range<String> totalRange = new Range<>(
            min != null ? min : PartitionKeyInternalHelper.MinimumInclusiveEffectivePartitionKey,
            max != null ? max : PartitionKeyInternalHelper.MaximumExclusiveEffectivePartitionKey,
            true,
            false);

        return Pair.of(extractedContinuationTokens, totalRange);
    }

    /**
     * Collects continuation tokens from sortedTokens[fromIndex..toIndex) that overlap
     * with effectiveRange. Each collected token's range is trimmed to the overlapping
     * region. Applies an early-break optimization: stops scanning when a non-overlapping
     * token is encountered after at least one overlapping token has been found.
     *
     * @param sortedTokens the sorted continuation token list
     * @param effectiveRange the range to check for overlaps
     * @param fromIndex start index (inclusive)
     * @param toIndex end index (exclusive)
     * @param out list to append matching tokens to
     * @param minMax accumulator for tracking min/max of overlapping ranges;
     *               min is set to the first overlap's min, max to the last overlap's max
     */
    private void collectOverlapping(
        List<CompositeContinuationToken> sortedTokens,
        Range<String> effectiveRange,
        int fromIndex,
        int toIndex,
        List<CompositeContinuationToken> out,
        MinMaxAccumulator minMax) {

        for (int i = fromIndex; i < toIndex; i++) {
            CompositeContinuationToken compositeContinuationToken = sortedTokens.get(i);
            if (Range.checkOverlapping(effectiveRange, compositeContinuationToken.getRange())) {
                Range<String> overlappingRange =
                    getOverlappingRange(effectiveRange, compositeContinuationToken.getRange());
                out.add(new CompositeContinuationToken(
                    compositeContinuationToken.getToken(), overlappingRange));
                if (minMax.min == null) {
                    minMax.min = overlappingRange.getMin();
                }
                minMax.max = overlappingRange.getMax();
            } else {
                // Early-break: assumes overlapping tokens are contiguous after sorting.
                // Safe for non-overlapping partition ranges (Cosmos DB contract).
                // Inherited from original linear scan behavior.
                if (!out.isEmpty()) {
                    break;
                }
            }
        }
    }

    /**
     * Binary search to find the first index in the sorted token list where
     * overlapping tokens may start for the given effective range.
     * Uses the same comparator as the sort to ensure consistency.
     */
    private static int findFirstPotentialOverlapIndex(
        List<CompositeContinuationToken> sortedTokens,
        Range<String> effectiveRange) {

        int low = 0;
        int high = sortedTokens.size() - 1;
        int insertionPoint = sortedTokens.size();

        while (low <= high) {
            int mid = low + (high - low) / 2;
            int cmp = MIN_RANGE_COMPARATOR.compare(sortedTokens.get(mid).getRange(), effectiveRange);
            if (cmp > 0) {
                insertionPoint = mid;
                high = mid - 1;
            } else {
                low = mid + 1;
            }
        }

        // Back up by 1 to catch a token whose range.min <= effectiveRange.min
        // but whose range.max extends past effectiveRange.min
        return Math.max(0, insertionPoint - 1);
    }

    public ChangeFeedState extractForEffectiveRange(Range<String> effectiveRange) {
        checkNotNull(effectiveRange);

        Pair<List<CompositeContinuationToken>, Range<String>> effectiveTokensAndMinMax =
            this.extractContinuationTokens(effectiveRange);

        List<CompositeContinuationToken> extractedContinuationTokens = effectiveTokensAndMinMax.getLeft();
        Range<String> totalRange = effectiveTokensAndMinMax.getRight();

        FeedRangeEpkImpl feedRange = new FeedRangeEpkImpl(totalRange);

        return new ChangeFeedStateV1(
            this.getContainerRid(),
            feedRange,
            this.getMode(),
            this.getStartFromSettings(),
            FeedRangeContinuation.create(
                this.getContainerRid(),
                feedRange,
                extractedContinuationTokens
            )
        );
    }

    private Range<String> getOverlappingRange(Range<String> left, Range<String> right) {
        checkNotNull(left, "Argument 'left' must not be null");
        checkNotNull(right, "Argument 'right' must not be null");
        checkArgument(
            left.isMinInclusive() && !left.isMaxInclusive(),
            "Argument 'left' is using exclusive Min or inclusive Max.");
        checkArgument(
            right.isMinInclusive() && !right.isMaxInclusive(),
            "Argument 'right' is using exclusive Min or inclusive Max.");

        String min;
        String max;

        if (MIN_RANGE_COMPARATOR.compare(left, right) > 0) {
            min = left.getMin();
        } else {
            min = right.getMin();
        }

        if (MAX_RANGE_COMPARATOR.compare(left, right) < 0) {
            max = left.getMax();
        } else {
            max = right.getMax();
        }

        return new Range<>(min, max, true, false);
    }

    public static ChangeFeedState merge(ChangeFeedState[] states) {
        checkNotNull(states, "Argument 'states' must not be null.");
        checkArgument(states.length > 0, "Argument 'states' must not be empty.");

        ChangeFeedState firstState = states[0];
        if (states.length == 1) {
            return firstState;
        }

        List<CompositeContinuationToken> continuationTokens = new ArrayList<>(states.length);

        for (ChangeFeedState state : states) {
            validateConsistency(state, firstState);
            FeedRangeContinuation continuation = state.getContinuation();
            Collections.addAll(continuationTokens, continuation.getCurrentContinuationTokens());
        }

        continuationTokens.sort(ContinuationTokenRangeComparator.SINGLETON_INSTANCE);
        for (int i = 1; i < continuationTokens.size(); i++) {
            CompositeContinuationToken previous = continuationTokens.get(i - 1);
            CompositeContinuationToken current = continuationTokens.get(i);
            if (Range.checkOverlapping(previous.getRange(), current.getRange()))
            {
                throw new IllegalStateException(
                    String.format(
                        "Argument 'states' is invalid - it contains overlapping continuations '%s' and '%s'.",
                        previous.toJson(),
                        current.toJson()));
            }
        }

        return new ChangeFeedStateV1(
            firstState.getContainerRid(),
            (FeedRangeEpkImpl)FeedRange.forFullRange(),
            firstState.getMode(),
            firstState.getStartFromSettings(),
            FeedRangeContinuation.create(
                firstState.getContainerRid(),
                (FeedRangeEpkImpl)FeedRange.forFullRange(),
                continuationTokens
            )
        );
    }

    private static void validateConsistency(ChangeFeedState candidate, ChangeFeedState expected) {
        String containerRid = candidate.getContainerRid();
        if (Strings.isNullOrEmpty(containerRid) || !containerRid.equals(expected.getContainerRid())) {
            final String message = String.format(
                "The container %s for the reference change feed status is different from the current container %s.",
                expected.getContainerRid(),
                containerRid);
            throw new IllegalArgumentException(message);
        }

        ChangeFeedMode mode = candidate.getMode();
        if (mode == null || !mode.equals(expected.getMode())) {
            final String message = String.format(
                "The mode %s for the reference change feed status is different from the current mode %s.",
                expected.getMode(),
                mode);
            throw new IllegalArgumentException(message);
        }

        ChangeFeedStartFromInternal startFrom = candidate.getStartFromSettings();
        if (startFrom == null || !startFrom.toJson().equals(expected.getStartFromSettings().toJson())) {
            final String message = String.format(
                "The mode '%s' for the reference change feed status is different from the current mode '%s'.",
                expected.getStartFromSettings().toJson(),
                startFrom != null ? startFrom.toJson() : "null");
            throw new IllegalArgumentException(message);
        }
    }

    private static class ContinuationTokenRangeComparator
        implements Comparator<CompositeContinuationToken>, Serializable {

        public static final ContinuationTokenRangeComparator SINGLETON_INSTANCE =
            new ContinuationTokenRangeComparator();
        private static final long serialVersionUID = 1L;

        private ContinuationTokenRangeComparator() {
        }

        @Override
        public int compare(CompositeContinuationToken left, CompositeContinuationToken right) {
            checkNotNull(left, "Argument 'left' must not be null.");
            checkNotNull(right, "Argument 'right' must not be null.");

            return MIN_RANGE_COMPARATOR.compare(left.getRange(), right.getRange());
        }
    }

    /**
     * Tracks the min and max range values while collecting overlapping tokens.
     */
    private static final class MinMaxAccumulator {
        String min;
        String max;
    }

    /**
     * Holds a pre-sorted snapshot of continuation tokens along with the
     * continuation reference it was built from for cache invalidation.
     */
    private static final class SortedTokensSnapshot {
        final FeedRangeContinuation continuationRef;
        final List<CompositeContinuationToken> sortedTokens;

        SortedTokensSnapshot(
            FeedRangeContinuation continuationRef,
            List<CompositeContinuationToken> sortedTokens) {
            this.continuationRef = continuationRef;
            this.sortedTokens = sortedTokens;
        }
    }
}
