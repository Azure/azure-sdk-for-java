// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.service.implementation.keyvault.secrets;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.util.ClientOptions;
import com.azure.core.util.Configuration;
import com.azure.security.keyvault.secrets.SecretClientBuilder;
import com.azure.spring.cloud.core.implementation.credential.descriptor.AuthenticationDescriptor;
import com.azure.spring.cloud.core.implementation.credential.descriptor.TokenAuthenticationDescriptor;
import com.azure.spring.cloud.core.implementation.factory.AbstractAzureHttpClientBuilderFactory;
import com.azure.spring.cloud.core.implementation.properties.PropertyMapper;
import com.azure.spring.cloud.core.properties.AzureProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.function.BiConsumer;

/**
 * Azure Key Vault certificate client builder factory, it builds the {@link SecretClientBuilder}.
 */
public class SecretClientBuilderFactory extends AbstractAzureHttpClientBuilderFactory<SecretClientBuilder> {

    private static final Logger LOGGER = LoggerFactory.getLogger(SecretClientBuilderFactory.class);

    private final SecretClientProperties secretClientProperties;

    /**
     * Create a {@link SecretClientBuilderFactory} with the {@link SecretClientProperties}.
     * @param secretClientProperties the properties of the secret client.
     */
    public SecretClientBuilderFactory(SecretClientProperties secretClientProperties) {
        this.secretClientProperties = secretClientProperties;
    }

    @Override
    protected BiConsumer<SecretClientBuilder, ClientOptions> consumeClientOptions() {
        return SecretClientBuilder::clientOptions;
    }

    @Override
    protected BiConsumer<SecretClientBuilder, HttpClient> consumeHttpClient() {
        return SecretClientBuilder::httpClient;
    }

    @Override
    protected BiConsumer<SecretClientBuilder, HttpPipelinePolicy> consumeHttpPipelinePolicy() {
        return SecretClientBuilder::addPolicy;
    }

    @Override
    protected BiConsumer<SecretClientBuilder, HttpPipeline> consumeHttpPipeline() {
        return SecretClientBuilder::pipeline;
    }

    @Override
    protected BiConsumer<SecretClientBuilder, HttpLogOptions> consumeHttpLogOptions() {
        return SecretClientBuilder::httpLogOptions;
    }

    @Override
    protected SecretClientBuilder createBuilderInstance() {
        return new SecretClientBuilder();
    }

    @Override
    protected AzureProperties getAzureProperties() {
        return this.secretClientProperties;
    }

    @Override
    protected List<AuthenticationDescriptor<?>> getAuthenticationDescriptors(SecretClientBuilder builder) {
        return Arrays.asList(
            new TokenAuthenticationDescriptor(this.tokenCredentialResolver, builder::credential)
        );
    }

    @Override
    protected void configureService(SecretClientBuilder builder) {
        PropertyMapper map = new PropertyMapper();
        map.from(secretClientProperties.getEndpoint()).to(builder::vaultUrl);
        map.from(secretClientProperties.getServiceVersion()).to(builder::serviceVersion);
    }

    @Override
    protected BiConsumer<SecretClientBuilder, Configuration> consumeConfiguration() {
        return SecretClientBuilder::configuration;
    }

    @Override
    protected BiConsumer<SecretClientBuilder, TokenCredential> consumeDefaultTokenCredential() {
        return SecretClientBuilder::credential;
    }

    @Override
    protected BiConsumer<SecretClientBuilder, String> consumeConnectionString() {
        LOGGER.debug("Connection string is not supported to configure in SecretClientBuilder");
        return (a, b) -> { };
    }

    @Override
    protected BiConsumer<SecretClientBuilder, RetryPolicy> consumeRetryPolicy() {
        return SecretClientBuilder::retryPolicy;
    }
}
