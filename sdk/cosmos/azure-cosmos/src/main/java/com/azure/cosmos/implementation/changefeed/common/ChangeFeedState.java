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
    ChangeFeedState() {
    }

    public abstract FeedRangeContinuation getContinuation();

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
        return getSortedContinuationTokens();
    }

    /**
     * Extracts a {@link ChangeFeedState} for a single effective range.
     * <p>
     * For callers that need to extract states for multiple ranges from the same
     * continuation, prefer {@link #extractForEffectiveRanges(List)} which sorts
     * the continuation tokens once and reuses the sorted list across all ranges.
     *
     * @param effectiveRange the partition range to extract for
     * @return a new {@link ChangeFeedState} scoped to the given range
     */
    public ChangeFeedState extractForEffectiveRange(Range<String> effectiveRange) {
        checkNotNull(effectiveRange);
        return extractForEffectiveRanges(Collections.singletonList(effectiveRange)).get(0);
    }

    /**
     * Extracts a list of {@link ChangeFeedState} instances, one per input effective range.
     * <p>
     * Continuation tokens are sorted once (O(T log T)) and then each range lookup uses
     * binary search (O(log T)) to find the starting position, followed by a forward scan
     * through contiguous overlapping tokens. This follows the same binary search pattern as
     * {@link com.azure.cosmos.implementation.routing.InMemoryCollectionRoutingMap#getOverlappingRanges}
     * and assumes non-overlapping, contiguous partition ranges (Cosmos DB contract).
     * <p>
     * The returned list preserves the input order: result.get(i) corresponds to
     * effectiveRanges.get(i).
     *
     * @param effectiveRanges the list of partition ranges to extract for
     * @return a list of {@link ChangeFeedState}, one per input range, in the same order
     */
    public List<ChangeFeedState> extractForEffectiveRanges(List<Range<String>> effectiveRanges) {
        checkNotNull(effectiveRanges, "Argument 'effectiveRanges' must not be null.");
        checkArgument(!effectiveRanges.isEmpty(), "Argument 'effectiveRanges' must not be empty.");

        List<CompositeContinuationToken> sortedTokens = getSortedContinuationTokens();

        List<ChangeFeedState> results = new ArrayList<>(effectiveRanges.size());
        for (Range<String> effectiveRange : effectiveRanges) {
            checkNotNull(effectiveRange, "Effective range must not be null.");

            Pair<List<CompositeContinuationToken>, Range<String>> extracted =
                extractContinuationTokensForRange(effectiveRange, sortedTokens);

            List<CompositeContinuationToken> tokens = extracted.getLeft();
            Range<String> totalRange = extracted.getRight();

            FeedRangeEpkImpl feedRange = new FeedRangeEpkImpl(totalRange);

            results.add(new ChangeFeedStateV1(
                this.getContainerRid(),
                feedRange,
                this.getMode(),
                this.getStartFromSettings(),
                FeedRangeContinuation.create(
                    this.getContainerRid(),
                    feedRange,
                    tokens
                )
            ));
        }

        return results;
    }

    private List<CompositeContinuationToken> getSortedContinuationTokens() {
        FeedRangeContinuation continuation = this.getContinuation();
        if (continuation == null) {
            return Collections.emptyList();
        }

        List<CompositeContinuationToken> sortedTokens = new ArrayList<>();
        Collections.addAll(sortedTokens, continuation.getCurrentContinuationTokens());
        sortedTokens.sort(ContinuationTokenRangeComparator.SINGLETON_INSTANCE);
        return sortedTokens;
    }

    /**
     * Finds overlapping continuation tokens for an effective range using binary search
     * to locate the starting position, then scanning forward through contiguous overlapping
     * tokens. Follows the same pattern as
     * {@link com.azure.cosmos.implementation.routing.InMemoryCollectionRoutingMap#getOverlappingRanges}.
     */
    private Pair<List<CompositeContinuationToken>, Range<String>> extractContinuationTokensForRange(
        Range<String> effectiveRange,
        List<CompositeContinuationToken> sortedTokens) {

        List<CompositeContinuationToken> extractedTokens = new ArrayList<>();
        String min = null;
        String max = null;

        if (!sortedTokens.isEmpty()) {
            int startIndex = Collections.binarySearch(
                sortedTokens,
                new CompositeContinuationToken(null, effectiveRange),
                ContinuationTokenRangeComparator.SINGLETON_INSTANCE);
            if (startIndex < 0) {
                startIndex = Math.max(0, -startIndex - 2);
            }

            for (int i = startIndex; i < sortedTokens.size(); i++) {
                CompositeContinuationToken token = sortedTokens.get(i);
                if (Range.checkOverlapping(effectiveRange, token.getRange())) {
                    Range<String> overlappingRange = getOverlappingRange(effectiveRange, token.getRange());
                    extractedTokens.add(new CompositeContinuationToken(
                        token.getToken(), overlappingRange));
                    if (min == null) {
                        min = overlappingRange.getMin();
                    }
                    max = overlappingRange.getMax();
                } else if (!extractedTokens.isEmpty()) {
                    break;
                }
            }
        }

        Range<String> totalRange = new Range<>(
            min != null ? min : PartitionKeyInternalHelper.MinimumInclusiveEffectivePartitionKey,
            max != null ? max : PartitionKeyInternalHelper.MaximumExclusiveEffectivePartitionKey,
            true,
            false);

        return Pair.of(extractedTokens, totalRange);
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
}
