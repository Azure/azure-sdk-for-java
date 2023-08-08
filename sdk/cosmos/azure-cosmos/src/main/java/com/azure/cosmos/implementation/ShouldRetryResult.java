// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation;

import java.time.Duration;

public class ShouldRetryResult {
    public final static ShouldRetryResult NO_RETRY =
        ShouldRetryResult.noRetry();
    public final static ShouldRetryResult RETRY_NOW =
        ShouldRetryResult.retryAfter(Duration.ZERO);

    /// <summary>
    /// How long to wait before next retry. 0 indicates retry immediately.
    /// </summary>
    public final Duration backOffTime;
    public final Exception exception;
    public final Quadruple<Boolean, Boolean, Duration, Integer> policyArg;
    public boolean shouldRetry;
    public boolean nonRelatedException;

    private ShouldRetryResult(Duration dur, Exception e, boolean shouldRetry,
                              Quadruple<Boolean, Boolean, Duration, Integer> policyArg, boolean nonRelatedException) {
        this.backOffTime = dur;
        this.exception = e;
        this.shouldRetry = shouldRetry;
        this.policyArg = policyArg;
        this.nonRelatedException = nonRelatedException;
    }

    public static ShouldRetryResult error(Exception e) {
        Utils.checkNotNullOrThrow(e, "exception", "cannot be null");
        return new ShouldRetryResult(null, e, false, null, false);
    }

    public static ShouldRetryResult noRetry() {
        return new ShouldRetryResult(null, null, false, null, false);
    }

    public static ShouldRetryResult noRetryOnNonRelatedException() {
        return new ShouldRetryResult(null, null, false, null, true);
    }

    public static ShouldRetryResult noRetry(Quadruple<Boolean, Boolean, Duration, Integer> policyArg) {
        return new ShouldRetryResult(
            null,
            null,
            false,
            policyArg,
            false);
    }

    public static ShouldRetryResult retryAfter(Duration dur,
                                               Quadruple<Boolean, Boolean, Duration, Integer> policyArg) {
        Utils.checkNotNullOrThrow(dur, "duration", "cannot be null");
        return new ShouldRetryResult(dur, null, true, policyArg, false);
    }

    public static ShouldRetryResult retryAfter(Duration dur) {
        Utils.checkNotNullOrThrow(dur, "duration", "cannot be null");
        return new ShouldRetryResult(dur, null, true, null, false);
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
