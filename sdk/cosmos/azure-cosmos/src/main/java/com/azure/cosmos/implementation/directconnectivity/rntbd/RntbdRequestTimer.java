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
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

public final class RntbdRequestTimer implements AutoCloseable {

    private static final Logger logger = LoggerFactory.getLogger(RntbdRequestTimer.class);

    private static final ThreadFactory threadFactory = new RntbdThreadFactory(
        "request-timer",
        true,
        Thread.NORM_PRIORITY);

    private final long tcpNetworkRequestTimeoutInNanos;
    private final Timer timer;

    public RntbdRequestTimer(final long tcpNetworkRequestTimeoutInNanos, final long requestTimerResolutionInNanos) {
        // The HashWheelTimer code shows that cancellation of a timeout takes two timer resolution units to complete
        this.timer = new HashedWheelTimer(threadFactory, requestTimerResolutionInNanos, TimeUnit.NANOSECONDS);
        this.tcpNetworkRequestTimeoutInNanos = tcpNetworkRequestTimeoutInNanos;
    }

    @Override
    public void close() {
        final Set<Timeout> cancelledTimeouts = this.timer.stop();
        logger.debug("request expiration tasks cancelled: {}", cancelledTimeouts.size());
    }

    public Timeout newTimeout(final TimerTask task) {
        return this.timer.newTimeout(task, this.tcpNetworkRequestTimeoutInNanos, TimeUnit.NANOSECONDS);
    }
}
