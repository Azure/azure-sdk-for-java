// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.service.implementation.eventgrid.factory;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.util.ClientOptions;
import com.azure.core.util.Configuration;
import com.azure.messaging.eventgrid.EventGridPublisherClientBuilder;
import com.azure.spring.cloud.core.implementation.credential.descriptor.AuthenticationDescriptor;
import com.azure.spring.cloud.core.implementation.credential.descriptor.KeyAuthenticationDescriptor;
import com.azure.spring.cloud.core.implementation.credential.descriptor.SasAuthenticationDescriptor;
import com.azure.spring.cloud.core.implementation.credential.descriptor.TokenAuthenticationDescriptor;
import com.azure.spring.cloud.core.implementation.factory.AbstractAzureHttpClientBuilderFactory;
import com.azure.spring.cloud.core.implementation.properties.PropertyMapper;
import com.azure.spring.cloud.core.properties.AzureProperties;
import com.azure.spring.cloud.service.implementation.eventgrid.properties.EventGridPublisherClientProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.function.BiConsumer;

/**
 * Azure Event Grid Publisher client builder factory, it builds the {@link EventGridPublisherClientBuilder}.
 */
public class EventGridPublisherClientBuilderFactory extends AbstractAzureHttpClientBuilderFactory<EventGridPublisherClientBuilder> {

    public static final Logger LOGGER = LoggerFactory.getLogger(EventGridPublisherClientBuilderFactory.class);

    private final EventGridPublisherClientProperties eventGridPublisherClientProperties;

    public EventGridPublisherClientBuilderFactory(EventGridPublisherClientProperties properties) {
        this.eventGridPublisherClientProperties = properties;
    }

    @Override
    protected BiConsumer<EventGridPublisherClientBuilder, ClientOptions> consumeClientOptions() {
        return EventGridPublisherClientBuilder::clientOptions;
    }

    @Override
    protected BiConsumer<EventGridPublisherClientBuilder, HttpClient> consumeHttpClient() {
        return EventGridPublisherClientBuilder::httpClient;
    }

    @Override
    protected BiConsumer<EventGridPublisherClientBuilder, HttpPipelinePolicy> consumeHttpPipelinePolicy() {
        return EventGridPublisherClientBuilder::addPolicy;
    }

    @Override
    protected BiConsumer<EventGridPublisherClientBuilder, HttpPipeline> consumeHttpPipeline() {
        return EventGridPublisherClientBuilder::pipeline;
    }

    @Override
    protected BiConsumer<EventGridPublisherClientBuilder, HttpLogOptions> consumeHttpLogOptions() {
        return EventGridPublisherClientBuilder::httpLogOptions;
    }

    @Override
    protected BiConsumer<EventGridPublisherClientBuilder, RetryPolicy> consumeRetryPolicy() {
        return EventGridPublisherClientBuilder::retryPolicy;
    }

    @Override
    protected EventGridPublisherClientBuilder createBuilderInstance() {
        return new EventGridPublisherClientBuilder();
    }

    @Override
    protected AzureProperties getAzureProperties() {
        return this.eventGridPublisherClientProperties;
    }

    @Override
    protected List<AuthenticationDescriptor<?>> getAuthenticationDescriptors(EventGridPublisherClientBuilder builder) {
        return Arrays.asList(
            new KeyAuthenticationDescriptor(builder::credential),
            new SasAuthenticationDescriptor(builder::credential),
            new TokenAuthenticationDescriptor(this.tokenCredentialResolver, builder::credential)
        );
    }

    @Override
    protected void configureService(EventGridPublisherClientBuilder builder) {
        PropertyMapper map = new PropertyMapper();
        map.from(eventGridPublisherClientProperties.getEndpoint()).to(builder::endpoint);
        map.from(eventGridPublisherClientProperties.getServiceVersion()).to(builder::serviceVersion);
    }

    @Override
    protected BiConsumer<EventGridPublisherClientBuilder, Configuration> consumeConfiguration() {
        return EventGridPublisherClientBuilder::configuration;
    }

    @Override
    protected BiConsumer<EventGridPublisherClientBuilder, TokenCredential> consumeDefaultTokenCredential() {
        return EventGridPublisherClientBuilder::credential;
    }

    @Override
    protected BiConsumer<EventGridPublisherClientBuilder, String> consumeConnectionString() {
        LOGGER.debug("Connection string is not supported to configure in EventGridPublisherClientBuilder");
        return (a, b) -> { };
    }
}
