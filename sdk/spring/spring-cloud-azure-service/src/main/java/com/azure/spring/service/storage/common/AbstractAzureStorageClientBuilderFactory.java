// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.service.storage.common;

import com.azure.spring.core.factory.AbstractAzureHttpClientBuilderFactory;
import com.azure.spring.core.properties.retry.RetryProperties;
import com.azure.storage.common.policy.RequestRetryOptions;

import java.util.function.BiConsumer;

/**
 * Abstract factory for Storage client builder.
 * @param <T> Storage service builder
 */
public abstract class AbstractAzureStorageClientBuilderFactory<T> extends AbstractAzureHttpClientBuilderFactory<T> {

    private final AzureRequestRetryOptionsConverter requestRetryOptionsConverter = new AzureRequestRetryOptionsConverter();

    protected abstract BiConsumer<T, RequestRetryOptions> consumeRequestRetryOptions();

    @Override
    protected void configureRetry(T builder) {
        RetryProperties retryProperties = getAzureProperties().getRetry();
        if (retryProperties instanceof StorageRetryProperties) {
            RequestRetryOptions requestRetryOptions =
                requestRetryOptionsConverter.convert((StorageRetryProperties) retryProperties);
            consumeRequestRetryOptions().accept(builder, requestRetryOptions);
        }
    }
}
