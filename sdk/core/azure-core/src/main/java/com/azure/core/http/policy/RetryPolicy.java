// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.policy;

import static com.azure.core.util.CoreUtils.isNullOrEmpty;

import java.time.temporal.ChronoUnit;

import java.util.Objects;

import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;

import com.azure.core.util.logging.ClientLogger;

import reactor.core.publisher.Mono;

import java.time.Duration;

/**
 * A pipeline policy that retries when a recoverable HTTP error occurs.
 */
public class RetryPolicy implements HttpPipelinePolicy {

    private final ClientLogger logger = new ClientLogger(RetryPolicy.class);
    private final RetryStrategy retryStrategy;

    private final String retryAfterHeader;
    private final ChronoUnit retryAfterTimeUnit;

    /**
     * Creates a default {@link ExponentialBackoff} retry policy.
     */
    public RetryPolicy() {
        this(new ExponentialBackoff(), null, null);
    }

    /**
     * Creates a default {@link ExponentialBackoff} retry policy.
     * @param retryAfterHeader The retry after http header name to be used get retry after value from
     * @param retryAfterTimeUnit The time unit to use while applying retry based on value specified in
     *      * {@code retryAfterHeader} in {@link HttpResponse}.
     * {@link HttpResponse}.
     */
    public RetryPolicy(String retryAfterHeader, ChronoUnit retryAfterTimeUnit) {
        this(new ExponentialBackoff(), retryAfterHeader, retryAfterTimeUnit);
    }

    /**
     * Creates a RetryPolicy with the provided {@link RetryStrategy}.
     *
     * @param retryStrategy The {@link RetryStrategy} used for retries.
     * @throws NullPointerException if {@code retryStrategy} is {@code null}.
     */
    public RetryPolicy(RetryStrategy retryStrategy) {
        this(retryStrategy, null, null);
    }

    /**
     * Creates a {@link RetryPolicy} with the provided {@link RetryStrategy} and {@code retryAfterHeader}.
     *
     * @param retryStrategy The {@link RetryStrategy} used for retries.
     * @param retryAfterHeader The retry after http header name to be used get retry after value from
     * {@link HttpResponse}. The value {@code null} is valid.
     * @param retryAfterTimeUnit The time unit to use while applying retry based on value specified in
     * {@code retryAfterHeader} in {@link HttpResponse}.The value {@code null} is valid only in case when
     * {@code retryAfterHeader} is empty or {@code null}.
     * @throws NullPointerException if {@code retryStrategy} is {@code null}.
     */
    public RetryPolicy(RetryStrategy retryStrategy, String retryAfterHeader, ChronoUnit retryAfterTimeUnit) {
        this.retryStrategy = Objects.requireNonNull(retryStrategy, "'retryStrategy' cannot be null.");
        this.retryAfterHeader = retryAfterHeader;
        this.retryAfterTimeUnit = retryAfterTimeUnit;
        if (!isNullOrEmpty(retryAfterHeader)) {
            Objects.requireNonNull(retryAfterTimeUnit, "'retryAfterTimeUnit' cannot be null.");
        }
    }

    @Override
    public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
        return attemptAsync(context, next, context.getHttpRequest(), 0);
    }

    private Mono<HttpResponse> attemptAsync(final HttpPipelineCallContext context, final HttpPipelineNextPolicy next,
                                            final HttpRequest originalHttpRequest, final int tryCount) {
        context.setHttpRequest(originalHttpRequest.copy());
        return next.clone().process()
            .flatMap(httpResponse -> {
                if (shouldRetry(httpResponse, tryCount)) {
                    final Duration delayDuration = determineDelayDuration(httpResponse, tryCount);
                    logger.verbose("[Retrying] Try count: {}, Delay duration in seconds: {}", tryCount,
                        delayDuration.getSeconds());
                    return attemptAsync(context, next, originalHttpRequest, tryCount + 1)
                        .delaySubscription(delayDuration);
                } else {
                    return Mono.just(httpResponse);
                }
            })
            .onErrorResume(err -> {
                int maxRetries = retryStrategy.getMaxRetries();
                if (tryCount < maxRetries) {
                    logger.verbose("[Error Resume] Try count: {}, Error: {}", tryCount, err);
                    return attemptAsync(context, next, originalHttpRequest, tryCount + 1)
                        .delaySubscription(retryStrategy.calculateRetryDelay(tryCount));
                } else {
                    return Mono.error(new RuntimeException(
                        String.format("Max retries %d times exceeded. Error Details: %s", maxRetries, err.getMessage()),
                        err));
                }
            });
    }

    private boolean shouldRetry(HttpResponse response, int tryCount) {
        return tryCount < retryStrategy.getMaxRetries() && retryStrategy.shouldRetry(response);
    }

    /**
     * Determines the delay duration that should be waited before retrying.
     * @param response HTTP response
     * @return If the HTTP response has a retry-after-ms header that will be returned,
     *     otherwise the duration used during the construction of the policy.
     */
    private Duration determineDelayDuration(HttpResponse response, int tryCount) {
        int code = response.getStatusCode();

        // Response will not have a retry-after-ms header.
        if (code != 429        // too many requests
            && code != 503) {  // service unavailable
            return retryStrategy.calculateRetryDelay(tryCount);
        }

        String retryHeaderValue = null;

        if (!isNullOrEmpty(retryAfterHeader)) {
            retryHeaderValue = response.getHeaderValue(retryAfterHeader);
        }

        // Retry header is missing or empty, return the default delay duration.
        if (isNullOrEmpty(retryHeaderValue)) {
            return retryStrategy.calculateRetryDelay(tryCount);
        }

        // Use the response delay duration, the server returned it for a reason.
        return Duration.of(Integer.parseInt(retryHeaderValue), retryAfterTimeUnit);
    }
}
