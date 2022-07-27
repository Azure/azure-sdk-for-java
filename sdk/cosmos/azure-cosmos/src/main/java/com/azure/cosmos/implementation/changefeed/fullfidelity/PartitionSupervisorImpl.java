// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.changefeed.fullfidelity;

import com.azure.cosmos.implementation.CosmosSchedulers;
import com.azure.cosmos.implementation.changefeed.CancellationToken;
import com.azure.cosmos.implementation.changefeed.CancellationTokenSource;
import com.azure.cosmos.implementation.changefeed.ChangeFeedObserver;
import com.azure.cosmos.implementation.changefeed.ChangeFeedObserverCloseReason;
import com.azure.cosmos.implementation.changefeed.ChangeFeedObserverContext;
import com.azure.cosmos.implementation.changefeed.Lease;
import com.azure.cosmos.implementation.changefeed.LeaseRenewer;
import com.azure.cosmos.implementation.changefeed.PartitionProcessor;
import com.azure.cosmos.implementation.changefeed.PartitionSupervisor;
import com.azure.cosmos.implementation.changefeed.exceptions.LeaseLostException;
import com.azure.cosmos.implementation.changefeed.exceptions.ObserverException;
import com.azure.cosmos.implementation.changefeed.exceptions.PartitionSplitException;
import com.azure.cosmos.implementation.changefeed.exceptions.TaskCancelledException;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;

import java.time.Duration;

/**
 * Implementation for {@link PartitionSupervisor}.
 */
class PartitionSupervisorImpl implements PartitionSupervisor {
    private final Lease lease;
    private final ChangeFeedObserver observer;
    private final PartitionProcessor processor;
    private final LeaseRenewer renewer;
    private CancellationTokenSource childShutdownCts;

    private volatile RuntimeException resultException;

    private Scheduler scheduler;

    public PartitionSupervisorImpl(Lease lease, ChangeFeedObserver observer, PartitionProcessor processor, LeaseRenewer renewer, Scheduler scheduler) {
        this.lease = lease;
        this.observer = observer;
        this.processor = processor;
        this.renewer = renewer;
        this.scheduler = scheduler;
        this.childShutdownCts = new CancellationTokenSource();
    }

    @Override
    public Mono<Void> run(CancellationToken shutdownToken) {
        this.resultException = null;

        ChangeFeedObserverContext context = new ChangeFeedObserverContextImpl(this.lease.getLeaseToken());

        this.observer.open(context);

        this.scheduler.schedule(() -> this.processor.run(this.childShutdownCts.getToken())
            .subscribe());

        this.scheduler.schedule(() -> this.renewer.run(this.childShutdownCts.getToken())
            .subscribe());

        return Mono.just(this)
            .delayElement(Duration.ofMillis(100), CosmosSchedulers.COSMOS_PARALLEL)
            .repeat( () -> !shutdownToken.isCancellationRequested() && this.processor.getResultException() == null && this.renewer.getResultException() == null)
            .last()
            .flatMap( value -> this.afterRun(context, shutdownToken));
    }

    private Mono<Void> afterRun(ChangeFeedObserverContext context, CancellationToken shutdownToken) {
        ChangeFeedObserverCloseReason closeReason = ChangeFeedObserverCloseReason.UNKNOWN;

        try {

            this.childShutdownCts.cancel();

            closeReason = shutdownToken.isCancellationRequested() ?
                ChangeFeedObserverCloseReason.SHUTDOWN :
                ChangeFeedObserverCloseReason.UNKNOWN;

            RuntimeException workerException = this.processor.getResultException();

            // Priority must be given to any exception from the processor worker unless it is a task being cancelled.
            if (workerException == null || workerException instanceof TaskCancelledException) {
                if (this.renewer.getResultException() != null) {
                    workerException = this.renewer.getResultException();
                }
            }

            if (workerException != null) {
                throw workerException;
            }
        } catch (LeaseLostException llex) {
            closeReason = ChangeFeedObserverCloseReason.LEASE_LOST;
            this.resultException = llex;
        } catch (PartitionSplitException pex) {
            closeReason = ChangeFeedObserverCloseReason.LEASE_GONE;
            this.resultException = pex;
        } catch (TaskCancelledException tcex) {
            closeReason = ChangeFeedObserverCloseReason.SHUTDOWN;
            this.resultException = null;
        } catch (ObserverException oex) {
            closeReason = ChangeFeedObserverCloseReason.OBSERVER_ERROR;
            this.resultException = oex;
        } catch (Exception ex) {
            closeReason = ChangeFeedObserverCloseReason.UNKNOWN;
        } finally {
            this.observer.close(context, closeReason);
        }

        if (this.resultException != null) {
            return Mono.error(this.resultException);
        } else {
            return Mono.empty();
        }
    }

    @Override
    public RuntimeException getResultException() {
        return this.resultException;
    }

    @Override
    public void shutdown() {
        if (this.childShutdownCts != null) {
            this.childShutdownCts.cancel();
        }
    }
}
