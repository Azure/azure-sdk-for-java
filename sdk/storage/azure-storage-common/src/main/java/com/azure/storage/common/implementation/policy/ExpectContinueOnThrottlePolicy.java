// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.common.implementation.policy;

import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.util.Configuration;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Pipeline policy that applies the HTTP header {@code Expect: 100-continue} to requests that carry a body, but only for
 * a window of time after the service has indicated it is under load.
 * <p>
 * While the service is healthy the header is omitted, so requests do not pay for the additional round trip it costs.
 * Once the service responds 429, 500, or 503, the header is applied to subsequent requests until the window elapses,
 * so that a body is not uploaded just to be rejected again.
 * <p>
 * This policy must be placed after the retry policy so that it is evaluated on every retry attempt. Placed before the
 * retry policy it would be evaluated once, before any response had been seen, and could not take effect until a later
 * call.
 * <p>
 * RESERVED FOR INTERNAL USE.
 */
public final class ExpectContinueOnThrottlePolicy extends ExpectContinuePolicy {
    /*
     * Cap the window so that adding it to System.nanoTime() cannot overflow. Roughly 73 years, far longer than any
     * meaningful configuration.
     */
    private static final long MAX_INTERVAL_NANOS = Long.MAX_VALUE / 4;

    private final long autoIntervalNanos;

    /*
     * The nanoTime after which the throttle window has elapsed. Initialized to now, meaning no window is open until a
     * triggering response is seen. Compared using subtraction so that nanoTime wraparound is handled correctly.
     */
    private final AtomicLong windowExpiryNanos = new AtomicLong(System.nanoTime());

    /**
     * Creates a policy that applies {@code Expect: 100-continue} for the given interval after the service indicates it
     * is under load.
     *
     * @param autoInterval The interval for which the header is applied after a triggering response.
     * @param contentLengthThreshold The minimum request {@code Content-Length} for applying the header. A null value
     * means every request with a body is eligible.
     */
    public ExpectContinueOnThrottlePolicy(Duration autoInterval, Long contentLengthThreshold) {
        super(contentLengthThreshold);
        this.autoIntervalNanos = toNanos(autoInterval);
    }

    /**
     * Creates a policy reading the opt out from the given configuration. For testing.
     *
     * @param autoInterval The interval for which the header is applied after a triggering response.
     * @param contentLengthThreshold The minimum request {@code Content-Length} for applying the header.
     * @param configuration The configuration to read the opt out from.
     */
    ExpectContinueOnThrottlePolicy(Duration autoInterval, Long contentLengthThreshold, Configuration configuration) {
        super(contentLengthThreshold, configuration);
        this.autoIntervalNanos = toNanos(autoInterval);
    }

    @Override
    boolean shouldApplyExpectContinue(HttpRequest request) {
        return isWithinThrottleWindow() && super.shouldApplyExpectContinue(request);
    }

    @Override
    protected HttpResponse afterReceivedResponse(HttpPipelineCallContext context, HttpResponse response) {
        if (response != null && isThrottleResponse(response.getStatusCode())) {
            windowExpiryNanos.set(System.nanoTime() + autoIntervalNanos);
        }

        return super.afterReceivedResponse(context, response);
    }

    private boolean isWithinThrottleWindow() {
        return System.nanoTime() - windowExpiryNanos.get() < 0;
    }

    private static boolean isThrottleResponse(int statusCode) {
        return statusCode == 429 || statusCode == 500 || statusCode == 503;
    }

    private static long toNanos(Duration autoInterval) {
        if (autoInterval == null || autoInterval.isNegative()) {
            return 0;
        }

        try {
            return Math.min(autoInterval.toNanos(), MAX_INTERVAL_NANOS);
        } catch (ArithmeticException ex) {
            // Duration too large to express in nanoseconds.
            return MAX_INTERVAL_NANOS;
        }
    }
}
