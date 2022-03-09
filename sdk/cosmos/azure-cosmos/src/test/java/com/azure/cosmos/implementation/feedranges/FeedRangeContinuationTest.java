// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.feedranges;

import com.azure.cosmos.implementation.routing.PartitionKeyInternal;
import com.azure.cosmos.implementation.routing.PartitionKeyInternalUtils;
import com.azure.cosmos.implementation.routing.Range;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class FeedRangeContinuationTest {
    @Test(groups = "unit")
    public void feedRangeCompositeContinuation_Epk_toJsonFromJson() {
        String continuationDummy = UUID.randomUUID().toString();
        Range<String> range = new Range<>("AA", "EE", true, false);
        FeedRangeEpkImpl feedRange = new FeedRangeEpkImpl(range);

        List<Range<String>> ranges = new ArrayList<>();
        ranges.add(new Range<>("AA", "BB", true, false));
        ranges.add(new Range<>("CC", "DD", true, false));

        String containerRid = "/cols/" + UUID.randomUUID().toString();

        FeedRangeCompositeContinuationImpl continuation = new FeedRangeCompositeContinuationImpl(
            containerRid,
            feedRange,
            ranges,
            continuationDummy
        );

        String representation = continuation.toJson();
        assertThat(representation)
            .isEqualTo(
                String.format(
                    "{\"V\":1," +
                        "\"Rid\":\"%s\"," +
                        "\"Continuation\":[" +
                        "{\"token\":\"%s\",\"range\":{\"min\":\"AA\",\"max\":\"BB\"}}," +
                        "{\"token\":\"%s\",\"range\":{\"min\":\"CC\",\"max\":\"DD\"}}" +
                        "]," +
                        "\"Range\":{\"min\":\"AA\",\"max\":\"EE\"}}",
                    containerRid,
                    continuationDummy,
                    continuationDummy));

        assertThat(FeedRangeContinuation.convert(representation))
            .isNotNull()
            .isInstanceOf(FeedRangeCompositeContinuationImpl.class);

        FeedRangeCompositeContinuationImpl continuationDeserialized =
            (FeedRangeCompositeContinuationImpl)FeedRangeContinuation.convert(representation);

        String representationAfterDeserialization = continuationDeserialized.toJson();
        assertThat(representationAfterDeserialization).isEqualTo(representation);
    }

    @Test(groups = "unit")
    public void feedRangeCompositeContinuation_PKRangeId_toJsonFromJson() {
        String continuationDummy = UUID.randomUUID().toString();
        String pkRangeId = UUID.randomUUID().toString();
        FeedRangePartitionKeyRangeImpl feedRange = new FeedRangePartitionKeyRangeImpl(pkRangeId);

        List<Range<String>> ranges = new ArrayList<>();
        ranges.add(new Range<>("AA", "BB", true, false));
        ranges.add(new Range<>("CC", "DD", true, false));

        String containerRid = "/cols/" + UUID.randomUUID().toString();

        FeedRangeCompositeContinuationImpl continuation = new FeedRangeCompositeContinuationImpl(
            containerRid,
            feedRange,
            ranges,
            continuationDummy
        );

        String representation = continuation.toJson();
        assertThat(representation)
            .isEqualTo(
                String.format(
                    "{\"V\":1," +
                        "\"Rid\":\"%s\"," +
                        "\"Continuation\":[" +
                        "{\"token\":\"%s\",\"range\":{\"min\":\"AA\",\"max\":\"BB\"}}," +
                        "{\"token\":\"%s\",\"range\":{\"min\":\"CC\",\"max\":\"DD\"}}" +
                        "]," +
                        "\"PKRangeId\":\"%s\"}",
                    containerRid,
                    continuationDummy,
                    continuationDummy,
                    pkRangeId));

        assertThat(FeedRangeContinuation.convert(representation))
            .isNotNull()
            .isInstanceOf(FeedRangeCompositeContinuationImpl.class);

        FeedRangeCompositeContinuationImpl continuationDeserialized =
            (FeedRangeCompositeContinuationImpl)FeedRangeContinuation.convert(representation);

        String representationAfterDeserialization = continuationDeserialized.toJson();
        assertThat(representationAfterDeserialization).isEqualTo(representation);
    }

    @Test(groups = "unit")
    public void feedRangeCompositeContinuation_PK_toJsonFromJson() {
        String continuationDummy = UUID.randomUUID().toString();
        PartitionKeyInternal partitionKey = PartitionKeyInternalUtils.createPartitionKeyInternal(
            "Test");
        FeedRangePartitionKeyImpl feedRange = new FeedRangePartitionKeyImpl(partitionKey);

        List<Range<String>> ranges = new ArrayList<>();
        ranges.add(new Range<>("AA", "BB", true, false));
        ranges.add(new Range<>("CC", "DD", true, false));

        String containerRid = "/cols/" + UUID.randomUUID().toString();

        FeedRangeCompositeContinuationImpl continuation = new FeedRangeCompositeContinuationImpl(
            containerRid,
            feedRange,
            ranges,
            continuationDummy
        );

        String representation = continuation.toJson();
        assertThat(representation)
            .isEqualTo(
                String.format(
                    "{\"V\":1," +
                        "\"Rid\":\"%s\"," +
                        "\"Continuation\":[" +
                        "{\"token\":\"%s\",\"range\":{\"min\":\"AA\",\"max\":\"BB\"}}," +
                        "{\"token\":\"%s\",\"range\":{\"min\":\"CC\",\"max\":\"DD\"}}" +
                        "]," +
                        "\"PK\":[\"Test\"]}",
                    containerRid,
                    continuationDummy,
                    continuationDummy));

        assertThat(FeedRangeContinuation.convert(representation))
            .isNotNull()
            .isInstanceOf(FeedRangeCompositeContinuationImpl.class);

        FeedRangeCompositeContinuationImpl continuationDeserialized =
            (FeedRangeCompositeContinuationImpl)FeedRangeContinuation.convert(representation);

        String representationAfterDeserialization = continuationDeserialized.toJson();
        assertThat(representationAfterDeserialization).isEqualTo(representation);
    }
}
