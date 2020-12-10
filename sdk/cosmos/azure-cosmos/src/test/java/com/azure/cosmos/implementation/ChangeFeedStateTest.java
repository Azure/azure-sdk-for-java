// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation;

import com.azure.cosmos.implementation.changefeed.implementation.ChangeFeedMode;
import com.azure.cosmos.implementation.changefeed.implementation.ChangeFeedStartFromInternal;
import com.azure.cosmos.implementation.changefeed.implementation.ChangeFeedState;
import com.azure.cosmos.implementation.changefeed.implementation.ChangeFeedStateV1;
import com.azure.cosmos.implementation.feedranges.FeedRangeContinuation;
import com.azure.cosmos.implementation.feedranges.FeedRangePartitionKeyRangeImpl;
import com.azure.cosmos.implementation.routing.Range;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class ChangeFeedStateTest {
    @Test(groups = "unit")
    public void changeFeedState_startFromNow_PKRangeId_toJsonFromJson() throws IOException {
        String containerRid = "/cols/" + UUID.randomUUID().toString();
        String pkRangeId = UUID.randomUUID().toString();
        FeedRangePartitionKeyRangeImpl feedRange = new FeedRangePartitionKeyRangeImpl(pkRangeId);
        ChangeFeedStartFromInternal startFromSettings = ChangeFeedStartFromInternal.createFromNow();
        ChangeFeedState stateWithoutContinuation = new ChangeFeedStateV1(
            containerRid,
            feedRange,
            ChangeFeedMode.INCREMENTAL,
            startFromSettings,
            null);

        String representation = stateWithoutContinuation.toJson();
        assertThat(representation)
            .isEqualTo(
                String.format(
                    "{\"V\":0," +
                    "\"Rid\":\"%s\"," +
                    "\"Mode\":\"INCREMENTAL\"," +
                    "\"StartFrom\":{\"Type\":\"NOW\"}," +
                    "\"PKRangeId\":\"%s\"}",
                    containerRid,
                    pkRangeId));

        assertThat(ChangeFeedState.fromJson(representation))
            .isNotNull()
            .isInstanceOf(ChangeFeedStateV1.class);

        ChangeFeedStateV1 stateWithoutContinuationDeserialized =
            (ChangeFeedStateV1)ChangeFeedState.fromJson(representation);

        String representationAfterDeserialization = stateWithoutContinuationDeserialized.toJson();
        assertThat(representationAfterDeserialization).isEqualTo(representation);

        List<Range<String>> ranges = new ArrayList<>();
        ranges.add(new Range<>("AA", "BB", true, false));
        ranges.add(new Range<>("CC", "DD", true, false));

        String continuationDummy = UUID.randomUUID().toString();
        String continuationJson = String.format(
            "{\"V\":0," +
                "\"Rid\":\"%s\"," +
                "\"Continuation\":[" +
                "{\"token\":\"%s\",\"range\":{\"min\":\"AA\",\"max\":\"BB\"}}," +
                "{\"token\":\"%s\",\"range\":{\"min\":\"CC\",\"max\":\"DD\"}}" +
                "]," +
                "\"PKRangeId\":\"%s\"}",
            containerRid,
            continuationDummy,
            continuationDummy,
            pkRangeId);

        FeedRangeContinuation continuation = FeedRangeContinuation.convert(continuationJson);

        ChangeFeedState stateWithContinuation = stateWithoutContinuation.setContinuation(continuation);
        representation = stateWithContinuation.toJson();
        assertThat(representation)
            .isEqualTo(
                String.format(
                    "{\"V\":0," +
                        "\"Rid\":\"%s\"," +
                        "\"Mode\":\"INCREMENTAL\"," +
                        "\"StartFrom\":{\"Type\":\"NOW\"}," +
                        "\"Continuation\":%s}",
                    containerRid,
                    continuationJson));

        assertThat(ChangeFeedState.fromJson(representation))
            .isNotNull()
            .isInstanceOf(ChangeFeedStateV1.class);

        ChangeFeedStateV1 stateWithContinuationDeserialized =
            (ChangeFeedStateV1)ChangeFeedState.fromJson(representation);

        representationAfterDeserialization = stateWithContinuationDeserialized.toJson();
        assertThat(representationAfterDeserialization).isEqualTo(representation);
    }
}
