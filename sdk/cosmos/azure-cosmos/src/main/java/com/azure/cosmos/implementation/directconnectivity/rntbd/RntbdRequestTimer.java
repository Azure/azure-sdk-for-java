// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.directconnectivity.rntbd;

import io.netty.util.HashedWheelTimer;
import io.netty.util.Timeout;
import io.netty.util.TimerTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public final class RntbdRequestTimer implements AutoCloseable {

    private static final long TIMER_RESOLUTION_IN_NANOS = 100_000_000L; // 100 ms

    private static final Logger logger = LoggerFactory.getLogger(RntbdRequestTimer.class);
    private final AtomicBoolean closed = new AtomicBoolean();

    private final long requestTimeoutInNanos;
    private final HashedWheelTimer timer;

    public RntbdRequestTimer(final long requestTimeoutInNanos) {
        // HashedWheelTimer code inspection shows that timeout tasks expire within two timer resolution units
        this.timer = new HashedWheelTimer(TIMER_RESOLUTION_IN_NANOS, TimeUnit.NANOSECONDS);
        this.requestTimeoutInNanos = requestTimeoutInNanos;
    }

    public long getRequestTimeout(final TimeUnit unit) {
        return unit.convert(this.requestTimeoutInNanos, TimeUnit.NANOSECONDS);
    }

    @Override
    public void close() {

        if (this.closed.compareAndSet(false, true)) {

            final Set<Timeout> timeouts = this.timer.stop();
            final int count = timeouts.size();

            if (count > 0) {

                for (final Timeout timeout : timeouts) {
                    if (!timeout.isExpired()) {
                        try {
                            timeout.task().run(timeout);
                        } catch (Throwable error) {
                            logger.warn("timeout task failed due to ", error);
                        }
                    }
                }
            }
        }
    }

    public Timeout newTimeout(final TimerTask task) {
        return this.timer.newTimeout(task, this.requestTimeoutInNanos, TimeUnit.NANOSECONDS);
    }
}
