// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.service.appconfiguration;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.util.Configuration;
import com.azure.data.appconfiguration.ConfigurationClientBuilder;
import com.azure.spring.core.credential.descriptor.AuthenticationDescriptor;
import com.azure.spring.core.credential.descriptor.TokenAuthenticationDescriptor;
import com.azure.spring.core.factory.AbstractAzureHttpClientBuilderFactory;
import com.azure.spring.core.properties.AzureProperties;
import com.azure.spring.core.properties.util.PropertyMapper;

import java.util.Arrays;
import java.util.List;
import java.util.function.BiConsumer;

/**
 * Azure App Configuration client builder factory, it builds the {@link ConfigurationClientBuilder}.
 */
public class ConfigurationClientBuilderFactory extends AbstractAzureHttpClientBuilderFactory<ConfigurationClientBuilder> {

    private final AppConfigurationProperties appConfigurationProperties;


    public ConfigurationClientBuilderFactory(AppConfigurationProperties configurationProperties) {
        this.appConfigurationProperties = configurationProperties;
    }

    @Override
    protected BiConsumer<ConfigurationClientBuilder, HttpClient> consumeHttpClient() {
        return ConfigurationClientBuilder::httpClient;
    }

    @Override
    protected BiConsumer<ConfigurationClientBuilder, HttpPipelinePolicy> consumeHttpPipelinePolicy() {
        return ConfigurationClientBuilder::addPolicy;
    }

    @Override
    protected BiConsumer<ConfigurationClientBuilder, HttpPipeline> consumeHttpPipeline() {
        return ConfigurationClientBuilder::pipeline;
    }

    @Override
    protected BiConsumer<ConfigurationClientBuilder, HttpLogOptions> consumeHttpLogOptions() {
        return ConfigurationClientBuilder::httpLogOptions;
    }

    @Override
    protected ConfigurationClientBuilder createBuilderInstance() {
        return new ConfigurationClientBuilder();
    }

    @Override
    protected AzureProperties getAzureProperties() {
        return this.appConfigurationProperties;
    }

    @Override
    protected List<AuthenticationDescriptor<?>> getAuthenticationDescriptors(ConfigurationClientBuilder builder) {
        return Arrays.asList(
            new TokenAuthenticationDescriptor(p -> builder.credential(p.getCredential()))
        );
    }

    @Override
    protected void configureService(ConfigurationClientBuilder builder) {
        PropertyMapper map = new PropertyMapper();
        map.from(appConfigurationProperties.getEndpoint()).to(builder::endpoint);
        map.from(appConfigurationProperties.getConnectionString()).to(builder::connectionString);
        map.from(appConfigurationProperties.getServiceVersion()).to(builder::serviceVersion);
    }

    @Override
    protected BiConsumer<ConfigurationClientBuilder, Configuration> consumeConfiguration() {
        return ConfigurationClientBuilder::configuration;
    }

    @Override
    protected BiConsumer<ConfigurationClientBuilder, TokenCredential> consumeDefaultTokenCredential() {
        return ConfigurationClientBuilder::credential;
    }

    @Override
    protected BiConsumer<ConfigurationClientBuilder, String> consumeConnectionString() {
        return ConfigurationClientBuilder::connectionString;
    }

    @Override
    protected BiConsumer<ConfigurationClientBuilder, RetryPolicy> consumeRetryPolicy() {
        return ConfigurationClientBuilder::retryPolicy;
    }
}
