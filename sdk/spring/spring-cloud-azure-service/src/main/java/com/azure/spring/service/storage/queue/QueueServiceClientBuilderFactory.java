// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.service.storage.queue;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.util.ClientOptions;
import com.azure.core.util.Configuration;
import com.azure.spring.core.credential.descriptor.AuthenticationDescriptor;
import com.azure.spring.core.credential.descriptor.SasAuthenticationDescriptor;
import com.azure.spring.core.credential.descriptor.TokenAuthenticationDescriptor;
import com.azure.spring.core.properties.AzureProperties;
import com.azure.spring.core.properties.util.PropertyMapper;
import com.azure.spring.service.storage.common.AbstractAzureStorageClientBuilderFactory;
import com.azure.spring.service.storage.common.credential.StorageSharedKeyAuthenticationDescriptor;
import com.azure.storage.common.policy.RequestRetryOptions;
import com.azure.storage.queue.QueueMessageEncoding;
import com.azure.storage.queue.QueueServiceClientBuilder;

import java.util.Arrays;
import java.util.List;
import java.util.function.BiConsumer;

/**
 * Storage Queue Service client builder factory, it builds the storage blob client according the configuration context
 * and blob properties.
 */
public class QueueServiceClientBuilderFactory extends AbstractAzureStorageClientBuilderFactory<QueueServiceClientBuilder> {

    private final StorageQueueProperties queueProperties;

    public QueueServiceClientBuilderFactory(StorageQueueProperties queueProperties) {
        this.queueProperties = queueProperties;
    }

    @Override
    protected BiConsumer<QueueServiceClientBuilder, ClientOptions> consumeClientOptions() {
        return QueueServiceClientBuilder::clientOptions;
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
    protected BiConsumer<QueueServiceClientBuilder, HttpPipeline> consumeHttpPipeline() {
        return QueueServiceClientBuilder::pipeline;
    }

    @Override
    protected BiConsumer<QueueServiceClientBuilder, HttpLogOptions> consumeHttpLogOptions() {
        return QueueServiceClientBuilder::httpLogOptions;
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
        PropertyMapper map = new PropertyMapper();
        map.from(queueProperties.getMessageEncoding()).to(p -> builder.messageEncoding(convertToMessageEncoding(p)));
        map.from(queueProperties.getServiceVersion()).to(builder::serviceVersion);
        map.from(queueProperties.getEndpoint()).to(builder::endpoint);
    }

    @Override
    protected BiConsumer<QueueServiceClientBuilder, Configuration> consumeConfiguration() {
        return QueueServiceClientBuilder::configuration;
    }

    @Override
    protected BiConsumer<QueueServiceClientBuilder, TokenCredential> consumeDefaultTokenCredential() {
        return QueueServiceClientBuilder::credential;
    }

    @Override
    protected BiConsumer<QueueServiceClientBuilder, String> consumeConnectionString() {
        return QueueServiceClientBuilder::connectionString;
    }

    private QueueMessageEncoding convertToMessageEncoding(String messageEncoding) {
        return QueueMessageEncoding.BASE64
            .name()
            .equalsIgnoreCase(messageEncoding) ? QueueMessageEncoding.BASE64 : QueueMessageEncoding.NONE;
    }

    @Override
    protected BiConsumer<QueueServiceClientBuilder, RequestRetryOptions> consumeRequestRetryOptions() {
        return QueueServiceClientBuilder::retryOptions;
    }
}
