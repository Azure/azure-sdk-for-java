// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.core.implementation.converter;

import com.azure.core.http.policy.ExponentialBackoffOptions;
import com.azure.core.http.policy.FixedDelayOptions;
import com.azure.core.http.policy.RetryOptions;
import com.azure.spring.cloud.core.provider.RetryOptionsProvider;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;

import java.time.Duration;

/**
 * Converts a {@link RetryOptionsProvider.RetryOptions} to a {@link RetryOptions}.
 */
public final class AzureHttpRetryOptionsConverter implements Converter<RetryOptionsProvider.RetryOptions, RetryOptions> {

    public static final AzureHttpRetryOptionsConverter HTTP_RETRY_CONVERTER = new AzureHttpRetryOptionsConverter();

    private AzureHttpRetryOptionsConverter() {

    }

    @Override
    public RetryOptions convert(@NonNull RetryOptionsProvider.RetryOptions retry) {
        Integer maxRetries = retry.getMaxRetries();
        if (maxRetries == null) {
            return null;
        }

        Duration baseDelay = retry.getBaseDelay();
        RetryOptionsProvider.RetryMode retryMode = retry.getMode();
        if (RetryOptionsProvider.RetryMode.EXPONENTIAL.equals(retryMode)) {
            ExponentialBackoffOptions exponentialBackoffOptions = new ExponentialBackoffOptions();
            exponentialBackoffOptions.setMaxRetries(maxRetries);
            exponentialBackoffOptions.setBaseDelay(baseDelay);
            exponentialBackoffOptions.setMaxDelay(retry.getMaxDelay());

            return new RetryOptions(exponentialBackoffOptions);
        } else if (RetryOptionsProvider.RetryMode.FIXED.equals(retryMode)) {
            FixedDelayOptions fixedDelayOptions = new FixedDelayOptions(maxRetries, baseDelay);
            return new RetryOptions(fixedDelayOptions);
        }
        return null;
    }
}
