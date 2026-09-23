// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.kafka.connect.implementation.source;

import org.apache.kafka.connect.errors.ConnectException;
import org.apache.kafka.connect.errors.RetriableException;
import org.apache.kafka.connect.source.SourceRecord;
import org.apache.kafka.connect.source.SourceTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Base class for source tasks whose source read can block indefinitely.
 *
 * <p>The source read runs on one background thread. Kafka Connect polls a bounded queue and
 * therefore regains control regularly for pause and shutdown.
 */
public abstract class BufferedSourceTask extends SourceTask {
    private static final Logger LOGGER = LoggerFactory.getLogger(BufferedSourceTask.class);
    private static final int POLL_RESULT_CAPACITY = 1;
    private static final Duration DEFAULT_KAFKA_POLL_WAIT = Duration.ofSeconds(1);
    private static final Duration DEFAULT_POLLING_THREAD_SHUTDOWN_WAIT = Duration.ofSeconds(1);
    private static final Duration RETRIABLE_ERROR_BACKOFF = Duration.ofMillis(100);

    private final Duration kafkaPollWait;
    private final Duration pollingThreadShutdownWait;
    private final BlockingQueue<PollResult> pollResults =
        new LinkedBlockingQueue<>(POLL_RESULT_CAPACITY);
    private volatile boolean stopping;
    private volatile Future<?> pollingFuture;
    private ExecutorService pollingExecutor;

    protected BufferedSourceTask() {
        this(DEFAULT_KAFKA_POLL_WAIT, DEFAULT_POLLING_THREAD_SHUTDOWN_WAIT);
    }

    BufferedSourceTask(
        Duration kafkaPollWait,
        Duration pollingThreadShutdownWait) {
        this.kafkaPollWait = kafkaPollWait;
        this.pollingThreadShutdownWait = pollingThreadShutdownWait;
    }

    @Override
    public final synchronized void start(Map<String, String> props) {
        if (this.pollingExecutor != null) {
            throw new ConnectException("Source task is already running");
        }
        this.stopping = false;
        this.pollingFuture = null;
        this.pollResults.clear();

        try {
            this.startTask(props);
            String pollingThreadName = this.getPollingThreadName(props);
            this.pollingExecutor = Executors.newSingleThreadExecutor(runnable -> {
                Thread thread = new Thread(runnable, pollingThreadName);
                thread.setDaemon(true);
                return thread;
            });
        } catch (RuntimeException | Error error) {
            this.stopping = true;
            this.stopTask();
            throw error;
        }
    }

    @Override
    public final List<SourceRecord> poll() {
        this.startReaderIfNeeded();

        PollResult result;
        try {
            result = this.pollResults.poll(
                this.kafkaPollWait.toMillis(),
                TimeUnit.MILLISECONDS);
        } catch (InterruptedException exception) {
            if (this.stopping) {
                return Collections.emptyList();
            }
            Thread.currentThread().interrupt();
            throw new ConnectException("Interrupted while waiting for source poll result", exception);
        }

        if (result == null) {
            this.throwIfReaderStopped();
            return Collections.emptyList();
        }

        if (result.error != null) {
            throwPollError(result.error, this.stopping);
        }
        return result.records;
    }

    @Override
    public final synchronized void stop() {
        if (this.stopping || this.pollingExecutor == null) {
            return;
        }
        this.stopping = true;

        try {
            this.stopTask();
        } finally {
            if (this.pollingExecutor != null) {
                this.pollingExecutor.shutdownNow();
                this.awaitPollingThreadShutdown();
                this.pollingExecutor = null;
            }
        }
    }

    protected abstract void startTask(Map<String, String> props);

    protected abstract List<SourceRecord> pollTask();

    protected abstract void stopTask();

    protected String getPollingThreadName(Map<String, String> props) {
        return this.getClass().getSimpleName() + "-poll";
    }

