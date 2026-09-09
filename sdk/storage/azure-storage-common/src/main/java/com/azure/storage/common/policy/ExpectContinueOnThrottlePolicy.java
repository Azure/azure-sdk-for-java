// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.common.policy;

import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.policy.HttpPipelineSyncPolicy;
import com.azure.core.util.Configuration;
import com.azure.core.util.logging.ClientLogger;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Pipeline policy that applies the HTTP header {@code Expect: 100-continue} to requests that carry a body, for a window
 * of time after the service responds 429, 500, or 503.
 * <p>
 * Must be placed after the retry policy so that it is evaluated on every retry attempt.
 * <p>
 * RESERVED FOR INTERNAL USE.
 */
public final class ExpectContinueOnThrottlePolicy extends HttpPipelineSyncPolicy {
    private static final ClientLogger LOGGER = new ClientLogger(ExpectContinueOnThrottlePolicy.class);

    // Capped so that adding the interval to System.nanoTime() cannot overflow.
    private static final long MAX_INTERVAL_NANOS = Long.MAX_VALUE / 4;

    private final long throttleIntervalNanos;
    private final long contentLengthThreshold;
    private final boolean disabled;

    // The nanoTime after which the window has elapsed. Compared by subtraction to handle nanoTime wraparound.
    private final AtomicLong windowExpiryNanos = new AtomicLong(System.nanoTime());

    /**
     * Creates a policy that applies {@code Expect: 100-continue} for the given interval after the service indicates it
     * is under load.
     *
     * @param throttleInterval The interval for which the header is applied after a triggering response.
     * @param contentLengthThreshold The minimum request {@code Content-Length} for applying the header. A null value
     * means every request with a body is eligible.
     */
    public ExpectContinueOnThrottlePolicy(Duration throttleInterval, Long contentLengthThreshold) {
        this(throttleInterval, contentLengthThreshold, Configuration.getGlobalConfiguration());
    }

    /**
     * Creates a policy reading the opt out from the given configuration.
     *
     * @param throttleInterval The interval for which the header is applied after a triggering response.
     * @param contentLengthThreshold The minimum request {@code Content-Length} for applying the header.
     * @param configuration The configuration to read the opt out from.
     */
    ExpectContinueOnThrottlePolicy(Duration throttleInterval, Long contentLengthThreshold,
        Configuration configuration) {
        this.throttleIntervalNanos = toNanos(throttleInterval);
        this.contentLengthThreshold = contentLengthThreshold == null ? 0 : contentLengthThreshold;
        this.disabled = ExpectContinuePolicyHelper.isDisabled(configuration);
    }

    @Override
    protected void beforeSendingRequest(HttpPipelineCallContext context) {
        HttpRequest request = context.getHttpRequest();
        if (!disabled
            && isWithinThrottleWindow()
            && ExpectContinuePolicyHelper.isEligible(request, contentLengthThreshold)) {
            ExpectContinuePolicyHelper.applyHeader(request);
            ExpectContinuePolicyHelper.installObservationHolder(context);
        }
    }

    @Override
    protected HttpResponse afterReceivedResponse(HttpPipelineCallContext context, HttpResponse response) {
        if (response != null && isThrottleResponse(response.getStatusCode())) {
            windowExpiryNanos.set(System.nanoTime() + throttleIntervalNanos);
        }

        ExpectContinuePolicyHelper.logObservation(context, LOGGER);
        return super.afterReceivedResponse(context, response);
    }

    private boolean isWithinThrottleWindow() {
        return System.nanoTime() - windowExpiryNanos.get() < 0;
    }

    private static boolean isThrottleResponse(int statusCode) {
        return statusCode == 429 || statusCode == 500 || statusCode == 503;
    }

    private static long toNanos(Duration throttleInterval) {
        if (throttleInterval == null || throttleInterval.isNegative()) {
            return 0;
        }

        try {
            return Math.min(throttleInterval.toNanos(), MAX_INTERVAL_NANOS);
        } catch (ArithmeticException ex) {
            return MAX_INTERVAL_NANOS;
        }
    }
}
