// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.core.implementation.converter;

import com.azure.core.http.policy.ExponentialBackoffOptions;
import com.azure.core.http.policy.FixedDelayOptions;
import com.azure.core.http.policy.RetryOptions;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.http.policy.RetryStrategy;
import com.azure.spring.core.aware.RetryAware;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;

/**
 * Converts a {@link RetryAware.HttpRetry} to a {@link RetryPolicy}.
 */
public final class AzureHttpRetryPolicyConverter implements Converter<RetryAware.HttpRetry, RetryOptions> {

    public static final AzureHttpRetryPolicyConverter HTTP_RETRY_CONVERTER = new AzureHttpRetryPolicyConverter();

    private AzureHttpRetryPolicyConverter() {

    }

    @Override
    public RetryOptions convert(@NonNull RetryAware.HttpRetry httpRetry) {
        Integer maxAttempts = httpRetry.getMaxAttempts();
        if (maxAttempts == null) {
            return null;
        }

        final RetryAware.Backoff backoff = httpRetry.getBackoff();
        RetryStrategy retryStrategy;

        if (backoff.getMultiplier() != null && backoff.getMultiplier() > 0) {
            // TODO (xiada): multiplier can't be set to the ExponentialBackoff, should we write our own strategy here?
            return new RetryOptions(new ExponentialBackoffOptions()
                .setMaxRetries(maxAttempts)
                .setBaseDelay(backoff.getDelay())
                .setMaxDelay(backoff.getMaxDelay()));
        } else {
            return new RetryOptions(new FixedDelayOptions()
                .setMaxRetries(maxAttempts)
                .setDelay(backoff.getDelay()));
        }
    }
}
