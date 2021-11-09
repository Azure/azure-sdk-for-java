// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.service.storage.common;

import com.azure.core.http.policy.RetryPolicy;
import com.azure.spring.core.aware.RetryAware;
import com.azure.spring.core.factory.AbstractAzureHttpClientBuilderFactory;
import com.azure.storage.common.policy.RequestRetryOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.BiConsumer;

import static com.azure.spring.service.storage.common.AzureStorageRetryOptionsConverter.STORAGE_RETRY_CONVERTER;

/**
 * Abstract factory for Storage client builder.
 * @param <T> Storage service builder
 */
public abstract class AbstractAzureStorageClientBuilderFactory<T> extends AbstractAzureHttpClientBuilderFactory<T> {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractAzureStorageClientBuilderFactory.class);
    protected abstract BiConsumer<T, RequestRetryOptions> consumeRequestRetryOptions();

    @Override
    protected void configureRetry(T builder) {
        RetryAware.Retry retry = getAzureProperties().getRetry();
        if (retry instanceof StorageRetry) {
            RequestRetryOptions requestRetryOptions = STORAGE_RETRY_CONVERTER.convert((StorageRetry) retry);
            consumeRequestRetryOptions().accept(builder, requestRetryOptions);
        } else {
            LOGGER.warn("The retry in a storage client builder is of type {}", retry.getClass().getName());
        }
    }

    @Override
    protected BiConsumer<T, RetryPolicy> consumeRetryPolicy() {
        return (a, b) -> { };
    }
}
