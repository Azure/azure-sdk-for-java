// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.core.implementation.concurrent;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;

/**
 * Implementation of a {@link ThreadFactory} that creates threads with an {@code azure-sdk-pool} prefix.
 */
public final class AzureThreadFactory implements ThreadFactory {
    private static final AtomicInteger POOL_NUMBER = new AtomicInteger(1);
    private final ThreadGroup group;
    private final String namePrefix;

    private static final AtomicIntegerFieldUpdater<AzureThreadFactory> THREAD_NUMBER_UPDATER =
        AtomicIntegerFieldUpdater.newUpdater(AzureThreadFactory.class, "threadNumber");
    private volatile int threadNumber = 1;

    @SuppressWarnings("removal")
    public AzureThreadFactory() {
        SecurityManager s = System.getSecurityManager();
        group = (s != null) ? s.getThreadGroup() : Thread.currentThread().getThreadGroup();
        namePrefix = "azure-sdk-pool-" + POOL_NUMBER.getAndIncrement() + "-thread-";
    }

    public Thread newThread(Runnable r) {
        Thread t = new Thread(group, r, namePrefix + THREAD_NUMBER_UPDATER.getAndIncrement(this), 0);
        if (t.isDaemon()) {
            t.setDaemon(false);
        }

        if (t.getPriority() != Thread.NORM_PRIORITY) {
            t.setPriority(Thread.NORM_PRIORITY);
        }

        return t;
    }
}
