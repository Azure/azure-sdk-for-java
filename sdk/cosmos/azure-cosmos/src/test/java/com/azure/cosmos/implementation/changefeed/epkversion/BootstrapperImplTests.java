// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.changefeed.epkversion;

import com.azure.cosmos.implementation.changefeed.LeaseStore;
import com.azure.cosmos.implementation.changefeed.LeaseStoreManager;
import org.mockito.Mockito;
import org.testng.annotations.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.times;

public class BootstrapperImplTests {

    @Test(groups = "unit")
    public void initializeStoreFromPkVersionLeaseStore() {
        Duration lockTime = Duration.ofSeconds(5);
        Duration expireTIme = Duration.ofSeconds(5);

        PartitionSynchronizer partitionSynchronizerMock = Mockito.mock(PartitionSynchronizer.class);
        Mockito.when(partitionSynchronizerMock.createMissingLeases(Mockito.any())).thenReturn(Mono.empty());

        LeaseStore leaseStoreMock = Mockito.mock(LeaseStore.class);
        Mockito
            .when(leaseStoreMock.isInitialized())
            .thenReturn(Mono.just(false))
            .thenReturn(Mono.just(true));
        Mockito.when(leaseStoreMock.acquireInitializationLock(lockTime)).thenReturn(Mono.just(true));
        Mockito.when(leaseStoreMock.markInitialized()).thenReturn(Mono.empty());

        LeaseStoreManager pkVersionLeaseStoreManagerMock = Mockito.mock(LeaseStoreManager.class);
        Mockito.when(pkVersionLeaseStoreManagerMock.isInitialized()).thenReturn(Mono.just(true));
        Mockito.when(pkVersionLeaseStoreManagerMock.getAllLeases()).thenReturn(Flux.empty());
        Mockito.when(pkVersionLeaseStoreManagerMock.delete(Mockito.anyList())).thenReturn(Mono.empty());
        BootstrapperImpl bootstrapper = new BootstrapperImpl(
            partitionSynchronizerMock,
            leaseStoreMock,
            lockTime,
            expireTIme,
            pkVersionLeaseStoreManagerMock);

        bootstrapper.initialize().block();

        Mockito.verify(pkVersionLeaseStoreManagerMock, times(1)).isInitialized();
        Mockito.verify(pkVersionLeaseStoreManagerMock, times(1)).getAllLeases();
        Mockito.verify(partitionSynchronizerMock, times(1)).createMissingLeases(anyList());
        Mockito.verify(pkVersionLeaseStoreManagerMock, Mockito.times(1)).delete(anyList());
        Mockito.verify(leaseStoreMock, times(2)).isInitialized();
    }

    @Test(groups = "unit")
    public void initializeStoreFromScratch() {
        Duration lockTime = Duration.ofSeconds(5);
        Duration expireTIme = Duration.ofSeconds(5);

        PartitionSynchronizer partitionSynchronizerMock = Mockito.mock(PartitionSynchronizer.class);
        Mockito.when(partitionSynchronizerMock.createMissingLeases()).thenReturn(Mono.empty());

        LeaseStore leaseStoreMock = Mockito.mock(LeaseStore.class);
        Mockito
            .when(leaseStoreMock.isInitialized())
            .thenReturn(Mono.just(false))
            .thenReturn(Mono.just(true));
        Mockito.when(leaseStoreMock.acquireInitializationLock(lockTime)).thenReturn(Mono.just(true));
        Mockito.when(leaseStoreMock.markInitialized()).thenReturn(Mono.empty());

        BootstrapperImpl bootstrapper = new BootstrapperImpl(
            partitionSynchronizerMock,
            leaseStoreMock,
            lockTime,
            expireTIme,
            null);

        bootstrapper.initialize().block();
        Mockito.verify(partitionSynchronizerMock, times(0)).createMissingLeases(anyList());
        Mockito.verify(partitionSynchronizerMock, times(1)).createMissingLeases();
        Mockito.verify(leaseStoreMock, times(2)).isInitialized();
    }
}
