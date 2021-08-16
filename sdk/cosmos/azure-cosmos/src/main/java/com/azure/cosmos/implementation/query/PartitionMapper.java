// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.query;

import com.azure.cosmos.implementation.Strings;
import com.azure.cosmos.implementation.apachecommons.lang.tuple.Pair;
import com.azure.cosmos.implementation.feedranges.FeedRangeEpkImpl;
import com.azure.cosmos.implementation.routing.Range;

import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

public final class PartitionMapper {
    public static <T extends IPartitionedToken> PartitionMapping<T> getPartitionMapping(
        List<FeedRangeEpkImpl> feedRangeEpkList,
        List<T> tokens) {
        checkNotNull(feedRangeEpkList);
        checkNotNull(tokens);
        if (feedRangeEpkList.isEmpty()) {
            throw new IllegalArgumentException("feedRanges should not be empty");
        }
        if (tokens.isEmpty()) {
            throw new IllegalArgumentException("tokens should not be empty");
        }

        List<FeedRangeEpkImpl> mergedFeedRanges = mergeRangesWhenPossible(feedRangeEpkList);
        Map<FeedRangeEpkImpl, T> splitRangesAndTokens = splitRangesBasedOffContinuationToken(mergedFeedRanges, tokens);
        FeedRangeEpkImpl targetFeedRange = getTargetFeedRange(tokens);

        return createPartitionMapping(splitRangesAndTokens, tokens, targetFeedRange);
    }

    /**
     * Merges all the feed ranges as much as possible.
     * example:
     * [(A, B), (B, C), (E, F), (H, I), (I, J)]
     *     => [(A, C), (E, F), (H, J)]
     * @param feedRangeEpkList the feedRangeEpkList
     * @return merged list of FeedRangeEPK
     */
    private static List<FeedRangeEpkImpl> mergeRangesWhenPossible(List<FeedRangeEpkImpl> feedRangeEpkList) {
        Deque<Pair<String, String>> mergedRanges = new ArrayDeque<>(feedRangeEpkList.size());

        for (FeedRangeEpkImpl feedRangeEpk : feedRangeEpkList) {
            if (mergedRanges.isEmpty()) {
                // If the stack is empty, then just add the range to get things started.
                mergedRanges.push(Pair.of(feedRangeEpk.getRange().getMin(), feedRangeEpk.getRange().getMax()));
            } else {
                Pair<String, String> pop = mergedRanges.pop();
                String min = pop.getLeft();
                String max = pop.getRight();
                if (max.equals(feedRangeEpk.getRange().getMin())) {
                    // This means that the ranges are consecutive and can be merged.
                    mergedRanges.push(Pair.of(min, feedRangeEpk.getRange().getMax()));
                } else {
                    // Just push the ranges separately
                    mergedRanges.push(pop);
                    mergedRanges.push(Pair.of(feedRangeEpk.getRange().getMin(), feedRangeEpk.getRange().getMax()));
                }
            }
        }

        List<FeedRangeEpkImpl> mergedFeedRanges = mergedRanges.stream()
                                                      .map(pair -> {
                                                          Range<String> range = new Range<String>(pair.getLeft(),
                                                                                                  pair.getRight(),
                                                                                                  true,
                                                                                                  false);
                                                          return new FeedRangeEpkImpl(range);
                                                      })
                                                      .collect(Collectors.toList());

        return mergedFeedRanges;
    }

