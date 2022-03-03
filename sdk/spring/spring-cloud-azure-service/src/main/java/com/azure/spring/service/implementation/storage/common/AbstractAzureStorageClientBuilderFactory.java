// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.service.implementation.storage.common;

import com.azure.core.http.policy.RetryPolicy;
import com.azure.spring.cloud.core.aware.RetryOptionsAware;
import com.azure.spring.cloud.core.implementation.factory.AbstractAzureHttpClientBuilderFactory;
import com.azure.spring.cloud.core.properties.AzureProperties;
import com.azure.storage.common.policy.RequestRetryOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.BiConsumer;

import static com.azure.spring.service.implementation.storage.AzureStorageRetryOptionsConverter.STORAGE_RETRY_CONVERTER;

/**
 * Abstract factory for Storage client builder.
 * @param <T> Storage service builder
 */
public abstract class AbstractAzureStorageClientBuilderFactory<T> extends AbstractAzureHttpClientBuilderFactory<T> {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractAzureStorageClientBuilderFactory.class);

    /**
     * Return a {@link BiConsumer} of how the {@link T} builder consume a {@link RequestRetryOptions}.
     * @return The consumer of how the {@link T} builder consume a {@link RequestRetryOptions}.
     */
    protected abstract BiConsumer<T, RequestRetryOptions> consumeRequestRetryOptions();

    @Override
    protected void configureRetry(T builder) {
        RetryOptionsAware.Retry retry;
        AzureProperties azureProperties = getAzureProperties();
        if (azureProperties instanceof RetryOptionsAware) {
            retry = ((RetryOptionsAware) azureProperties).getRetry();
        } else {
            LOGGER.warn("The properties {} is not of type RetryOptionsAware", azureProperties.getClass().getName());
            return;
        }

        if (retry instanceof StorageRetry) {
            RequestRetryOptions requestRetryOptions = STORAGE_RETRY_CONVERTER.convert((StorageRetry) retry);
            consumeRequestRetryOptions().accept(builder, requestRetryOptions);
        } else {
            LOGGER.warn("The retry in a storage client builder is of type {}", retry.getClass().getName());
        }
    }

    /**
     * The default implementation for setting retry policy in Storage clients. The storage clients are not using the
     * retry policy as other HTTP-based clients.
     * @return the empty consumer for setting retry policy.
     */
    @Override
    protected BiConsumer<T, RetryPolicy> consumeRetryPolicy() {
        return (a, b) -> { };
    }
}
