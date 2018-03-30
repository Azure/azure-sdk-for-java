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

import rx.Single;

// TODO update documentation
/**
 * While this class is public, but it is not part of our published public APIs.
 * This is meant to be internally used only by our sdk.
 */
public interface IRetryPolicy  {
    // this capture all the retry logic
    // TODO: design decision should this return a single or an observable?

    /// <summary>
    /// Method that is called to determine from the policy that needs to retry on the exception
    /// </summary>
    /// <param name="exception">Exception during the callback method invocation</param>
    /// <param name="cancellationToken"></param>
    /// <returns>If the retry needs to be attempted or not</returns>
    public Single<ShouldRetryResult> shouldRetry(Exception e);

    public static class ShouldRetryResult {
        /// <summary>
        /// How long to wait before next retry. 0 indicates retry immediately.
        /// </summary>
        public final Duration backOffTime;
        public final Exception exception;
        public boolean shouldRetry;
                
        private ShouldRetryResult(Duration dur, Exception e, boolean shouldRetry) {
            this.backOffTime = dur;
            this.exception = e;
            this.shouldRetry = shouldRetry;
        }

        public static ShouldRetryResult retryAfter(Duration dur) {
            Utils.checkNotNullOrThrow(dur, "duration", "cannot be null");
            return new ShouldRetryResult(dur, null, true);
        }

        public static ShouldRetryResult error(Exception e) {
            Utils.checkNotNullOrThrow(e, "exception", "cannot be null");
            return new ShouldRetryResult(null, e, false);
        }

        public static ShouldRetryResult noRetry() {
            return new ShouldRetryResult(null, null, false);
        }
    }
}
