// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.changefeed.epkversion;

import com.azure.cosmos.implementation.changefeed.CancellationToken;
import com.azure.cosmos.implementation.changefeed.CancellationTokenSource;
import com.azure.cosmos.implementation.changefeed.ChangeFeedObserver;
import com.azure.cosmos.implementation.changefeed.ChangeFeedObserverContext;
import com.azure.cosmos.implementation.changefeed.CheckpointFrequency;
import com.azure.cosmos.implementation.changefeed.Lease;
import com.azure.cosmos.implementation.changefeed.LeaseCheckpointer;
import com.azure.cosmos.implementation.changefeed.LeaseContainer;
import com.azure.cosmos.implementation.changefeed.LeaseManager;
import com.azure.cosmos.implementation.changefeed.LeaseRenewer;
import com.azure.cosmos.implementation.changefeed.PartitionSupervisor;
import com.azure.cosmos.implementation.changefeed.PartitionSupervisorFactory;
import com.azure.cosmos.implementation.changefeed.common.AutoCheckpointer;
import com.azure.cosmos.implementation.changefeed.common.ChangeFeedState;
import com.azure.cosmos.implementation.changefeed.exceptions.LeaseLostException;
import com.azure.cosmos.implementation.changefeed.exceptions.TaskCancelledException;
import com.fasterxml.jackson.databind.JsonNode;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.time.Instant;
import java.util.Collections;

import static org.testng.Assert.assertFalse;

public class CheckpointLifecycleTests {
    @DataProvider(name = "ownership")
    public Object[][] ownership() {
        return new Object[][] {{false}, {true}};
    }

    @Test(groups = "unit", dataProvider = "ownership")
    @SuppressWarnings("unchecked")
    public void supervisorWaitsForProcessingBeforeFlushAndHonorsLeaseLoss(boolean leaseLost) {
        Lease lease = Mockito.mock(Lease.class);
        Mockito.when(lease.getLeaseToken()).thenReturn("lease");
        PartitionProcessor processor = Mockito.mock(PartitionProcessor.class);
        Sinks.One<Void> processing = Sinks.one();
        Mockito.when(processor.run(Mockito.any())).thenReturn(processing.asMono());
        Mockito.when(processor.getLastProcessedTime()).thenReturn(Instant.now());
        LeaseRenewer renewer = Mockito.mock(LeaseRenewer.class);
        Mockito.when(renewer.run(Mockito.any())).thenReturn(Mono.empty());
        Mockito.when(renewer.getLeaseRenewInterval()).thenReturn(Duration.ofSeconds(17));
        if (leaseLost) {
            Mockito.when(renewer.getResultException()).thenReturn(new LeaseLostException(lease));
        }
        ChangeFeedObserver<JsonNode> handler = Mockito.mock(ChangeFeedObserver.class);
        Mockito.when(handler.processChanges(Mockito.any(), Mockito.anyList())).thenReturn(Mono.empty());
        ChangeFeedObserverContext<JsonNode> context = Mockito.mock(ChangeFeedObserverContext.class);
        Mockito.when(context.checkpointOnShutdown()).thenReturn(Mono.empty());
        AutoCheckpointer<JsonNode> observer = new AutoCheckpointer<>(
            new CheckpointFrequency().withProcessedDocumentCount(10), handler);
        observer.processChanges(context, Collections.singletonList(null)).block();

        PartitionSupervisor supervisor = new PartitionSupervisorImpl<>(
            lease, observer, processor, renewer, Schedulers.immediate());
        CancellationTokenSource shutdown = new CancellationTokenSource();
        shutdown.cancel();
        StepVerifier.Step<Void> verification = StepVerifier.create(supervisor.run(shutdown.getToken()))
            .expectSubscription()
            .thenAwait(Duration.ofMillis(150))
            .then(() -> {
                Mockito.verify(context, Mockito.never()).checkpointOnShutdown();
                Mockito.verify(handler, Mockito.never()).close(Mockito.any(), Mockito.any());
                processing.tryEmitEmpty();
            });
        if (leaseLost) {
            verification.expectError(LeaseLostException.class).verify(Duration.ofSeconds(5));
        } else {
            verification.expectComplete().verify(Duration.ofSeconds(5));
        }
        Mockito.verify(context, Mockito.times(leaseLost ? 0 : 1)).checkpointOnShutdown();
        Mockito.verify(handler).close(Mockito.any(), Mockito.any());
    }

