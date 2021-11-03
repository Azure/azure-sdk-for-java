// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.core.converter;

import com.azure.core.http.policy.ExponentialBackoff;
import com.azure.core.http.policy.FixedDelay;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.http.policy.RetryStrategy;
import com.azure.spring.core.properties.retry.HttpRetryProperties;
import com.azure.spring.core.properties.retry.RetryProperties;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;

/**
 * Converts a {@link HttpRetryProperties} to a {@link RetryPolicy}.
 */
public final class AzureHttpRetryPolicyConverter implements Converter<HttpRetryProperties, RetryPolicy> {

    public static final AzureHttpRetryPolicyConverter HTTP_RETRY_CONVERTER = new AzureHttpRetryPolicyConverter();

    @Override
    public RetryPolicy convert(@NonNull HttpRetryProperties properties) {
        Integer maxAttempts = properties.getMaxAttempts();
        if (maxAttempts == null) {
            return new RetryPolicy();
        }

        final RetryProperties.BackoffProperties backoff = properties.getBackoff();
        RetryStrategy retryStrategy;

        if (backoff.getMultiplier() != null && backoff.getMultiplier() > 0) {
            // TODO (xiada): multiplier can't be set to the ExponentialBackoff, should we write our own strategy here?
            retryStrategy = new ExponentialBackoff(maxAttempts, backoff.getDelay(), backoff.getMaxDelay());
        } else {
            retryStrategy = new FixedDelay(maxAttempts, backoff.getDelay());
        }

        return new RetryPolicy(retryStrategy, properties.getRetryAfterHeader(), properties.getRetryAfterTimeUnit());
    }
}
