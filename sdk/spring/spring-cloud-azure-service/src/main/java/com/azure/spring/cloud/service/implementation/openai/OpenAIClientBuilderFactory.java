// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.service.implementation.openai;

import com.azure.ai.openai.OpenAIClientBuilder;
import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.util.ClientOptions;
import com.azure.core.util.Configuration;
import com.azure.spring.cloud.core.implementation.credential.descriptor.AuthenticationDescriptor;
import com.azure.spring.cloud.core.implementation.credential.descriptor.KeyAuthenticationDescriptor;
import com.azure.spring.cloud.core.implementation.credential.descriptor.TokenAuthenticationDescriptor;
import com.azure.spring.cloud.core.implementation.factory.AbstractAzureHttpClientBuilderFactory;
import com.azure.spring.cloud.core.implementation.properties.PropertyMapper;
import com.azure.spring.cloud.core.properties.AzureProperties;
import com.azure.spring.cloud.service.implementation.openai.credential.NonAzureOpenAIKeyAuthenticationDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.function.BiConsumer;

/**
 * Azure OpenAI client builder factory, it builds the OpenAI client according the configuration context
 * and OpenAI properties.
 */
public class OpenAIClientBuilderFactory extends AbstractAzureHttpClientBuilderFactory<OpenAIClientBuilder> {

    private static final Logger LOGGER = LoggerFactory.getLogger(OpenAIClientBuilderFactory.class);
    private final OpenAIClientProperties openAIClientProperties;

    /**
     * Create a {@link OpenAIClientBuilderFactory} instance with a {@link OpenAIClientProperties}.
     * @param openAIClientProperties the properties for the OpenAI client.
     */
    public OpenAIClientBuilderFactory(OpenAIClientProperties openAIClientProperties) {
        this.openAIClientProperties = openAIClientProperties;
    }

    @Override
    protected BiConsumer<OpenAIClientBuilder, ClientOptions> consumeClientOptions() {
        return OpenAIClientBuilder::clientOptions;
    }

    @Override
    protected BiConsumer<OpenAIClientBuilder, HttpClient> consumeHttpClient() {
        return OpenAIClientBuilder::httpClient;
    }

    @Override
    protected BiConsumer<OpenAIClientBuilder, HttpPipelinePolicy> consumeHttpPipelinePolicy() {
        return OpenAIClientBuilder::addPolicy;
    }

    @Override
    protected BiConsumer<OpenAIClientBuilder, HttpPipeline> consumeHttpPipeline() {
        return OpenAIClientBuilder::pipeline;
    }

    @Override
    protected BiConsumer<OpenAIClientBuilder, HttpLogOptions> consumeHttpLogOptions() {
        return OpenAIClientBuilder::httpLogOptions;
    }

    @Override
    protected BiConsumer<OpenAIClientBuilder, RetryPolicy> consumeRetryPolicy() {
        return OpenAIClientBuilder::retryPolicy;
    }

    @Override
    protected OpenAIClientBuilder createBuilderInstance() {
        return new OpenAIClientBuilder();
    }

    @Override
    protected AzureProperties getAzureProperties() {
        return this.openAIClientProperties;
    }

    @Override
    protected List<AuthenticationDescriptor<?>> getAuthenticationDescriptors(OpenAIClientBuilder builder) {
        return Arrays.asList(
            new KeyAuthenticationDescriptor(builder::credential),
            new TokenAuthenticationDescriptor(this.tokenCredentialResolver,  builder::credential),
            new NonAzureOpenAIKeyAuthenticationDescriptor(builder::credential)
        );
    }

    @Override
    protected void configureService(OpenAIClientBuilder builder) {
        PropertyMapper map = new PropertyMapper();
        map.from(this.openAIClientProperties.getEndpoint()).to(builder::endpoint);
        map.from(this.openAIClientProperties.getServiceVersion()).to(builder::serviceVersion);
    }

    @Override
    protected BiConsumer<OpenAIClientBuilder, Configuration> consumeConfiguration() {
        return OpenAIClientBuilder::configuration;
    }

    @Override
    protected BiConsumer<OpenAIClientBuilder, TokenCredential> consumeDefaultTokenCredential() {
        return OpenAIClientBuilder::credential;
    }

    @Override
    protected BiConsumer<OpenAIClientBuilder, String> consumeConnectionString() {
        LOGGER.debug("Connection string is not supported to configure in OpenAIClientBuilder");
        return (a, b) -> { };
    }
}
