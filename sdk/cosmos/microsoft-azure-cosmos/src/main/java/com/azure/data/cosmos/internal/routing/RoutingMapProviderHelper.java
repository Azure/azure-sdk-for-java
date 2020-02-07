// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.cosmos.internal.routing;

import com.azure.data.cosmos.internal.IRoutingMapProvider;
import com.azure.data.cosmos.internal.PartitionKeyRange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.ListIterator;

/**
 * Provide utility functionality to route request in direct connectivity mode in the Azure Cosmos DB database service.
 */
public final class RoutingMapProviderHelper {
    private static final Range.MaxComparator<String> MAX_COMPARATOR = new Range.MaxComparator<String>();

    private static String max(String left, String right) {
        return left.compareTo(right) < 0 ? right : left;
    }

    private static <T extends Comparable<T>> boolean isSortedAndNonOverlapping(List<Range<T>> list) {
        for (int i = 1; i < list.size(); i++) {
            Range<T> previousRange = list.get(i - 1);
            Range<T> currentRange = list.get(i);

            int compareResult = previousRange.getMax().compareTo(currentRange.getMin());
            if (compareResult > 0) {
                return false;
            } else if (compareResult == 0 && previousRange.isMaxInclusive() && currentRange.isMinInclusive()) {
                return false;
            }
        }

        return true;
    }

    public static Collection<PartitionKeyRange> getOverlappingRanges(RoutingMapProvider routingMapProvider,
            String collectionSelfLink, List<Range<String>> sortedRanges) {
        if (!isSortedAndNonOverlapping(sortedRanges)) {
            throw new IllegalArgumentException("sortedRanges");
        }

        List<PartitionKeyRange> targetRanges = new ArrayList<PartitionKeyRange>();
        int currentProvidedRange = 0;
        while (currentProvidedRange < sortedRanges.size()) {
            if (sortedRanges.get(currentProvidedRange).isEmpty()) {
                currentProvidedRange++;
                continue;
            }

            Range<String> queryRange;
            if (!targetRanges.isEmpty()) {
                String left = max(targetRanges.get(targetRanges.size() - 1).getMaxExclusive(),
                        sortedRanges.get(currentProvidedRange).getMin());

                boolean leftInclusive = left.compareTo(sortedRanges.get(currentProvidedRange).getMin()) == 0
                        ? sortedRanges.get(currentProvidedRange).isMinInclusive() : false;

                queryRange = new Range<String>(left, sortedRanges.get(currentProvidedRange).getMax(), leftInclusive,
                        sortedRanges.get(currentProvidedRange).isMaxInclusive());
            } else {
                queryRange = sortedRanges.get(currentProvidedRange);
            }

            targetRanges.addAll(routingMapProvider.getOverlappingRanges(collectionSelfLink, queryRange, false));

            Range<String> lastKnownTargetRange = targetRanges.get(targetRanges.size() - 1).toRange();
            while (currentProvidedRange < sortedRanges.size()
                    && MAX_COMPARATOR.compare(sortedRanges.get(currentProvidedRange), lastKnownTargetRange) <= 0) {
                currentProvidedRange++;
            }
        }

        return targetRanges;
    }

    public static Mono<List<PartitionKeyRange>> getOverlappingRanges(IRoutingMapProvider routingMapProvider,
                                                                     String resourceId, List<Range<String>> sortedRanges) {

        if (routingMapProvider == null){
            throw new IllegalArgumentException("routingMapProvider");
        }

        if (sortedRanges == null) {
            throw new IllegalArgumentException("sortedRanges");
        }

        if (!isSortedAndNonOverlapping(sortedRanges)) {
            throw new IllegalArgumentException("sortedRanges");
        }

        List<PartitionKeyRange> targetRanges = new ArrayList<>();
        final ListIterator<Range<String>> iterator = sortedRanges.listIterator();

        return Flux.defer(() -> {
            if (!iterator.hasNext()) {
                return Flux.empty();
            }

            Range<String> queryRange;
            Range<String> sortedRange = iterator.next();
            if (!targetRanges.isEmpty()) {
                String left = max(targetRanges.get(targetRanges.size() - 1).getMaxExclusive(),
                    sortedRange.getMin());

                boolean leftInclusive = left.compareTo(sortedRange.getMin()) == 0 && sortedRange.isMinInclusive();

                queryRange = new Range<String>(left, sortedRange.getMax(), leftInclusive,
                    sortedRange.isMaxInclusive());
            } else {
                queryRange = sortedRange;
            }

            return routingMapProvider.tryGetOverlappingRangesAsync(resourceId, queryRange, false, null)
                                     .map(ranges -> ranges.v)
                                     .map(targetRanges::addAll)
                                     .flatMap(aBoolean -> {
                                         if (!targetRanges.isEmpty()) {
                                             Range<String> lastKnownTargetRange = targetRanges.get(targetRanges.size() - 1).toRange();
                                             while (iterator.hasNext()) {
                                                 Range<String> value = iterator.next();
                                                 if (MAX_COMPARATOR.compare(value, lastKnownTargetRange) > 0) {
                                                     // Since we already moved forward on iterator to check above condition, we
                                                     // go to previous when it fails so the the value is not skipped on iteration
                                                     iterator.previous();
                                                     break;
                                                 }
                                             }
                                         }
                                         return Mono.just(targetRanges);
                                     }).flux();
        }).repeat(sortedRanges.size())
                   .takeUntil(stringRange -> !iterator.hasNext())
                   .last()
                   .single();
    }
}
