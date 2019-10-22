// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.policy;

import static com.azure.core.implementation.util.ImplUtils.isNullOrEmpty;

import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import java.util.Objects;
import reactor.core.publisher.Mono;

import java.net.HttpURLConnection;
import java.time.Duration;
import java.time.temporal.ChronoUnit;

/**
 * A pipeline policy that retries when a recoverable HTTP error occurs.
 */
public class RetryPolicy implements HttpPipelinePolicy {
    private static final String RETRY_AFTER_MS_HEADER = "retry-after-ms";
    private final RetryStrategy retryStrategy;

    /**
     * Creates a {@link FixedDelay} RetryPolicy with a default {@link ExponentialBackoff} strategy.
     */
    public RetryPolicy() {
        this(new ExponentialBackoff());
    }

    /**
     * Creates a RetryPolicy with the provided {@link RetryStrategy}.
     *
     * @param retryStrategy The {@link RetryStrategy} used for retries.
     */
    public RetryPolicy(RetryStrategy retryStrategy) {
        this.retryStrategy = Objects.requireNonNull(retryStrategy, "'retryStrategy' cannot be null");
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
                    return attemptAsync(context, next, originalHttpRequest, tryCount + 1)
                        .delaySubscription(determineDelayDuration(httpResponse, tryCount));
                } else {
                    return Mono.just(httpResponse);
                }
            })
            .onErrorResume(err -> {
                if (tryCount < retryStrategy.getMaxRetries()) {
                    return attemptAsync(context, next, originalHttpRequest, tryCount + 1)
                        .delaySubscription(retryStrategy.calculateRetryDelay(tryCount));
                } else {
                    return Mono.error(err);
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

        String retryHeader = response.getHeaderValue(RETRY_AFTER_MS_HEADER);

        // Retry header is missing or empty, return the default delay duration.
        if (isNullOrEmpty(retryHeader)) {
            return retryStrategy.calculateRetryDelay(tryCount);
        }

        // Use the response delay duration, the server returned it for a reason.
        return Duration.ofMillis(Integer.parseInt(retryHeader));
    }
}
