/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.rest.v3.policy;

import com.microsoft.rest.v3.http.HttpRequest;
import com.microsoft.rest.v3.http.HttpResponse;
import reactor.core.publisher.Mono;

import java.net.HttpURLConnection;
import java.time.Duration;
import java.time.temporal.ChronoUnit;

/**
 * Creates a RequestPolicy which retries when a recoverable HTTP error occurs.
 */
public class RetryPolicyFactory implements RequestPolicyFactory {
    private static final int DEFAULT_MAX_RETRIES = 3;
    private static final int DEFAULT_DELAY = 0;
    private static final ChronoUnit DEFAULT_TIME_UNIT = ChronoUnit.MILLIS;
    private final int maxRetries;
    private final long delayTime;
    private final ChronoUnit timeUnit;

    /**
     * Creates a RetryPolicyFactory with the default number of retry attempts and delay between retries.
     */
    public RetryPolicyFactory() {
        maxRetries = DEFAULT_MAX_RETRIES;
        delayTime = DEFAULT_DELAY;
        timeUnit = DEFAULT_TIME_UNIT;
    }

    /**
     * Creates a RetryPolicyFactory.
     * @param maxRetries The maximum number of retries to attempt.
     * @param delayTime the delay between retries
     * @param timeUnit the time unit of the delay
     */
    public RetryPolicyFactory(int maxRetries, long delayTime, ChronoUnit timeUnit) {
        this.maxRetries = maxRetries;
        this.delayTime = delayTime;
        this.timeUnit = timeUnit;
    }

    @Override
    public RequestPolicy create(RequestPolicy next, RequestPolicyOptions options) {
        return new RetryPolicy(next);
    }

    private final class RetryPolicy implements RequestPolicy {
        private final RequestPolicy next;
        private RetryPolicy(RequestPolicy next) {
            this.next = next;
        }

        @Override
        public Mono<HttpResponse> sendAsync(final HttpRequest request) {
            return attemptAsync(request, 0);
        }

        private Mono<HttpResponse> attemptAsync(final HttpRequest request, final int tryCount) {
            return next.sendAsync(request.buffer())
                    .flatMap(httpResponse -> {
                        if (shouldRetry(httpResponse, tryCount)) {
                            return attemptAsync(request, tryCount + 1).delaySubscription(Duration.of(delayTime, timeUnit));
                        } else {
                            return Mono.just(httpResponse);
                        }
                    })
                    .onErrorResume(err -> {
                        if (tryCount < maxRetries) {
                            return attemptAsync(request, tryCount + 1).delaySubscription(Duration.of(delayTime, timeUnit));
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
}
