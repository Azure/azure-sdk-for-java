// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.kafka.connect.implementation.source;

import org.apache.kafka.connect.errors.ConnectException;
import org.apache.kafka.connect.errors.RetriableException;
import org.apache.kafka.connect.source.SourceRecord;
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
 * Runs the blocking Cosmos change-feed request outside Kafka Connect's task thread.
 *
 * <p>Kafka Connect polls a capacity-one result queue and therefore regains control regularly
 * for pause and shutdown. The background reader is generation-scoped so an old reader can
 * never publish records into a replacement task generation.
 */
public class BufferedCosmosSourceTask extends CosmosSourceTask {
    private static final Logger LOGGER = LoggerFactory.getLogger(BufferedCosmosSourceTask.class);
    private static final int POLL_RESULT_CAPACITY = 1;
    private static final Duration KAFKA_POLL_WAIT = Duration.ofSeconds(1);
    private static final Duration POLLING_THREAD_SHUTDOWN_WAIT = Duration.ofSeconds(1);
    private static final Duration RETRIABLE_ERROR_BACKOFF = Duration.ofMillis(100);

    private final Duration kafkaPollWait;
    private final Duration pollingThreadShutdownWait;
    private final AtomicBoolean delegateStarted = new AtomicBoolean();
    private final AtomicReference<PollingGeneration> generation = new AtomicReference<>();

    public BufferedCosmosSourceTask() {
        this(KAFKA_POLL_WAIT, POLLING_THREAD_SHUTDOWN_WAIT);
    }

    BufferedCosmosSourceTask(
        Duration kafkaPollWait,
        Duration pollingThreadShutdownWait) {
        this.kafkaPollWait = kafkaPollWait;
        this.pollingThreadShutdownWait = pollingThreadShutdownWait;
    }

    @Override
    public synchronized void start(Map<String, String> props) {
        if (!this.delegateStarted.compareAndSet(false, true)) {
            throw new ConnectException("Cosmos source task is already running");
        }

        try {
            this.startDelegate(props);
            String taskId = props.getOrDefault(CosmosSourceTaskConfig.SOURCE_TASK_ID, "unknown");
            PollingGeneration newGeneration = new PollingGeneration(taskId);
            if (!this.generation.compareAndSet(null, newGeneration)) {
                newGeneration.executor.shutdownNow();
                throw new ConnectException("Cosmos polling executor is already running");
            }

            newGeneration.executor.execute(
                preserveMdc(() -> {
                    this.pollContinuously(newGeneration);
                    newGeneration.readerFinished.set(true);
                }));
        } catch (RuntimeException error) {
            PollingGeneration failedGeneration = this.generation.getAndSet(null);
            if (failedGeneration != null) {
                failedGeneration.stopping.set(true);
                failedGeneration.executor.shutdownNow();
            }
            this.stopDelegateOnce();
            throw error;
        }
    }

    @Override
    public List<SourceRecord> poll() {
        PollingGeneration currentGeneration = this.generation.get();
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
            throw new ConnectException("Interrupted while waiting for Cosmos poll result", exception);
        }

        if (result == null) {
            if (currentGeneration.readerFinished.get()
                && !currentGeneration.stopping.get()) {
                Throwable terminalError = currentGeneration.terminalError.get();
                if (terminalError != null) {
                    throwPollError(terminalError, false);
                }
                throw new ConnectException("Cosmos background polling thread stopped unexpectedly");
            }
            return Collections.emptyList();
        }

        if (result.error != null) {
            throwPollError(result.error, currentGeneration.stopping.get());
        }
        return result.records;
    }

    @Override
    public synchronized void stop() {
        PollingGeneration currentGeneration = this.generation.getAndSet(null);
        if (currentGeneration != null) {
            currentGeneration.stopping.set(true);
        }

        try {
            // Release this task's client-cache ownership before interrupting the reader.
            this.stopDelegateOnce();
        } finally {
            if (currentGeneration != null) {
                // Reactor's blocking subscriber responds to interruption; cached client closure
                // remains deferred until the cache refcount and idle-TTL conditions are met.
                currentGeneration.executor.shutdownNow();
                this.awaitPollingThreadShutdown(currentGeneration.executor);
            }
        }
    }

    void startDelegate(Map<String, String> props) {
        super.start(props);
    }

    List<SourceRecord> pollDelegate() {
        return super.poll();
    }

    void stopDelegate() {
        super.stop();
    }

    private void pollContinuously(PollingGeneration currentGeneration) {
        while (!currentGeneration.stopping.get()) {
            try {
                List<SourceRecord> records = this.pollDelegate();
                if (!publishResult(currentGeneration, PollResult.records(records))) {
                    return;
                }
            } catch (Exception error) {
                if (!currentGeneration.stopping.get()) {
                    if (!publishResult(currentGeneration, PollResult.error(error))) {
                        return;
                    }
                    if (isRetriable(error) && waitBeforeRetry(currentGeneration)) {
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
                    "Cosmos polling thread did not stop within {} ms after cancellation",
                    this.pollingThreadShutdownWait.toMillis());
            }
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            LOGGER.warn("Interrupted while waiting for Cosmos polling thread shutdown");
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

    private void stopDelegateOnce() {
        if (this.delegateStarted.compareAndSet(true, false)) {
            this.stopDelegate();
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
            throw new ConnectException("Cosmos background poll was interrupted", error);
        }
        if (error instanceof RuntimeException) {
            throw (RuntimeException) error;
        }
        if (error instanceof Error) {
            throw (Error) error;
        }
        throw new ConnectException("Cosmos change-feed poll failed", error);
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

        private PollingGeneration(String taskId) {
            this.executor = Executors.newSingleThreadExecutor(runnable -> {
                Thread thread = new Thread(
                    runnable,
                    "cosmos-change-feed-poll-" + taskId);
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
