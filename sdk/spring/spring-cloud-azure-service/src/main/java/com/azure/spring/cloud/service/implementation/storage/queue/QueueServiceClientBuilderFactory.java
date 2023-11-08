// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.service.implementation.storage.queue;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.util.ClientOptions;
import com.azure.core.util.Configuration;
import com.azure.spring.cloud.core.implementation.credential.descriptor.AuthenticationDescriptor;
import com.azure.spring.cloud.core.implementation.credential.descriptor.SasAuthenticationDescriptor;
import com.azure.spring.cloud.core.implementation.credential.descriptor.TokenAuthenticationDescriptor;
import com.azure.spring.cloud.core.implementation.properties.PropertyMapper;
import com.azure.spring.cloud.core.properties.AzureProperties;
import com.azure.spring.cloud.service.implementation.storage.common.AbstractAzureStorageClientBuilderFactory;
import com.azure.spring.cloud.service.implementation.storage.common.credential.StorageSharedKeyAuthenticationDescriptor;
import com.azure.storage.common.policy.RequestRetryOptions;
import com.azure.storage.queue.QueueServiceClientBuilder;

import java.util.Arrays;
import java.util.List;
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
        return this.queueServiceClientProperties;
    }

    @Override
    protected List<AuthenticationDescriptor<?>> getAuthenticationDescriptors(QueueServiceClientBuilder builder) {
        return Arrays.asList(
            new StorageSharedKeyAuthenticationDescriptor(builder::credential),
            new SasAuthenticationDescriptor(builder::credential),
            new TokenAuthenticationDescriptor(this.tokenCredentialResolver, builder::credential));
    }

    @Override
    protected void configureService(QueueServiceClientBuilder builder) {
        PropertyMapper map = new PropertyMapper();
        map.from(queueServiceClientProperties.getMessageEncoding()).to(builder::messageEncoding);
        map.from(queueServiceClientProperties.getServiceVersion()).to(builder::serviceVersion);
        map.from(queueServiceClientProperties.getEndpoint()).to(builder::endpoint);
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

    @Override
    protected BiConsumer<QueueServiceClientBuilder, RequestRetryOptions> consumeRequestRetryOptions() {
        return QueueServiceClientBuilder::retryOptions;
    }
}
