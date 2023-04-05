// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.changefeed.epkversion;

import com.azure.cosmos.implementation.PartitionKeyRange;
import com.azure.cosmos.implementation.changefeed.epkversion.feedRangeGoneHandler.FeedRangeGoneMergeHandler;
import com.azure.cosmos.implementation.feedranges.FeedRangeEpkImpl;
import com.azure.cosmos.implementation.routing.Range;
import org.testng.annotations.Test;
import reactor.test.StepVerifier;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
public class FeedRangeOneMergeHandlerTests {

    @Test(groups = "unit")
    public void mergeHandlerForEpkBasedLease() {

        // Testing an imaginary scenario FeedRange "AA-BB" has been merged into "AA-CC"
        // For epk based lease, we are going to use the same to keep draining results
        FeedRangeEpkImpl feedRangeForLeaseWithGoneException = new FeedRangeEpkImpl(
                new Range<>("AA", "BB", true, false));

        ServiceItemLeaseV1 leaseWithGoneException =
                new ServiceItemLeaseV1()
                        .withLeaseToken("AA-BB")
                        .withFeedRange(feedRangeForLeaseWithGoneException);
        leaseWithGoneException.setId("TestLease-" + UUID.randomUUID());

        FeedRangeGoneMergeHandler mergeHandler = new FeedRangeGoneMergeHandler(
                leaseWithGoneException,
                new PartitionKeyRange("1", "AA", "CC"));

        StepVerifier.create(mergeHandler.handlePartitionGone()).expectNext(leaseWithGoneException).verifyComplete();
        assertThat(mergeHandler.shouldDeleteCurrentLease()).isEqualTo(false);
    }
}
