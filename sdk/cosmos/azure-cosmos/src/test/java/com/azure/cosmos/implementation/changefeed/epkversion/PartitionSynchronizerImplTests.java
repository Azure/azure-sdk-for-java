// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.changefeed.epkversion;

import com.azure.cosmos.implementation.PartitionKeyRange;
import com.azure.cosmos.implementation.changefeed.ChangeFeedContextClient;
import com.azure.cosmos.implementation.changefeed.Lease;
import com.azure.cosmos.implementation.changefeed.LeaseContainer;
import com.azure.cosmos.implementation.changefeed.LeaseManager;
import com.azure.cosmos.implementation.changefeed.common.ChangeFeedMode;
import com.azure.cosmos.implementation.changefeed.common.ChangeFeedState;
import com.azure.cosmos.implementation.changefeed.pkversion.ServiceItemLease;
import com.azure.cosmos.implementation.feedranges.FeedRangeEpkImpl;
import com.azure.cosmos.implementation.routing.PartitionKeyInternalHelper;
import com.azure.cosmos.implementation.routing.Range;
import com.azure.cosmos.models.ChangeFeedProcessorOptions;
import org.assertj.core.api.Assertions;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class PartitionSynchronizerImplTests {
    @Test
    public void createAllLeases() {
        ChangeFeedContextClient feedContextClientMock = Mockito.mock(ChangeFeedContextClient.class);
        String feedContainerSelfLink = "/db/TestDB/coll/TestContainer";
        LeaseContainer leaseContainerMock = Mockito.mock(LeaseContainer.class);
        LeaseManager leaseManagerMock = Mockito.mock(LeaseManager.class);
        ChangeFeedProcessorOptions changeFeedProcessorOptions = Mockito.mock(ChangeFeedProcessorOptions.class);

        PartitionSynchronizerImpl partitionSynchronizer = new PartitionSynchronizerImpl(
                feedContextClientMock,
                feedContainerSelfLink,
                leaseContainerMock,
                leaseManagerMock,
                1,
                1,
                changeFeedProcessorOptions,
                ChangeFeedMode.INCREMENTAL);

        List<PartitionKeyRange> overlappingRanges = new ArrayList<>();
        overlappingRanges.add(new PartitionKeyRange("1", "AA", "BB"));
        overlappingRanges.add(new PartitionKeyRange("2", "BB", "CC"));

        ServiceItemLeaseV1 childLease1 =
                new ServiceItemLeaseV1()
                        .withLeaseToken("AA-BB")
                        .withFeedRange(new FeedRangeEpkImpl(new Range<>("AA", "BB", true, false)));
        childLease1.setId("TestLease-" + UUID.randomUUID());

        ServiceItemLeaseV1 childLease2 =
                new ServiceItemLeaseV1()
                        .withLeaseToken("BB-CC")
                        .withFeedRange(new FeedRangeEpkImpl(new Range<>("BB", "CC", true, false)));
        childLease1.setId("TestLease-" + UUID.randomUUID());

        when(feedContextClientMock.getOverlappingRanges(PartitionKeyInternalHelper.FullRange))
                .thenReturn(Mono.just(overlappingRanges));
        when(leaseContainerMock.getAllLeases()).thenReturn(Flux.empty());
        when(leaseManagerMock.createLeaseIfNotExist((FeedRangeEpkImpl) any(), any()))
                .thenReturn(Mono.just(childLease1))
                .thenReturn(Mono.just(childLease2));

        StepVerifier.create(partitionSynchronizer.createMissingLeases())
                .verifyComplete();

        ArgumentCaptor<FeedRangeEpkImpl> epkArgumentCaptor = ArgumentCaptor.forClass(FeedRangeEpkImpl.class);
        verify(leaseManagerMock, times(2)).createLeaseIfNotExist(epkArgumentCaptor.capture(), any());
        List<FeedRangeEpkImpl> capturedEpkArguments = epkArgumentCaptor.getAllValues();
        Assertions.assertThat(capturedEpkArguments.size()).isEqualTo(2);
        Assertions.assertThat(capturedEpkArguments.get(0).getRange()).isEqualTo(overlappingRanges.get(0).toRange());
        Assertions.assertThat(capturedEpkArguments.get(1).getRange()).isEqualTo(overlappingRanges.get(1).toRange());
    }

    @Test
    public void createMissingLeases() {
        ChangeFeedContextClient feedContextClientMock = Mockito.mock(ChangeFeedContextClient.class);
        String feedContainerSelfLink = "/db/TestDB/coll/TestContainer";
        LeaseContainer leaseContainerMock = Mockito.mock(LeaseContainer.class);
        LeaseManager leaseManagerMock = Mockito.mock(LeaseManager.class);
        ChangeFeedProcessorOptions changeFeedProcessorOptions = Mockito.mock(ChangeFeedProcessorOptions.class);

        PartitionSynchronizerImpl partitionSynchronizer = new PartitionSynchronizerImpl(
                feedContextClientMock,
                feedContainerSelfLink,
                leaseContainerMock,
                leaseManagerMock,
                1,
                1,
                changeFeedProcessorOptions,
                ChangeFeedMode.INCREMENTAL);

        List<PartitionKeyRange> overlappingRanges = new ArrayList<>();
        overlappingRanges.add(new PartitionKeyRange("1", "AA", "BB"));
        overlappingRanges.add(new PartitionKeyRange("2", "BB", "DD"));
        overlappingRanges.add(new PartitionKeyRange("3", "DD", "EE"));

        ServiceItemLeaseV1 childLease1 =
                new ServiceItemLeaseV1()
                        .withLeaseToken("AA-BB")
                        .withFeedRange(new FeedRangeEpkImpl(new Range<>("AA", "BB", true, false)));
        childLease1.setId("TestLease-" + UUID.randomUUID());

        ServiceItemLeaseV1 childLease2 =
                new ServiceItemLeaseV1()
                        .withLeaseToken("BB-DD")
                        .withFeedRange(new FeedRangeEpkImpl(new Range<>("BB", "DD", true, false)));
        childLease1.setId("TestLease-" + UUID.randomUUID());

        ServiceItemLeaseV1 childLease3 =
                new ServiceItemLeaseV1()
                        .withLeaseToken("DD-EE")
                        .withFeedRange(new FeedRangeEpkImpl(new Range<>("DD", "EE", true, false)));
        childLease1.setId("TestLease-" + UUID.randomUUID());

        when(feedContextClientMock.getOverlappingRanges(PartitionKeyInternalHelper.FullRange))
                .thenReturn(Mono.just(overlappingRanges));
        when(leaseContainerMock.getAllLeases()).thenReturn(Flux.fromIterable(Arrays.asList(childLease1, childLease2)));

        when(leaseManagerMock.createLeaseIfNotExist((FeedRangeEpkImpl) any(), any()))
                .thenReturn(Mono.just(childLease3));

        StepVerifier.create(partitionSynchronizer.createMissingLeases())
                .verifyComplete();

        // Verify there is only new lease create for partition key range 3
        // partition key range 1: there is one partition based lease exists
        // partition key range 2: there is one epk based lease exists.
        ArgumentCaptor<FeedRangeEpkImpl> feedRangeEpkArgumentCaptor = ArgumentCaptor.forClass(FeedRangeEpkImpl.class);
        verify(leaseManagerMock, times(1)).createLeaseIfNotExist(feedRangeEpkArgumentCaptor.capture(), any());
        assertThat(feedRangeEpkArgumentCaptor.getAllValues().size()).isEqualTo(1);
        assertThat(feedRangeEpkArgumentCaptor.getAllValues().get(0).getRange()).isEqualTo(overlappingRanges.get(2).toRange());
    }

    @Test(groups = "unit")
    public void createMissingLeasesFromPkRangeIdVersionLeases() {
        ChangeFeedContextClient feedContextClientMock = Mockito.mock(ChangeFeedContextClient.class);
        String feedContainerSelfLink = "/db/TestDB/coll/TestContainer";
        LeaseContainer leaseContainerMock = Mockito.mock(LeaseContainer.class);
        LeaseManager leaseManagerMock = Mockito.mock(LeaseManager.class);
        ChangeFeedProcessorOptions changeFeedProcessorOptions = Mockito.mock(ChangeFeedProcessorOptions.class);

        PartitionSynchronizerImpl partitionSynchronizer = new PartitionSynchronizerImpl(
            feedContextClientMock,
            feedContainerSelfLink,
            leaseContainerMock,
            leaseManagerMock,
            1,
            1,
            changeFeedProcessorOptions,
            ChangeFeedMode.INCREMENTAL);

        List<PartitionKeyRange> overlappingRanges = new ArrayList<>();
        overlappingRanges.add(new PartitionKeyRange("1", "AA", "BB"));
        overlappingRanges.add(new PartitionKeyRange("2", "BB", "DD"));
        overlappingRanges.add(new PartitionKeyRange("3", "DD", "EE"));

        List<Lease> pkRangeIdVersionLeases = new ArrayList<>();

        for (PartitionKeyRange partitionKeyRange : overlappingRanges) {
            ServiceItemLease pkRangeIdVersionLease =
                new ServiceItemLease()
                    .withLeaseToken(partitionKeyRange.getId())
                    .withETag(String.valueOf(partitionKeyRange.getId()));
            pkRangeIdVersionLease.setId("TestLease-" + UUID.randomUUID());
            pkRangeIdVersionLeases.add(pkRangeIdVersionLease);
        }

        when(feedContextClientMock.getOverlappingRanges(PartitionKeyInternalHelper.FullRange))
            .thenReturn(Mono.just(overlappingRanges));

        when(leaseContainerMock.getAllLeases()).thenReturn(Flux.empty());
        when(leaseManagerMock.createLeaseIfNotExist((FeedRangeEpkImpl) any(), any()))
            .thenReturn(Mono.empty());

        StepVerifier.create(partitionSynchronizer.createMissingLeases(pkRangeIdVersionLeases))
            .verifyComplete();

        // Verify the new lease will start from the lsn from pk version lease
        ArgumentCaptor<FeedRangeEpkImpl> feedRangeEpkArgumentCaptor = ArgumentCaptor.forClass(FeedRangeEpkImpl.class);
        ArgumentCaptor<String> continuationTokenArgumentCaptor = ArgumentCaptor.forClass(String.class);
        verify(leaseManagerMock, times(3))
            .createLeaseIfNotExist(feedRangeEpkArgumentCaptor.capture(), continuationTokenArgumentCaptor.capture());
        assertThat(feedRangeEpkArgumentCaptor.getAllValues().size()).isEqualTo(3);
        assertThat(continuationTokenArgumentCaptor.getAllValues().size()).isEqualTo(3);

        for (int i = 0; i < pkRangeIdVersionLeases.size(); i++) {
            assertThat(feedRangeEpkArgumentCaptor.getAllValues().get(i).getRange()).isEqualTo(overlappingRanges.get(i).toRange());
            ChangeFeedState changeFeedState = ChangeFeedState.fromString(continuationTokenArgumentCaptor.getAllValues().get(i));
            assertThat(changeFeedState.getFeedRange()).isInstanceOf(FeedRangeEpkImpl.class);
            assertThat(((FeedRangeEpkImpl)changeFeedState.getFeedRange()).getRange()).isEqualTo(overlappingRanges.get(i).toRange());
            assertThat(changeFeedState.getContinuation().getCurrentContinuationToken().getToken()).isEqualTo(pkRangeIdVersionLeases.get(i).getContinuationToken());
        }
    }
}