    @DataProvider(name = "workerCompletion")
    public Object[][] workerCompletion() {
        return new Object[][] {{false}, {true}};
    }

    @Test(groups = "unit", dataProvider = "workerCompletion")
    public void controllerShutdownWaitsForWorkerAndLeaseRelease(boolean workerFinished) {
        Lease lease = Mockito.mock(Lease.class);
        Mockito.when(lease.getLeaseToken()).thenReturn("lease");
        LeaseContainer container = Mockito.mock(LeaseContainer.class);
        Mockito.when(container.getOwnedLeases()).thenReturn(Flux.empty());
        LeaseManager manager = Mockito.mock(LeaseManager.class);
        Mockito.when(manager.acquire(lease)).thenReturn(Mono.just(lease));
        Sinks.One<Void> released = Sinks.one();
        Mockito.when(manager.release(lease)).thenReturn(released.asMono());
        PartitionSupervisor supervisor = Mockito.mock(PartitionSupervisor.class);
        Sinks.One<Void> worker = Sinks.one();
        Mockito.when(supervisor.run(Mockito.any())).thenReturn(worker.asMono());
        PartitionSupervisorFactory factory = Mockito.mock(PartitionSupervisorFactory.class);
        Mockito.when(factory.create(lease)).thenReturn(supervisor);
        PartitionControllerImpl controller = new PartitionControllerImpl(container, manager, factory,
            Mockito.mock(PartitionSynchronizer.class), Schedulers.immediate());
        controller.initialize().block();
        controller.addOrUpdateLease(lease).block();
        if (workerFinished) {
            worker.tryEmitEmpty();
        }

        StepVerifier.create(controller.shutdown())
            .expectSubscription()
            .then(() -> {
                if (!workerFinished) {
                    Mockito.verify(manager, Mockito.never()).release(lease);
                    worker.tryEmitEmpty();
                }
                Mockito.verify(manager).release(lease);
            })
            .expectNoEvent(Duration.ofMillis(10))
            .then(() -> released.tryEmitEmpty())
            .expectComplete().verify(Duration.ofSeconds(5));
    }

    @Test(groups = "unit", dataProvider = "ownership")
    public void shutdownCheckpointUsesSeparateTokenAndPreservesOwnershipErrors(boolean leaseLost) {
        ChangeFeedState state = Mockito.mock(ChangeFeedState.class, Mockito.RETURNS_DEEP_STUBS);
        Mockito.when(state.getContinuation().getContinuationTokenCount()).thenReturn(1);
        Mockito.when(state.getContinuation().getCurrentContinuationToken().getToken()).thenReturn("continuation");
        Lease lease = Mockito.mock(Lease.class);
        LeaseCheckpointer leaseCheckpointer = Mockito.mock(LeaseCheckpointer.class);
        Mockito.when(leaseCheckpointer.checkpoint(Mockito.eq(lease), Mockito.anyString(), Mockito.any()))
            .thenReturn(leaseLost ? Mono.error(new LeaseLostException(lease)) : Mono.just(lease));
        PartitionCheckpointerImpl checkpointer = new PartitionCheckpointerImpl(leaseCheckpointer, lease);
        CancellationTokenSource shutdown = new CancellationTokenSource();
        shutdown.cancel();
        checkpointer.setCancellationToken(shutdown.getToken());
        StepVerifier.create(checkpointer.checkpointPartition(state)).expectError(TaskCancelledException.class).verify();

        if (leaseLost) {
            StepVerifier.create(checkpointer.checkpointPartitionOnShutdown(state))
                .expectError(LeaseLostException.class).verify();
        } else {
            StepVerifier.create(checkpointer.checkpointPartitionOnShutdown(state)).expectNext(lease).verifyComplete();
        }
        ArgumentCaptor<CancellationToken> token = ArgumentCaptor.forClass(CancellationToken.class);
        Mockito.verify(leaseCheckpointer).checkpoint(Mockito.eq(lease), Mockito.anyString(), token.capture());
        assertFalse(token.getValue().isCancellationRequested());
    }
}
