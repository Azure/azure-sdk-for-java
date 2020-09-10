// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.cpu;


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
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Monitors history of CPU consumption. This is a singleton class and can support multiple cosmos clients.
 *
 * is used for tracking multiple cosmos clients registers to this CPU monitor.
 * in the absence of a listener the CpuMonitor will shutdown.
 */
public class CpuMonitor {
    private final static int DEFAULT_REFRESH_INTERVAL_IN_SECONDS = 5;
    private final static int HISTORY_LENGTH = 6;
    private static Duration refreshInterval = Duration.ofSeconds(DEFAULT_REFRESH_INTERVAL_IN_SECONDS);

    private static final Logger logger = LoggerFactory.getLogger(CpuMonitor.class);
    private static final CpuReader cpuReader = new CpuReader();
    private static final ScheduledThreadPoolExecutor scheduledExecutorService = new ScheduledThreadPoolExecutor(1, new DaemonThreadFactory());

    static {
        scheduledExecutorService.setRemoveOnCancelPolicy(true);
    }

    private static final ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();

    // used for tracking the cosmos clients.
    private static final List<WeakReference<CpuListener>> cpuListeners = new ArrayList<>();
    private static final Object lifeCycleLock = new Object();

    private static final CpuLoadHistory DEFAULT_READING = new CpuLoadHistory(Collections.emptyList(), refreshInterval);

    private static CpuLoadHistory currentReading = DEFAULT_READING; // Guarded by rwLock.
    private static final CpuLoad[] buffer = new CpuLoad[CpuMonitor.HISTORY_LENGTH];

    private static ScheduledFuture<?> future;

    // CpuMonitor users get a copy of the internal buffer to avoid racing
    // against changes.
    private static int clockHand = 0;

    /**
     * any client interested in receiving cpu info should implement {@link CpuListener}.
     *
     * and invoke {@link CpuMonitor#register(CpuListener)} when starting up and
     * {@link CpuMonitor#unregister(CpuListener)} } when shutting down.
     *
     * This is merely is used as a singal to {@link CpuMonitor} to control whether it should keep using
     * its internal thread or it it should shut it down in the absence of any CosmosClient.
     * @param listener interested in cpu update.
     */
    public static void register(CpuListener listener) {
        synchronized (lifeCycleLock) {
            if (cpuListeners.size() == 0) {
                start();
            }

            cpuListeners.add(new WeakReference<>(listener));
        }
    }

    /**
     * any client interested in receiving cpu info should implement {@link CpuListener}.
     *
     * and invoke {@link CpuMonitor#register(CpuListener)} when starting up and
     * {@link CpuMonitor#unregister(CpuListener)} } when shutting down.
     *
     * This is merely is used as a singal to {@link CpuMonitor} to control whether it should keep using
     * its internal thread or it it should shut it down in the absence of any CosmosClient.
     * @param listener the listener which is not interested in the cpu update anymore.
     */
    public static void unregister(CpuListener listener) {
        synchronized (lifeCycleLock) {
            Iterator<WeakReference<CpuListener>> it = cpuListeners.iterator();

            while (it.hasNext()) {
                WeakReference<CpuListener> reference = it.next();
                CpuListener val = reference.get();

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

    private static class DaemonThreadFactory implements ThreadFactory {

        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(r);
            t.setDaemon(true);
            return t;
        }
    }

}
