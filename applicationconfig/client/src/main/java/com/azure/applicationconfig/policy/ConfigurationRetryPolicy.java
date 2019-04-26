// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.applicationconfig.policy;

import com.azure.common.http.HttpPipelineCallContext;
import com.azure.common.http.HttpPipelineNextPolicy;
import com.azure.common.http.HttpRequest;
import com.azure.common.http.HttpResponse;
import com.azure.common.http.policy.HttpPipelinePolicy;
import io.netty.handler.codec.http.HttpResponseStatus;
import reactor.core.publisher.Mono;

import java.time.Duration;

/**
 * A pipeline policy that retries when a recoverable HTTP error occurs.
 */
public class ConfigurationRetryPolicy implements HttpPipelinePolicy {
    private static final int DEFAULT_MAX_RETRIES = 3;

    private final int maxRetries;

    /**
     * Creates a ConfigurationRetryPolicy with the default number of retry attempts.
     */
    public ConfigurationRetryPolicy() {
        maxRetries = DEFAULT_MAX_RETRIES;
    }

    /**
     * Creates a ConfigurationRetryPolicy
     * @param maxRetries the maximum number of retries to attempt.
     */
    public ConfigurationRetryPolicy(int maxRetries) {
        this.maxRetries = maxRetries;
    }

    @Override
    public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
        return attemptAsync(context, next, context.httpRequest(), 0);
    }

    private Mono<HttpResponse> attemptAsync(final HttpPipelineCallContext context, final HttpPipelineNextPolicy next, final HttpRequest originalHttpRequest, final int tryCount) {
        context.withHttpRequest(originalHttpRequest.buffer());
        return next.clone().process()
            .flatMap(httpResponse -> {
                if (shouldRetry(httpResponse.statusCode(), tryCount)) {
                    int retryWait = Integer.parseInt(httpResponse.headerValue("retry-after-ms"));
                    return attemptAsync(context, next, originalHttpRequest, tryCount + 1).delaySubscription(Duration.ofMillis(retryWait));
                }

                return Mono.just(httpResponse);
            });
    }

    /**
     * Determine if the request should be retried.
     * @param statusCode HTTP response status code.
     * @param tryCount Number of retries already attempted.
     * @return True if the status code is 429 or 503 and the number of retries already attempted is less than the max.
     */
    private boolean shouldRetry(int statusCode, int tryCount) {
        return tryCount < maxRetries
            && (statusCode == HttpResponseStatus.SERVICE_UNAVAILABLE.code()
                || statusCode == HttpResponseStatus.TOO_MANY_REQUESTS.code());
    }
}
