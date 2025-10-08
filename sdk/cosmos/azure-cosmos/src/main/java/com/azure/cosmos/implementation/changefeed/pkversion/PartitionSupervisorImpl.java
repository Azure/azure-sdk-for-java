// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.changefeed.pkversion;

import com.azure.cosmos.implementation.CosmosSchedulers;
import com.azure.cosmos.implementation.changefeed.CancellationToken;
import com.azure.cosmos.implementation.changefeed.CancellationTokenSource;
import com.azure.cosmos.implementation.changefeed.ChangeFeedObserver;
import com.azure.cosmos.implementation.changefeed.ChangeFeedObserverCloseReason;
import com.azure.cosmos.implementation.changefeed.ChangeFeedObserverContext;
import com.azure.cosmos.implementation.changefeed.Lease;
import com.azure.cosmos.implementation.changefeed.LeaseRenewer;
import com.azure.cosmos.implementation.changefeed.PartitionSupervisor;
import com.azure.cosmos.implementation.changefeed.common.ChangeFeedObserverContextImpl;
import com.azure.cosmos.implementation.changefeed.exceptions.FeedRangeGoneException;
import com.azure.cosmos.implementation.changefeed.exceptions.LeaseLostException;
import com.azure.cosmos.implementation.changefeed.exceptions.ObserverException;
import com.azure.cosmos.implementation.changefeed.exceptions.TaskCancelledException;
import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;

import java.time.Duration;
import java.time.Instant;

/**
 * Implementation for {@link PartitionSupervisor}.
 */
class PartitionSupervisorImpl implements PartitionSupervisor {
    private static final Logger logger = LoggerFactory.getLogger(PartitionSupervisorImpl.class);
    private final Lease lease;
    private final ChangeFeedObserver<JsonNode> observer;
    private final PartitionProcessor processor;
    private final LeaseRenewer renewer;
    private final CancellationTokenSource childShutdownCts;
    private static final int VERIFICATION_FACTOR = 25;

    private volatile RuntimeException resultException;

    private final Scheduler scheduler;

    public PartitionSupervisorImpl(Lease lease, ChangeFeedObserver<JsonNode> observer, PartitionProcessor processor,
                                   LeaseRenewer renewer, Scheduler scheduler) {
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

        ChangeFeedObserverContext<JsonNode> context = new ChangeFeedObserverContextImpl<>(this.lease.getLeaseToken());

        this.observer.open(context);

        this.scheduler.schedule(() -> this.processor.run(this.childShutdownCts.getToken())
            .subscribe());

        this.scheduler.schedule(() -> this.renewer.run(this.childShutdownCts.getToken())
            .subscribe());

        return Mono.just(this)
            .delayElement(Duration.ofMillis(100), CosmosSchedulers.COSMOS_PARALLEL)
            .repeat( () -> shouldContinue(shutdownToken))
            .last()
            .flatMap( value -> this.afterRun(context, shutdownToken));
    }

    private boolean shouldContinue(CancellationToken shutdownToken) {
        Duration timeSinceLastProcessedChanges = Duration.between(processor.getLastProcessedTime(), Instant.now());
        // if cfp has seen successes processing, we do a renew,
        // otherwise we do not to allow lease stealing
        if (timeSinceLastProcessedChanges.getSeconds() > this.renewer.getLeaseRenewInterval().getSeconds() * VERIFICATION_FACTOR) {
            logger.info("Lease with token {}: skipping renew as no batches processed.", this.lease.getLeaseToken());
            return false;
        }
        return !shutdownToken.isCancellationRequested() && this.processor.getResultException() == null && this.renewer.getResultException() == null;
    }

    private Mono<Void> afterRun(ChangeFeedObserverContext<JsonNode> context, CancellationToken shutdownToken) {
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
        } catch (FeedRangeGoneException pex) {
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
        this.childShutdownCts.cancel();
    }
}
