// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.common.implementation.policy;

import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.policy.HttpPipelineSyncPolicy;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Configuration;
import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.common.implementation.Constants;
import com.azure.storage.common.policy.ExpectContinueMode;
import com.azure.storage.common.policy.ExpectContinueOptions;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.LongSupplier;

/**
 * Pipeline policy that applies the HTTP header {@code Expect: 100-continue} to requests that carry a body, either
 * always or, in mode {@link ExpectContinueMode#APPLY_ON_THROTTLE}, for a window of time after the service responds
 * 429, 500, or 503.
 * <p>
 * Must be placed after the retry policy so that it is evaluated on every retry attempt, and after any policy that
 * changes the request body so that the {@code Content-Length} it reads is the one actually sent.
 * <p>
 * RESERVED FOR INTERNAL USE.
 */
public final class ExpectContinuePolicy extends HttpPipelineSyncPolicy {
    private static final ClientLogger LOGGER = new ClientLogger(ExpectContinuePolicy.class);
    private static final String CONTINUE = "100-continue";

    /*
     * Context key under which an AtomicBoolean holder is placed for the HTTP client to set when it observes an interim
     * "100 Continue" response. This must match the key the Netty client reads
     * (NettyAsyncHttpClient.EXPECT_CONTINUE_RECEIVED_KEY). It cannot be shared as a constant because the two modules
     * do not share a common module beyond azure-core; ideally this contract is promoted into azure-core.
     */
    static final String EXPECT_CONTINUE_RECEIVED_KEY = "azure-http-client-expect-continue-received";

    // Capped so that adding the interval to the current nanoTime cannot overflow.
    private static final long MAX_INTERVAL_NANOS = Long.MAX_VALUE / 4;

    private final boolean disabled;
    private final boolean applyOnThrottleOnly;
    private final long contentLengthThreshold;
    private final long throttleIntervalNanos;
    private final LongSupplier nanoClock;

    /*
     * The nanoTime after which the throttle window has elapsed. Compared by subtraction to handle nanoTime wraparound.
     * Starts at the current time, an already elapsed window. It must not start at 0, as nanoTime may be negative,
     * which would make the window appear open before any throttling response is received.
     */
    private final AtomicLong windowExpiryNanos;

    /**
     * Creates a policy applying {@code Expect: 100-continue} as described by the given options.
     *
     * @param options The options determining when the header is applied.
     * @param configuration The configuration to read the opt out from. A null value uses the global configuration.
     * @throws NullPointerException If {@code options} is null.
     */
    public ExpectContinuePolicy(ExpectContinueOptions options, Configuration configuration) {
        this(options, configuration, System::nanoTime);
    }

    ExpectContinuePolicy(ExpectContinueOptions options, Configuration configuration, LongSupplier nanoClock) {
        Objects.requireNonNull(options, "'options' cannot be null.");
        this.disabled = options.getMode() == ExpectContinueMode.OFF || isDisabled(configuration);
        this.applyOnThrottleOnly = options.getMode() == ExpectContinueMode.APPLY_ON_THROTTLE;
        this.contentLengthThreshold
            = options.getContentLengthThreshold() == null ? 0 : options.getContentLengthThreshold();
        this.throttleIntervalNanos = toNanos(options.getThrottleInterval());
        this.nanoClock = nanoClock;
        this.windowExpiryNanos = new AtomicLong(nanoClock.getAsLong());
    }

    @Override
    protected void beforeSendingRequest(HttpPipelineCallContext context) {
        // The call context is shared by every retry attempt, so drop an observation left by an earlier attempt.
        if (context.getData(EXPECT_CONTINUE_RECEIVED_KEY).isPresent()) {
            context.setData(EXPECT_CONTINUE_RECEIVED_KEY, null);
        }

        HttpRequest request = context.getHttpRequest();
        if (!disabled && (!applyOnThrottleOnly || isWithinThrottleWindow()) && isEligible(request)) {
            request.getHeaders().set(HttpHeaderName.EXPECT, CONTINUE);
            context.setData(EXPECT_CONTINUE_RECEIVED_KEY, new AtomicBoolean(false));
        }
    }

    @Override
    protected HttpResponse afterReceivedResponse(HttpPipelineCallContext context, HttpResponse response) {
        if (applyOnThrottleOnly && response != null && isThrottleResponse(response.getStatusCode())) {
            extendThrottleWindow();
        }

        logObservation(context);
        return super.afterReceivedResponse(context, response);
    }

    private boolean isWithinThrottleWindow() {
        return nanoClock.getAsLong() - windowExpiryNanos.get() < 0;
    }

    // Only ever moves the expiry later, so a concurrent response computed from an earlier time cannot shorten it.
    private void extendThrottleWindow() {
        long candidate = nanoClock.getAsLong() + throttleIntervalNanos;
        windowExpiryNanos.accumulateAndGet(candidate, (current, next) -> next - current > 0 ? next : current);
    }

    /*
     * Determines whether the request carries a non-empty body at least as large as the threshold. A body whose length
     * cannot be determined ahead of time is always eligible. HTTP prohibits the expectation on requests without
     * content, so a body known to be empty never is.
     */
    private boolean isEligible(HttpRequest request) {
        BinaryData body = request.getBodyAsBinaryData();
        if (body == null) {
            return false;
        }

        Long contentLength = getContentLength(request, body);
        return contentLength == null || (contentLength > 0 && contentLength >= contentLengthThreshold);
    }

    private static void logObservation(HttpPipelineCallContext context) {
        Object holder = context.getData(EXPECT_CONTINUE_RECEIVED_KEY).orElse(null);
        if (holder instanceof AtomicBoolean) {
            boolean received = ((AtomicBoolean) holder).get();
            LOGGER.verbose("Expect: 100-continue was {} from the service.", received ? "received" : "not received");
        }
    }

    private static boolean isDisabled(Configuration configuration) {
        Configuration source = configuration == null ? Configuration.getGlobalConfiguration() : configuration;
        return source.get(Constants.PROPERTY_AZURE_STORAGE_DISABLE_EXPECT_CONTINUE_HEADER, false);
    }

    private static boolean isThrottleResponse(int statusCode) {
        return statusCode == 429 || statusCode == 500 || statusCode == 503;
    }

    private static Long getContentLength(HttpRequest request, BinaryData body) {
        String headerValue = request.getHeaders().getValue(HttpHeaderName.CONTENT_LENGTH);
        if (headerValue != null) {
            try {
                return Long.parseLong(headerValue);
            } catch (NumberFormatException ex) {
                // Fall back to the body's own length.
            }
        }

        return body.getLength();
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
