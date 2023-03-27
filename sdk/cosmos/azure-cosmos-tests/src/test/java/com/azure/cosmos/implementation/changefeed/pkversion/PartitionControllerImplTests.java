// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.changefeed.pkversion;

import com.azure.cosmos.implementation.changefeed.LeaseContainer;
import com.azure.cosmos.implementation.changefeed.LeaseManager;
import com.azure.cosmos.implementation.changefeed.PartitionSupervisor;
import com.azure.cosmos.implementation.changefeed.PartitionSupervisorFactory;
import com.azure.cosmos.implementation.changefeed.exceptions.FeedRangeGoneException;
import org.mockito.Mockito;
import org.testng.annotations.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class PartitionControllerImplTests {
    @Test(groups = "unit")
    public void handleSplit() throws InterruptedException {
        LeaseContainer leaseContainer = Mockito.mock(LeaseContainer.class);
        when(leaseContainer.getOwnedLeases()).thenReturn(Flux.empty());

        LeaseManager leaseManager = Mockito.mock(LeaseManager.class);
        PartitionSupervisorFactory partitionSupervisorFactory = Mockito.mock(PartitionSupervisorFactory.class);
        PartitionSynchronizer synchronizer = Mockito.mock(PartitionSynchronizer.class);
        Scheduler scheduler = Schedulers.boundedElastic();

        PartitionControllerImpl partitionController =
               new PartitionControllerImpl(
                        leaseContainer,
                        leaseManager,
                        partitionSupervisorFactory,
                        synchronizer,
                        scheduler);

        ServiceItemLease lease = new ServiceItemLease().withLeaseToken("1");
        lease.setId("TestLease-" + UUID.randomUUID());

        ServiceItemLease childLease1 = new ServiceItemLease().withLeaseToken("2");
        lease.setId("TestLease-" + UUID.randomUUID());

        ServiceItemLease childLease2 = new ServiceItemLease().withLeaseToken("3");
        lease.setId("TestLease-" + UUID.randomUUID());

        Mockito.when(synchronizer.splitPartition(lease)).thenReturn(Flux.fromIterable(Arrays.asList(childLease1, childLease2)));

        this.setDefaultLeaseManagerBehavior(leaseManager, Arrays.asList(lease,childLease1, childLease2));

        this.setPartitionSupervisorWithFeedRangeGoneBehavior(partitionSupervisorFactory, Arrays.asList(lease));
        this.setDefaultPartitionSupervisorBehavior(partitionSupervisorFactory, Arrays.asList(childLease1, childLease2));

        StepVerifier.create(partitionController.initialize()).verifyComplete();
        StepVerifier.create(partitionController.addOrUpdateLease(lease))
                .expectNext(lease)
                .verifyComplete();

        // addOrUpdateLease for childLease1 and childLease2 are executed async
        // add some waiting time here so that we can capture all the calls
        Thread.sleep(500);

        // Verify total three leases are acquired
        verify(leaseManager, times(1)).acquire(lease);
        verify(leaseManager, times(1)).acquire(childLease1);
        verify(leaseManager, times(1)).acquire(childLease2);

        // Verify partitionSupervisor is created for each lease
        verify(partitionSupervisorFactory, times(1)).create(lease);
        verify(partitionSupervisorFactory, times(1)).create(childLease1);
        verify(partitionSupervisorFactory, times(1)).create(childLease2);

        // Verify only the lease with feedRangeGone exception will be deleted from lease container
        verify(leaseManager, times(1)).delete(lease);
        verify(leaseManager, Mockito.never()).delete(childLease1);
        verify(leaseManager, Mockito.never()).delete(childLease2);

        // Verify at the end, all the leases will be released
        verify(leaseManager, times(1)).release(lease);
        verify(leaseManager, times(1)).release(childLease1);
        verify(leaseManager, times(1)).release(childLease2);

        verify(leaseManager, Mockito.never()).updateProperties(Mockito.any());
    }

    private void setDefaultLeaseManagerBehavior(LeaseManager leaseManager, List<ServiceItemLease> leases) {
        leases.forEach(l -> {
            when(leaseManager.acquire(l)).thenReturn(Mono.just(l));
            when(leaseManager.delete(l)).thenReturn(Mono.empty());
            when(leaseManager.release(l)).thenReturn(Mono.empty());
            when(leaseManager.updateProperties(l)).thenReturn(Mono.just(l));
        });
    }

    private void setDefaultPartitionSupervisorBehavior(PartitionSupervisorFactory factory, List<ServiceItemLease> leases) {
        leases.forEach(lease -> {
            PartitionSupervisor partitionSupervisor = Mockito.mock(PartitionSupervisor.class);
            when(factory.create(lease)).thenReturn(partitionSupervisor);
            doNothing().when(partitionSupervisor).shutdown();

            when(partitionSupervisor.run(Mockito.any())).thenReturn(Mono.empty());
        });
    }

    private void setPartitionSupervisorWithFeedRangeGoneBehavior(PartitionSupervisorFactory factory, List<ServiceItemLease> leases) {
        leases.forEach(lease -> {
            PartitionSupervisor partitionSupervisor = Mockito.mock(PartitionSupervisor.class);
            when(factory.create(lease)).thenReturn(partitionSupervisor);
            doNothing().when(partitionSupervisor).shutdown();

            when(partitionSupervisor.run(Mockito.any()))
                    .thenReturn(Mono.error(new FeedRangeGoneException("Test", null)));
        });
    }
}
