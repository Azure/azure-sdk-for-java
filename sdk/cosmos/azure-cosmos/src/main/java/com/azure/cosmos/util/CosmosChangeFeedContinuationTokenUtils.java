// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.util;

import com.azure.cosmos.CosmosException;
import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.changefeed.common.ChangeFeedState;
import com.azure.cosmos.implementation.changefeed.common.ChangeFeedStateV1;
import com.azure.cosmos.implementation.feedranges.FeedRangeContinuation;
import com.azure.cosmos.implementation.feedranges.FeedRangeEpkImpl;
import com.azure.cosmos.implementation.query.CompositeContinuationToken;
import com.azure.cosmos.implementation.routing.Range;
import com.azure.cosmos.models.FeedRange;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

public final class CosmosChangeFeedContinuationTokenUtils {
    private CosmosChangeFeedContinuationTokenUtils() {}

    /***
     * Utility method to help extract continuation tokens for sub-feedRange
     * @param changeFeedContinuationToken the original change feed continuation token being returned from queryChangeFeed.
     * @return a map of sub-feedRange to its mapping continuation token string
     */
    public static Map<FeedRange, String> extractContinuationTokens(String changeFeedContinuationToken) {
        return extractContinuationTokens(changeFeedContinuationToken, -1);
    }

    /***
     * Utility method to help extract continuation tokens for sub-range
     * @param changeFeedContinuationToken the original change feed continuation token being returned from queryChangeFeed.
     * @param targetedContinuationCount the targeted continuation token count.
     *                                 Max will be capped by the count of sub-feedRanges included in the continuation token.
     *                                 Using -1 to extract continuations for each sub-feedRanges.
     *                                 Using null will be same as using -1.
     * @return a map of sub-feedRange to its mapping continuation token string
     */
    public static Map<FeedRange, String> extractContinuationTokens(
        String changeFeedContinuationToken,
        Integer targetedContinuationCount) {

        checkNotNull(changeFeedContinuationToken, "Argument 'changeFeedContinuationToken' cannot be null.");
        if (targetedContinuationCount == null) {
            targetedContinuationCount = -1;
        }

        final ChangeFeedState changeFeedState = ChangeFeedState.fromString(changeFeedContinuationToken);
        List<CompositeContinuationToken> allTokens = changeFeedState.extractContinuationTokens();
        allTokens.sort(new Comparator<CompositeContinuationToken>() {
            @Override
            public int compare(CompositeContinuationToken o1, CompositeContinuationToken o2) {
                return o1.getRange().getMin().compareTo(o2.getRange().getMin());
            }
        });

        Map<FeedRange, String> extractedContinuationTokenMap = new ConcurrentHashMap<>();
        int effectiveTargetedContinuationCount =
            targetedContinuationCount <= 0 ? allTokens.size() : Math.min(targetedContinuationCount, allTokens.size());
        List<List<CompositeContinuationToken>> segmentedTokens =
            getSegmentedTokens(allTokens, effectiveTargetedContinuationCount);
        for (List<CompositeContinuationToken> segmentedToken : segmentedTokens) {
            FeedRangeEpkImpl effectiveChildRange =
                new FeedRangeEpkImpl(
                    new Range<>(
                        segmentedToken.get(0).getRange().getMin(),
                        segmentedToken.get(segmentedToken.size()-1).getRange().getMax(),
                        segmentedToken.get(0).getRange().isMinInclusive(),
                        segmentedToken.get(segmentedToken.size()-1).getRange().isMaxInclusive()));

            ChangeFeedState newChildFeedRangeState = new ChangeFeedStateV1(
                changeFeedState.getContainerRid(),
                effectiveChildRange,
                changeFeedState.getMode(),
                changeFeedState.getStartFromSettings(),
                FeedRangeContinuation.create(
                    changeFeedState.getContainerRid(),
                    effectiveChildRange,
                    segmentedToken
                )
            );

            extractedContinuationTokenMap.put(effectiveChildRange, newChildFeedRangeState.toString());
        }

        return extractedContinuationTokenMap;
    }

    private static List<List<CompositeContinuationToken>> getSegmentedTokens(
        List<CompositeContinuationToken> allTokens,
        int targetedContinuationCount) {

        List<List<CompositeContinuationToken>> segmentedTokens = new ArrayList<>();
        int subListMinSize = allTokens.size() / targetedContinuationCount;
        int remainingSize = allTokens.size() % targetedContinuationCount;

        int subListStartIndex = 0;
        for (int i = 1; i <= targetedContinuationCount; i++) {
            int subListEndIndex = subListStartIndex + subListMinSize + (remainingSize > 0 ? 1 : 0);
            segmentedTokens.add(new ArrayList<>(allTokens.subList(subListStartIndex, subListEndIndex)));
            subListStartIndex = subListEndIndex;
            remainingSize--;
        }

        return segmentedTokens;
    }

    public static String extractContinuationTokenFromCosmosException(CosmosException ce) {
        Map<String, String> responseHeaders = ce.getResponseHeaders();
        return getValueOrNull(responseHeaders, HttpConstants.HttpHeaders.E_TAG);
    }

    private static String getValueOrNull(Map<String, String> map, String key) {
        if (map != null) {
            return map.get(key);
        }
        return null;
    }
}