    /**
     * Splits the ranges into the ranges from the continuation token.
     * example:
     * ranges: [(A, E), (H, K)],
     * tokens: [(A, C):5, (I, J): 6]
     *   => [(A,C): 5, (C, E): null, (H, I): null, (I, J): 6, (J, K): null]
     *
     * @param feedRangeEpks the feedranges
     * @param tokens the continuation tokens
     * @return Map of FeedRangeEpk and continuation tokens
     */
    private static <T extends IPartitionedToken> Map<FeedRangeEpkImpl, T> splitRangesBasedOffContinuationToken(
        List<FeedRangeEpkImpl> feedRangeEpks,
        List<T> tokens) {

        HashSet<FeedRangeEpkImpl> remainingRanges = new HashSet<>(feedRangeEpks);
        Map<FeedRangeEpkImpl, T> splitRangesAndTokens = new HashMap<>();
        for (T token : tokens) {
            List <FeedRangeEpkImpl> rangesThatOverlapToken = remainingRanges
                .stream()
                .filter(feedRangeEpk -> {
                    boolean tokenRightOfStart = Strings.isNullOrEmpty(feedRangeEpk.getRange().getMin())
                                                    || (!(Strings.isNullOrEmpty(token.getRange().getMin()))
                                                            && (token.getRange().getMin()
                                                                    .compareTo(feedRangeEpk.getRange().getMin()) >= 0));
                    boolean tokenLeftOfEnd = Strings.isNullOrEmpty(feedRangeEpk.getRange().getMax())
                                                 || (!(Strings.isNullOrEmpty(token.getRange().getMax()))
                                                         && (token.getRange().getMax()
                                                                 .compareTo(feedRangeEpk.getRange().getMax()) <= 0));

                    boolean rangeCompletelyOverlapsToken = tokenRightOfStart && tokenLeftOfEnd;
                    return rangeCompletelyOverlapsToken;
                })
                .collect(Collectors.toList());

            if (rangesThatOverlapToken.size() == 0) {
                // Do nothing
            } else if (rangesThatOverlapToken.size() == 1) {
                FeedRangeEpkImpl feedRangeEpk = rangesThatOverlapToken.get(0);
                //Remove the range and split it into 3 sections:
                remainingRanges.remove(feedRangeEpk);

                // 1) Left of Token range
                if (!feedRangeEpk.getRange().getMin().equals(token.getRange().getMin())) {
                    FeedRangeEpkImpl leftOfOverlap = new FeedRangeEpkImpl(
                        new Range<String>(feedRangeEpk.getRange().getMin(),
                                          token.getRange().getMin(),
                                          true,
                                          false)
                    );
                    remainingRanges.add(leftOfOverlap);
                }

                // 2) Token Range
                FeedRangeEpkImpl overlappingSection = new FeedRangeEpkImpl(
                    new Range<String>(token.getRange().getMin(),
                                      token.getRange().getMax(),
                                      true,
                                      false)
                );
                splitRangesAndTokens.put(overlappingSection, token);

                // 3) Right of Token Range
                if (!token.getRange().getMax().equals(feedRangeEpk.getRange().getMax())) {
                    FeedRangeEpkImpl rightOfOverlap = new FeedRangeEpkImpl(
                        new Range<String>(token.getRange().getMax(),
                                          feedRangeEpk.getRange().getMax(),
                                          true,
                                          false)
                    );
                    remainingRanges.add(rightOfOverlap);
                }
            } else {
                throw new IllegalStateException("Token was overlapped by multiple ranges");
            }
        }

        for (FeedRangeEpkImpl remainingRange : remainingRanges) {
            // Unmatched ranges just match to null tokens
            splitRangesAndTokens.put(remainingRange, null);
        }

        return splitRangesAndTokens;
    }

    private static <T extends IPartitionedToken> FeedRangeEpkImpl getTargetFeedRange(List<T> tokens){
        T firstContinuationToken =
            tokens.stream().sorted(Comparator.comparing(t -> t.getRange().getMin()))
                .collect(Collectors.toList())
                .get(0);

        // Construct and return the targetFeedRange
        return new FeedRangeEpkImpl(
            new Range<>(firstContinuationToken.getRange().getMin(),
                        firstContinuationToken.getRange().getMax(),
                        true,
                        false)
        );
    }

