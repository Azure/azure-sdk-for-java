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

public final class RntbdRequestTimer implements AutoCloseable {

    private static final Logger logger = LoggerFactory.getLogger(RntbdRequestTimer.class);
    private final long requestTimeoutInNanos;
    private final Timer timer;

    public RntbdRequestTimer(final long requestTimeoutInNanos, final long requestTimerResolutionInNanos) {
        // The HashWheelTimer code shows that cancellation of a timeout takes two timer resolution units to complete.
        this.timer = new HashedWheelTimer(requestTimerResolutionInNanos, TimeUnit.NANOSECONDS);
        this.requestTimeoutInNanos = requestTimeoutInNanos;
    }

    public long getRequestTimeout(final TimeUnit unit) {
        return unit.convert(requestTimeoutInNanos, TimeUnit.NANOSECONDS);
    }

    @Override
    public void close() {
        final Set<Timeout> timeouts = this.timer.stop();
        if (logger.isDebugEnabled()) {
            final int count = timeouts.size();
            if (count > 0) {
                logger.debug("request expiration tasks cancelled: {}", count);
            }
        }
    }

    public Timeout newTimeout(final TimerTask task) {
        return this.timer.newTimeout(task, this.requestTimeoutInNanos, TimeUnit.NANOSECONDS);
    }
}
