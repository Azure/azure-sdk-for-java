package com.azure.cosmos.implementation.directconnectivity.rntbd;

import io.netty.util.HashedWheelTimer;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import static com.azure.cosmos.implementation.guava27.Strings.lenientFormat;

/**
 * A factory for creating daemon threads.
 * <p>
 * The default factory used by a {@link HashedWheelTimer} creates a user thread.
 */
class RntbdThreadFactory implements ThreadFactory {

    private static final String NAME_TEMPLATE = "cosmos-rntbd-%s[%s]";
    private final boolean daemon;
    private final String name;
    private final int priority;
    private final AtomicInteger threadCount;

    @SuppressWarnings("removal")
    RntbdThreadFactory(final String name, final boolean daemon, final int priority) {
        this.daemon = daemon;
        this.name = name;
        this.priority = priority;
        this.threadCount = new AtomicInteger();
    }

    @Override
    public Thread newThread(final Runnable runnable) {

        final String name = lenientFormat(NAME_TEMPLATE, this.name, this.threadCount.incrementAndGet());
        final Thread thread = new Thread(runnable, name);

        if (thread.isDaemon() != this.daemon) {
            thread.setDaemon(this.daemon);
        }

        if (thread.getPriority() != this.priority) {
            thread.setPriority(this.priority);
        }

        return thread;
    }
}