    /**
     * Segments the ranges and their tokens into a partition mapping.
     *
     * @param splitRangesAndTokensMap map of splitRanges and tokens
     * @param tokens list of continuation tokens
     * @param targetRange target feedrangeEpk
     * @param <T> the type
     * @return the PartitionMapping
     */
    private static <T extends IPartitionedToken> PartitionMapping<T> createPartitionMapping(
        Map<FeedRangeEpkImpl, T>  splitRangesAndTokensMap,
        List<T> tokens,
        FeedRangeEpkImpl targetRange) {

        // TODO: Simplify below to use map directly instead of list of pairs
        List<Pair<FeedRangeEpkImpl, T>> splitRangesAndTokens =
            splitRangesAndTokensMap.entrySet()
                .stream()
                .map(e -> Pair.of(e.getKey(), e.getValue()))
                .collect(Collectors.toList());

        List<Pair<FeedRangeEpkImpl, T>> sortedRanges = splitRangesAndTokens.stream()
                                                           .sorted(Comparator
                                                                       .comparing(p -> p.getLeft().getRange().getMin()))
                                                           .collect(Collectors.toList());

        Optional<Integer> matchedIndex = Optional.empty();
        for (int i = 0; (i < sortedRanges.size()) && !matchedIndex.isPresent(); i++) {
            Pair<FeedRangeEpkImpl, T> feedRangeEpkTPair = sortedRanges.get(i);
            if (feedRangeEpkTPair.getLeft().equals(targetRange)) {
                matchedIndex = Optional.of(i);
            }
        }

        if (!matchedIndex.isPresent()) {
            if (splitRangesAndTokens.size() != 1) {
                throw new IllegalStateException("Could not find continuation token for range " + targetRange);
            }
            // The user is doing a partition key query that got split, so it no longer aligns with our continuation
            // token.
            sortedRanges = Collections.singletonList(Pair.of(sortedRanges.get(0).getLeft(), tokens.get(0)));
            matchedIndex = Optional.of(0);
        }

        Map<FeedRangeEpkImpl, T> partitionsLeftOfTarget = new HashMap<>();
        if (matchedIndex.get() != 0) {
            List<Pair<FeedRangeEpkImpl, T>> pairs = sortedRanges.subList(0, matchedIndex.get());
            for (Pair<FeedRangeEpkImpl, T> p : pairs) {
                partitionsLeftOfTarget.put(p.getLeft(), p.getRight());
            }
        }

        List<Pair<FeedRangeEpkImpl, T>> pairs = sortedRanges.subList(matchedIndex.get(), matchedIndex.get()+1);
        Map<FeedRangeEpkImpl, T> targetPartition = new HashMap<>();
        for (Pair<FeedRangeEpkImpl, T> p : pairs) {
            targetPartition.put(p.getLeft(), p.getRight());
        }

        Map<FeedRangeEpkImpl, T> partitionsRightOfTarget = new HashMap<>();
        if (matchedIndex.get() == sortedRanges.size() - 1) {
            // partitionsRightOfTarget should be empty
        } else {
            List<Pair<FeedRangeEpkImpl, T>> rPairs = sortedRanges.subList(matchedIndex.get() + 1, sortedRanges.size());
            for (Pair<FeedRangeEpkImpl, T> p : rPairs) {
                partitionsRightOfTarget.put(p.getLeft(), p.getRight());
            }
        }

        return new PartitionMapping<>(partitionsLeftOfTarget, targetPartition, partitionsRightOfTarget);
    }

    public static class PartitionMapping<T extends IPartitionedToken> {
        private final Map<FeedRangeEpkImpl, T> mappingLeftOfTarget;
        private final Map<FeedRangeEpkImpl, T> targetMapping;
        private final Map<FeedRangeEpkImpl, T> mappingRightOfTarget;

        public PartitionMapping(
            Map<FeedRangeEpkImpl, T> mappingLeftOfTarget,
            Map<FeedRangeEpkImpl, T> targetMapping,
            Map<FeedRangeEpkImpl, T> mappingRightOfTarget) {
            checkNotNull(mappingLeftOfTarget);
            checkNotNull(targetMapping);
            checkNotNull(mappingLeftOfTarget);
            this.mappingLeftOfTarget = mappingLeftOfTarget;
            this.targetMapping = targetMapping;
            this.mappingRightOfTarget = mappingRightOfTarget;
        }

        /**
         * Getter for property 'mappingLeftOfTarget'.
         *
         * @return Value for property 'mappingLeftOfTarget'.
         */
        public Map<FeedRangeEpkImpl, T> getMappingLeftOfTarget() {
            return mappingLeftOfTarget;
        }

        /**
         * Getter for property 'targetMapping'.
         *
         * @return Value for property 'targetMapping'.
         */
        public Map<FeedRangeEpkImpl, T> getTargetMapping() {
            return targetMapping;
        }

        /**
         * Getter for property 'mappingRightOfTarget'.
         *
         * @return Value for property 'mappingRightOfTarget'.
         */
        public Map<FeedRangeEpkImpl, T> getMappingRightOfTarget() {
            return mappingRightOfTarget;
        }
    }
}
