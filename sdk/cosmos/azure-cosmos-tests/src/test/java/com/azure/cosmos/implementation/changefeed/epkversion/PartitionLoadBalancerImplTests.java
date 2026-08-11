// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.changefeed.epkversion;

import com.azure.cosmos.implementation.changefeed.Lease;
import com.azure.cosmos.implementation.changefeed.LeaseContainer;
import com.azure.cosmos.implementation.changefeed.PartitionController;
import com.azure.cosmos.implementation.changefeed.PartitionLoadBalancingStrategy;
import org.mockito.Mockito;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class PartitionLoadBalancerImplTests {
    private final int PARTITION_LOAD_BALANCER_TIMEOUT = 5000;

    @DataProvider(name = "loadBalancingSucceededArgProvider")
    public static Object[][] loadBalancingSucceededArgProvider() {
        return new Object[][]{
            // load balancing call succeeded
            { false },
            { true }
        };
    }

    @Test(groups = "unit", dataProvider = "loadBalancingSucceededArgProvider")
    public void run(boolean loadBalancingSucceeded) throws InterruptedException {
        PartitionController partitionControllerMock = Mockito.mock(PartitionController.class);
        LeaseContainer leaseContainerMock = Mockito.mock(LeaseContainer.class);
        PartitionLoadBalancingStrategy partitionLoadBalancingStrategyMock = Mockito.mock(PartitionLoadBalancingStrategy.class);

        ServiceItemLeaseV1 lease = new ServiceItemLeaseV1().withLeaseToken("1");
        lease.setId("TestLease-" + UUID.randomUUID());

        List<Lease> allLeases = Arrays.asList(lease);
        Mockito.when(leaseContainerMock.getAllLeases()).thenReturn(Flux.fromIterable(allLeases));

        if (loadBalancingSucceeded) {
            Mockito
                .when(partitionLoadBalancingStrategyMock.selectLeasesToTake(allLeases))
                .thenReturn(allLeases)
                .thenReturn(Arrays.asList());
        } else {
            Mockito
                .when(partitionLoadBalancingStrategyMock.selectLeasesToTake(allLeases))
                .thenThrow(new IllegalArgumentException("Something is wrong"));
        }

        Mockito.when(partitionControllerMock.shutdown()).thenReturn(Mono.empty());

        PartitionLoadBalancerImpl partitionLoadBalancerImpl =
            new PartitionLoadBalancerImpl(
                partitionControllerMock,
                leaseContainerMock,
                partitionLoadBalancingStrategyMock,
                Duration.ofSeconds(2),
                Schedulers.boundedElastic(),
                null
            );

        partitionLoadBalancerImpl
            .start()
            .timeout(Duration.ofMillis(PARTITION_LOAD_BALANCER_TIMEOUT))
            .subscribeOn(Schedulers.boundedElastic())
            .subscribe();
        Thread.sleep(Duration.ofSeconds(5).toMillis());
        Mockito.verify(leaseContainerMock, Mockito.atMost(3)).getAllLeases();
        partitionLoadBalancerImpl
            .stop()
            .timeout(Duration.ofMillis(PARTITION_LOAD_BALANCER_TIMEOUT))
            .subscribeOn(Schedulers.boundedElastic())
            .subscribe();
    }

    @Test(groups = "unit")
    public void run_multipleLeasesReturnedByStrategy_areAllAttempted() {
        PartitionController partitionControllerMock = Mockito.mock(PartitionController.class);
        LeaseContainer leaseContainerMock = Mockito.mock(LeaseContainer.class);
        PartitionLoadBalancingStrategy partitionLoadBalancingStrategyMock = Mockito.mock(PartitionLoadBalancingStrategy.class);

        ServiceItemLeaseV1 lease1 = new ServiceItemLeaseV1().withLeaseToken("1");
        lease1.setId("TestLease-" + UUID.randomUUID());
        ServiceItemLeaseV1 lease2 = new ServiceItemLeaseV1().withLeaseToken("2");
        lease2.setId("TestLease-" + UUID.randomUUID());
        ServiceItemLeaseV1 lease3 = new ServiceItemLeaseV1().withLeaseToken("3");
        lease3.setId("TestLease-" + UUID.randomUUID());

        List<Lease> allLeases = Arrays.asList(lease1, lease2, lease3);
        Mockito.when(leaseContainerMock.getAllLeases()).thenReturn(Flux.fromIterable(allLeases));

        // PartitionLoadBalancer collects leases into a new list instance each cycle, so don't match on identity.
        Mockito.when(partitionLoadBalancingStrategyMock.selectLeasesToTake(Mockito.anyList()))
            .thenReturn(allLeases)
            .thenReturn(Collections.emptyList());

        // Ensure the "happy path" doesn't get swallowed by the load balancer's error handler.
        Mockito.when(partitionControllerMock.addOrUpdateLease(Mockito.any()))
            .thenAnswer(invocation -> Mono.just((Lease) invocation.getArgument(0)));
        Mockito.when(partitionControllerMock.shutdown()).thenReturn(Mono.empty());

        PartitionLoadBalancerImpl partitionLoadBalancerImpl =
            new PartitionLoadBalancerImpl(
                partitionControllerMock,
                leaseContainerMock,
                partitionLoadBalancingStrategyMock,
                Duration.ofSeconds(1),
                Schedulers.boundedElastic(),
                null
            );

        partitionLoadBalancerImpl
            .start()
            .timeout(Duration.ofMillis(PARTITION_LOAD_BALANCER_TIMEOUT))
            .subscribeOn(Schedulers.boundedElastic())
            .subscribe();

        // Wait until the first balancing cycle attempts all leases returned by the strategy.
        Mockito.verify(partitionControllerMock, Mockito.timeout(PARTITION_LOAD_BALANCER_TIMEOUT).times(allLeases.size()))
            .addOrUpdateLease(Mockito.any());

        partitionLoadBalancerImpl
            .stop()
            .timeout(Duration.ofMillis(PARTITION_LOAD_BALANCER_TIMEOUT))
            .subscribeOn(Schedulers.boundedElastic())
            .subscribe();
    }
}
