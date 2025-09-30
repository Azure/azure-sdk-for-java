// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.changefeed.epkversion;

import com.azure.cosmos.implementation.changefeed.CancellationToken;
import com.azure.cosmos.implementation.changefeed.CancellationTokenSource;
import com.azure.cosmos.implementation.changefeed.ChangeFeedObserver;
import com.azure.cosmos.implementation.changefeed.Lease;
import com.azure.cosmos.implementation.changefeed.LeaseRenewer;
import com.fasterxml.jackson.databind.JsonNode;
import org.mockito.Mockito;
import org.testng.annotations.Test;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.lang.reflect.Method;
import java.time.Duration;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for the PartitionSupervisorImpl class
 */
public class PartitionSupervisorImplTests {

    private Lease leaseMock;
    private PartitionProcessor processorMock;
    private LeaseRenewer renewerMock;
    private ChangeFeedObserver<JsonNode> observerMock; // added field

    @SuppressWarnings({"unchecked", "rawtypes"})
    public void setup() {
        leaseMock = Mockito.mock(Lease.class);
        Mockito.when(leaseMock.getLeaseToken()).thenReturn("-FF");
        observerMock = Mockito.mock(ChangeFeedObserver.class); // assign to field
        processorMock = Mockito.mock(PartitionProcessor.class);
        renewerMock = Mockito.mock(LeaseRenewer.class);

        Mockito.doNothing().when(observerMock).open(Mockito.any());
        Mockito.when(processorMock.run(Mockito.any())).thenReturn(Mono.empty());
        Mockito.when(renewerMock.run(Mockito.any())).thenReturn(Mono.empty());
        Mockito.when(renewerMock.getLeaseRenewInterval()).thenReturn(Duration.ofSeconds(1));
    }

    private PartitionSupervisorImpl createSupervisor() {
        return new PartitionSupervisorImpl(
            leaseMock,
            observerMock,
            processorMock,
            renewerMock,
            Schedulers.immediate());
    }

    private boolean invokeShouldContinue(PartitionSupervisorImpl sup, CancellationToken token) throws Exception {
        Method m = PartitionSupervisorImpl.class.getDeclaredMethod("shouldContinue", CancellationToken.class);
        m.setAccessible(true);
        return (boolean) m.invoke(sup, token);
    }

    @Test(groups = "unit")
    public void shouldContinue_NoVerificationWindow_NotCancelled_NoErrors() throws Exception {
        this.setup();
        PartitionSupervisorImpl sup = createSupervisor();
        CancellationTokenSource cts = new CancellationTokenSource();
        Mockito.when(processorMock.getResultException()).thenReturn(null);
        Mockito.when(renewerMock.getResultException()).thenReturn(null);
        Mockito.when(processorMock.getLastProcessedTime()).thenReturn(Instant.now());

        boolean result = invokeShouldContinue(sup, cts.getToken());
        // double check this metho
        assertThat(result).isTrue();
    }

    @Test(groups = "unit")
    public void shouldContinue_VerificationWindow_ProcessedBatchesTrue() throws Exception {
        this.setup();
        PartitionSupervisorImpl sup = createSupervisor();
        CancellationTokenSource cts = new CancellationTokenSource();
        Duration renewInterval = renewerMock.getLeaseRenewInterval();
        // Force verification window elapsed: interval * 25 + 1s
        Mockito.when(processorMock.getLastProcessedTime()).thenReturn(Instant.now().minus(renewInterval
            .multipliedBy(23)).minusSeconds(1));
        Mockito.when(processorMock.getResultException()).thenReturn(null);
        Mockito.when(renewerMock.getResultException()).thenReturn(null);


        boolean result = invokeShouldContinue(sup, cts.getToken());

        assertThat(result).isTrue();
    }

    @Test(groups = "unit")
    public void shouldContinue_VerificationWindow_ProcessedBatchesFalse() throws Exception {
        this.setup();
        PartitionSupervisorImpl sup = createSupervisor();
        CancellationTokenSource cts = new CancellationTokenSource();
        Duration renewInterval = renewerMock.getLeaseRenewInterval();
        Mockito.when(processorMock.getLastProcessedTime()).thenReturn(Instant.now().minus(renewInterval.multipliedBy(25)).minusSeconds(1));
        Mockito.when(processorMock.getResultException()).thenReturn(null);
        Mockito.when(renewerMock.getResultException()).thenReturn(null);

        boolean result = invokeShouldContinue(sup, cts.getToken());
        assertThat(result).isFalse(); // should stop due to no processed batches
    }

    @Test(groups = "unit")
    public void shouldContinue_ProcessorError_Stops() throws Exception {
        this.setup();
        PartitionSupervisorImpl sup = createSupervisor();
        CancellationTokenSource cts = new CancellationTokenSource();
        Mockito.when(processorMock.getLastProcessedTime()).thenReturn(Instant.now());
        Mockito.when(processorMock.getResultException()).thenReturn(new RuntimeException("failure"));
        Mockito.when(renewerMock.getResultException()).thenReturn(null);

        boolean result = invokeShouldContinue(sup, cts.getToken());
        assertThat(result).isFalse();
    }

    @Test(groups = "unit")
    public void shouldContinue_RenewerError_Stops() throws Exception {
        this.setup();
        PartitionSupervisorImpl sup = createSupervisor();
        CancellationTokenSource cts = new CancellationTokenSource();
        Mockito.when(processorMock.getLastProcessedTime()).thenReturn(Instant.now());
        Mockito.when(processorMock.getResultException()).thenReturn(null);
        Mockito.when(renewerMock.getResultException()).thenReturn(new RuntimeException("lease error"));

        boolean result = invokeShouldContinue(sup, cts.getToken());
        assertThat(result).isFalse();
    }

    @Test(groups = "unit")
    public void shouldContinue_ShutdownRequested_Stops() throws Exception {
        this.setup();
        PartitionSupervisorImpl sup = createSupervisor();
        CancellationTokenSource cts = new CancellationTokenSource();
        cts.cancel();
        Mockito.when(processorMock.getLastProcessedTime()).thenReturn(Instant.now());
        Mockito.when(processorMock.getResultException()).thenReturn(null);
        Mockito.when(renewerMock.getResultException()).thenReturn(null);

        boolean result = invokeShouldContinue(sup, cts.getToken());
        assertThat(result).isFalse();
    }
}
