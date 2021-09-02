// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.storage.queue;

import com.azure.core.http.HttpClient;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.util.Configuration;
import com.azure.spring.cloud.autoconfigure.storage.common.credential.StorageSharedKeyAuthenticationDescriptor;
import com.azure.spring.core.credential.descriptor.AuthenticationDescriptor;
import com.azure.spring.core.credential.descriptor.SasAuthenticationDescriptor;
import com.azure.spring.core.credential.descriptor.TokenAuthenticationDescriptor;
import com.azure.spring.core.factory.AbstractAzureHttpClientBuilderFactory;
import com.azure.spring.core.properties.AzureProperties;
import com.azure.storage.queue.QueueMessageEncoding;
import com.azure.storage.queue.QueueServiceClientBuilder;
import org.springframework.boot.context.properties.PropertyMapper;

import java.util.Arrays;
import java.util.List;
import java.util.function.BiConsumer;

import static com.azure.spring.core.ApplicationId.AZURE_SPRING_STORAGE_QUEUE;
import static com.azure.spring.core.ApplicationId.VERSION;

/**
 * Storage Queue Service client builder factory, it builds the storage blob client according the configuration context
 * and blob properties.
 */
public class QueueServiceClientBuilderFactory extends AbstractAzureHttpClientBuilderFactory<QueueServiceClientBuilder> {

    private final AzureStorageQueueProperties queueProperties;

    public QueueServiceClientBuilderFactory(AzureStorageQueueProperties queueProperties) {
        this.queueProperties = queueProperties;
    }

    @Override
    protected BiConsumer<QueueServiceClientBuilder, HttpClient> consumeHttpClient() {
        return QueueServiceClientBuilder::httpClient;
    }

    @Override
    protected BiConsumer<QueueServiceClientBuilder, HttpPipelinePolicy> consumeHttpPipelinePolicy() {
        return QueueServiceClientBuilder::addPolicy;
    }

    @Override
    protected QueueServiceClientBuilder createBuilderInstance() {
        return new QueueServiceClientBuilder();
    }

    @Override
    protected AzureProperties getAzureProperties() {
        return this.queueProperties;
    }

    @Override
    protected List<AuthenticationDescriptor<?>> getAuthenticationDescriptors(QueueServiceClientBuilder builder) {
        return Arrays.asList(
            new StorageSharedKeyAuthenticationDescriptor(provider -> builder.credential(provider.getCredential())),
            new SasAuthenticationDescriptor(provider -> builder.credential(provider.getCredential())),
            new TokenAuthenticationDescriptor(provider -> builder.credential(provider.getCredential())));
    }

    @Override
    protected void configureService(QueueServiceClientBuilder builder) {
        PropertyMapper map = PropertyMapper.get().alwaysApplyingWhenNonNull();
        map.from(queueProperties.getMessageEncoding()).to(p -> builder.messageEncoding(convertToMessageEncoding(p)));
        map.from(queueProperties.getServiceVersion()).to(builder::serviceVersion);
        map.from(queueProperties.getEndpoint()).to(builder::endpoint);
    }

    @Override
    protected BiConsumer<QueueServiceClientBuilder, Configuration> consumeConfiguration() {
        return QueueServiceClientBuilder::configuration;
    }

    @Override
    protected String getApplicationId() {
        return AZURE_SPRING_STORAGE_QUEUE + VERSION;
    }

    private QueueMessageEncoding convertToMessageEncoding(String messageEncoding) {
        return QueueMessageEncoding.BASE64
            .name()
            .equalsIgnoreCase(messageEncoding) ? QueueMessageEncoding.BASE64 : QueueMessageEncoding.NONE;
    }
}
