// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.changefeed.epkversion;

import com.azure.cosmos.implementation.PartitionKeyRange;
import com.azure.cosmos.implementation.changefeed.Lease;
import com.azure.cosmos.implementation.changefeed.LeaseManager;
import com.azure.cosmos.implementation.changefeed.epkversion.feedRangeGoneHandler.FeedRangeGoneSplitHandler;
import com.azure.cosmos.implementation.feedranges.FeedRangeEpkImpl;
import com.azure.cosmos.implementation.routing.Range;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.testng.annotations.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class FeedRangeGoneSplitHandlerTests {
    @Test(groups = "unit")
    public void splitHandlerForEpkBasedLease() {
        // Testing an imaginary scenario FeedRange "AA-CC" has been split into "''-BB", "BB-FF"
        // In this case, new child leases should be created for AA-BB, BB-CC
        ServiceItemLeaseV1 leaseWithGoneException =
                new ServiceItemLeaseV1()
                        .withLeaseToken("AA-CC")
                        .withFeedRange(new FeedRangeEpkImpl(new Range<>("AA", "CC", true, false)));
        leaseWithGoneException.setId("TestLease-" + UUID.randomUUID());

        List<PartitionKeyRange> childRanges = new ArrayList<>();
        // using a min less than AA to check we are using the min of the lease with gone exception
        childRanges.add(new PartitionKeyRange("1", "", "BB"));
        // using a max larger than CC to check we are using the max of the lease with gone exception
        childRanges.add(new PartitionKeyRange("2", "BB", "FF"));

        List<Lease> expectedChildLeases = new ArrayList<>();
        ServiceItemLeaseV1 childLease1 =
                new ServiceItemLeaseV1()
                        .withLeaseToken("AA-BB")
                        .withFeedRange(new FeedRangeEpkImpl(new Range<>("AA", "BB", true, false)));
        childLease1.setId("TestLease-" + UUID.randomUUID());
        expectedChildLeases.add(childLease1);

        ServiceItemLeaseV1 childLease2 =
                new ServiceItemLeaseV1()
                        .withLeaseToken("BB-CC")
                        .withFeedRange(new FeedRangeEpkImpl(new Range<>("BB", "CC", true, false)));
        childLease1.setId("TestLease-" + UUID.randomUUID());
        expectedChildLeases.add(childLease2);

        LeaseManager leaseManagerMock = Mockito.mock(LeaseManager.class);

        Mockito.when(leaseManagerMock.createLeaseIfNotExist(Mockito.any(FeedRangeEpkImpl.class), Mockito.any()))
                .thenReturn(Mono.just(expectedChildLeases.get(0)))
                .thenReturn(Mono.just(expectedChildLeases.get(1)));

        FeedRangeGoneSplitHandler splitHandler = new FeedRangeGoneSplitHandler(
                leaseWithGoneException,
                childRanges,
                leaseManagerMock
        );

        StepVerifier
                .create(splitHandler.handlePartitionGone())
                .expectNext(expectedChildLeases.get(0))
                .expectNext(expectedChildLeases.get(1))
                .verifyComplete();

        ArgumentCaptor<FeedRangeEpkImpl> epkArgumentCaptor = ArgumentCaptor.forClass(FeedRangeEpkImpl.class);
        ArgumentCaptor<String> continuationTokenArgumentCapture = ArgumentCaptor.forClass(String.class);

        verify(leaseManagerMock, times(2))
                .createLeaseIfNotExist(epkArgumentCaptor.capture(), continuationTokenArgumentCapture.capture());
        List<FeedRangeEpkImpl> capturedEpkArguments = epkArgumentCaptor.getAllValues();
        assertThat(capturedEpkArguments.size()).isEqualTo(2);
        assertThat(capturedEpkArguments.get(0)).isEqualTo(expectedChildLeases.get(0).getFeedRange());
        assertThat(capturedEpkArguments.get(1)).isEqualTo(expectedChildLeases.get(1).getFeedRange());

        assertThat(splitHandler.shouldDeleteCurrentLease()).isEqualTo(true);
    }
}
