// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.service.implementation.storage;

import com.azure.spring.core.aware.RetryAware;
import com.azure.spring.service.storage.common.StorageRetry;
import com.azure.storage.common.policy.RequestRetryOptions;
import com.azure.storage.common.policy.RetryPolicyType;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;

import java.time.Duration;

/**
 * Converts a {@link StorageRetry} to a {@link RequestRetryOptions}.
 */
public final class AzureStorageRetryOptionsConverter implements Converter<StorageRetry, RequestRetryOptions> {

    public static final AzureStorageRetryOptionsConverter STORAGE_RETRY_CONVERTER = new AzureStorageRetryOptionsConverter();

    private AzureStorageRetryOptionsConverter() {

    }

    @Override
    public RequestRetryOptions convert(@NonNull StorageRetry storageRetry) {
        RetryPolicyType retryPolicyType = null;
        Duration delay = null;
        Duration maxDelay = null;
        final RetryAware.Backoff backoff = storageRetry.getBackoff();
        if (backoff != null) {
            if (backoff.getMultiplier() != null && backoff.getMultiplier() > 0) {
                retryPolicyType = RetryPolicyType.EXPONENTIAL;
            } else {
                retryPolicyType = RetryPolicyType.FIXED;
            }
            delay = backoff.getDelay();
            maxDelay = backoff.getMaxDelay();
        }
        return new RequestRetryOptions(retryPolicyType, storageRetry.getMaxAttempts(),
            storageRetry.getTimeout(), delay, maxDelay, storageRetry.getSecondaryHost());
    }
}
