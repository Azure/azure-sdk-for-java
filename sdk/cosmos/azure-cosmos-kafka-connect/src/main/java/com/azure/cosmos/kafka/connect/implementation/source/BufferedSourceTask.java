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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Base class for source tasks whose source read can block indefinitely.
 *
 * <p>The source read runs on one generation-scoped background thread. Kafka Connect polls a
 * capacity-one result queue and therefore regains control regularly for pause and shutdown.
 */
public abstract class BufferedSourceTask extends SourceTask {
    private static final Logger LOGGER = LoggerFactory.getLogger(BufferedSourceTask.class);
    private static final int POLL_RESULT_CAPACITY = 1;
    private static final Duration DEFAULT_KAFKA_POLL_WAIT = Duration.ofSeconds(1);
    private static final Duration DEFAULT_POLLING_THREAD_SHUTDOWN_WAIT = Duration.ofSeconds(1);
    private static final Duration RETRIABLE_ERROR_BACKOFF = Duration.ofMillis(100);

    private final Duration kafkaPollWait;
    private final Duration pollingThreadShutdownWait;
    private final AtomicBoolean taskStarted = new AtomicBoolean();
    private final AtomicReference<PollingGeneration> generation = new AtomicReference<>();
    private volatile Map<String, String> taskProperties;

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
        if (!this.taskStarted.compareAndSet(false, true)) {
            throw new ConnectException("Source task is already running");
        }

        try {
            this.startTask(props);
            this.taskProperties = props;
        } catch (RuntimeException | Error error) {
            PollingGeneration failedGeneration = this.generation.getAndSet(null);
            if (failedGeneration != null) {
                failedGeneration.stopping.set(true);
                failedGeneration.executor.shutdownNow();
            }
            this.stopTaskOnce();
            throw error;
        }
    }

    @Override
    public final List<SourceRecord> poll() {
        PollingGeneration currentGeneration = this.ensurePollingGeneration();
        if (currentGeneration == null) {
            return Collections.emptyList();
        }

        PollResult result;
        try {
            result = currentGeneration.pollResults.poll(
                this.kafkaPollWait.toMillis(),
                TimeUnit.MILLISECONDS);
        } catch (InterruptedException exception) {
            if (currentGeneration.stopping.get()) {
                return Collections.emptyList();
            }
            Thread.currentThread().interrupt();
            throw new ConnectException("Interrupted while waiting for source poll result", exception);
        }

        if (result == null) {
            if (currentGeneration.readerFinished.get()
                && !currentGeneration.stopping.get()) {
                Throwable terminalError = currentGeneration.terminalError.get();
                if (terminalError != null) {
                    throwPollError(terminalError, false);
                }
                throw new ConnectException("Background source polling thread stopped unexpectedly");
            }
            return Collections.emptyList();
        }

        if (result.error != null) {
            throwPollError(result.error, currentGeneration.stopping.get());
        }
        return result.records;
    }

    @Override
    public final synchronized void stop() {
        PollingGeneration currentGeneration = this.generation.getAndSet(null);
        if (currentGeneration != null) {
            currentGeneration.stopping.set(true);
        }

        try {
            this.stopTaskOnce();
        } finally {
            this.taskProperties = null;
            if (currentGeneration != null) {
                currentGeneration.executor.shutdownNow();
                this.awaitPollingThreadShutdown(currentGeneration.executor);
            }
        }
    }

    protected abstract void startTask(Map<String, String> props);

    protected abstract List<SourceRecord> pollTask();

    protected abstract void stopTask();

    protected String getPollingThreadName(Map<String, String> props) {
        return this.getClass().getSimpleName() + "-poll";
    }

    private PollingGeneration ensurePollingGeneration() {
        PollingGeneration currentGeneration = this.generation.get();
        if (currentGeneration != null || !this.taskStarted.get()) {
            return currentGeneration;
        }

        Map<String, String> properties = this.taskProperties;
        if (properties == null) {
            return null;
        }

        PollingGeneration newGeneration =
            new PollingGeneration(this.getPollingThreadName(properties));
        if (!this.generation.compareAndSet(null, newGeneration)) {
            newGeneration.executor.shutdownNow();
            return this.generation.get();
        }
        try {
            newGeneration.executor.execute(
                preserveMdc(() -> {
                    this.pollContinuously(newGeneration);
                    newGeneration.readerFinished.set(true);
                }));
            return newGeneration;
        } catch (RuntimeException error) {
            this.generation.compareAndSet(newGeneration, null);
            newGeneration.stopping.set(true);
            newGeneration.executor.shutdownNow();
            throw error;
        }
    }

    private void pollContinuously(PollingGeneration currentGeneration) {
        while (!currentGeneration.stopping.get()) {
            try {
                List<SourceRecord> records = this.pollTask();
                if (!publishResult(currentGeneration, PollResult.records(records))) {
                    return;
                }
            } catch (Exception error) {
                if (!currentGeneration.stopping.get()) {
                    currentGeneration.terminalError.set(error);
                    if (!publishResult(currentGeneration, PollResult.error(error))) {
                        return;
                    }
                    if (isRetriable(error) && waitBeforeRetry(currentGeneration)) {
                        currentGeneration.terminalError.compareAndSet(error, null);
                        continue;
                    }
                }
                return;
            }
        }
    }

    private boolean publishResult(
        PollingGeneration currentGeneration,
        PollResult result) {
        while (!currentGeneration.stopping.get()) {
            try {
                if (currentGeneration.pollResults.offer(result, 100, TimeUnit.MILLISECONDS)) {
                    return true;
                }
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
                return false;
            }
        }
        return false;
    }

    private void awaitPollingThreadShutdown(ExecutorService executor) {
        try {
            if (!executor.awaitTermination(
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

    private boolean waitBeforeRetry(PollingGeneration currentGeneration) {
        try {
            Thread.sleep(RETRIABLE_ERROR_BACKOFF.toMillis());
            return !currentGeneration.stopping.get();
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    private void stopTaskOnce() {
        if (this.taskStarted.compareAndSet(true, false)) {
            this.stopTask();
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

    private static final class PollingGeneration {
        private final BlockingQueue<PollResult> pollResults =
            new LinkedBlockingQueue<>(POLL_RESULT_CAPACITY);
        private final AtomicBoolean stopping = new AtomicBoolean();
        private final AtomicBoolean readerFinished = new AtomicBoolean();
        private final AtomicReference<Throwable> terminalError = new AtomicReference<>();
        private final ExecutorService executor;

        private PollingGeneration(String threadName) {
            this.executor = Executors.newSingleThreadExecutor(runnable -> {
                Thread thread = new Thread(runnable, threadName);
                thread.setDaemon(true);
                thread.setUncaughtExceptionHandler((ignored, error) -> {
                    this.terminalError.compareAndSet(null, error);
                    this.readerFinished.set(true);
                });
                return thread;
            });
        }
    }
}
