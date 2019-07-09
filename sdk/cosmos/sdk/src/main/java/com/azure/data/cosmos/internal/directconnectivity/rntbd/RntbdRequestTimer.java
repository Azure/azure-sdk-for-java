/*
 * The MIT License (MIT)
 * Copyright (c) 2018 Microsoft Corporation
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

package com.azure.data.cosmos.internal.directconnectivity.rntbd;

import io.netty.util.HashedWheelTimer;
import io.netty.util.Timeout;
import io.netty.util.Timer;
import io.netty.util.TimerTask;

import java.util.concurrent.TimeUnit;

public final class RntbdRequestTimer implements AutoCloseable {

    private static final long FIVE_MILLISECONDS = 5000000L;
    private final long requestTimeout;
    private final Timer timer;

    public RntbdRequestTimer(final long requestTimeout) {

        // Inspection of the HashWheelTimer code indicates that our choice of a 5 millisecond timer resolution ensures
        // a request will timeout within 10 milliseconds of the specified requestTimeout interval. This is because
        // cancellation of a timeout takes two timer resolution units to complete.

        this.timer = new HashedWheelTimer(FIVE_MILLISECONDS, TimeUnit.NANOSECONDS);
        this.requestTimeout = requestTimeout;
    }

    public long getRequestTimeout(TimeUnit unit) {
        return unit.convert(requestTimeout, TimeUnit.NANOSECONDS);
    }

    @Override
    public void close() throws RuntimeException {
        this.timer.stop();
    }

    public Timeout newTimeout(final TimerTask task) {
        return this.timer.newTimeout(task, this.requestTimeout, TimeUnit.NANOSECONDS);
    }
}
