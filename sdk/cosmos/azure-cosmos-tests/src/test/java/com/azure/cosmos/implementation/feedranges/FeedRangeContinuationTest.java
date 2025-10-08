// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.feedranges;

import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.implementation.GoneException;
import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.PartitionKeyRange;
import com.azure.cosmos.implementation.RxDocumentClientImpl;
import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.implementation.caches.RxPartitionKeyRangeCache;
import com.azure.cosmos.implementation.query.CompositeContinuationToken;
import com.azure.cosmos.implementation.routing.PartitionKeyInternal;
import com.azure.cosmos.implementation.routing.PartitionKeyInternalUtils;
import com.azure.cosmos.implementation.routing.Range;
import org.mockito.Mockito;
import org.testng.annotations.Test;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;

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

    @Test(groups = "unit")
    public void feedRangeCompositeContinuation_split() {
        String continuationDummy = UUID.randomUUID().toString();
        PartitionKeyInternal partitionKey = PartitionKeyInternalUtils.createPartitionKeyInternal(
            "Test");
        FeedRangePartitionKeyImpl feedRange = new FeedRangePartitionKeyImpl(partitionKey);

        List<Range<String>> ranges = new ArrayList<>();
        ranges.add(new Range<>("AA", "DD", true, false));

        String containerRid = "/cols/" + UUID.randomUUID();

        FeedRangeCompositeContinuationImpl continuation = new FeedRangeCompositeContinuationImpl(
            containerRid,
            feedRange,
            ranges,
            continuationDummy
        );

        GoneException goneException = new GoneException("Test");
        BridgeInternal.setSubStatusCode(
            goneException,
            HttpConstants.SubStatusCodes.PARTITION_KEY_RANGE_GONE);

        RxDocumentClientImpl clientMock = Mockito.mock(RxDocumentClientImpl.class);
        RxPartitionKeyRangeCache cacheMock = Mockito.mock(RxPartitionKeyRangeCache.class);
        Mockito.when(clientMock.getPartitionKeyRangeCache()).thenReturn(cacheMock);

        List<PartitionKeyRange> childRanges = new ArrayList<>();
        childRanges.add(new PartitionKeyRange("1", "AA", "BB"));
        childRanges.add(new PartitionKeyRange("1", "BB", "DD"));

        Mockito.when(
            cacheMock.tryGetOverlappingRangesAsync(
                isNull(),
                eq(containerRid),
                any(),
                eq(true),
                isNull(),
                any())).thenReturn(Mono.just(new Utils.ValueHolder<>(childRanges)));

        continuation.handleFeedRangeGone(clientMock, goneException).block();
        assertThat(continuation.getCompositeContinuationTokens().size()).isEqualTo(2);
        CompositeContinuationToken token1 = continuation.getCompositeContinuationTokens().poll();
        CompositeContinuationToken token2 = continuation.getCompositeContinuationTokens().poll();

        // Validate new child partition tokens are added
        validateCompositeContinuationToken(
            token1,
            childRanges.get(0).toRange(),
            continuationDummy);

        validateCompositeContinuationToken(
            token2,
            childRanges.get(1).toRange(),
            continuationDummy);

        validateCompositeContinuationToken(
            continuation.getCurrentContinuationToken(),
            childRanges.get(0).toRange(),
            continuationDummy);
    }

    @Test(groups = "unit")
    public void feedRangeCompositeContinuation_merge() {
        String continuationDummy = UUID.randomUUID().toString();
        PartitionKeyInternal partitionKey = PartitionKeyInternalUtils.createPartitionKeyInternal(
            "Test");
        FeedRangePartitionKeyImpl feedRange = new FeedRangePartitionKeyImpl(partitionKey);

        List<Range<String>> ranges = new ArrayList<>();
        ranges.add(new Range<>("AA", "BB", true, false));
        ranges.add(new Range<>("CC", "DD", true, false));

        String containerRid = "/cols/" + UUID.randomUUID();

        FeedRangeCompositeContinuationImpl continuation = new FeedRangeCompositeContinuationImpl(
            containerRid,
            feedRange,
            ranges,
            continuationDummy
        );

        GoneException goneException = new GoneException("Test");
        BridgeInternal.setSubStatusCode(
            goneException,
            HttpConstants.SubStatusCodes.PARTITION_KEY_RANGE_GONE);

        RxDocumentClientImpl clientMock = Mockito.mock(RxDocumentClientImpl.class);
        RxPartitionKeyRangeCache cacheMock = Mockito.mock(RxPartitionKeyRangeCache.class);
        Mockito.when(clientMock.getPartitionKeyRangeCache()).thenReturn(cacheMock);

        List<PartitionKeyRange> parentRanges = new ArrayList<>();
        parentRanges.add(new PartitionKeyRange("3", "AA", "DD"));

        Mockito.when(
            cacheMock.tryGetOverlappingRangesAsync(
                isNull(),
                eq(containerRid),
                any(),
                eq(true),
                isNull(),
                any())).thenReturn(Mono.just(new Utils.ValueHolder<>(parentRanges)));

        continuation.handleFeedRangeGone(clientMock, goneException).block();
        assertThat(continuation.getCompositeContinuationTokens().size()).isEqualTo(2);
        CompositeContinuationToken token1 = continuation.getCompositeContinuationTokens().poll();
        CompositeContinuationToken token2 = continuation.getCompositeContinuationTokens().poll();

        // Validate no parent token is being added, child ranges will be kept
        validateCompositeContinuationToken(
            token1,
            ranges.get(0),
            continuationDummy);

        validateCompositeContinuationToken(
            token2,
            ranges.get(1),
            continuationDummy);

        validateCompositeContinuationToken(
            continuation.getCurrentContinuationToken(),
            ranges.get(0),
            continuationDummy);
    }

    private void validateCompositeContinuationToken(CompositeContinuationToken token, Range<String> matchedRange, String continuationToken) {
        assertThat(token.getRange().getMin()).isEqualTo(matchedRange.getMin());
        assertThat(token.getRange().getMax()).isEqualTo(matchedRange.getMax());
        assertThat(token.getToken()).isEqualTo(continuationToken);
    }
}
