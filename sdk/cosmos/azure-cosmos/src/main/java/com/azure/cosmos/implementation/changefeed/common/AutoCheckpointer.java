// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.changefeed.common;

import com.azure.cosmos.implementation.changefeed.ChangeFeedObserver;
import com.azure.cosmos.implementation.changefeed.ChangeFeedObserverCloseReason;
import com.azure.cosmos.implementation.changefeed.ChangeFeedObserverContext;
import com.azure.cosmos.implementation.changefeed.CheckpointFrequency;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Auto check-pointer implementation for {@link ChangeFeedObserver}.
 */
public class AutoCheckpointer<T> implements ChangeFeedObserver<T> {
    private final Logger logger = LoggerFactory.getLogger(AutoCheckpointer.class);
    private final CheckpointFrequency checkpointFrequency;
    private final Clock clock;
    private final ChangeFeedObserver<T> observer;
    private final AtomicLong processedDocCount;
    private volatile Instant lastCheckpointTime;
    private ChangeFeedObserverContext<T> pendingContext;

    public AutoCheckpointer(CheckpointFrequency checkpointFrequency, ChangeFeedObserver<T> observer) {
        this(checkpointFrequency, observer, Clock.systemUTC());
    }

    AutoCheckpointer(CheckpointFrequency checkpointFrequency, ChangeFeedObserver<T> observer, Clock clock) {
        if (checkpointFrequency == null) {
            throw new IllegalArgumentException("checkpointFrequency");
        }

        if (observer == null) {
            throw new IllegalArgumentException("observer");
        }

        this.checkpointFrequency = checkpointFrequency;
        this.clock = clock;
        this.observer = observer;
        this.lastCheckpointTime = this.clock.instant();
        this.processedDocCount = new AtomicLong();
    }

    @Override
    public void open(ChangeFeedObserverContext<T> context) {
        this.observer.open(context);
    }

    @Override
    public void close(ChangeFeedObserverContext<T> context, ChangeFeedObserverCloseReason reason) {
        this.observer.close(context, reason);
    }

    @Override
    public Mono<Void> closeAsync(ChangeFeedObserverContext<T> context, ChangeFeedObserverCloseReason reason) {
        return Mono.defer(() -> {
            Mono<Void> checkpoint = reason == ChangeFeedObserverCloseReason.SHUTDOWN && this.pendingContext != null
                ? this.checkpoint(this.pendingContext, true) : Mono.empty();
            return checkpoint.doOnTerminate(() -> this.observer.close(context, reason));
        });
    }

    @Override
    public Mono<Void> processChanges(ChangeFeedObserverContext<T> context, List<T> docs) {
        return Mono.defer(() -> this.observer.processChanges(context, docs))
            .doOnError(throwable -> {
                logger.warn("Unexpected exception from thread: " + Thread.currentThread().getId(), throwable);
            })
            .then(Mono.defer(() -> this.afterProcessChanges(context, docs.size())));
    }

    @Override
    public Mono<Void> processEmptyBatch(ChangeFeedObserverContext<T> context) {
        return Mono.defer(() -> this.afterProcessChanges(context, 0));
    }

    private Mono<Void> afterProcessChanges(ChangeFeedObserverContext<T> context, int documentCount) {
        this.processedDocCount.addAndGet(documentCount);
        this.pendingContext = context;
        return this.isCheckpointNeeded() ? this.checkpoint(context, false) : Mono.empty();
    }

    private Mono<Void> checkpoint(ChangeFeedObserverContext<T> context, boolean shutdown) {
        return Mono.defer(() -> shutdown ? context.checkpointOnShutdown() : context.checkpoint())
            .doOnError(throwable -> {
                logger.warn("Checkpoint failed; this worker will be killed", throwable);
            })
            .doOnSuccess(lease -> {
                this.processedDocCount.set(0);
                this.lastCheckpointTime = this.clock.instant();
                this.pendingContext = null;
            })
            .then();
    }

    private boolean isCheckpointNeeded() {
        int documentCount = this.checkpointFrequency.getProcessedDocumentCount();
        Duration interval = this.checkpointFrequency.getTimeInterval();
        if (documentCount == 0 && interval == null) {
            return true;
        }

        if (documentCount > 0 && this.processedDocCount.get() >= documentCount) {
            return true;
        }

        return interval != null && Duration.between(this.lastCheckpointTime, this.clock.instant()).compareTo(interval) >= 0;
    }
}
