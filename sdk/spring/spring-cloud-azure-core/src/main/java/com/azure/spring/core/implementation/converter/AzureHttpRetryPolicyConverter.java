// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.core.implementation.converter;

import com.azure.core.http.policy.ExponentialBackoff;
import com.azure.core.http.policy.FixedDelay;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.http.policy.RetryStrategy;
import com.azure.spring.core.aware.RetryOptionsAware;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;

/**
 * Converts a {@link RetryOptionsAware.HttpRetry} to a {@link RetryPolicy}.
 */
public final class AzureHttpRetryPolicyConverter implements Converter<RetryOptionsAware.HttpRetry, RetryPolicy> {

    public static final AzureHttpRetryPolicyConverter HTTP_RETRY_CONVERTER = new AzureHttpRetryPolicyConverter();

    private AzureHttpRetryPolicyConverter() {

    }

    @Override
    public RetryPolicy convert(@NonNull RetryOptionsAware.HttpRetry httpRetry) {
        Integer maxAttempts = httpRetry.getMaxAttempts();
        if (maxAttempts == null) {
            return new RetryPolicy();
        }

        final RetryOptionsAware.Backoff backoff = httpRetry.getBackoff();
        RetryStrategy retryStrategy;

        if (backoff.getMultiplier() != null && backoff.getMultiplier() > 0) {
            // TODO (xiada): multiplier can't be set to the ExponentialBackoff, should we write our own strategy here?
            retryStrategy = new ExponentialBackoff(maxAttempts, backoff.getDelay(), backoff.getMaxDelay());
        } else {
            retryStrategy = new FixedDelay(maxAttempts, backoff.getDelay());
        }

        return new RetryPolicy(retryStrategy, httpRetry.getRetryAfterHeader(), httpRetry.getRetryAfterTimeUnit());
    }
}
