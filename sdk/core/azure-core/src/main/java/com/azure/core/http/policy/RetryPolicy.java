// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.policy;

import static com.azure.core.util.CoreUtils.isNullOrEmpty;

import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import java.util.Objects;
import com.azure.core.util.logging.ClientLogger;
import reactor.core.publisher.Mono;

import java.time.Duration;

/**
 * A pipeline policy that retries when a recoverable HTTP error occurs.
 * @see RetryPolicyOptions
 */
public class RetryPolicy implements HttpPipelinePolicy {

    private final ClientLogger logger = new ClientLogger(RetryPolicy.class);

    private final RetryPolicyOptions retryPolicyOptions;

    /**
     * Creates a default {@link RetryPolicy} with default {@link RetryPolicyOptions}.
     */
    public RetryPolicy() {
        this(new RetryPolicyOptions());
    }

    /**
     * Creates a RetryPolicy with the provided {@link RetryStrategy}.
     *
     * @param retryStrategy The {@link RetryStrategy} used for retries.
     */
    public RetryPolicy(RetryStrategy retryStrategy) {
        Objects.requireNonNull(retryStrategy, "'retryStrategy' cannot be null");
        this.retryPolicyOptions = new RetryPolicyOptions().setRetryStrategy(retryStrategy);
    }

    /**
     * Creates a {@link RetryPolicy} with the provided {@link RetryPolicyOptions}.
     *
     * @param retryPolicyOptions with given {@link RetryPolicyOptions}.
     * @throws NullPointerException if {@code retryPolicyOptions} is {@code null}.
     */
    public RetryPolicy(RetryPolicyOptions retryPolicyOptions) {
        this.retryPolicyOptions = Objects.requireNonNull(retryPolicyOptions,
            "'retryPolicyOptions' cannot be null.");
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
                int maxRetries = retryPolicyOptions.getRetryStrategy().getMaxRetries();
                if (tryCount < maxRetries) {
                    logger.verbose("[Error Resume] Try count: {}, Error: {}", tryCount, err);
                    return attemptAsync(context, next, originalHttpRequest, tryCount + 1)
                        .delaySubscription(retryPolicyOptions.getRetryStrategy().calculateRetryDelay(tryCount));
                } else {
                    return Mono.error(new RuntimeException(
                        String.format("Max retries %d times exceeded. Error Details: %s", maxRetries, err.getMessage()),
                        err));
                }
            });
    }

    private boolean shouldRetry(HttpResponse response, int tryCount) {
        return tryCount < retryPolicyOptions.getRetryStrategy().getMaxRetries() && retryPolicyOptions.getRetryStrategy()
            .shouldRetry(response);
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
            return retryPolicyOptions.getRetryStrategy().calculateRetryDelay(tryCount);
        }

        String retryHeaderValue = null;

        if (!isNullOrEmpty(retryPolicyOptions.getRetryAfterHeader())) {
            retryHeaderValue = response.getHeaderValue(retryPolicyOptions.getRetryAfterHeader());
        }

        // Retry header is missing or empty, return the default delay duration.
        if (isNullOrEmpty(retryHeaderValue)) {
            return retryPolicyOptions.getRetryStrategy().calculateRetryDelay(tryCount);
        }

        // Use the response delay duration, the server returned it for a reason.
        return Duration.of(Integer.parseInt(retryHeaderValue), retryPolicyOptions.getRetryAfterTimeUnit());
    }
}
