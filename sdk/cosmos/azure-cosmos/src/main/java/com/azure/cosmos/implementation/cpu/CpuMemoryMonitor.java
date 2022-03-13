// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.cpu;


import com.azure.cosmos.implementation.Configs;
import com.azure.cosmos.implementation.CosmosDaemonThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.ref.WeakReference;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Monitors history of CPU consumption. This is a singleton class and can support multiple cosmos clients.
 *
 * is used for tracking multiple cosmos clients registers to this CPU monitor.
 * in the absence of a listener the CpuMemoryMonitor will shutdown.
 */
public class CpuMemoryMonitor {
    private final static int DEFAULT_REFRESH_INTERVAL_IN_SECONDS = 5;
    private final static int HISTORY_LENGTH = 6;
    private static Duration refreshInterval = Duration.ofSeconds(DEFAULT_REFRESH_INTERVAL_IN_SECONDS);

    private static final Logger logger = LoggerFactory.getLogger(CpuMemoryMonitor.class);
    private static final CpuMemoryReader CPU_MEMORY_READER = new CpuMemoryReader();
    private static final ScheduledThreadPoolExecutor scheduledExecutorService =
        new ScheduledThreadPoolExecutor(1, new CosmosDaemonThreadFactory("CpuMemoryMonitor"));

    static {
        scheduledExecutorService.setRemoveOnCancelPolicy(true);
    }

    private static final ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();

    // used for tracking the cosmos clients.
    private static final List<WeakReference<CpuMemoryListener>> cpuListeners = new ArrayList<>();
    private static final Object lifeCycleLock = new Object();

    private static final CpuLoadHistory DEFAULT_READING = new CpuLoadHistory(Collections.emptyList(), refreshInterval);

    private static CpuLoadHistory currentReading = DEFAULT_READING; // Guarded by rwLock.
    private static final CpuLoad[] buffer = new CpuLoad[CpuMemoryMonitor.HISTORY_LENGTH];
    private static final int clientTelemetryLength = Configs.getClientTelemetrySchedulingInSec()/DEFAULT_REFRESH_INTERVAL_IN_SECONDS;
    private static double[] clientTelemetryCpuLatestList = new double[clientTelemetryLength];
    private static double[] clientTelemetryMemoryLatestList = new double[clientTelemetryLength];

    private static ScheduledFuture<?> future;

    // CpuMemoryMonitor users get a copy of the internal buffer to avoid racing
    // against changes.
    private static int clockHand = 0;

    private static int clientTelemetryIndex = 0;

    /**
     * any client interested in receiving cpu info should implement {@link CpuMemoryListener}.
     *
     * and invoke {@link CpuMemoryMonitor#register(CpuMemoryListener)} when starting up and
     * {@link CpuMemoryMonitor#unregister(CpuMemoryListener)} } when shutting down.
     *
     * This is merely is used as a singal to {@link CpuMemoryMonitor} to control whether it should keep using
     * its internal thread or it it should shut it down in the absence of any CosmosClient.
     * @param listener interested in cpu update.
     */
    public static void register(CpuMemoryListener listener) {
        synchronized (lifeCycleLock) {
            if (cpuListeners.size() == 0) {
                start();
            }

            cpuListeners.add(new WeakReference<>(listener));
        }
    }

    /**
     * any client interested in receiving cpu info should implement {@link CpuMemoryListener}.
     *
     * and invoke {@link CpuMemoryMonitor#register(CpuMemoryListener)} when starting up and
     * {@link CpuMemoryMonitor#unregister(CpuMemoryListener)} } when shutting down.
     *
     * This is merely is used as a singal to {@link CpuMemoryMonitor} to control whether it should keep using
     * its internal thread or it it should shut it down in the absence of any CosmosClient.
     * @param listener the listener which is not interested in the cpu update anymore.
     */
    public static void unregister(CpuMemoryListener listener) {
        synchronized (lifeCycleLock) {
            Iterator<WeakReference<CpuMemoryListener>> it = cpuListeners.iterator();

            while (it.hasNext()) {
                WeakReference<CpuMemoryListener> reference = it.next();
                CpuMemoryListener val = reference.get();

                if (val == null || val == listener) {
                    it.remove();
                }
            }

            if (cpuListeners.isEmpty()) {
                closeInternal();
            }
        }
    }

    // Returns a read-only collection of CPU load measurements, or null if
    // no results are available yet.
    public static CpuLoadHistory getCpuLoad() {
        rwLock.readLock().lock();
        try {
            return currentReading;
        } finally {
            rwLock.readLock().unlock();
        }
    }

    // Returns a clientTelemetryCpuHistogram for percentile creation
    public static double[] getClientTelemetryCpuLatestList() {
        rwLock.readLock().lock();
        try {
            return clientTelemetryCpuLatestList;
        } finally {
            rwLock.readLock().unlock();
        }
    }

    // Returns a clientTelemetryMemoryHistogram for percentile creation
    public static double[] getClientTelemetryMemoryLatestList() {
        rwLock.readLock().lock();
        try {
            return clientTelemetryMemoryLatestList;
        } finally {
            rwLock.readLock().unlock();
        }
    }

    private static void closeInternal() {
        synchronized (lifeCycleLock) {
            if (future != null) {
                future.cancel(false);
                future = null;
            }

            rwLock.writeLock().lock();
            try {
                // sets a dummy value for current reading.
                currentReading = DEFAULT_READING;
            } finally {
                rwLock.writeLock().unlock();
            }
        }
    }

    private static void refresh() {
        try {
            Instant now = Instant.now();
            float currentCpuUtilization = CPU_MEMORY_READER.getSystemWideCpuUsage() * 100;
            float currentMemoryUtilization = CPU_MEMORY_READER.getSystemWideMemoryUsage();

            if (!Float.isNaN(currentCpuUtilization) && currentCpuUtilization >= 0) {
                List<CpuLoad> cpuLoadHistory = new ArrayList<>(buffer.length);
                CpuLoadHistory newReading = new CpuLoadHistory(
                    cpuLoadHistory,
                    CpuMemoryMonitor.refreshInterval);

                buffer[clockHand] = new CpuLoad(now, currentCpuUtilization);
                clockHand = (clockHand + 1) % buffer.length;

                clientTelemetryCpuLatestList[clientTelemetryIndex] = currentCpuUtilization;
                clientTelemetryMemoryLatestList[clientTelemetryIndex] = currentMemoryUtilization;
                clientTelemetryIndex = (clientTelemetryIndex + 1) % clientTelemetryLength;

                for (int i = 0; i < buffer.length; i++) {
                    int index = (clockHand + i) % buffer.length;

                    if (buffer[index] != null && buffer[index].timestamp != null && !buffer[index].timestamp.equals(Instant.MIN)) {
                        cpuLoadHistory.add(buffer[index]);
                    }
                }

                synchronized (lifeCycleLock) {
                    unregister(null);
                }

                rwLock.writeLock().lock();
                try {
                    currentReading = newReading;
                } finally {
                    rwLock.writeLock().unlock();
                }
            }
        } catch (Exception e) {
            logger.error("Failed to refresh the cpu history", e);
        }
    }

    private static void start() {
        synchronized (lifeCycleLock) {
            rwLock.writeLock().lock();
            try {
                // sets a dummy value for current reading.
                currentReading = DEFAULT_READING;
                future = scheduledExecutorService.scheduleAtFixedRate(
                    () -> refresh(),
                    0,
                    refreshInterval.toMillis() / TimeUnit.SECONDS.toMillis(1),
                    TimeUnit.SECONDS);
            } finally {
                rwLock.writeLock().unlock();
            }
        }
    }
}