    private void startReaderIfNeeded() {
        if (this.stopping || this.pollingFuture != null) {
            return;
        }
        synchronized (this) {
            if (this.stopping || this.pollingFuture != null) {
                return;
            }
            if (this.pollingExecutor == null) {
                throw new ConnectException("Source task has not been started");
            }
            this.pollingFuture = this.pollingExecutor.submit(
                preserveMdc(this::pollContinuously));
        }
    }

    private void pollContinuously() {
        while (!this.stopping) {
            try {
                List<SourceRecord> records = this.pollTask();
                if (!this.publishResult(PollResult.records(records))) {
                    return;
                }
            } catch (Exception error) {
                if (this.stopping) {
                    return;
                }

                if (!this.publishResult(PollResult.error(error))) {
                    return;
                }

                if (!isRetriable(error) || !this.waitBeforeRetry()) {
                    return;
                }
            }
        }
    }

    private boolean publishResult(PollResult result) {
        while (!this.stopping) {
            try {
                if (this.pollResults.offer(result, 100, TimeUnit.MILLISECONDS)) {
                    return true;
                }
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
                return false;
            }
        }
        return false;
    }

    private void throwIfReaderStopped() {
        Future<?> currentPollingFuture = this.pollingFuture;
        if (currentPollingFuture == null
            || !currentPollingFuture.isDone()
            || this.stopping) {
            return;
        }

        try {
            currentPollingFuture.get();
        } catch (ExecutionException error) {
            throwPollError(error.getCause(), false);
        } catch (CancellationException error) {
            throw new ConnectException("Background source polling thread was cancelled unexpectedly", error);
        } catch (InterruptedException error) {
            Thread.currentThread().interrupt();
            throw new ConnectException("Interrupted while checking background source polling thread", error);
        }

        throw new ConnectException("Background source polling thread stopped unexpectedly");
    }

    private void awaitPollingThreadShutdown() {
        try {
            if (!this.pollingExecutor.awaitTermination(
                this.pollingThreadShutdownWait.toMillis(),
                TimeUnit.MILLISECONDS)) {
                LOGGER.error(
                    "Source polling thread did not stop within {} ms after cancellation",
                    this.pollingThreadShutdownWait.toMillis());
            }
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            LOGGER.warn("Interrupted while waiting for source polling thread shutdown");
        }
    }

    private boolean waitBeforeRetry() {
        try {
            Thread.sleep(RETRIABLE_ERROR_BACKOFF.toMillis());
            return !this.stopping;
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    private static boolean isRetriable(Throwable error) {
        return error instanceof RetriableException
            || error instanceof org.apache.kafka.common.errors.RetriableException;
    }

    private static void throwPollError(Throwable error, boolean stopping) {
        if (error instanceof InterruptedException) {
            if (stopping) {
                return;
            }
            Thread.currentThread().interrupt();
            throw new ConnectException("Background source poll was interrupted", error);
        }
        if (error instanceof RuntimeException) {
            throw (RuntimeException) error;
        }
        if (error instanceof Error) {
            throw (Error) error;
        }
        throw new ConnectException("Background source poll failed", error);
    }

    private static Runnable preserveMdc(Runnable runnable) {
        Map<String, String> context = MDC.getCopyOfContextMap();
        return () -> {
            Map<String, String> originalContext = MDC.getCopyOfContextMap();
            if (context != null) {
                MDC.setContextMap(context);
            } else {
                MDC.clear();
            }
            try {
                runnable.run();
            } finally {
                if (originalContext != null) {
                    MDC.setContextMap(originalContext);
                } else {
                    MDC.clear();
                }
            }
        };
    }

    private static final class PollResult {
        private final List<SourceRecord> records;
        private final Throwable error;

        private PollResult(List<SourceRecord> records, Throwable error) {
            this.records = records;
            this.error = error;
        }

        private static PollResult records(List<SourceRecord> records) {
            return new PollResult(
                records == null ? Collections.emptyList() : records,
                null);
        }

        private static PollResult error(Throwable error) {
            return new PollResult(Collections.emptyList(), error);
        }
    }
}
