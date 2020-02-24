// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.cosmos.internal;

import reactor.core.publisher.Mono;

import java.time.Duration;

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
    Mono<ShouldRetryResult> shouldRetry(Exception e);


    class ShouldRetryResult {
        /// <summary>
        /// How long to wait before next retry. 0 indicates retry immediately.
        /// </summary>
        public final Duration backOffTime;
        public final Exception exception;
        public boolean shouldRetry;
        public final Quadruple<Boolean, Boolean, Duration, Integer> policyArg;

        private ShouldRetryResult(Duration dur, Exception e, boolean shouldRetry,
                Quadruple<Boolean, Boolean, Duration, Integer> policyArg) {
            this.backOffTime = dur;
            this.exception = e;
            this.shouldRetry = shouldRetry;
            this.policyArg = policyArg;
        }

        public static ShouldRetryResult retryAfter(Duration dur) {
            Utils.checkNotNullOrThrow(dur, "duration", "cannot be null");
            return new ShouldRetryResult(dur, null, true, null);
        }

        public static ShouldRetryResult retryAfter(Duration dur,
                Quadruple<Boolean, Boolean, Duration, Integer> policyArg) {
            Utils.checkNotNullOrThrow(dur, "duration", "cannot be null");
            return new ShouldRetryResult(dur, null, true, policyArg);
        }

        public static ShouldRetryResult error(Exception e) {
            Utils.checkNotNullOrThrow(e, "exception", "cannot be null");
            return new ShouldRetryResult(null, e, false, null);
        }

        public static ShouldRetryResult noRetry() {
            return new ShouldRetryResult(null, null, false, null);
        }

        public void throwIfDoneTrying(Exception capturedException) throws Exception {
            if (this.shouldRetry) {
                return;
            }

            if (this.exception == null) {
                throw capturedException;
            } else {
                throw this.exception;
            }
        }
    }
}
