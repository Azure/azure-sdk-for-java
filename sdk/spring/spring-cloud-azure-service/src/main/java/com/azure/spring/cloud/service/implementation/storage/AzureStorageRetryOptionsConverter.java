// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.service.implementation.storage;

import com.azure.spring.cloud.service.implementation.storage.common.StorageRetry;
import com.azure.storage.common.policy.RequestRetryOptions;
import com.azure.storage.common.policy.RetryPolicyType;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;

import static com.azure.spring.cloud.core.provider.RetryOptionsProvider.RetryMode.EXPONENTIAL;

/**
 * Converts a {@link StorageRetry} to a {@link RequestRetryOptions}.
 */
public final class AzureStorageRetryOptionsConverter implements Converter<StorageRetry, RequestRetryOptions> {

    public static final AzureStorageRetryOptionsConverter STORAGE_RETRY_CONVERTER = new AzureStorageRetryOptionsConverter();

    private AzureStorageRetryOptionsConverter() {

    }

    @Override
    public RequestRetryOptions convert(@NonNull StorageRetry storageRetry) {
        return new RequestRetryOptions(
            (EXPONENTIAL.equals(storageRetry.getMode()) ? RetryPolicyType.EXPONENTIAL : RetryPolicyType.FIXED),
            storageRetry.getMaxRetries(),
            storageRetry.getTryTimeout(),
            storageRetry.getBaseDelay(),
            storageRetry.getMaxDelay(),
            storageRetry.getSecondaryHost());
    }
}
