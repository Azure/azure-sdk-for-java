// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.service.implementation.storage.queue;

import com.azure.core.util.ClientOptions;
import com.azure.spring.core.properties.AzureProperties;
import com.azure.spring.core.properties.PropertyMapper;
import com.azure.spring.service.implementation.storage.common.AbstractAzureStorageClientBuilderFactory;
import com.azure.storage.common.policy.RequestRetryOptions;
import com.azure.storage.queue.QueueServiceClientBuilder;

import java.util.function.BiConsumer;

/**
 * Storage Queue Service client builder factory, it builds the storage blob client according the configuration context
 * and blob properties.
 */
public class QueueServiceClientBuilderFactory extends AbstractAzureStorageClientBuilderFactory<QueueServiceClientBuilder> {

    private final QueueServiceClientProperties queueServiceClientProperties;

    /**
     * Create a {@link QueueServiceClientBuilderFactory} instance with the properties.
     * @param queueServiceClientProperties the properties of the queue service client.
     */
    public QueueServiceClientBuilderFactory(QueueServiceClientProperties queueServiceClientProperties) {
        this.queueServiceClientProperties = queueServiceClientProperties;
    }

    @Override
    protected BiConsumer<QueueServiceClientBuilder, ClientOptions> consumeClientOptions() {
        return QueueServiceClientBuilder::clientOptions;
    }

    @Override
    protected QueueServiceClientBuilder createBuilderInstance() {
        return new QueueServiceClientBuilder();
    }

    @Override
    protected AzureProperties getAzureProperties() {
        return this.queueServiceClientProperties;
    }

    @Override
    protected void configureService(QueueServiceClientBuilder builder) {
        PropertyMapper map = new PropertyMapper();
        map.from(queueServiceClientProperties.getMessageEncoding()).to(builder::messageEncoding);
        map.from(queueServiceClientProperties.getServiceVersion()).to(builder::serviceVersion);
        map.from(queueServiceClientProperties.getEndpoint()).to(builder::endpoint);
    }

    @Override
    protected BiConsumer<QueueServiceClientBuilder, RequestRetryOptions> consumeRequestRetryOptions() {
        return QueueServiceClientBuilder::retryOptions;
    }
}
