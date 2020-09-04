// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.cpu;

import com.azure.cosmos.implementation.guava25.collect.ImmutableList;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class CpuMonitor implements AutoCloseable {

    private final ScheduledThreadPoolExecutor scheduledExecutorService;
    private final static int DefaultRefreshIntervalInSeconds = 10;
    private final static int HistoryLength = 6;

    private static Duration refreshInterval =
        Duration.ofSeconds(DefaultRefreshIntervalInSeconds);
    private CpuReader cpuReader;
    private ScheduledFuture<?> future;

    private boolean running = false;

    final ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();


//    private CancellationTokenSource cancellation;  // Guarded by rwLock.

    // CpuMonitor users get a copy of the internal buffer to avoid racing
    // against changes.
    private CpuLoadHistory currentReading;  // Guarded by rwLock.

//    private Task periodicTask;  // Guarded by rwLock.G

    public CpuMonitor() {
        scheduledExecutorService = new ScheduledThreadPoolExecutor(1);
        scheduledExecutorService.setRemoveOnCancelPolicy(true);
    }

    public void start() {

        this.throwIfDisposed();
        this.rwLock.writeLock().lock();
        this.future = scheduledExecutorService.schedule(() -> refresh(), refreshInterval.toSeconds(), TimeUnit.SECONDS);
        this.cpuReader = new CpuReader();
        this.rwLock.writeLock().unlock();
    }

    private void throwIfDisposed() {
    }


    public void stop() {
        future.cancel(false);
    }

    @Override
    public void close() throws Exception {
        scheduledExecutorService.shutdown();
    }


    //    private final static int DefaultRefreshIntervalInSeconds = 10;
//    private final static int HistoryLength = 6;
//    private static TimeSpan refreshInterval =
//        TimeSpan.FromSeconds(DefaultRefreshIntervalInSeconds);
//
//    private bool disposed = false;
//
//    private readonly ReaderWriterLockSlim rwLock =
//        new ReaderWriterLockSlim(LockRecursionPolicy.NoRecursion);
//    private CancellationTokenSource cancellation;  // Guarded by rwLock.
//
//    // CpuMonitor users get a copy of the internal buffer to avoid racing
//    // against changes.
//    private CpuLoadHistory currentReading;  // Guarded by rwLock.
//
//    private Task periodicTask;  // Guarded by rwLock.
//
//    // Allows tests to override the default refresh interval
//     static void OverrideRefreshInterval(Duration newRefreshInterval)
//    {
//        CpuMonitor.refreshInterval = newRefreshInterval;
//    }





    // Returns a read-only collection of CPU load measurements, or null if
    // no results are available yet.
    public CpuLoadHistory getCpuLoad() {
        this.throwIfDisposed();
        this.rwLock.readLock().lock();
        try {
            // throw if not initialized
//            if (this.periodicTask == null)
//            {
//                throw new InvalidOperationException("CpuMonitor was not started");
//            }
            return this.currentReading;
        } finally {
            this.rwLock.readLock().unlock();
        }
    }

    private void refresh() {
        Instant now = Instant.now();
        float currentUtilization = cpuReader.getSystemWideCpuUsage();

        CpuLoad[] buffer = new CpuLoad[CpuMonitor.HistoryLength];
        int clockHand = 0;

        if (!Float.isNaN(currentUtilization)) {
            List<CpuLoad> cpuLoadHistory = new ArrayList<>(buffer.length);
            CpuLoadHistory newReading = new CpuLoadHistory(
                cpuLoadHistory,
                CpuMonitor.refreshInterval);

            buffer[clockHand] = new CpuLoad(now, currentUtilization);
            clockHand = (clockHand + 1) % buffer.length;

            for (int i = 0; i < buffer.length; i++) {
                int index = (clockHand + i) % buffer.length;
                if (buffer[index].timestamp.equals(Instant.MIN)) {
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
    }
}
