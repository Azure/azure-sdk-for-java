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
    private final AtomicReference<Throwable> pollingError = new AtomicReference<>();
    private final AtomicBoolean startInvoked = new AtomicBoolean();
    private final AtomicBoolean started = new AtomicBoolean();
    private final AtomicBoolean stopping = new AtomicBoolean();
    private final AtomicBoolean readerStarted = new AtomicBoolean();
    private final AtomicBoolean readerFinished = new AtomicBoolean();
    private ExecutorService pollingExecutor;
    private String pollingThreadName;

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
        if (!this.startInvoked.compareAndSet(false, true)) {
            throw new ConnectException("Source task has already been started");
        }
        this.started.set(true);

        this.stopping.set(false);
        this.readerFinished.set(false);
        this.pollResults.clear();
        this.pollingError.set(null);
        this.pollingThreadName = this.getPollingThreadName(props);

        try {
            this.startTask(props);
            this.pollingExecutor = Executors.newSingleThreadExecutor(runnable -> {
                Thread thread = new Thread(runnable, this.pollingThreadName);
                thread.setDaemon(true);
                thread.setUncaughtExceptionHandler((ignored, error) -> {
                    this.pollingError.compareAndSet(null, error);
                    this.readerFinished.set(true);
                });
                return thread;
            });
        } catch (RuntimeException | Error error) {
            this.stopTaskOnce();
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
            if (this.stopping.get()) {
                return Collections.emptyList();
            }
            Thread.currentThread().interrupt();
            throw new ConnectException("Interrupted while waiting for source poll result", exception);
        }

        if (result == null) {
            this.checkPollingError();
            if (this.readerFinished.get() && !this.stopping.get()) {
                throw new ConnectException("Background source polling thread stopped unexpectedly");
            }
            return Collections.emptyList();
        }

        if (result.error != null) {
            throwPollError(result.error, this.stopping.get());
        }
        return result.records;
    }

    @Override
    public final synchronized void stop() {
        if (!this.stopping.compareAndSet(false, true)) {
            return;
        }

        try {
            this.stopTaskOnce();
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
        if (!this.started.get() || this.stopping.get() || this.readerStarted.get()) {
            return;
        }
        if (this.readerStarted.compareAndSet(false, true)) {
            try {
                this.pollingExecutor.execute(
                    preserveMdc(() -> {
                        try {
                            this.pollContinuously();
                        } finally {
                            this.readerFinished.set(true);
                        }
                    }));
            } catch (RuntimeException error) {
                if (!this.stopping.get()) {
                    throw error;
                }
            }
        }
    }

    private void pollContinuously() {
        while (!this.stopping.get()) {
            try {
                List<SourceRecord> records = this.pollTask();
                if (!this.publishResult(PollResult.records(records))) {
                    return;
                }
            } catch (Exception error) {
                if (this.stopping.get()) {
                    return;
                }

                this.pollingError.set(error);
                if (!this.publishResult(PollResult.error(error))) {
                    return;
                }
                this.pollingError.compareAndSet(error, null);

                if (!isRetriable(error) || !this.waitBeforeRetry()) {
                    return;
                }
            }
        }
    }

    private boolean publishResult(PollResult result) {
        while (!this.stopping.get()) {
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

    private void checkPollingError() {
        Throwable error = this.pollingError.get();
        if (error != null) {
            throwPollError(error, this.stopping.get());
        }
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
            return !this.stopping.get();
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    private void stopTaskOnce() {
        if (this.started.compareAndSet(true, false)) {
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
}
