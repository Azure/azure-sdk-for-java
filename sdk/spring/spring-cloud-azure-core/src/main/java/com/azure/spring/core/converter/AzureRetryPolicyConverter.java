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
import org.springframework.util.Assert;

/**
 * Converts a {@link HttpRetryProperties} to a {@link RetryPolicy}.
 */
public final class AzureRetryPolicyConverter implements Converter<HttpRetryProperties, RetryPolicy> {

    @Override
    public RetryPolicy convert(HttpRetryProperties properties) {
        if (properties == null || properties.getMaxAttempts() == null) {
            return new RetryPolicy();
        }

        Assert.isTrue(properties.getMaxAttempts().intValue() >= 0, "MaxAttempts can not be less than 0");
        RetryStrategy retryStrategy = null;
        final RetryProperties.BackoffProperties backoff = properties.getBackoff();
        int maxAttempts = properties.getMaxAttempts().intValue();
        if (backoff != null) {
            Assert.notNull(backoff.getDelay(), "Backoff delay can not be null");
            Assert.isTrue(backoff.getDelay().toMillis() > 0, "Backoff delay can not be less than or equal to 0");
            if (backoff.getMultiplier() != null && backoff.getMultiplier() > 0) {
                Assert.notNull(backoff.getMaxDelay(), "Backoff maxDelay can not be null");
                Assert.isTrue(backoff.getMaxDelay().toMillis() > backoff.getDelay().toMillis(),
                    "Backoff maxDelay can not be less than backoff delay");
                retryStrategy = new ExponentialBackoff(maxAttempts, backoff.getDelay(), backoff.getMaxDelay());
            } else {
                retryStrategy = new FixedDelay(maxAttempts, backoff.getDelay());
            }
        }
        return new RetryPolicy(retryStrategy, properties.getRetryAfterHeader(), properties.getRetryAfterTimeUnit());
    }
}
