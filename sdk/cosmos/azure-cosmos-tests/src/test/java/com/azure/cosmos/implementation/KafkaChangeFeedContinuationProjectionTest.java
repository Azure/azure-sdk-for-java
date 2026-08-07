// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation;

import com.azure.cosmos.implementation.changefeed.common.ChangeFeedMode;
import com.azure.cosmos.implementation.changefeed.common.ChangeFeedStartFromInternal;
import com.azure.cosmos.implementation.changefeed.common.ChangeFeedState;
import com.azure.cosmos.implementation.changefeed.common.ChangeFeedStateV1;
import com.azure.cosmos.implementation.feedranges.FeedRangeContinuation;
import com.azure.cosmos.implementation.feedranges.FeedRangeEpkImpl;
import com.azure.cosmos.implementation.query.CompositeContinuationToken;
import com.azure.cosmos.implementation.routing.Range;
import com.azure.cosmos.models.CosmosChangeFeedRequestOptions;
import com.azure.cosmos.models.FeedRange;
import com.azure.cosmos.models.ModelBridgeInternal;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class KafkaChangeFeedContinuationProjectionTest {
    private static final String CONTAINER_RID = "containerRid";
    private static final Range<String> LOW_RANGE = new Range<>("", "80", true, false);
    private static final Range<String> HIGH_RANGE = new Range<>("80", "FF", true, false);
    private static final FeedRangeEpkImpl FULL_RANGE =
        new FeedRangeEpkImpl(new Range<>("", "FF", true, false));

    @Test(groups = "unit")
    public void nullItemLsnPreservesChildContinuations() {
        ChangeFeedState parentState = createParentState("2050", "1000");

        assertProjectedToken(parentState, new FeedRangeEpkImpl(LOW_RANGE), null, LOW_RANGE, "2050");
        assertProjectedToken(parentState, new FeedRangeEpkImpl(HIGH_RANGE), null, HIGH_RANGE, "1000");
    }

    @Test(groups = "unit")
    public void itemLsnOnlyOverridesOwningChildContinuation() {
        ChangeFeedState parentState = createParentState("2050", "1000");

        assertProjectedToken(parentState, new FeedRangeEpkImpl(LOW_RANGE), "2055", LOW_RANGE, "2055");
        assertProjectedToken(parentState, new FeedRangeEpkImpl(HIGH_RANGE), "2055", HIGH_RANGE, "1000");
    }

    @Test(groups = "unit")
    public void itemLsnOwnershipFollowsCurrentTokenRange() {
        ChangeFeedState parentState = createParentState(
            Arrays.asList(
                new CompositeContinuationToken("1000", HIGH_RANGE),
                new CompositeContinuationToken("2050", LOW_RANGE)));

        assertProjectedToken(parentState, new FeedRangeEpkImpl(LOW_RANGE), "1005", LOW_RANGE, "2050");
        assertProjectedToken(parentState, new FeedRangeEpkImpl(HIGH_RANGE), "1005", HIGH_RANGE, "1005");
    }

    @Test(groups = "unit")
    public void itemLsnOnlyOverridesCurrentTokenForFullRangeTarget() {
        ChangeFeedState parentState = createParentState(
            Arrays.asList(
                new CompositeContinuationToken("1000", HIGH_RANGE),
                new CompositeContinuationToken("2050", LOW_RANGE)));

        CosmosChangeFeedRequestOptions options =
            ImplementationBridgeHelpers.CosmosChangeFeedRequestOptionsHelper
                .getCosmosChangeFeedRequestOptionsAccessor()
                .createForProcessingFromContinuation(parentState.toString(), FULL_RANGE, "1005");
        List<CompositeContinuationToken> projectedTokens =
            ModelBridgeInternal.getChangeFeedContinuationState(options).extractContinuationTokens();

        assertThat(projectedTokens).hasSize(2);
        assertThat(projectedTokens.get(0).getRange()).isEqualTo(LOW_RANGE);
        assertThat(projectedTokens.get(0).getToken()).isEqualTo("2050");
        assertThat(projectedTokens.get(1).getRange()).isEqualTo(HIGH_RANGE);
        assertThat(projectedTokens.get(1).getToken()).isEqualTo("1005");
    }

    @Test(groups = "unit")
    public void extractedContinuationIsScopedToTargetChild() {
        ChangeFeedState parentState = createParentState("2050", "1000");

        String projectedContinuation = ImplementationBridgeHelpers.CosmosChangeFeedRequestOptionsHelper
            .getCosmosChangeFeedRequestOptionsAccessor()
            .extractContinuationForFeedRange(parentState.toString(), new FeedRangeEpkImpl(HIGH_RANGE));

        List<CompositeContinuationToken> projectedTokens =
            ChangeFeedState.fromString(projectedContinuation).extractContinuationTokens();
        assertThat(projectedTokens).hasSize(1);
        assertThat(projectedTokens.get(0).getRange()).isEqualTo(HIGH_RANGE);
        assertThat(projectedTokens.get(0).getToken()).isEqualTo("1000");
    }

    @Test(groups = "unit")
    public void nonOverlappingTargetFails() {
        ChangeFeedState parentState = createParentState("2050", "1000");
        FeedRange nonOverlappingRange =
            new FeedRangeEpkImpl(new Range<>("FF", "FFFF", true, false));

        assertThatThrownBy(() -> ImplementationBridgeHelpers.CosmosChangeFeedRequestOptionsHelper
            .getCosmosChangeFeedRequestOptionsAccessor()
            .createForProcessingFromContinuation(parentState.toString(), nonOverlappingRange, null))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("does not overlap");
    }

    private static void assertProjectedToken(
        ChangeFeedState parentState,
        FeedRange targetRange,
        String itemLsn,
        Range<String> expectedRange,
        String expectedToken) {

        CosmosChangeFeedRequestOptions options =
            ImplementationBridgeHelpers.CosmosChangeFeedRequestOptionsHelper
                .getCosmosChangeFeedRequestOptionsAccessor()
                .createForProcessingFromContinuation(parentState.toString(), targetRange, itemLsn);
        List<CompositeContinuationToken> projectedTokens =
            ModelBridgeInternal.getChangeFeedContinuationState(options).extractContinuationTokens();

        assertThat(projectedTokens).hasSize(1);
        assertThat(projectedTokens.get(0).getRange()).isEqualTo(expectedRange);
        assertThat(projectedTokens.get(0).getToken()).isEqualTo(expectedToken);
    }

    private static ChangeFeedState createParentState(String lowToken, String highToken) {
        return createParentState(
            Arrays.asList(
                new CompositeContinuationToken(lowToken, LOW_RANGE),
                new CompositeContinuationToken(highToken, HIGH_RANGE)));
    }

    private static ChangeFeedState createParentState(List<CompositeContinuationToken> tokens) {
        return new ChangeFeedStateV1(
            CONTAINER_RID,
            FULL_RANGE,
            ChangeFeedMode.INCREMENTAL,
            ChangeFeedStartFromInternal.createFromBeginning(),
            FeedRangeContinuation.create(
                CONTAINER_RID,
                FULL_RANGE,
                tokens));
    }
}
