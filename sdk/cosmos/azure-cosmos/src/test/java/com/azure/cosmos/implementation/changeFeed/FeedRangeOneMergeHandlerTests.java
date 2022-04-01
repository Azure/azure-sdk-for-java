// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.changeFeed;

import com.azure.cosmos.implementation.PartitionKeyRange;
import com.azure.cosmos.implementation.changefeed.Lease;
import com.azure.cosmos.implementation.changefeed.LeaseManager;
import com.azure.cosmos.implementation.changefeed.implementation.FeedRangeGoneHandler.FeedRangeGoneMergeHandler;
import com.azure.cosmos.implementation.feedranges.FeedRangeEpkImpl;
import com.azure.cosmos.implementation.routing.Range;
import org.mockito.Mockito;
import org.testng.annotations.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class FeedRangeOneMergeHandlerTests {

    @Test(groups = "unit")
    public void mergeHandlerForPartitionKeyRangeBasedLease() {

        // Testing an imaginary scenario FeedRange "AA-BB" has been merged into "AA-CC"
        // To handle merge scenario, partition key based lease will be converted to epk based lease
        FeedRangeEpkImpl feedRangeForLeaseWithGoneException = new FeedRangeEpkImpl(
                new Range<>("AA", "BB", true, false));
        Lease leaseWithGoneException =
                Lease.builder()
                        .id("TestLease-" + UUID.randomUUID())
                        .leaseToken("0")
                        .feedRange(feedRangeForLeaseWithGoneException)
                        .buildPartitionBasedLease();

        Lease newLease =
                Lease.builder()
                        .id("TestLease-" + UUID.randomUUID())
                        .leaseToken("AA-CC")
                        .feedRange(new FeedRangeEpkImpl(new Range<>("AA", "CC", true, false)))
                        .buildEpkBasedLease();

        LeaseManager leaseManagerMock = Mockito.mock(LeaseManager.class);
        Mockito
                .when(leaseManagerMock.createLeaseIfNotExist(feedRangeForLeaseWithGoneException, null))
                .thenReturn(Mono.just(newLease));

        FeedRangeGoneMergeHandler mergeHandler = new FeedRangeGoneMergeHandler(
                leaseWithGoneException,
                new PartitionKeyRange("1", "AA", "CC"),
                leaseManagerMock
        );

        StepVerifier.create(mergeHandler.handlePartitionGone()).expectNext(newLease).verifyComplete();
        verify(leaseManagerMock, times(1))
                .createLeaseIfNotExist(feedRangeForLeaseWithGoneException, null);
        assertThat(mergeHandler.shouldDeleteCurrentLease()).isEqualTo(true);
    }

    @Test(groups = "unit")
    public void mergeHandlerForEpkBasedLease() {

        // Testing an imaginary scenario FeedRange "AA-BB" has been merged into "AA-CC"
        // For epk based lease, we are going to use the same to keep draining results
        FeedRangeEpkImpl feedRangeForLeaseWithGoneException = new FeedRangeEpkImpl(
                new Range<>("AA", "BB", true, false));
        Lease leaseWithGoneException =
                Lease.builder()
                        .id("TestLease-" + UUID.randomUUID())
                        .leaseToken("AA-BB")
                        .feedRange(feedRangeForLeaseWithGoneException)
                        .buildEpkBasedLease();

        LeaseManager leaseManagerMock = Mockito.mock(LeaseManager.class);
        FeedRangeGoneMergeHandler mergeHandler = new FeedRangeGoneMergeHandler(
                leaseWithGoneException,
                new PartitionKeyRange("1", "AA", "CC"),
                leaseManagerMock
        );

        StepVerifier.create(mergeHandler.handlePartitionGone()).expectNext(leaseWithGoneException).verifyComplete();
        verify(leaseManagerMock, never())
                .createLeaseIfNotExist(any(FeedRangeEpkImpl.class), anyString());
        assertThat(mergeHandler.shouldDeleteCurrentLease()).isEqualTo(false);
    }
}
