// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.changeFeed;

import com.azure.cosmos.implementation.PartitionKeyRange;
import com.azure.cosmos.implementation.changefeed.ChangeFeedContextClient;
import com.azure.cosmos.implementation.changefeed.Lease;
import com.azure.cosmos.implementation.changefeed.LeaseContainer;
import com.azure.cosmos.implementation.changefeed.LeaseManager;
import com.azure.cosmos.implementation.changefeed.implementation.PartitionSynchronizerImpl;
import com.azure.cosmos.implementation.feedranges.FeedRangeEpkImpl;
import com.azure.cosmos.implementation.routing.PartitionKeyInternalHelper;
import com.azure.cosmos.implementation.routing.Range;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.testng.annotations.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class PartitionSynchronizerImplTests {

    @Test
    public void createAllLeases() {
        ChangeFeedContextClient feedContextClientMock = Mockito.mock(ChangeFeedContextClient.class);
        LeaseContainer leaseContainerMock = Mockito.mock(LeaseContainer.class);
        LeaseManager leaseManagerMock = Mockito.mock(LeaseManager.class);

        PartitionSynchronizerImpl partitionSynchronizer = new PartitionSynchronizerImpl(
                feedContextClientMock,
                leaseContainerMock,
                leaseManagerMock,
                1);

        List<PartitionKeyRange> overlappingRanges = new ArrayList<>();
        overlappingRanges.add(new PartitionKeyRange("1", "AA", "BB"));
        overlappingRanges.add(new PartitionKeyRange("2", "BB", "CC"));

        Lease childLease1 = Lease.builder()
                    .id("TestLease-" + UUID.randomUUID())
                    .leaseToken("1")
                    .feedRange(new FeedRangeEpkImpl(new Range<>("AA", "BB", true, false)))
                    .buildPartitionBasedLease();
        Lease childLease2 = Lease.builder()
                .id("TestLease-" + UUID.randomUUID())
                .leaseToken("2")
                .feedRange(new FeedRangeEpkImpl(new Range<>("BB", "CC", true, false)))
                .buildPartitionBasedLease();

        when(feedContextClientMock.getOverlappingRanges(PartitionKeyInternalHelper.FullRange))
                .thenReturn(Mono.just(overlappingRanges));
        when(leaseContainerMock.getAllLeases()).thenReturn(Flux.empty());
        when(leaseManagerMock.createLeaseIfNotExist(overlappingRanges.get(0), null))
                .thenReturn(Mono.just(childLease1));
        when(leaseManagerMock.createLeaseIfNotExist(overlappingRanges.get(1), null))
                .thenReturn(Mono.just(childLease2));

        StepVerifier.create(partitionSynchronizer.createMissingLeases())
                .verifyComplete();

        verify(leaseManagerMock, times(1)).createLeaseIfNotExist(overlappingRanges.get(0), null);
        verify(leaseManagerMock, times(1)).createLeaseIfNotExist(overlappingRanges.get(1), null);
    }

    @Test
    public void createMissingLeases() {
        ChangeFeedContextClient feedContextClientMock = Mockito.mock(ChangeFeedContextClient.class);
        LeaseContainer leaseContainerMock = Mockito.mock(LeaseContainer.class);
        LeaseManager leaseManagerMock = Mockito.mock(LeaseManager.class);

        PartitionSynchronizerImpl partitionSynchronizer = new PartitionSynchronizerImpl(
                feedContextClientMock,
                leaseContainerMock,
                leaseManagerMock,
                1);

        List<PartitionKeyRange> overlappingRanges = new ArrayList<>();
        overlappingRanges.add(new PartitionKeyRange("1", "AA", "BB"));
        overlappingRanges.add(new PartitionKeyRange("2", "BB", "DD"));
        overlappingRanges.add(new PartitionKeyRange("3", "DD", "EE"));

        Lease childLease1 = Lease.builder()
                .id("TestLease-" + UUID.randomUUID())
                .leaseToken("1")
                .feedRange(new FeedRangeEpkImpl(new Range<>("AA", "BB", true, false)))
                .buildPartitionBasedLease();

        Lease childLease2 = Lease.builder()
                .id("TestLease-" + UUID.randomUUID())
                .leaseToken("BB-CC")
                .feedRange(new FeedRangeEpkImpl(new Range<>("BB", "CC", true, false)))
                .buildEpkBasedLease();

        Lease childLease3 = Lease.builder()
                .id("TestLease-" + UUID.randomUUID())
                .leaseToken("3")
                .feedRange(new FeedRangeEpkImpl(new Range<>("DD", "EE", true, false)))
                .buildPartitionBasedLease();

        when(feedContextClientMock.getOverlappingRanges(PartitionKeyInternalHelper.FullRange))
                .thenReturn(Mono.just(overlappingRanges));
        when(leaseContainerMock.getAllLeases()).thenReturn(Flux.fromIterable(Arrays.asList(childLease1, childLease2)));
        when(leaseManagerMock.createLeaseIfNotExist(overlappingRanges.get(2), null))
                .thenReturn(Mono.just(childLease3));

        StepVerifier.create(partitionSynchronizer.createMissingLeases())
                .verifyComplete();

        // Verify there is only new lease create for partition key range 3
        // partition key range 1: there is one partition based lease exists
        // partition key range 2: there is one epk based lease exists.
        ArgumentCaptor<PartitionKeyRange> partitionKeyRangeArgumentCaptor = ArgumentCaptor.forClass(PartitionKeyRange.class);
        ArgumentCaptor<String> continuationArgumentCaptor = ArgumentCaptor.forClass(String.class);
        verify(leaseManagerMock, times(1))
                .createLeaseIfNotExist(partitionKeyRangeArgumentCaptor.capture(), continuationArgumentCaptor.capture());
        assertThat(partitionKeyRangeArgumentCaptor.getAllValues().size()).isEqualTo(1);
        assertThat(partitionKeyRangeArgumentCaptor.getAllValues().get(0)).isEqualTo(overlappingRanges.get(2));
    }
}
