// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.changefeed.epkversion;

import com.azure.cosmos.implementation.changefeed.Bootstrapper;
import com.azure.cosmos.implementation.changefeed.LeaseStore;
import com.azure.cosmos.implementation.changefeed.LeaseStoreManager;
import org.mockito.Mockito;
import org.testng.annotations.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.times;

public class PkRangeIdVersionLeasesBootstrapperImplTests {
    @Test(groups = "unit")
    public void initializeStoreFromPkRangeIdVersionLeaseStore() {
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
        Mockito.when(leaseStoreMock.markInitialized()).thenReturn(Mono.just(Boolean.TRUE));
        Mockito.when(leaseStoreMock.releaseInitializationLock()).thenReturn(Mono.empty());

        LeaseStoreManager pkRangeIdVersionLeaseStoreManagerMock = Mockito.mock(LeaseStoreManager.class);
        Mockito.when(pkRangeIdVersionLeaseStoreManagerMock.isInitialized()).thenReturn(Mono.just(true));
        Mockito.when(pkRangeIdVersionLeaseStoreManagerMock.getAllLeases()).thenReturn(Flux.empty());
        Mockito.when(pkRangeIdVersionLeaseStoreManagerMock.deleteAll(anyList())).thenReturn(Mono.empty());
        Mockito.when(pkRangeIdVersionLeaseStoreManagerMock.releaseInitializationLock()).thenReturn(Mono.empty());
        Bootstrapper bootstrapper = new PkRangeIdVersionLeaseStoreBootstrapperImpl(
            partitionSynchronizerMock,
            leaseStoreMock,
            lockTime,
            expireTIme,
            pkRangeIdVersionLeaseStoreManagerMock);

        bootstrapper.initialize().block();

        Mockito.verify(pkRangeIdVersionLeaseStoreManagerMock, times(2)).isInitialized();
        Mockito.verify(pkRangeIdVersionLeaseStoreManagerMock, times(1)).getAllLeases();
        Mockito.verify(partitionSynchronizerMock, times(1)).createMissingLeases(anyList());
        Mockito.verify(pkRangeIdVersionLeaseStoreManagerMock, times(1)).deleteAll(anyList());
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
        Mockito.when(leaseStoreMock.markInitialized()).thenReturn(Mono.just(true));
        Mockito.when(leaseStoreMock.releaseInitializationLock()).thenReturn(Mono.just(true));

        LeaseStoreManager pkRangeIdVersionLeaseStoreManagerMock = Mockito.mock(LeaseStoreManager.class);
        Mockito.when(pkRangeIdVersionLeaseStoreManagerMock.isInitialized()).thenReturn(Mono.just(false));
        Mockito.when(pkRangeIdVersionLeaseStoreManagerMock.acquireInitializationLock(lockTime)).thenReturn(Mono.just(true));
        Mockito.when(pkRangeIdVersionLeaseStoreManagerMock.markInitialized()).thenReturn(Mono.empty());
        Mockito.when(pkRangeIdVersionLeaseStoreManagerMock.releaseInitializationLock()).thenReturn(Mono.just(true));
        Bootstrapper bootstrapper = new PkRangeIdVersionLeaseStoreBootstrapperImpl(
            partitionSynchronizerMock,
            leaseStoreMock,
            lockTime,
            expireTIme,
            pkRangeIdVersionLeaseStoreManagerMock);

        bootstrapper.initialize().block();
        Mockito.verify(partitionSynchronizerMock, times(0)).createMissingLeases(anyList());
        Mockito.verify(partitionSynchronizerMock, times(1)).createMissingLeases();
        Mockito.verify(leaseStoreMock, times(2)).isInitialized();
    }
}
