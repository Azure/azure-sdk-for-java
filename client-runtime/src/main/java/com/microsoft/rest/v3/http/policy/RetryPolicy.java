/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.rest.v3.http.policy;

import com.microsoft.rest.v3.http.HttpPipelineCallContext;
import com.microsoft.rest.v3.http.HttpRequest;
import com.microsoft.rest.v3.http.HttpResponse;
import com.microsoft.rest.v3.http.NextPolicy;
import reactor.core.publisher.Mono;

import java.net.HttpURLConnection;
import java.time.Duration;
import java.time.temporal.ChronoUnit;

/**
 * A pipeline policy that retries when a recoverable HTTP error occurs.
 */
public class RetryPolicy implements HttpPipelinePolicy {
    private static final int DEFAULT_MAX_RETRIES = 3;
    private static final int DEFAULT_DELAY = 0;
    private static final ChronoUnit DEFAULT_TIME_UNIT = ChronoUnit.MILLIS;
    private final int maxRetries;
    private final long delayTime;
    private final ChronoUnit timeUnit;

    /**
     * Creates a RetryPolicy with the default number of retry attempts and delay between retries.
     */
    public RetryPolicy() {
        maxRetries = DEFAULT_MAX_RETRIES;
        delayTime = DEFAULT_DELAY;
        timeUnit = DEFAULT_TIME_UNIT;
    }

    /**
     * Creates a RetryPolicy.
     *
     * @param maxRetries the maximum number of retries to attempt.
     * @param delayTime the delay between retries
     * @param timeUnit the time unit of the delay
     */
    public RetryPolicy(int maxRetries, long delayTime, ChronoUnit timeUnit) {
        this.maxRetries = maxRetries;
        this.delayTime = delayTime;
        this.timeUnit = timeUnit;
    }

    @Override
    public Mono<HttpResponse> process(HttpPipelineCallContext context, NextPolicy next) {
        return attemptAsync(context, next, context.httpRequest(), 0);
    }

    private Mono<HttpResponse> attemptAsync(final HttpPipelineCallContext context, final NextPolicy next, final HttpRequest originalHttpRequest, final int tryCount) {
        context.withHttpRequest(originalHttpRequest.buffer());
        return next.clone().process()
                .flatMap(httpResponse -> {
                    if (shouldRetry(httpResponse, tryCount)) {
                        return attemptAsync(context, next, originalHttpRequest, tryCount + 1).delaySubscription(Duration.of(delayTime, timeUnit));
                    } else {
                        return Mono.just(httpResponse);
                    }
                })
                .onErrorResume(err -> {
                    if (tryCount < maxRetries) {
                        return attemptAsync(context, next, originalHttpRequest, tryCount + 1).delaySubscription(Duration.of(delayTime, timeUnit));
                    } else {
                        return Mono.error(err);
                    }
                });
    }

    private boolean shouldRetry(HttpResponse response, int tryCount) {
        int code = response.statusCode();
        return tryCount < maxRetries
                && (code == HttpURLConnection.HTTP_CLIENT_TIMEOUT
                || (code >= HttpURLConnection.HTTP_INTERNAL_ERROR
                && code != HttpURLConnection.HTTP_NOT_IMPLEMENTED
                && code != HttpURLConnection.HTTP_VERSION));
    }
}