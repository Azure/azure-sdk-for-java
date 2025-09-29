// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.changefeed.pkversion;

import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.implementation.PartitionKeyRange;
import com.azure.cosmos.implementation.changefeed.ChangeFeedContextClient;
import com.azure.cosmos.implementation.changefeed.LeaseContainer;
import com.azure.cosmos.implementation.changefeed.LeaseManager;
import com.azure.cosmos.implementation.routing.PartitionKeyInternalHelper;
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
    private final String COLLECTION_RESOURCE_ID = "collection1";

    @Test
    public void createAllLeases() {
        ChangeFeedContextClient feedContextClientMock = Mockito.mock(ChangeFeedContextClient.class);
        CosmosAsyncContainer containerMock = Mockito.mock(CosmosAsyncContainer.class);
        LeaseContainer leaseContainerMock = Mockito.mock(LeaseContainer.class);
        LeaseManager leaseManagerMock = Mockito.mock(LeaseManager.class);

        PartitionSynchronizerImpl partitionSynchronizer = new PartitionSynchronizerImpl(
            feedContextClientMock,
            containerMock,
            leaseContainerMock,
            leaseManagerMock,
            1,
            1,
            COLLECTION_RESOURCE_ID,
            new ChangeFeedProcessorOptions(),
            "test-createAllLeases");

        List<PartitionKeyRange> overlappingRanges = new ArrayList<>();
        overlappingRanges.add(new PartitionKeyRange("1", "AA", "BB"));
        overlappingRanges.add(new PartitionKeyRange("2", "BB", "CC"));

        ServiceItemLease childLease1 = new ServiceItemLease()
            .withLeaseToken("1");
        childLease1.setId("TestLease-" + UUID.randomUUID());

        ServiceItemLease childLease2 = new ServiceItemLease()
            .withLeaseToken("2");
        childLease2.setId("TestLease-" + UUID.randomUUID());

        when(feedContextClientMock.getOverlappingRanges(PartitionKeyInternalHelper.FullRange, true))
            .thenReturn(Mono.just(overlappingRanges));
        when(leaseContainerMock.getAllLeases()).thenReturn(Flux.empty());
        when(leaseManagerMock.createLeaseIfNotExist(any(String.class), any()))
            .thenReturn(Mono.just(childLease1))
            .thenReturn(Mono.just(childLease2));

        StepVerifier.create(partitionSynchronizer.createMissingLeases())
            .verifyComplete();

        ArgumentCaptor<String> leaseTokenCaptor = ArgumentCaptor.forClass(String.class);
        verify(leaseManagerMock, times(2)).createLeaseIfNotExist(leaseTokenCaptor.capture(), any());
        List<String> capturedLeaseTokens = leaseTokenCaptor.getAllValues();
        Assertions.assertThat(capturedLeaseTokens.size()).isEqualTo(2);
        Assertions.assertThat(capturedLeaseTokens.get(0)).isEqualTo(overlappingRanges.get(0).getId());
        Assertions.assertThat(capturedLeaseTokens.get(1)).isEqualTo(overlappingRanges.get(1).getId());
    }

    @Test
    public void createMissingLeases() {
        ChangeFeedContextClient feedContextClientMock = Mockito.mock(ChangeFeedContextClient.class);
        CosmosAsyncContainer containerMock = Mockito.mock(CosmosAsyncContainer.class);
        LeaseContainer leaseContainerMock = Mockito.mock(LeaseContainer.class);
        LeaseManager leaseManagerMock = Mockito.mock(LeaseManager.class);

        PartitionSynchronizerImpl partitionSynchronizer = new PartitionSynchronizerImpl(
            feedContextClientMock,
            containerMock,
            leaseContainerMock,
            leaseManagerMock,
            1,
            1,
            COLLECTION_RESOURCE_ID,
            new ChangeFeedProcessorOptions(),
            "test-createMissingLeases");

        List<PartitionKeyRange> overlappingRanges = new ArrayList<>();
        overlappingRanges.add(new PartitionKeyRange("1", "AA", "BB"));
        overlappingRanges.add(new PartitionKeyRange("2", "BB", "DD"));
        overlappingRanges.add(new PartitionKeyRange("3", "DD", "EE"));

        ServiceItemLease childLease1 = new ServiceItemLease()
            .withLeaseToken("1");
        childLease1.setId("TestLease-" + UUID.randomUUID());

        ServiceItemLease childLease2 = new ServiceItemLease()
            .withLeaseToken("2");
        childLease2.setId("TestLease-" + UUID.randomUUID());

        ServiceItemLease childLease3 = new ServiceItemLease()
            .withLeaseToken("3");
        childLease3.setId("TestLease-" + UUID.randomUUID());

        when(feedContextClientMock.getOverlappingRanges(PartitionKeyInternalHelper.FullRange, true))
            .thenReturn(Mono.just(overlappingRanges));
        when(leaseContainerMock.getAllLeases()).thenReturn(Flux.fromIterable(Arrays.asList(childLease1, childLease2)));
        when(leaseManagerMock.createLeaseIfNotExist(any(String.class), any()))
            .thenReturn(Mono.just(childLease3));

        StepVerifier.create(partitionSynchronizer.createMissingLeases())
            .verifyComplete();

        ArgumentCaptor<String> leaseTokenCaptor = ArgumentCaptor.forClass(String.class);
        verify(leaseManagerMock, times(1)).createLeaseIfNotExist(leaseTokenCaptor.capture(), any());
        assertThat(leaseTokenCaptor.getAllValues().size()).isEqualTo(1);
        assertThat(leaseTokenCaptor.getAllValues().get(0)).isEqualTo(overlappingRanges.get(2).getId());
    }
}
