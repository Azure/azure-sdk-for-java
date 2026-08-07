// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.kafka.connect.implementation.source;

import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.implementation.changefeed.common.ChangeFeedMode;
import com.azure.cosmos.implementation.changefeed.common.ChangeFeedStartFromInternal;
import com.azure.cosmos.implementation.changefeed.common.ChangeFeedState;
import com.azure.cosmos.implementation.changefeed.common.ChangeFeedStateV1;
import com.azure.cosmos.implementation.feedranges.FeedRangeContinuation;
import com.azure.cosmos.implementation.feedranges.FeedRangeEpkImpl;
import com.azure.cosmos.implementation.query.CompositeContinuationToken;
import com.azure.cosmos.implementation.routing.Range;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class KafkaCosmosChangeFeedStateTest {
    private static final Range<String> LOW_RANGE = new Range<>("", "80", true, false);
    private static final Range<String> HIGH_RANGE = new Range<>("80", "FF", true, false);
    private static final FeedRangeEpkImpl FULL_RANGE =
        new FeedRangeEpkImpl(new Range<>("", "FF", true, false));

    @Test(groups = "unit")
    public void extractForFeedRangePreservesChildContinuationAndDropsParentItemLsn() {
        KafkaCosmosChangeFeedState parentState = createParentState("2050");

        KafkaCosmosChangeFeedState highChild =
            parentState.extractForFeedRange(new FeedRangeEpkImpl(HIGH_RANGE));
        List<CompositeContinuationToken> childTokens =
            ChangeFeedState.fromString(highChild.getResponseContinuation()).extractContinuationTokens();

        assertThat(childTokens).hasSize(1);
        assertThat(childTokens.get(0).getRange()).isEqualTo(HIGH_RANGE);
        assertThat(childTokens.get(0).getToken()).isEqualTo("1000");
        assertThat(highChild.getItemLsn()).isNull();
    }

    @Test(groups = "unit")
    public void extractForExactFeedRangePreservesItemLsn() {
        KafkaCosmosChangeFeedState parentState = createParentState("2050");

        KafkaCosmosChangeFeedState exactState = parentState.extractForFeedRange(FULL_RANGE);

        assertThat(exactState.getItemLsn()).isEqualTo("2050");
        assertThat(ChangeFeedState.fromString(exactState.getResponseContinuation()).extractContinuationTokens())
            .hasSize(2);
    }

    @Test(groups = "unit")
    public void serializationPreservesNullItemLsn() throws Exception {
        KafkaCosmosChangeFeedState childState = createParentState(null)
            .extractForFeedRange(new FeedRangeEpkImpl(HIGH_RANGE));

        String json = Utils.getSimpleObjectMapper().writeValueAsString(childState);
        KafkaCosmosChangeFeedState deserialized =
            Utils.getSimpleObjectMapper().readValue(json, KafkaCosmosChangeFeedState.class);

        assertThat(deserialized.getItemLsn()).isNull();
        assertThat(deserialized).isEqualTo(childState);
    }

    private static KafkaCosmosChangeFeedState createParentState(String itemLsn) {
        ChangeFeedState parentState = new ChangeFeedStateV1(
            "containerRid",
            FULL_RANGE,
            ChangeFeedMode.INCREMENTAL,
            ChangeFeedStartFromInternal.createFromBeginning(),
            FeedRangeContinuation.create(
                "containerRid",
                FULL_RANGE,
                Arrays.asList(
                    new CompositeContinuationToken("2050", LOW_RANGE),
                    new CompositeContinuationToken("1000", HIGH_RANGE))));

        return new KafkaCosmosChangeFeedState(parentState.toString(), FULL_RANGE, itemLsn);
    }
}
