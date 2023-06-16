// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.openai;

import com.azure.ai.openai.OpenAIAsyncClient;
import com.azure.ai.openai.OpenAIClient;
import com.azure.ai.openai.OpenAIClientBuilder;
import com.azure.spring.cloud.autoconfigure.AzureServiceConfigurationBase;
import com.azure.spring.cloud.autoconfigure.condition.ConditionalOnAnyProperty;
import com.azure.spring.cloud.autoconfigure.context.AzureGlobalProperties;
import com.azure.spring.cloud.autoconfigure.implementation.openai.AzureOpenAIProperties;
import com.azure.spring.cloud.core.customizer.AzureServiceClientBuilderCustomizer;
import com.azure.spring.cloud.service.implementation.openai.OpenAIClientBuilderFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * {@link EnableAutoConfiguration Auto-configuration} for Azure Open AI support.
 *
 * @since 4.9.0-beta.1
 */
@ConditionalOnClass(OpenAIClientBuilder.class)
@ConditionalOnProperty(value = "spring.cloud.azure.openai.enabled", havingValue = "true", matchIfMissing = true)
@ConditionalOnAnyProperty(prefix = "spring.cloud.azure.openai", name = "endpoint")
public class AzureOpenAIAutoConfiguration extends AzureServiceConfigurationBase {

    AzureOpenAIAutoConfiguration(AzureGlobalProperties azureGlobalProperties) {
        super(azureGlobalProperties);
    }

    @Bean
    @ConfigurationProperties(AzureOpenAIProperties.PREFIX)
    AzureOpenAIProperties azureOpenAIProperties() {
        return loadProperties(getAzureGlobalProperties(), new AzureOpenAIProperties());
    }

    /**
     * Autoconfigure the {@link OpenAIClient} instance.
     * @param builder the {@link OpenAIClientBuilder} to build the instance.
     * @return the azure open ai client instance.
     */
    @Bean
    @ConditionalOnMissingBean
    public OpenAIClient openAiClient(OpenAIClientBuilder builder) {
        return builder.buildClient();
    }

    /**
     * Autoconfigure the {@link OpenAIAsyncClient} instance.
     * @param builder the {@link OpenAIClientBuilder} to build the instance.
     * @return the azure open ai async client instance.
     */
    @Bean
    @ConditionalOnMissingBean
    public OpenAIAsyncClient openAIAsyncClient(OpenAIClientBuilder builder) {
        return builder.buildAsyncClient();
    }

    @Bean
    @ConditionalOnMissingBean
    OpenAIClientBuilder openAIClientBuilder(OpenAIClientBuilderFactory factory) {
        return factory.build();
    }

    @Bean
    @ConditionalOnMissingBean
    OpenAIClientBuilderFactory openAIClientBuilderFactory(AzureOpenAIProperties properties,
        ObjectProvider<AzureServiceClientBuilderCustomizer<OpenAIClientBuilder>> customizers) {
        OpenAIClientBuilderFactory factory = new OpenAIClientBuilderFactory(properties);
        customizers.orderedStream().forEach(factory::addBuilderCustomizer);
        return factory;
    }
}
