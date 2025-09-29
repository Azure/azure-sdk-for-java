// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.changefeed.epkversion;

import com.azure.cosmos.implementation.changefeed.CancellationTokenSource;
import com.azure.cosmos.implementation.changefeed.Lease;
import com.azure.cosmos.implementation.changefeed.LeaseManager;
import org.mockito.Mockito;
import org.testng.annotations.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.lang.reflect.Field;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class LeaseRenewerImplTests {

    @Test(groups = "unit")
    public void renewSkippedWhenNoBatchesProcessed() throws Exception {
        LeaseManager leaseManagerMock = Mockito.mock(LeaseManager.class);
        Lease leaseMock = Mockito.mock(ServiceItemLeaseV1.class);
        Duration leaseRenewInterval = Duration.ofMillis(20);
        AtomicBoolean processedBatches = new AtomicBoolean(false);
        LeaseRenewerImpl leaseRenewer = new LeaseRenewerImpl(leaseMock, leaseManagerMock, leaseRenewInterval, processedBatches);

        // Force lastVerification far in the past to hit the verification branch immediately
        Field lastVerificationField = LeaseRenewerImpl.class.getDeclaredField("lastVerification");
        lastVerificationField.setAccessible(true);
        lastVerificationField.set(leaseRenewer, Instant.now().minus(leaseRenewInterval.multipliedBy(2)));

        CancellationTokenSource cts = new CancellationTokenSource();
        Mono<Void> runMono = leaseRenewer.run(cts.getToken());
        Mono<Void> cancelMono = Mono.delay(Duration.ofMillis(60)).doOnNext(v -> cts.cancel()).then();

        StepVerifier.create(Mono.firstWithSignal(runMono, cancelMono)).verifyComplete();

        Mockito.verify(leaseManagerMock, Mockito.times(0)).renew(Mockito.any());
        assertThat(processedBatches.get()).isFalse();
    }

    @Test(groups = "unit")
    public void renewHappensWhenBatchesProcessedAndFlagResets() throws Exception {
        LeaseManager leaseManagerMock = Mockito.mock(LeaseManager.class);
        Lease leaseMock = Mockito.mock(ServiceItemLeaseV1.class);
        Duration leaseRenewInterval = Duration.ofMillis(20);
        AtomicBoolean processedBatches = new AtomicBoolean(true); // simulate batch processed
        Mockito.when(leaseManagerMock.renew(Mockito.any())).thenReturn(Mono.just(leaseMock));

        LeaseRenewerImpl leaseRenewer = new LeaseRenewerImpl(leaseMock, leaseManagerMock, leaseRenewInterval, processedBatches);

        // Force lastVerification far in the past to hit the verification branch immediately
        Field lastVerificationField = LeaseRenewerImpl.class.getDeclaredField("lastVerification");
        lastVerificationField.setAccessible(true);
        lastVerificationField.set(leaseRenewer, Instant.now().minus(leaseRenewInterval.multipliedBy(2)));

        CancellationTokenSource cts = new CancellationTokenSource();
        Mono<Void> runMono = leaseRenewer.run(cts.getToken());
        Mono<Void> cancelMono = Mono.delay(Duration.ofMillis(1000)).doOnNext(v -> cts.cancel()).then();

        StepVerifier.create(Mono.firstWithSignal(runMono, cancelMono)).verifyComplete();

        Mockito.verify(leaseManagerMock, Mockito.atLeastOnce()).renew(Mockito.any());
        assertThat(processedBatches.get()).isFalse(); // should be reset after renew
    }



}
