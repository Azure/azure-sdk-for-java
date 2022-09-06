// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.exporter.implementation.utils;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public final class ThreadPoolUtils {

    /**
     * {@code poolName} will be appended with a hyphen and the unique name.
     *
     * @param clazz The class holding the thread pool
     * @param uniqueId The identifier of the instance of {@code clazz}
     */
    public static ThreadFactory createDaemonThreadFactory(Class<?> clazz, String uniqueId) {
        return createNamedDaemonThreadFactory(String.format("%s_%s", clazz.getSimpleName(), uniqueId));
    }

    public static ThreadFactory createDaemonThreadFactory(Class<?> clazz) {
        return createNamedDaemonThreadFactory(clazz.getSimpleName());
    }

    public static ThreadFactory createNamedDaemonThreadFactory(String poolName) {
        return new ThreadFactory() {
            private final AtomicInteger threadId = new AtomicInteger();

            @Override
            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r);
                thread.setName(String.format("%s-%d", poolName, threadId.getAndIncrement()));
                thread.setDaemon(true);
                return thread;
            }
        };
    }

    private ThreadPoolUtils() {
    }
}
