// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.core.implementation.converter;

import com.azure.core.http.policy.ExponentialBackoffOptions;
import com.azure.core.http.policy.FixedDelayOptions;
import com.azure.core.http.policy.RetryOptions;
import com.azure.spring.core.aware.RetryOptionsAware;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;

import java.time.Duration;

/**
 * Converts a {@link RetryOptionsAware.Retry} to a {@link RetryOptions}.
 */
public final class AzureHttpRetryOptionsConverter implements Converter<RetryOptionsAware.Retry, RetryOptions> {

    public static final AzureHttpRetryOptionsConverter HTTP_RETRY_CONVERTER = new AzureHttpRetryOptionsConverter();

    private AzureHttpRetryOptionsConverter() {

    }

    @Override
    public RetryOptions convert(@NonNull RetryOptionsAware.Retry retry) {
        Integer maxRetries = retry.getMaxRetries();
        if (maxRetries == null) {
            return null;
        }

        Duration baseDelay = retry.getBaseDelay();
        RetryOptionsAware.RetryMode retryMode = retry.getMode();
        if (RetryOptionsAware.RetryMode.EXPONENTIAL.equals(retryMode)) {
            ExponentialBackoffOptions exponentialBackoffOptions = new ExponentialBackoffOptions();
            exponentialBackoffOptions.setMaxRetries(maxRetries);
            exponentialBackoffOptions.setBaseDelay(baseDelay);
            exponentialBackoffOptions.setMaxDelay(retry.getMaxDelay());

            return new RetryOptions(exponentialBackoffOptions);
        } else if (RetryOptionsAware.RetryMode.FIXED.equals(retryMode)) {
            FixedDelayOptions fixedDelayOptions = new FixedDelayOptions(maxRetries, baseDelay);
            return new RetryOptions(fixedDelayOptions);
        }
        return null;
    }
}
