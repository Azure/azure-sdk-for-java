// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.implementation;

import com.azure.core.http.HttpResponse;
import com.azure.core.http.policy.RequestRetryCondition;
import com.azure.core.http.policy.RetryStrategy;
import com.azure.core.util.logging.ClientLogger;

import java.io.IOException;
import java.time.Duration;
import java.util.function.Predicate;
import java.util.function.Function;

public class ImdsRetryStrategy implements RetryStrategy {
    private static final ClientLogger LOGGER = new ClientLogger(ImdsRetryStrategy.class);
    private final int maxRetries;
    private final Duration baseDelay;
    private final Predicate<RequestRetryCondition> shouldRetryCondition;

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
        long retries = retryAttempts + 1;
        long delay = (long) (baseDelay.toMillis() * Math.pow(2, retries - 1));
        return Duration.ofMillis(delay);
    }

    @Override
    public boolean shouldRetryCondition(RequestRetryCondition requestRetryCondition) {
        return this.shouldRetryCondition.test(requestRetryCondition);
    }

    private boolean defaultShouldRetryCondition(RequestRetryCondition condition) {
        HttpResponse response = condition.getResponse();
        Throwable throwable = condition.getThrowable();

        if (response != null) {
            int statusCode = response.getStatusCode();
            if (statusCode == 400) {
                return false;
            }
            if (statusCode == 403) {
                return response.getHeaderValue("ResponseMessage").contains("A socket operation was attempted to an unreachable network");
            }
            if (statusCode == 410 || statusCode == 429 || statusCode == 404 || (statusCode >= 500 && statusCode <= 599)) {
                return true;
            }
        }

        if (throwable instanceof IOException) {
            return true;
        }

        return false;
    }
}
