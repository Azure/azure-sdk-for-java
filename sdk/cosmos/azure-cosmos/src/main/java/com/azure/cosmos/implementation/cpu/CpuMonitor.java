// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.cpu;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Monitors history of CPU consumption.
 */
public class CpuMonitor implements AutoCloseable {
    private final static int DEFAULT_REFRESH_INTERVAL_IN_SECONDS = 10;
    private final static int HISTORY_LENGTH = 6;

    private static Duration refreshInterval = Duration.ofSeconds(DEFAULT_REFRESH_INTERVAL_IN_SECONDS);

    /*
     * there is a singleton instance of {@link CpuMonitor}.
     * If there are multiple clients they will share the same instance.
     */
    private static final AtomicInteger cnt = new AtomicInteger(0);

    private static CpuMonitor instance;

    private final Logger logger = LoggerFactory.getLogger(CpuMonitor.class);
    private final ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();
    private final ScheduledThreadPoolExecutor scheduledExecutorService;
    private final CpuReader cpuReader;
    private final AtomicBoolean isClosed = new AtomicBoolean(false);

    private ScheduledFuture<?> future;

    // CpuMonitor users get a copy of the internal buffer to avoid racing
    // against changes.
    private CpuLoad[] buffer = new CpuLoad[CpuMonitor.HISTORY_LENGTH];
    private int clockHand = 0;

    private CpuLoadHistory currentReading; // Guarded by rwLock.

    /**
     * Gets a singleton instance of CpuMonitor, if no instance available will initialize one. The caller needs to ensure
     * that CPUMonitor#close() is invoked when this instance is not needed anymore.
     *
     * @return CpuMonitor an instance of CpuMonitor.
     */
    public static CpuMonitor initializeAndGet() {
        synchronized (CpuMonitor.class) {
            if (cnt.getAndIncrement() <= 0) {
                instance = new CpuMonitor();
                instance.start();
            }

            assert instance != null;
            return instance;
        }
    }

    /**
     * Gets a reference to the existing instance of CPUMonitor
     *
     * @return CpuMonitor reference.
     */
    public static CpuMonitor getInstanceReference() {
        if (instance == null) {
            throw new IllegalStateException("CPUMonitor not initialized");
        }

        return instance;
    }

    private CpuMonitor() {
        this.cpuReader = new CpuReader();
        this.scheduledExecutorService = new ScheduledThreadPoolExecutor(1);
        this.scheduledExecutorService.setRemoveOnCancelPolicy(true);
    }

    @Override
    public void close() {
        synchronized (CpuMonitor.class) {
            if (isClosed.getAndSet(true)) {
                return;
            }

            if (cnt.decrementAndGet() <= 0) {
                closeInternal();
            }
        }
    }

    private void closeInternal() {
        this.rwLock.writeLock().lock();
        try {
            this.scheduledExecutorService.shutdown();
            instance = null;
        } finally {
            this.rwLock.writeLock().unlock();
        }
    }

    // Returns a read-only collection of CPU load measurements, or null if
    // no results are available yet.
    public CpuLoadHistory getCpuLoad() {
        this.rwLock.readLock().lock();
        try {
            return this.currentReading;
        } finally {
            this.rwLock.readLock().unlock();
        }
    }

    private void refresh() {
        try {
            Instant now = Instant.now();
            float currentUtilization = (float) cpuReader.getSystemWideCpuUsage() * 100;

            if (!Float.isNaN(currentUtilization) && currentUtilization >= 0) {
                List<CpuLoad> cpuLoadHistory = new ArrayList<>(buffer.length);
                CpuLoadHistory newReading = new CpuLoadHistory(
                    cpuLoadHistory,
                    CpuMonitor.refreshInterval);

                buffer[clockHand] = new CpuLoad(now, currentUtilization);
                clockHand = (clockHand + 1) % buffer.length;

                for (int i = 0; i < buffer.length; i++) {
                    int index = (clockHand + i) % buffer.length;

                    if (buffer[index] != null && buffer[index].timestamp != null && !buffer[index].timestamp.equals(Instant.MIN)) {
                        cpuLoadHistory.add(buffer[index]);
                    }
                }

                this.rwLock.writeLock().lock();
                try {
                    this.currentReading = newReading;
                } finally {
                    this.rwLock.writeLock().unlock();
                }
            }
        } catch (Exception e) {
            logger.error("Failed to refresh the cpu history", e);
        }
    }

    private void start() {
        this.rwLock.writeLock().lock();
        try {
            // sets a dummy value for current reading.
            this.currentReading = new CpuLoadHistory(Collections.emptyList(), refreshInterval);
            this.future = scheduledExecutorService.scheduleAtFixedRate(
                () -> refresh(),
                0,
                refreshInterval.toMillis() / 1000,
                TimeUnit.SECONDS);
        } finally {
            this.rwLock.writeLock().unlock();
        }
    }
}
