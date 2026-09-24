// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.kafka.connect.implementation.source;

import org.apache.kafka.connect.source.SourceRecord;
import org.apache.kafka.connect.source.SourceTask;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Base class for source tasks whose source read can block indefinitely.
 */
public abstract class BufferedSourceTask extends SourceTask {
    private static final long POLL_WAIT_MS = 1_000;
    private static final long THREAD_SHUTDOWN_WAIT_MS = 1_000;

    private final BlockingQueue<PollResult> pollResults = new LinkedBlockingQueue<>(1);
    private volatile boolean stopping;
    private Thread pollingThread;

    @Override
    public final synchronized void start(Map<String, String> props) {
        this.stopping = false;
        this.pollResults.clear();
        try {
            this.startTask(props);
            this.pollingThread = new Thread(this::pollContinuously);
            this.pollingThread.setDaemon(true);
        } catch (RuntimeException | Error error) {
            this.stopping = true;
            this.stopTask();
            throw error;
        }
    }

    @Override
    public final List<SourceRecord> poll() {
        this.startPollingThread();

        try {
            PollResult result = this.pollResults.poll(
                POLL_WAIT_MS,
                TimeUnit.MILLISECONDS);
            if (result == null) {
                return Collections.emptyList();
            }
            if (result.error != null) {
                throwPollingError(result.error);
            }
            return result.records;
        } catch (InterruptedException error) {
            Thread.currentThread().interrupt();
            return Collections.emptyList();
        }
    }

    @Override
    public final synchronized void stop() {
        if (this.stopping) {
            return;
        }
        this.stopping = true;
        try {
            this.stopTask();
        } finally {
            if (this.pollingThread != null) {
                this.pollingThread.interrupt();
                try {
                    this.pollingThread.join(THREAD_SHUTDOWN_WAIT_MS);
                } catch (InterruptedException error) {
                    Thread.currentThread().interrupt();
                }
                this.pollingThread = null;
            }
        }
    }

    protected abstract void startTask(Map<String, String> props);

    protected abstract List<SourceRecord> pollTask();

    protected abstract void stopTask();

    private synchronized void startPollingThread() {
        if (this.stopping || this.pollingThread.getState() != Thread.State.NEW) {
            return;
        }
        this.pollingThread.start();
    }

    private void pollContinuously() {
        while (!this.stopping) {
            try {
                List<SourceRecord> records = this.pollTask();
                this.pollResults.put(PollResult.records(records));
            } catch (RuntimeException error) {
                if (!this.stopping && !this.putPollingError(error)) {
                    return;
                }
            } catch (Error error) {
                if (!this.stopping) {
                    this.putPollingError(error);
                }
                return;
            } catch (InterruptedException error) {
                Thread.currentThread().interrupt();
                return;
            }
        }
    }

    private boolean putPollingError(Throwable error) {
        try {
            this.pollResults.put(PollResult.error(error));
            return true;
        } catch (InterruptedException interruptedError) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    private static void throwPollingError(Throwable error) {
        if (error instanceof RuntimeException) {
            throw (RuntimeException) error;
        }
        throw (Error) error;
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
