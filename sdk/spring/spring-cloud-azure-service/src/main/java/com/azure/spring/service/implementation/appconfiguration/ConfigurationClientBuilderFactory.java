// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.service.implementation.appconfiguration;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.util.ClientOptions;
import com.azure.core.util.Configuration;
import com.azure.data.appconfiguration.ConfigurationClientBuilder;
import com.azure.spring.core.credential.descriptor.AuthenticationDescriptor;
import com.azure.spring.core.credential.descriptor.TokenAuthenticationDescriptor;
import com.azure.spring.core.factory.AbstractAzureHttpClientBuilderFactory;
import com.azure.spring.core.properties.AzureProperties;
import com.azure.spring.core.properties.PropertyMapper;

import java.util.Arrays;
import java.util.List;
import java.util.function.BiConsumer;

/**
 * Azure App Configuration client builder factory, it builds the {@link ConfigurationClientBuilder}.
 */
public class ConfigurationClientBuilderFactory extends AbstractAzureHttpClientBuilderFactory<ConfigurationClientBuilder> {

    public ConfigurationClientBuilderFactory() {
    }

    @Override
    protected BiConsumer<ConfigurationClientBuilder, ClientOptions> consumeClientOptions() {
        return ConfigurationClientBuilder::clientOptions;
    }

    @Override
    protected ConfigurationClientBuilder createBuilderInstance() {
        return new ConfigurationClientBuilder();
    }

    @Override
    protected AzureProperties getAzureProperties() {
        // TODO, remove method after configuration is implemented everywhere
        return null;
    }

    @Override
    protected void configureService(ConfigurationClientBuilder builder) {

    }
}
