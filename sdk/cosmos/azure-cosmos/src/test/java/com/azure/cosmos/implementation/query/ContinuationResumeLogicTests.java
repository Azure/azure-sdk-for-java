// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.query;

import com.azure.cosmos.implementation.Strings;
import com.azure.cosmos.implementation.feedranges.FeedRangeEpkImpl;
import com.azure.cosmos.implementation.routing.Range;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class ContinuationResumeLogicTests {

    private static void validateInitializationInfo(
        Map<FeedRangeEpkImpl, IPartitionedToken> expectedLeftMapping,
        Map<FeedRangeEpkImpl, IPartitionedToken> expectedTargetMapping,
        Map<FeedRangeEpkImpl, IPartitionedToken> expectedRightMapping,
        List<FeedRangeEpkImpl> partitionKeyRanges,
        List<IPartitionedToken> partitionedTokens) {

        PartitionMapper.PartitionMapping<IPartitionedToken> partitionMapping =
            PartitionMapper.getPartitionMapping(partitionKeyRanges,
                                                partitionedTokens);

        assertThat(expectedLeftMapping).isEqualTo(partitionMapping.getMappingLeftOfTarget());
        assertThat(expectedTargetMapping).isEqualTo(partitionMapping.getTargetMapping());
        assertThat(expectedRightMapping).isEqualTo(partitionMapping.getMappingRightOfTarget());
    }

    private static FeedRangeEpkImpl combineRanges(FeedRangeEpkImpl range1, FeedRangeEpkImpl range2) {
        assert (range1 != null);
        assert (range2 != null);
        assertThat(range1.getRange().getMin().compareTo(range2.getRange().getMin())).isLessThan(0);
        assertThat(range1.getRange().getMax()).isEqualTo(range2.getRange().getMin());

        return new FeedRangeEpkImpl(new Range<>(range1.getRange().getMin(),
                                                range2.getRange().getMax(),
                                                true,
                                                false));
    }

    private static Map<FeedRangeEpkImpl, IPartitionedToken> mapping(
        FeedRangeEpkImpl feedRangeEpk,
        IPartitionedToken token) {

        Map<FeedRangeEpkImpl, IPartitionedToken> mapping = new HashMap<>();
        mapping.put(feedRangeEpk, token);
        return mapping;
    }

    private static Range<String> createRange(String min, String max) {
        return new Range<>(min, max, true, false);
    }

    @Test(groups = {"unit"})
    public void resumeEmptyStart() {
        Range<String> range = createRange(Strings.Emtpy, "A");
        FeedRangeEpkImpl range1 = new FeedRangeEpkImpl(range);
        FeedRangeEpkImpl range2 = new FeedRangeEpkImpl(createRange("A", "B"));
        FeedRangeEpkImpl range3 = new FeedRangeEpkImpl(createRange("B", "FF"));
        IPartitionedToken token = new CompositeContinuationToken(UUID.randomUUID().toString(),
                                                                 range);

        validateInitializationInfo(new HashMap<>(),
                                   mapping(range1, token),
                                   mapping(combineRanges(range2, range3), null),
                                   Arrays.asList(range1, range2, range3),
                                   Collections.singletonList(token));

    }

    @Test(groups = {"unit"})
    public void resumeMaxEnd() {
        FeedRangeEpkImpl range1 = new FeedRangeEpkImpl(createRange(Strings.Emtpy, "A"));
        FeedRangeEpkImpl range2 = new FeedRangeEpkImpl(createRange("A", "B"));
        Range<String> range = createRange("B", "FF");
        FeedRangeEpkImpl range3 = new FeedRangeEpkImpl(range);
        IPartitionedToken token = new CompositeContinuationToken(UUID.randomUUID().toString(),
                                                                 range);

        validateInitializationInfo(mapping(combineRanges(range1, range2), null),
                                   mapping(range3, token),
                                   new HashMap<>(),
                                   Arrays.asList(range1, range2, range3),
                                   Collections.singletonList(token));

    }

    @Test(groups = {"unit"})
    public void resumeLeftPartition() {
        Range<String> range = createRange(Strings.Emtpy, "A");
        FeedRangeEpkImpl range1 = new FeedRangeEpkImpl(range);
        FeedRangeEpkImpl range2 = new FeedRangeEpkImpl(createRange("A", "B"));
        FeedRangeEpkImpl range3 = new FeedRangeEpkImpl(createRange("B", "C"));
        IPartitionedToken token = new CompositeContinuationToken(UUID.randomUUID().toString(), range);

        validateInitializationInfo(new HashMap<>(),
                                   mapping(range1, token),
                                   mapping(combineRanges(range2, range3), null),
                                   Arrays.asList(range1, range2, range3),
                                   Collections.singletonList(token));

    }

    @Test(groups = {"unit"})
    public void resumeMiddlePartition() {
        Range<String> range = createRange("A", "B");
        FeedRangeEpkImpl range1 = new FeedRangeEpkImpl(createRange(Strings.Emtpy, "A"));
        FeedRangeEpkImpl range2 = new FeedRangeEpkImpl(range);
        FeedRangeEpkImpl range3 = new FeedRangeEpkImpl(createRange("B", "C"));
        IPartitionedToken token = new CompositeContinuationToken(UUID.randomUUID().toString(), range);

        validateInitializationInfo(mapping(range1, null),
                                   mapping(range2, token),
                                   mapping(range3, null),
                                   Arrays.asList(range1, range2, range3),
                                   Collections.singletonList(token));
    }

    @Test(groups = {"unit"})
    public void resumeRightPartition() {
        Range<String> range = createRange("B", "C");
        FeedRangeEpkImpl range1 = new FeedRangeEpkImpl(createRange(Strings.Emtpy, "A"));
        FeedRangeEpkImpl range2 = new FeedRangeEpkImpl(createRange("A", "B"));
        FeedRangeEpkImpl range3 = new FeedRangeEpkImpl(range);
        IPartitionedToken token = new CompositeContinuationToken(UUID.randomUUID().toString(), range);

        validateInitializationInfo(mapping(combineRanges(range1, range2), null),
                                   mapping(range3, token),
                                   new HashMap<>(),
                                   Arrays.asList(range1, range2, range3),
                                   Collections.singletonList(token));
    }

    @Test(groups = {"unit"})
    public void resumeOnMerge() {
        // Suppose that we read from range 1
        Range<String> range = createRange(Strings.Emtpy, "A");
        FeedRangeEpkImpl range1 = new FeedRangeEpkImpl(range);

        // Then Range 1 Merged with Range 2
        FeedRangeEpkImpl range2 = new FeedRangeEpkImpl(createRange("A", "B"));

        // And we have a continuation token for range 1
        IPartitionedToken token = new CompositeContinuationToken(UUID.randomUUID().toString(), range);

        // Then we should resume on range 1 with epk range filtering
        // and still have range 2 with null continuation.
        validateInitializationInfo(new HashMap<>(),
                                   mapping(range1, token),
                                   mapping(range2, null),
                                   Collections.singletonList(combineRanges(range1, range2)), /* merge occurs here */
                                   Collections.singletonList(token));
    }

    @Test(groups = {"unit"})
    public void resumeOnMerge_logicalPartition() {
        Range<String> range = createRange("C", "E");
        // Suppose that we read from range 2 with a logical partition key that hashes to D
        FeedRangeEpkImpl range2 = new FeedRangeEpkImpl(range);

        //Then Range 1
        FeedRangeEpkImpl range1 = new FeedRangeEpkImpl(createRange("A", "C"));

        FeedRangeEpkImpl range3 = new FeedRangeEpkImpl(createRange("E", "G"));

        // and we have a continuation token for range 2
        IPartitionedToken token = new CompositeContinuationToken(UUID.randomUUID().toString(), range);

        // Then we should resume on range 2 with epk range filtering
        // and still have range 1 and 3 with null continuation (but, since there is a logical partition key it won't match any results).
        validateInitializationInfo(mapping(range1, null),
                                   mapping(range2, token),
                                   mapping(range3, null),
                                   Arrays.asList(combineRanges(range1, range2), range3),
                                   Collections.singletonList(token));
    }

    @Test(groups = {"unit"})
    public void resumeOnSplit() {
        FeedRangeEpkImpl range1 = new FeedRangeEpkImpl(createRange("A", "C"));
        FeedRangeEpkImpl range2 = new FeedRangeEpkImpl(createRange("C", "E"));
        FeedRangeEpkImpl range3 = new FeedRangeEpkImpl(createRange("E", "F"));
        IPartitionedToken token = new CompositeContinuationToken(UUID.randomUUID().toString(), createRange("A", "E"));

        validateInitializationInfo(new HashMap<>(),
                                   mapping(combineRanges(range1, range2), token),
                                   mapping(range3, null),
                                   Arrays.asList(range1, range2, range3),
                                   Collections.singletonList(token));
    }

    @Test(groups = {"unit"})
    public void resumeOnSplit_logicalPartition() {
        // Suppose the partition spans epk range A to E
        // And the user send a query with partition key that hashes to C
        // The the token will look like:
        IPartitionedToken token = new CompositeContinuationToken(UUID.randomUUID().toString(), createRange("A", "E"));


        // Now suppose there is a split that creates two partitions A to B and B to E
        // Now C will map to the partition that goes from B to E
        FeedRangeEpkImpl range = new FeedRangeEpkImpl(createRange("B", "E"));

        validateInitializationInfo(new HashMap<>(),
                                   mapping(range, token),
                                   new HashMap<>(),
                                   Collections.singletonList(range),
                                   Collections.singletonList(token));
    }

    @Test(groups = {"unit"})
    public void resumeOnMultipleTokens() {
        FeedRangeEpkImpl range = new FeedRangeEpkImpl(createRange("A", "F"));
        Range<String> r1 = createRange("A", "C");
        Range<String> r2 = createRange("C", "E");

        IPartitionedToken token1 = new CompositeContinuationToken(UUID.randomUUID().toString(), r1);
        IPartitionedToken token2 = new CompositeContinuationToken(UUID.randomUUID().toString(), r2);

        Map<FeedRangeEpkImpl, IPartitionedToken> mapping = mapping(new FeedRangeEpkImpl(r2), token2);
        mapping.put(new FeedRangeEpkImpl(createRange("E", "F")), null);
        validateInitializationInfo(new HashMap<>(),
                                   mapping(new FeedRangeEpkImpl(r1), token1),
                                   mapping,
                                   Collections.singletonList(range),
                                   Arrays.asList(token1, token2));
    }

}
