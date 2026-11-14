// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.kafka.connect.implementation.source;

import org.apache.kafka.connect.source.SourceRecord;
import org.apache.kafka.connect.source.SourceTask;
import reactor.core.Exceptions;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Base class that keeps one asynchronous source read in flight while bounding each Kafka poll call.
 */
public abstract class AsyncSourceTask extends SourceTask {
    private static final long POLL_WAIT_MS = 1_000;

    private volatile boolean stopping;
    private CompletableFuture<List<SourceRecord>> inFlightPoll;

    @Override
    public final List<SourceRecord> poll() {
        CompletableFuture<List<SourceRecord>> poll = this.getOrStartPoll();
        if (poll == null) {
            return Collections.emptyList();
        }

        try {
            List<SourceRecord> result = poll.get(POLL_WAIT_MS, TimeUnit.MILLISECONDS);
            this.clearCompletedPoll(poll);
            return result;
        } catch (TimeoutException error) {
            return Collections.emptyList();
        } catch (CancellationException error) {
            this.clearCompletedPoll(poll);
            return Collections.emptyList();
        } catch (ExecutionException error) {
            this.clearCompletedPoll(poll);
            Throwable cause = Exceptions.unwrap(error.getCause());
            if (cause instanceof RuntimeException) {
                throw (RuntimeException) cause;
            }
            if (cause instanceof Error) {
                throw (Error) cause;
            }
            throw Exceptions.propagate(cause);
        } catch (InterruptedException error) {
            Thread.currentThread().interrupt();
            return Collections.emptyList();
        }
    }

    @Override
    public final void stop() {
        CompletableFuture<List<SourceRecord>> poll;
        synchronized (this) {
            if (this.stopping) {
                return;
            }
            this.stopping = true;
            poll = this.inFlightPoll;
        }

        if (poll != null) {
            poll.cancel(true);
        }

        try {
            this.stopTask();
        } finally {
            synchronized (this) {
                if (this.inFlightPoll == poll) {
                    this.inFlightPoll = null;
                }
            }
        }
    }

    protected abstract Mono<List<SourceRecord>> pollTask();

    /**
     * Releases resources used by {@link #pollTask()} so an in-flight poll can exit.
     */
    protected abstract void stopTask();

    private synchronized CompletableFuture<List<SourceRecord>> getOrStartPoll() {
        if (this.stopping) {
            return null;
        }
        if (this.inFlightPoll == null) {
            this.inFlightPoll = this.pollTask().toFuture();
        }
        return this.inFlightPoll;
    }

    private synchronized void clearCompletedPoll(CompletableFuture<List<SourceRecord>> poll) {
        if (this.inFlightPoll == poll) {
            this.inFlightPoll = null;
        }
    }
}
