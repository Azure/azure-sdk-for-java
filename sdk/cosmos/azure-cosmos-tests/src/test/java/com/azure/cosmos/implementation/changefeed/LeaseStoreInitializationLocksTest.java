// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.changefeed;

import com.azure.cosmos.implementation.changefeed.common.LeaseStoreInitializationLocks;
import org.mockito.InOrder;
import org.testng.annotations.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;

import static org.mockito.Mockito.*;

public class LeaseStoreInitializationLocksTest {
    private static final Duration LOCK_TIME = Duration.ofSeconds(5);

    @Test(groups = "unit")
    public void contentionReleasesFirstLockBeforeRetryAndUsesConsistentOrdering() {
        LeaseStore epk = mock(LeaseStore.class);
        LeaseStore pk = mock(LeaseStore.class);
        when(epk.acquireInitializationLock(LOCK_TIME)).thenReturn(Mono.just(true));
        when(pk.acquireInitializationLock(LOCK_TIME)).thenReturn(Mono.just(false), Mono.just(true));
        when(epk.releaseInitializationLock()).thenReturn(Mono.just(true));
        when(pk.releaseInitializationLock()).thenReturn(Mono.empty());
        LeaseStoreInitializationLocks locks = new LeaseStoreInitializationLocks(epk, pk, LOCK_TIME);
        StepVerifier.create(locks.acquire()).expectNext(false).verifyComplete();
        StepVerifier.create(locks.acquire()).expectNext(true).verifyComplete();
        StepVerifier.create(locks.release()).expectNext(true).verifyComplete();
        StepVerifier.create(locks.release()).expectNext(false).verifyComplete();

        InOrder order = inOrder(epk, pk);
        order.verify(epk).acquireInitializationLock(LOCK_TIME);
        order.verify(pk).acquireInitializationLock(LOCK_TIME);
        order.verify(epk).releaseInitializationLock();
        order.verify(epk).acquireInitializationLock(LOCK_TIME);
        order.verify(pk).acquireInitializationLock(LOCK_TIME);
        order.verify(pk).releaseInitializationLock();
        order.verify(epk).releaseInitializationLock();
        order.verifyNoMoreInteractions();
    }

    @Test(groups = "unit")
    public void unavailableFirstLockNeverAcquiresOrReleasesSecondLock() {
        LeaseStore epk = mock(LeaseStore.class);
        LeaseStore pk = mock(LeaseStore.class);
        when(epk.acquireInitializationLock(LOCK_TIME)).thenReturn(Mono.just(false));
        LeaseStoreInitializationLocks locks = new LeaseStoreInitializationLocks(epk, pk, LOCK_TIME);
        StepVerifier.create(locks.acquire()).expectNext(false).verifyComplete();
        StepVerifier.create(locks.release()).expectNext(false).verifyComplete();
        verifyNoInteractions(pk);
        verify(epk, never()).releaseInitializationLock();
    }

    @Test(groups = "unit")
    public void secondLockFailureReleasesFirstLockAndPropagatesError() {
        LeaseStore epk = mock(LeaseStore.class);
        LeaseStore pk = mock(LeaseStore.class);
        IllegalStateException failure = new IllegalStateException("lock read failed");
        when(epk.acquireInitializationLock(LOCK_TIME)).thenReturn(Mono.just(true));
        when(pk.acquireInitializationLock(LOCK_TIME)).thenReturn(Mono.error(failure));
        when(epk.releaseInitializationLock()).thenReturn(Mono.just(true));
        LeaseStoreInitializationLocks locks = new LeaseStoreInitializationLocks(epk, pk, LOCK_TIME);
        StepVerifier.create(locks.acquire()).expectErrorMatches(error -> error == failure).verify();
        verify(epk).releaseInitializationLock();
        verify(pk, never()).releaseInitializationLock();
    }

    @Test(groups = "unit")
    public void secondNamespaceReleaseFailureStillReleasesFirstNamespace() {
        LeaseStore epk = mock(LeaseStore.class);
        LeaseStore pk = mock(LeaseStore.class);
        IllegalStateException failure = new IllegalStateException("lock release failed");
        when(epk.acquireInitializationLock(LOCK_TIME)).thenReturn(Mono.just(true));
        when(pk.acquireInitializationLock(LOCK_TIME)).thenReturn(Mono.just(true));
        when(epk.releaseInitializationLock()).thenReturn(Mono.just(true));
        when(pk.releaseInitializationLock()).thenReturn(Mono.error(failure));
        LeaseStoreInitializationLocks locks = new LeaseStoreInitializationLocks(epk, pk, LOCK_TIME);
        StepVerifier.create(locks.acquire()).expectNext(true).verifyComplete();
        StepVerifier.create(locks.release()).expectErrorMatches(error -> error == failure).verify();
        verify(epk).releaseInitializationLock();
        verify(pk).releaseInitializationLock();
    }
}
