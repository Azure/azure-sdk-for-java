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

import java.time.Duration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.microsoft.azure.cosmosdb.DocumentClientException;
import com.microsoft.azure.cosmosdb.internal.HttpConstants;

import rx.Single;

/**
 * While this class is public, but it is not part of our published public APIs.
 * This is meant to be internally used only by our sdk.
 */
public class ResourceThrottleRetryPolicy implements IDocumentClientRetryPolicy{

    private final static Logger logger = LoggerFactory.getLogger(ResourceThrottleRetryPolicy.class);

    private final static int DefaultMaxWaitTimeInSeconds = 60;
    private final static int DefaultRetryInSeconds = 5;
    private final int backoffDelayFactor;
    private final int maxAttemptCount;
    private final Duration maxWaitTime;

    // TODO: is this thread safe?
    // should we make this atomic int?
    private int currentAttemptCount;
    private Duration cumulativeRetryDelay;

    public ResourceThrottleRetryPolicy(int maxAttemptCount, int maxWaitTimeInSeconds) {
        this(maxAttemptCount, maxWaitTimeInSeconds, 1);
    }

    public ResourceThrottleRetryPolicy(int maxAttemptCount) {
        this(maxAttemptCount, DefaultMaxWaitTimeInSeconds, 1);
    }

    public ResourceThrottleRetryPolicy(int maxAttemptCount, int maxWaitTimeInSeconds, int backoffDelayFactor) {
        Utils.checkStateOrThrow(maxWaitTimeInSeconds < Integer.MAX_VALUE / 1000, "maxWaitTimeInSeconds", "maxWaitTimeInSeconds must be less than " + Integer.MAX_VALUE / 1000);

        this.maxAttemptCount = maxAttemptCount;
        this.backoffDelayFactor = backoffDelayFactor;
        this.maxWaitTime = Duration.ofSeconds(maxWaitTimeInSeconds); 
        this.currentAttemptCount = 0;
        this.cumulativeRetryDelay = Duration.ZERO;
    }

    @Override
    public Single<ShouldRetryResult> shouldRetry(Exception exception) {
        Duration retryDelay = Duration.ZERO;

        if (this.currentAttemptCount < this.maxAttemptCount &&
                (retryDelay = checkIfRetryNeeded(exception)) != null) {
            this.currentAttemptCount++;
            logger.warn(
                    "Operation will be retried after {} milliseconds. Current attempt {}, Cumulative delay {} Exception: {}", 
                    retryDelay.toMillis(), 
                    this.currentAttemptCount,
                    this.cumulativeRetryDelay,
                    exception);
            return Single.just(ShouldRetryResult.retryAfter(retryDelay));
        } else {
            logger.debug(
                    "Operation will NOT be retried. Current attempt {}, Exception: {} ", 
                    this.currentAttemptCount, 
                    exception);
            return Single.just(ShouldRetryResult.noRetry());
        }
    }

    @Override
    public void onBeforeSendRequest(RxDocumentServiceRequest request) {
        // no op
    }

    // if retry not needed reaturns null
    /// <summary>
    /// Returns True if the given exception <paramref name="exception"/> is retriable
    /// </summary>
    /// <param name="exception">Exception to examine</param>
    /// <param name="retryDelay">retryDelay</param>
    /// <returns>True if the exception is retriable; False otherwise</returns>
    private Duration checkIfRetryNeeded(Exception exception) {
        Duration retryDelay = Duration.ZERO;

        DocumentClientException dce = Utils.as(exception, DocumentClientException.class);

        if (dce != null){

            if (Exceptions.isStatusCode(dce, HttpConstants.StatusCodes.TOO_MANY_REQUESTS))  {
                retryDelay = Duration.ofMillis(dce.getRetryAfterInMilliseconds());
                if (this.backoffDelayFactor > 1) {
                    retryDelay = Duration.ofNanos(retryDelay.toNanos() * this.backoffDelayFactor);
                }

                if (retryDelay.toMillis() < this.maxWaitTime.toMillis() &&
                        this.maxWaitTime.toMillis() >= (this.cumulativeRetryDelay = retryDelay.plus(this.cumulativeRetryDelay)).toMillis())
                {
                    if (retryDelay == Duration.ZERO){
                        // we should never reach here as BE should turn non-zero of retryDelay
                        logger.trace("Received retryDelay of 0 with Http 429: {}", exception);
                        retryDelay = Duration.ofSeconds(DefaultRetryInSeconds);
                    }

                    return retryDelay;
                }
            }
        }
        // if retry not needed returns null
        return null;
    }
}
