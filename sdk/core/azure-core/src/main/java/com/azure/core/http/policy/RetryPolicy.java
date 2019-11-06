// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.policy;

import static com.azure.core.util.CoreUtils.isNullOrEmpty;

import java.util.Objects;
import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.HttpHeaderType;
import com.azure.core.util.logging.ClientLogger;

import reactor.core.publisher.Mono;

import java.time.Duration;

/**
 * A pipeline policy that retries when a recoverable HTTP error occurs.
 */
public class RetryPolicy implements HttpPipelinePolicy {

    private final ClientLogger logger = new ClientLogger(RetryPolicy.class);
    private final RetryStrategy retryStrategy;

    private final HttpHeaderType retryAfterHeader;

    /**
     * Creates a default {@link ExponentialBackoff} retry policy.
     */
    public RetryPolicy() {
        this(new ExponentialBackoff(), HttpHeaderType.AZURE_RETRY_AFTER_MS_HEADER);
    }

    /**
     * Creates a default {@link ExponentialBackoff} retry policy.
     * @param retryAfterHeader The retry after http header name to be used get retry after value from
     * {@link HttpResponse}.
     * @throws NullPointerException if {@code retryAfterHeader} is {@code null}.
     */
    public RetryPolicy(HttpHeaderType retryAfterHeader) {
        this(new ExponentialBackoff(), retryAfterHeader);
    }

    /**
     * Creates a RetryPolicy with the provided {@link RetryStrategy}.
     *
     * @param retryStrategy The {@link RetryStrategy} used for retries.
     * @throws NullPointerException if {@code retryStrategy} is {@code null}.
     */
    public RetryPolicy(RetryStrategy retryStrategy) {
        this(retryStrategy, HttpHeaderType.AZURE_RETRY_AFTER_MS_HEADER);
    }

    /**
     * Creates a {@link RetryPolicy} with the provided {@link RetryStrategy} and {@link HttpHeaderType}.
     *
     * @param retryStrategy The {@link RetryStrategy} used for retries.
     * @param retryAfterHeader The retry after http header name to be used get retry after value from
     * {@link HttpResponse}.
     * @throws NullPointerException if {@code retryStrategy} or {@code retryAfterHeader} is {@code null}.
     */
    public RetryPolicy(RetryStrategy retryStrategy, HttpHeaderType retryAfterHeader) {
        this.retryStrategy = Objects.requireNonNull(retryStrategy, "'retryStrategy' cannot be null");
        this.retryAfterHeader = Objects.requireNonNull(retryAfterHeader, "'retryAfterHeader' cannot be null");
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

        String retryHeaderValue = response.getHeaderValue(retryAfterHeader.name());

        // Retry header is missing or empty, return the default delay duration.
        if (isNullOrEmpty(retryHeaderValue)) {
            return retryStrategy.calculateRetryDelay(tryCount);
        }

        // Use the response delay duration, the server returned it for a reason.
        return Duration.ofMillis(Integer.parseInt(retryHeaderValue));
    }
}
