// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.changefeed.epkversion;

import com.azure.cosmos.implementation.changefeed.LeaseContainer;
import com.azure.cosmos.implementation.changefeed.LeaseManager;
import com.azure.cosmos.implementation.changefeed.PartitionSupervisor;
import com.azure.cosmos.implementation.changefeed.PartitionSupervisorFactory;
import com.azure.cosmos.implementation.changefeed.epkversion.feedRangeGoneHandler.FeedRangeGoneHandler;
import com.azure.cosmos.implementation.changefeed.exceptions.FeedRangeGoneException;
import com.azure.cosmos.implementation.changefeed.exceptions.TaskCancelledException;
import com.azure.cosmos.implementation.feedranges.FeedRangeEpkImpl;
import com.azure.cosmos.implementation.routing.Range;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.DataProvider;
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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class PartitionControllerImplTests {

    @DataProvider(name = "shouldSkipDirectLeaseAssignmentArgProvider")
    public static Object[][] shouldSkipDirectLeaseAssignmentArgProvider() {
        return new Object[][]{
            // shouldSkipDirectLeaseAssignment
            { true },
            { false }
        };
    }

    private AutoCloseable mocksCloseable;

    @BeforeTest
    public void openMocks() {
        mocksCloseable = MockitoAnnotations.openMocks(this);
    }

    @AfterTest
    public void cleanupMocks() throws Exception {
        if (mocksCloseable != null) {
            mocksCloseable.close();
        }
    }

    @Test(groups = "unit", dataProvider = "shouldSkipDirectLeaseAssignmentArgProvider")
    public void handleSplit(boolean shouldSkipDirectLeaseAssignment) throws InterruptedException {
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

        ServiceItemLeaseV1 lease =
                new ServiceItemLeaseV1()
                        .withLeaseToken("AA-CC")
                        .withFeedRange(new FeedRangeEpkImpl(new Range<>("AA", "CC", true, false)));
        lease.setId("TestLease-" + UUID.randomUUID());

        ServiceItemLeaseV1 childLease1 =
                new ServiceItemLeaseV1()
                        .withLeaseToken("AA-BB")
                        .withFeedRange(new FeedRangeEpkImpl(new Range<>("AA", "BB", true, false)));
        lease.setId("TestLease-" + UUID.randomUUID());

        ServiceItemLeaseV1 childLease2 =
                new ServiceItemLeaseV1()
                        .withLeaseToken("BB-CC")
                        .withFeedRange(new FeedRangeEpkImpl(new Range<>("BB", "CC", true, false)));
        lease.setId("TestLease-" + UUID.randomUUID());

        this.setDefaultLeaseManagerBehavior(leaseManager, Arrays.asList(lease,childLease1, childLease2));

        this.setPartitionSupervisorWithFeedRangeGoneBehavior(partitionSupervisorFactory, Arrays.asList(lease));
        this.setDefaultPartitionSupervisorBehavior(partitionSupervisorFactory, Arrays.asList(childLease1, childLease2));

        FeedRangeGoneHandler feedRangeGoneHandler =
                this.setDefaultFeedRangeGoneHandlerBehavior(
                        synchronizer,
                        lease,
                        Arrays.asList(childLease1, childLease2),
                        true,
                        shouldSkipDirectLeaseAssignment);

        StepVerifier.create(partitionController.initialize()).verifyComplete();
        StepVerifier.create(partitionController.addOrUpdateLease(lease))
                .expectNext(lease)
                .verifyComplete();

        // addOrUpdateLease for childLease1 and childLease2 are executed async
        // add some waiting time here so that we can capture all the calls
        Thread.sleep(500);

        if (shouldSkipDirectLeaseAssignment) {
            // Verify only parent lease is acquired
            verify(leaseManager, times(1)).acquire(lease);
            verify(leaseManager, never()).acquire(childLease1);
            verify(leaseManager, never()).acquire(childLease2);

            // Verify partitionSupervisor is created for parent lease
            verify(partitionSupervisorFactory, times(1)).create(lease);
            verify(partitionSupervisorFactory, never()).create(childLease1);
            verify(partitionSupervisorFactory, never()).create(childLease2);

            // Verify only the lease with feedRangeGone exception will be deleted from lease container
            verify(leaseManager, times(1)).delete(lease);
            verify(leaseManager, Mockito.never()).delete(childLease1);
            verify(leaseManager, Mockito.never()).delete(childLease2);

            // Verify at the end, only parent lease will be released
            verify(leaseManager, times(1)).release(lease);
            verify(leaseManager, never()).release(childLease1);
            verify(leaseManager, never()).release(childLease2);

            verify(leaseManager, Mockito.never()).updateProperties(Mockito.any());
            verify(feedRangeGoneHandler, times(1)).handlePartitionGone();
        } else {
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
            verify(feedRangeGoneHandler, times(1)).handlePartitionGone();
        }
    }


    @Test(groups = "unit", invocationCount = 1000)
    public void handleMerge() throws InterruptedException {
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

        ServiceItemLeaseV1 lease =
                new ServiceItemLeaseV1()
                        .withLeaseToken("AA-CC")
                        .withFeedRange(new FeedRangeEpkImpl(new Range<>("AA", "CC", true, false)));
        lease.setId("TestLease-" + UUID.randomUUID());

        this.setDefaultLeaseManagerBehavior(leaseManager, Arrays.asList(lease));

        PartitionSupervisor partitionSupervisor = Mockito.mock(PartitionSupervisor.class);
        when(partitionSupervisorFactory.create(lease)).thenReturn(partitionSupervisor);
        doNothing().when(partitionSupervisor).shutdown();

        when(partitionSupervisor.run(Mockito.any()))
                .thenReturn(Mono.error(new FeedRangeGoneException("Test", null)))
                .thenReturn(Mono.empty()); // second time return successfully

        FeedRangeGoneHandler feedRangeGoneHandler =
                this.setDefaultFeedRangeGoneHandlerBehavior(
                        synchronizer,
                        lease,
                        Arrays.asList(lease), // For merge with epkBased lease, we are going to reuse the lease
                        false,
                        false);

        StepVerifier.create(partitionController.initialize()).verifyComplete();
        StepVerifier.create(partitionController.addOrUpdateLease(lease))
                .expectNext(lease)
                .verifyComplete();

        // addOrUpdateLease for childLease1 and childLease2 are executed async
        // add some waiting time here so that we can capture all the calls
        Thread.sleep(500);

        verify(leaseManager, times(1)).acquire(lease);
        verify(partitionSupervisorFactory, times(1)).create(lease);
        verify(leaseManager, times(1)).release(lease);
        verify(feedRangeGoneHandler, times(1)).handlePartitionGone();

        verify(leaseManager, Mockito.never()).delete(lease);
        verify(leaseManager, times(1)).updateProperties(lease);
    }

    @Test(groups = "unit")
    public void handleNonPartitionGoneException() throws InterruptedException {
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

        ServiceItemLeaseV1 lease =
            new ServiceItemLeaseV1()
                .withLeaseToken("AA-CC")
                .withFeedRange(new FeedRangeEpkImpl(new Range<>("AA", "CC", true, false)));
        lease.setId("TestLease-" + UUID.randomUUID());

        this.setDefaultLeaseManagerBehavior(leaseManager, Arrays.asList(lease));

        PartitionSupervisor partitionSupervisor = Mockito.mock(PartitionSupervisor.class);
        when(partitionSupervisorFactory.create(lease)).thenReturn(partitionSupervisor);
        doNothing().when(partitionSupervisor).shutdown();

        when(partitionSupervisor.run(Mockito.any()))
            .thenReturn(Mono.error(new TaskCancelledException()))
            .thenReturn(Mono.empty()); // second time return successfully

        StepVerifier.create(partitionController.initialize()).verifyComplete();
        StepVerifier.create(partitionController.addOrUpdateLease(lease))
            .expectNext(lease)
            .verifyComplete();

        // addOrUpdateLease for childLease1 and childLease2 are executed async
        // add some waiting time here so that we can capture all the calls
        Thread.sleep(500);

        verify(leaseManager, times(1)).acquire(lease);
        verify(partitionSupervisorFactory, times(1)).create(lease);
        verify(leaseManager, times(1)).release(lease); // the lease will be removed

        verify(leaseManager, Mockito.never()).delete(lease);
        verify(leaseManager, times(0)).updateProperties(lease);
    }

    private void setDefaultLeaseManagerBehavior(LeaseManager leaseManager, List<ServiceItemLeaseV1> leases) {
        leases.forEach(l -> {
            when(leaseManager.acquire(l)).thenReturn(Mono.just(l));
            when(leaseManager.delete(l)).thenReturn(Mono.empty());
            when(leaseManager.release(l)).thenReturn(Mono.empty());
            when(leaseManager.updateProperties(l)).thenReturn(Mono.just(l));
        });
    }

    private void setDefaultPartitionSupervisorBehavior(PartitionSupervisorFactory factory, List<ServiceItemLeaseV1> leases) {
        leases.forEach(lease -> {
            PartitionSupervisor partitionSupervisor = Mockito.mock(PartitionSupervisor.class);
            when(factory.create(lease)).thenReturn(partitionSupervisor);
            doNothing().when(partitionSupervisor).shutdown();

            when(partitionSupervisor.run(Mockito.any())).thenReturn(Mono.empty());
        });
    }

    private void setPartitionSupervisorWithFeedRangeGoneBehavior(PartitionSupervisorFactory factory, List<ServiceItemLeaseV1> leases) {
        leases.forEach(lease -> {
            PartitionSupervisor partitionSupervisor = Mockito.mock(PartitionSupervisor.class);
            when(factory.create(lease)).thenReturn(partitionSupervisor);
            doNothing().when(partitionSupervisor).shutdown();

            when(partitionSupervisor.run(Mockito.any()))
                    .thenReturn(Mono.error(new FeedRangeGoneException("Test", null)));
        });
    }

    private FeedRangeGoneHandler setDefaultFeedRangeGoneHandlerBehavior(
            PartitionSynchronizer partitionSynchronizer,
            ServiceItemLeaseV1 leaseWithException,
            List<ServiceItemLeaseV1> newLeases,
            boolean shouldDeleteLeaseWithException,
            boolean shouldSkipDirectLeaseAssignment){
        FeedRangeGoneHandler feedRangeGoneHandler = Mockito.mock(FeedRangeGoneHandler.class);
        when(feedRangeGoneHandler.handlePartitionGone()).thenReturn(Flux.fromIterable(newLeases));
        when(feedRangeGoneHandler.shouldDeleteCurrentLease()).thenReturn(shouldDeleteLeaseWithException);
        when(feedRangeGoneHandler.shouldSkipDirectLeaseAssignment()).thenReturn(shouldSkipDirectLeaseAssignment);

        when(partitionSynchronizer.getFeedRangeGoneHandler(leaseWithException)).thenReturn(Mono.just(feedRangeGoneHandler));

        return feedRangeGoneHandler;
    }
}
