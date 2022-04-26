// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.service.implementation.storage;

import com.azure.spring.cloud.core.provider.RetryOptionsProvider;
import com.azure.spring.cloud.service.implementation.storage.common.StorageRetry;
import com.azure.storage.common.policy.RequestRetryOptions;
import com.azure.storage.common.policy.RetryPolicyType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;

import static com.azure.spring.cloud.core.provider.RetryOptionsProvider.RetryMode.EXPONENTIAL;
import static com.azure.spring.cloud.core.provider.RetryOptionsProvider.RetryMode.FIXED;

/**
 * Converts a {@link StorageRetry} to a {@link RequestRetryOptions}.
 */
public final class AzureStorageRetryOptionsConverter implements Converter<StorageRetry, RequestRetryOptions> {

    public static final AzureStorageRetryOptionsConverter STORAGE_RETRY_CONVERTER = new AzureStorageRetryOptionsConverter();
    private static final Logger LOGGER = LoggerFactory.getLogger(AzureStorageRetryOptionsConverter.class);

    private AzureStorageRetryOptionsConverter() {

    }

    @Override
    public RequestRetryOptions convert(@NonNull StorageRetry storageRetry) {
        RetryOptionsProvider.RetryMode retryMode = storageRetry.getMode();

        if (EXPONENTIAL == retryMode) {
            RetryOptionsProvider.RetryOptions.ExponentialRetryOptions exponential = storageRetry.getExponential();
            if (exponential != null && exponential.getMaxRetries() != null) {
                return new RequestRetryOptions(RetryPolicyType.EXPONENTIAL,
                    exponential.getMaxRetries(),
                    storageRetry.getTryTimeout(),
                    exponential.getBaseDelay(),
                    exponential.getMaxDelay(),
                    storageRetry.getSecondaryHost());
            } else {
                LOGGER.debug("The max-retries is not set, skip the convert.");
            }
        } else if (FIXED == retryMode) {
            RetryOptionsProvider.RetryOptions.FixedRetryOptions fixed = storageRetry.getFixed();
            if (fixed != null && fixed.getMaxRetries() != null) {
                return new RequestRetryOptions(RetryPolicyType.FIXED,
                    fixed.getMaxRetries(),
                    storageRetry.getTryTimeout(),
                    fixed.getDelay(),
                    fixed.getDelay(),
                    storageRetry.getSecondaryHost());
            } else {
                LOGGER.debug("The max-retries is not set, skip the convert.");
            }
        }
        return null;
    }
}
