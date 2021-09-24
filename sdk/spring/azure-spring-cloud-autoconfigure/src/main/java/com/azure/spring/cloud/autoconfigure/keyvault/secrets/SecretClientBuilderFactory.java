// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.keyvault.secrets;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.util.Configuration;
import com.azure.security.keyvault.secrets.SecretClientBuilder;
import com.azure.spring.core.credential.descriptor.AuthenticationDescriptor;
import com.azure.spring.core.credential.descriptor.TokenAuthenticationDescriptor;
import com.azure.spring.core.factory.AbstractAzureHttpClientBuilderFactory;
import com.azure.spring.core.properties.AzureProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.PropertyMapper;

import java.util.Collections;
import java.util.List;
import java.util.function.BiConsumer;

/**
 * Azure Key Vault certificate client builder factory, it builds the {@link SecretClientBuilder}.
 */
public class SecretClientBuilderFactory extends AbstractAzureHttpClientBuilderFactory<SecretClientBuilder> {

    private static final Logger LOGGER = LoggerFactory.getLogger(SecretClientBuilderFactory.class);

    private final AzureKeyVaultSecretProperties secretProperties;

    public SecretClientBuilderFactory(AzureKeyVaultSecretProperties keyVaultProperties) {
        this.secretProperties = keyVaultProperties;
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
    protected SecretClientBuilder createBuilderInstance() {
        return new SecretClientBuilder();
    }

    @Override
    protected AzureProperties getAzureProperties() {
        return this.secretProperties;
    }

    @Override
    protected List<AuthenticationDescriptor<?>> getAuthenticationDescriptors(SecretClientBuilder builder) {
        return Collections.singletonList(
            new TokenAuthenticationDescriptor(provider -> builder.credential(provider.getCredential())));
    }

    @Override
    protected void configureService(SecretClientBuilder builder) {
        PropertyMapper map = PropertyMapper.get().alwaysApplyingWhenNonNull();
        map.from(secretProperties.getVaultUrl()).to(builder::vaultUrl);
        map.from(secretProperties.getServiceVersion()).to(builder::serviceVersion);
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

}
