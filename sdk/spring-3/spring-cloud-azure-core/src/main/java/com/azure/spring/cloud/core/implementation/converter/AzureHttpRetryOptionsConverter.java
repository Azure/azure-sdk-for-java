// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.core.implementation.converter;

import com.azure.core.http.policy.ExponentialBackoffOptions;
import com.azure.core.http.policy.FixedDelayOptions;
import com.azure.core.http.policy.RetryOptions;
import com.azure.spring.cloud.core.provider.RetryOptionsProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;

/**
 * Converts a {@link RetryOptionsProvider.RetryOptions} to a {@link RetryOptions}.
 */
public final class AzureHttpRetryOptionsConverter implements Converter<RetryOptionsProvider.RetryOptions, RetryOptions> {

    private static final Logger LOGGER = LoggerFactory.getLogger(AzureHttpRetryOptionsConverter.class);
    public static final AzureHttpRetryOptionsConverter HTTP_RETRY_CONVERTER = new AzureHttpRetryOptionsConverter();

    private AzureHttpRetryOptionsConverter() {

    }

    @Override
    public RetryOptions convert(@NonNull RetryOptionsProvider.RetryOptions retry) {

        RetryOptionsProvider.RetryMode retryMode = retry.getMode();

        if (RetryOptionsProvider.RetryMode.EXPONENTIAL == retryMode) {
            RetryOptionsProvider.RetryOptions.ExponentialRetryOptions exponential = retry.getExponential();
            if (exponential != null && exponential.getMaxRetries() != null) {
                ExponentialBackoffOptions exponentialBackoffOptions = new ExponentialBackoffOptions();
                exponentialBackoffOptions.setMaxRetries(exponential.getMaxRetries());
                exponentialBackoffOptions.setBaseDelay(exponential.getBaseDelay());
                exponentialBackoffOptions.setMaxDelay(exponential.getMaxDelay());
                return new RetryOptions(exponentialBackoffOptions);
            } else {
                LOGGER.debug("The max-retries is not set, skip the convert.");
            }
        } else if (RetryOptionsProvider.RetryMode.FIXED == retryMode) {
            RetryOptionsProvider.RetryOptions.FixedRetryOptions fixed = retry.getFixed();
            if (fixed != null && fixed.getMaxRetries() != null) {
                FixedDelayOptions fixedDelayOptions = new FixedDelayOptions(fixed.getMaxRetries(), fixed.getDelay());
                return new RetryOptions(fixedDelayOptions);
            } else {
                LOGGER.debug("The max-retries is not set, skip the convert.");
            }
        }
        return null;
    }
}
