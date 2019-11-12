// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.directconnectivity.rntbd;

import io.netty.util.HashedWheelTimer;
import io.netty.util.Timeout;
import io.netty.util.Timer;
import io.netty.util.TimerTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;
import java.util.concurrent.TimeUnit;

import static com.google.common.base.Strings.lenientFormat;

public final class RntbdRequestTimer implements AutoCloseable {

    private static final long TIMER_RESOLUTION_IN_NANOS = 100_000_000L; // 100 ms // 5_000_000L;

    private static final Logger logger = LoggerFactory.getLogger(RntbdRequestTimer.class);
    private final long requestTimeout;
    private final Timer timer;

    public RntbdRequestTimer(final long requestTimeout) {
        // Inspection of the HashedWheelTimer code shows that expiration of a timeout task takes two timer resolution
        // units to complete
        this.timer = new HashedWheelTimer(TIMER_RESOLUTION_IN_NANOS, TimeUnit.NANOSECONDS);
        this.requestTimeout = requestTimeout;
    }

    public long getRequestTimeout(final TimeUnit unit) {
        return unit.convert(requestTimeout, TimeUnit.NANOSECONDS);
    }

    @Override
    public void close() {

        final Set<Timeout> timeouts = this.timer.stop();
        final int count = timeouts.size();

        if (count == 0) {
            logger.debug("no outstanding request timeout tasks");
            return;
        }

        logger.debug("stopping {} request timeout tasks", count);

        for (final Timeout timeout : timeouts) {
            if (!timeout.isExpired()) {
                try {
                    timeout.task().run(timeout);
                } catch (Throwable error) {
                    logger.warn(lenientFormat("request timeout task failed due to ", error));
                }
            }
        }

        logger.debug("{} request timeout tasks stopped", count);
    }

    public Timeout newTimeout(final TimerTask task) {
        return this.timer.newTimeout(task, this.requestTimeout, TimeUnit.NANOSECONDS);
    }
}
