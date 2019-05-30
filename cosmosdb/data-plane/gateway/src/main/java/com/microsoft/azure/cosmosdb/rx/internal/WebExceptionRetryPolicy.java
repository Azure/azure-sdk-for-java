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
 */

package com.microsoft.azure.cosmosdb.rx.internal;

import com.microsoft.azure.cosmosdb.internal.directconnectivity.WebExceptionUtility;
import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Single;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

public class WebExceptionRetryPolicy implements IRetryPolicy {
    private final static Logger logger = LoggerFactory.getLogger(WebExceptionRetryPolicy.class);

    // total wait time in seconds to retry. should be max of primary reconfigrations/replication wait duration etc
    private final static int waitTimeInSeconds = 30;
    private final static int initialBackoffSeconds = 1;
    private final static int backoffMultiplier = 2;

    private StopWatch durationTimer = new StopWatch();
    private int attemptCount = 1;
    // Don't penalise first retry with delay.
    private int currentBackoffSeconds = WebExceptionRetryPolicy.initialBackoffSeconds;

    public WebExceptionRetryPolicy() {
        durationTimer.start();
    }


    @Override
    public Single<ShouldRetryResult> shouldRetry(Exception exception) {
        Duration backoffTime = Duration.ofSeconds(0);

        if (!WebExceptionUtility.isWebExceptionRetriable(exception)) {
            // Have caller propagate original exception.
            this.durationTimer.stop();
            return Single.just(ShouldRetryResult.noRetry());
        }

        // Don't penalise first retry with delay.
        if (attemptCount++ > 1) {
            int remainingSeconds = WebExceptionRetryPolicy.waitTimeInSeconds - Math.toIntExact(this.durationTimer.getTime(TimeUnit.SECONDS));
            if (remainingSeconds <= 0) {
                this.durationTimer.stop();
                return Single.just(ShouldRetryResult.noRetry());
            }

            backoffTime = Duration.ofSeconds(Math.min(this.currentBackoffSeconds, remainingSeconds));
            this.currentBackoffSeconds *= WebExceptionRetryPolicy.backoffMultiplier;
        }

        logger.warn("Received retriable web exception, will retry", exception);

        return Single.just(ShouldRetryResult.retryAfter(backoffTime));
    }
}
