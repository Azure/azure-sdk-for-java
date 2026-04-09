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
import reactor.core.Disposable;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Auto check-pointer implementation for {@link ChangeFeedObserver}.
 */
public class AutoCheckpointer<T> implements ChangeFeedObserver<T> {
    private final Logger logger = LoggerFactory.getLogger(AutoCheckpointer.class);
    private final CheckpointFrequency checkpointFrequency;
    private final ChangeFeedObserver<T> observer;
    private final AtomicInteger processedDocCount;
    private final AtomicLong latestProgressVersion;
    private final AtomicReference<Mono<Void>> checkpointInProgress;
    private final AtomicReference<ChangeFeedObserverContext<T>> latestContext;
    private volatile boolean hasUncheckpointedProgress;
    private volatile Disposable intervalCheckpointDisposable;
    private volatile Instant lastCheckpointTime;

    public AutoCheckpointer(CheckpointFrequency checkpointFrequency, ChangeFeedObserver<T> observer) {
        if (checkpointFrequency == null) {
            throw new IllegalArgumentException("checkpointFrequency");
        }

        if (observer == null) {
            throw new IllegalArgumentException("observer");
        }

        this.checkpointFrequency = checkpointFrequency;
        this.observer = observer;
        this.lastCheckpointTime = Instant.now();
        this.processedDocCount = new AtomicInteger();
        this.latestProgressVersion = new AtomicLong();
        this.checkpointInProgress = new AtomicReference<>();
        this.latestContext = new AtomicReference<>();
        this.hasUncheckpointedProgress = false;
    }

    @Override
    public void open(ChangeFeedObserverContext<T> context) {
        this.latestContext.set(context);
        this.observer.open(context);
        this.startIntervalCheckpointing();
    }

    @Override
    public void close(ChangeFeedObserverContext<T> context, ChangeFeedObserverCloseReason reason) {
        this.stopIntervalCheckpointing();
        this.observer.close(context, reason);
    }

    @Override
    public Mono<Void> processChanges(ChangeFeedObserverContext<T> context, List<T> docs) {
        this.latestContext.set(context);

        return this.observer.processChanges(context, docs)
            .doOnError(throwable -> logger.warn(
                "Unexpected exception from thread: " + Thread.currentThread().getId(), throwable))
            .then(this.afterProcessChanges(context, docs));
    }

    private Mono<Void> afterProcessChanges(ChangeFeedObserverContext<T> context, List<T> docs) {
        this.processedDocCount.addAndGet(docs.size());
        this.latestProgressVersion.incrementAndGet();
        this.hasUncheckpointedProgress = true;

        if (this.isCheckpointNeeded()) {
            return this.checkpointOnce(context);
        }

        return Mono.empty();
    }

    private boolean isCheckpointNeeded() {
        if (this.isEveryBatchCheckpoint()) {
            return true;
        }

        if (this.checkpointFrequency.getProcessedDocumentCount() > 0
            && this.processedDocCount.get() >= this.checkpointFrequency.getProcessedDocumentCount()) {
            return true;
        }

        Duration interval = this.checkpointFrequency.getTimeInterval();
        if (interval == null) {
            return false;
        }

        Duration delta = Duration.between(this.lastCheckpointTime, Instant.now());

        return delta.compareTo(interval) >= 0;
    }

    private boolean isEveryBatchCheckpoint() {
        return this.checkpointFrequency.getProcessedDocumentCount() <= 0
            && this.checkpointFrequency.getTimeInterval() == null;
    }

    private void startIntervalCheckpointing() {
        Duration interval = this.checkpointFrequency.getTimeInterval();
        if (interval == null) {
            return;
        }

        this.intervalCheckpointDisposable = Flux.interval(interval, interval)
            .concatMap(ignored -> this.checkpointIfIntervalElapsed())
            .subscribe(
                ignored -> {
                },
                throwable -> logger.warn("Interval checkpointing stream terminated", throwable));
    }

    private void stopIntervalCheckpointing() {
        Disposable disposable = this.intervalCheckpointDisposable;
        if (disposable != null) {
            disposable.dispose();
            this.intervalCheckpointDisposable = null;
        }
    }

    private Mono<Void> checkpointIfIntervalElapsed() {
        ChangeFeedObserverContext<T> context = this.latestContext.get();
        if (context == null || !this.hasUncheckpointedProgress) {
            return Mono.empty();
        }

        Duration interval = this.checkpointFrequency.getTimeInterval();
        if (interval == null || Duration.between(this.lastCheckpointTime, Instant.now()).compareTo(interval) < 0) {
            return Mono.empty();
        }

        return this.checkpointOnce(context);
    }

    private Mono<Void> checkpointOnce(ChangeFeedObserverContext<T> context) {
        Mono<Void> checkpointMono = this.checkpointInProgress.get();
        if (checkpointMono != null) {
            return checkpointMono;
        }

        long checkpointedProgressVersion = this.latestProgressVersion.get();
        AtomicReference<Mono<Void>> checkpointAttemptRef = new AtomicReference<>();
        Mono<Void> checkpointAttempt = context.checkpoint()
            .doOnError(throwable -> logger.warn("Checkpoint failed", throwable))
            .doOnSuccess(ignored -> this.onCheckpointSuccess(checkpointedProgressVersion))
            .then()
            .cache()
            .doFinally(ignored -> this.checkpointInProgress.compareAndSet(checkpointAttemptRef.get(), null));

        checkpointAttemptRef.set(checkpointAttempt);

        if (this.checkpointInProgress.compareAndSet(null, checkpointAttempt)) {
            return checkpointAttempt;
        }

        Mono<Void> inProgress = this.checkpointInProgress.get();
        return inProgress != null ? inProgress : Mono.empty();
    }

    private void onCheckpointSuccess(long checkpointedProgressVersion) {
        this.lastCheckpointTime = Instant.now();

        // Keep progress markers if newer batches arrived while this checkpoint was in flight.
        if (this.latestProgressVersion.get() == checkpointedProgressVersion) {
            this.processedDocCount.set(0);
            this.hasUncheckpointedProgress = false;
        }
    }
}
