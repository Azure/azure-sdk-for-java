// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.implementation;

import com.azure.core.http.HttpResponse;
import com.azure.core.http.policy.RequestRetryCondition;
import com.azure.core.http.policy.RetryStrategy;

import java.io.IOException;
import java.time.Duration;
import java.util.function.Predicate;

/**
 * The retry strategy when sending a request for the IMDS endpoint.
 */
public class ImdsRetryStrategy implements RetryStrategy {
    private static final int MAX_RETRIES = 5;
    private final int maxRetries;
    private final Duration baseDelay;
    private final Predicate<RequestRetryCondition> shouldRetryCondition;

    public ImdsRetryStrategy() {
        this(MAX_RETRIES, Duration.ofMillis(800));
    }
    public ImdsRetryStrategy(int maxRetries) {
        this(maxRetries, Duration.ofMillis(800));
    }

    public ImdsRetryStrategy(int maxRetries, Duration baseDelay) {
        this.maxRetries = maxRetries;
        this.baseDelay = baseDelay;
        this.shouldRetryCondition = this::defaultShouldRetryCondition;
    }

    @Override
    public int getMaxRetries() {
        return maxRetries;
    }

    @Override
    public Duration calculateRetryDelay(int retryAttempts) {
        long delay = (long) (baseDelay.toMillis() * Math.pow(2, retryAttempts));
        return Duration.ofMillis(delay);
    }

    @Override
    public boolean shouldRetryCondition(RequestRetryCondition requestRetryCondition) {
        return this.shouldRetryCondition.test(requestRetryCondition);
    }

    @Override
    public boolean shouldRetry(HttpResponse httpResponse) {
        if (httpResponse != null) {
            int statusCode = httpResponse.getStatusCode();
            if (statusCode == 400) {
                return false;
            }
            if (statusCode == 403) {
                return httpResponse.getHeaderValue("ResponseMessage").contains("A socket operation was attempted to an unreachable network");
            }
            if (statusCode == 410 || statusCode == 429 || statusCode == 404 || (statusCode >= 500 && statusCode <= 599)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean shouldRetryException(Throwable throwable) {
        return throwable instanceof IOException;
    }

    private boolean defaultShouldRetryCondition(RequestRetryCondition condition) {
        HttpResponse response = condition.getResponse();
        Throwable throwable = condition.getThrowable();

        if (response != null) {
            return shouldRetry(response);
        }

        return shouldRetryException(throwable);
    }
}
