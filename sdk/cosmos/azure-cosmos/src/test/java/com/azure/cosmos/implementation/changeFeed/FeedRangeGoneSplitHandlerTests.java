// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.changeFeed;

import com.azure.cosmos.implementation.PartitionKeyRange;
import com.azure.cosmos.implementation.changefeed.Lease;
import com.azure.cosmos.implementation.changefeed.LeaseManager;
import com.azure.cosmos.implementation.changefeed.implementation.FeedRangeGoneHandler.FeedRangeGoneSplitHandler;
import com.azure.cosmos.implementation.feedranges.FeedRangeEpkImpl;
import com.azure.cosmos.implementation.feedranges.FeedRangeInternal;
import com.azure.cosmos.implementation.routing.Range;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.testng.annotations.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class FeedRangeGoneSplitHandlerTests {

    @Test(groups = "unit")
    public void splitHandlerForPartitionKeyRangeBasedLease() {
        // Testing an imaginary scenario FeedRange "AA-CC" has been split into "AA-BB", "BB-CC"
        // To handle split scenario, new partition based lease will be created for each child range

        Lease leaseWithGoneException =
                Lease.builder()
                        .id("TestLease-" + UUID.randomUUID())
                        .leaseToken("0")
                        .feedRange(new FeedRangeEpkImpl(new Range<>("AA", "CC", true, false)))
                        .buildPartitionBasedLease();

        List<PartitionKeyRange> childRanges = new ArrayList<>();
        childRanges.add(new PartitionKeyRange("1", "AA", "BB"));
        childRanges.add(new PartitionKeyRange("2", "BB", "CC"));

        List<Lease> expectedChildLeases = new ArrayList<>();
        childRanges.forEach(pkRange -> {
            FeedRangeInternal feedRangeInternal =
                    new FeedRangeEpkImpl(
                            new Range<>(pkRange.getMinInclusive(), pkRange.getMaxExclusive(), true, false));
            expectedChildLeases.add(
                    Lease.builder()
                            .id("TestLease-" + UUID.randomUUID())
                            .leaseToken(pkRange.getId())
                            .feedRange(feedRangeInternal)
                            .buildPartitionBasedLease());
        });

        LeaseManager leaseManagerMock = Mockito.mock(LeaseManager.class);

        Mockito.when(leaseManagerMock.createLeaseIfNotExist(Mockito.any(PartitionKeyRange.class), Mockito.any()))
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

        ArgumentCaptor<PartitionKeyRange> epkArgumentCaptor = ArgumentCaptor.forClass(PartitionKeyRange.class);
        ArgumentCaptor<String> continuationTokenArgumentCapture = ArgumentCaptor.forClass(String.class);

        verify(leaseManagerMock, times(2))
                .createLeaseIfNotExist(epkArgumentCaptor.capture(), continuationTokenArgumentCapture.capture());
        List<PartitionKeyRange> capturedEpkArguments = epkArgumentCaptor.getAllValues();
        assertThat(capturedEpkArguments.size()).isEqualTo(2);
        assertThat(capturedEpkArguments.get(0)).isEqualTo(childRanges.get(0));
        assertThat(capturedEpkArguments.get(1)).isEqualTo(childRanges.get(1));

        assertThat(splitHandler.shouldDeleteCurrentLease()).isEqualTo(true);
    }

    @Test(groups = "unit")
    public void splitHandlerForEpkBasedLease() {
        // Testing an imaginary scenario FeedRange "AA-CC" has been split into "''-BB", "BB-FF"
        // In this case, new child leases should be created for AA-BB, BB-CC
        Lease leaseWithGoneException =
                Lease.builder()
                        .id("TestLease-" + UUID.randomUUID())
                        .leaseToken("AA-CC")
                        .feedRange(new FeedRangeEpkImpl(new Range<>("AA", "CC", true, false)))
                        .buildEpkBasedLease();

        List<PartitionKeyRange> childRanges = new ArrayList<>();
        // using a min less than AA to check we are using the min of the lease with gone exception
        childRanges.add(new PartitionKeyRange("1", "", "BB"));
        // using a max larger than CC to check we are using the max of the lease with gone exception
        childRanges.add(new PartitionKeyRange("2", "BB", "FF"));

        List<Lease> expectedChildLeases = new ArrayList<>();
        expectedChildLeases.add(
                Lease.builder()
                        .id("TestLease-" + UUID.randomUUID())
                        .leaseToken("AA-BB")
                        .feedRange(new FeedRangeEpkImpl(new Range<>("AA", "BB", true, false)))
                        .buildEpkBasedLease());
        expectedChildLeases.add(
                Lease.builder()
                        .id("TestLease-" + UUID.randomUUID())
                        .leaseToken("BB-CC")
                        .feedRange(new FeedRangeEpkImpl(new Range<>("BB", "CC", true, false)))
                        .buildEpkBasedLease());

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
