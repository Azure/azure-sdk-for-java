// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.openai;

import com.azure.ai.openai.OpenAIServiceVersion;
import com.azure.ai.openai.OpenAIAsyncClient;
import com.azure.ai.openai.OpenAIClient;
import com.azure.ai.openai.OpenAIClientBuilder;
import com.azure.spring.cloud.autoconfigure.AbstractAzureServiceConfigurationTests;
import com.azure.spring.cloud.autoconfigure.context.AzureGlobalProperties;
import com.azure.spring.cloud.autoconfigure.implementation.openai.AzureOpenAIProperties;
import com.azure.spring.cloud.service.implementation.openai.OpenAIClientBuilderFactory;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AzureOpenAIAutoConfigurationTests extends AbstractAzureServiceConfigurationTests<
    OpenAIClientBuilderFactory, AzureOpenAIProperties> {

    static final String TEST_ENDPOINT_HTTPS = "https://test.openai.azure.com/";
    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(AzureOpenAIAutoConfiguration.class));

    @Override
    protected ApplicationContextRunner getMinimalContextRunner() {
        final OpenAIClientBuilder mockOpenAIClientBuilder = mock(OpenAIClientBuilder.class);
        when(mockOpenAIClientBuilder.buildClient()).thenReturn(mock(OpenAIClient.class));
        when(mockOpenAIClientBuilder.buildAsyncClient()).thenReturn(mock(OpenAIAsyncClient.class));

        return this.contextRunner
            .withBean(OpenAIClientBuilder.class, () -> mockOpenAIClientBuilder)
            .withPropertyValues("spring.cloud.azure.openai.endpoint="  + TEST_ENDPOINT_HTTPS);
    }

    @Override
    protected String getPropertyPrefix() {
        return AzureOpenAIProperties.PREFIX;
    }

    @Override
    protected Class<OpenAIClientBuilderFactory> getBuilderFactoryType() {
        return OpenAIClientBuilderFactory.class;
    }

    @Override
    protected Class<AzureOpenAIProperties> getConfigurationPropertiesType() {
        return AzureOpenAIProperties.class;
    }

    @Test
    void configureWithoutOpenAIClientBuilder() {
        this.contextRunner
            .withPropertyValues("spring.cloud.azure.openai.endpoint="  + TEST_ENDPOINT_HTTPS)
            .withClassLoader(new FilteredClassLoader(OpenAIClientBuilder.class))
            .run(context -> assertThat(context).doesNotHaveBean(AzureOpenAIAutoConfiguration.class));
    }

    @Test
    void configureWithOpenAIDisabled() {
        this.contextRunner
            .withPropertyValues(
                "spring.cloud.azure.openai.enabled=false",
                "spring.cloud.azure.openai.endpoint="  + TEST_ENDPOINT_HTTPS)
            .run(context -> assertThat(context).doesNotHaveBean(AzureOpenAIAutoConfiguration.class));
    }

    @Test
    void configureWithoutEndpoint() {
        this.contextRunner
            .run(context -> assertThat(context).doesNotHaveBean(AzureOpenAIAutoConfiguration.class));
    }

    @Test
    void configureWithEndpoint() {
        this.contextRunner
            .withPropertyValues("spring.cloud.azure.openai.endpoint="  + TEST_ENDPOINT_HTTPS)
            .withBean(AzureGlobalProperties.class, AzureGlobalProperties::new)
            .run(context -> {
                assertThat(context).hasSingleBean(AzureOpenAIAutoConfiguration.class);
                assertThat(context).hasSingleBean(AzureOpenAIProperties.class);
                assertThat(context).hasSingleBean(OpenAIClientBuilderFactory.class);
                assertThat(context).hasSingleBean(OpenAIClientBuilder.class);
                assertThat(context).hasSingleBean(OpenAIClient.class);
                assertThat(context).hasSingleBean(OpenAIAsyncClient.class);
            });
    }

    @Test
    void configurationPropertiesShouldBind() {
        String azureKeyCredential = "azure-key-credential";
        String nonAzureOpenAIKeyCredential = "non-azure-key-credential";
        this.contextRunner
            .withPropertyValues(
                "spring.cloud.azure.openai.endpoint=" + TEST_ENDPOINT_HTTPS,
                "spring.cloud.azure.openai.key=" + azureKeyCredential,
                "spring.cloud.azure.openai.non-azure-openai-key-credential=" + nonAzureOpenAIKeyCredential,
                "spring.cloud.azure.openai.service-version=v2022_12_01",
                "spring.cloud.azure.credential.client-id=openai-client-id"
            )
            .withBean(AzureGlobalProperties.class, AzureGlobalProperties::new)
            .run(context -> {
                assertThat(context).hasSingleBean(AzureOpenAIProperties.class);
                AzureOpenAIProperties properties = context.getBean(AzureOpenAIProperties.class);
                assertEquals(TEST_ENDPOINT_HTTPS, properties.getEndpoint());
                assertEquals(azureKeyCredential, properties.getKey());
                assertEquals(nonAzureOpenAIKeyCredential, properties.getNonAzureOpenAIKeyCredential());
                assertEquals(OpenAIServiceVersion.V2022_12_01, properties.getServiceVersion());
                assertEquals("openai-client-id", properties.getCredential().getClientId());
            });
    }
}
