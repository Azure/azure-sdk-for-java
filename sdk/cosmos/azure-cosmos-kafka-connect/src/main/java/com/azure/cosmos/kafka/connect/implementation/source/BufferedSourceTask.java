// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.kafka.connect.implementation.source;

import org.apache.kafka.connect.source.SourceRecord;
import org.apache.kafka.connect.source.SourceTask;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;

/**
 * Base class for source tasks whose source read can block indefinitely.
 */
public abstract class BufferedSourceTask extends SourceTask {
    private static final long POLL_WAIT_MS = 1_000;
    private static final long THREAD_SHUTDOWN_WAIT_MS = 1_000;

    private final BlockingQueue<Object> pollResults = new SynchronousQueue<>();
    private volatile boolean stopping;
    private Thread pollingThread;

    @Override
    @SuppressWarnings("unchecked")
    public final List<SourceRecord> poll() {
        this.startPollingThread();

        try {
            Object result = this.pollResults.poll(
                POLL_WAIT_MS,
                TimeUnit.MILLISECONDS);
            if (result == null) {
                return Collections.emptyList();
            }
            if (result instanceof RuntimeException) {
                throw (RuntimeException) result;
            }
            if (result instanceof Error) {
                throw (Error) result;
            }
            return (List<SourceRecord>) result;
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

    protected abstract List<SourceRecord> pollTask();

    protected abstract void stopTask();

    private synchronized void startPollingThread() {
        if (this.stopping || this.pollingThread != null) {
            return;
        }
        this.pollingThread = new Thread(this::pollContinuously);
        this.pollingThread.setDaemon(true);
        this.pollingThread.start();
    }

    private void pollContinuously() {
        while (!this.stopping) {
            Object result;
            try {
                result = this.pollTask();
            } catch (RuntimeException | Error error) {
                result = error;
            }

            if (this.stopping) {
                return;
            }
            try {
                this.pollResults.put(result);
            } catch (InterruptedException error) {
                Thread.currentThread().interrupt();
                return;
            }
        }
    }
}
