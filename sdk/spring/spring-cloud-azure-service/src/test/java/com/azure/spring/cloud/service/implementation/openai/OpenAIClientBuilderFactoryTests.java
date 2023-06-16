// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.service.implementation.openai;

import com.azure.ai.openai.OpenAIClientBuilder;
import com.azure.ai.openai.OpenAIServiceVersion;
import com.azure.ai.openai.models.NonAzureOpenAIKeyCredential;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.credential.TokenCredential;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.util.HttpClientOptions;
import com.azure.spring.cloud.service.implementation.AzureHttpClientBuilderFactoryBaseTests;
import com.azure.spring.cloud.service.implementation.core.http.TestHttpClient;
import org.junit.jupiter.api.Test;
import org.mockito.verification.VerificationMode;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class OpenAIClientBuilderFactoryTests
    extends AzureHttpClientBuilderFactoryBaseTests<OpenAIClientBuilder,
    AzureOpenAITestProperties, OpenAIClientBuilderFactoryTests.OpenAIClientBuilderFactoryTestsExt> {

    private static final String ENDPOINT = "https://test.openai.azure.com/";

    @Test
    void testAzureKeyCredentialConfigured() {
        AzureOpenAITestProperties properties = new AzureOpenAITestProperties();

        properties.setKey("test-key");

        final OpenAIClientBuilderFactoryTestsExt factoryExt = new OpenAIClientBuilderFactoryTestsExt(properties);
        final OpenAIClientBuilder builder = factoryExt.build();

        verify(builder, times(1)).credential(any(AzureKeyCredential.class));
    }

    @Test
    void testNonAzureOpenAIKeyCredentialConfigured() {
        AzureOpenAITestProperties properties = new AzureOpenAITestProperties();

        properties.setNonAzureOpenAIKeyCredential("non-azure-openai-key");

        final OpenAIClientBuilderFactoryTestsExt factoryExt = new OpenAIClientBuilderFactoryTestsExt(properties);
        final OpenAIClientBuilder builder = factoryExt.build();

        verify(builder, times(1)).credential(any(NonAzureOpenAIKeyCredential.class));
    }

    @Override
    protected void buildClient(OpenAIClientBuilder builder) {
        builder.buildClient();
    }

    @Override
    protected AzureOpenAITestProperties createMinimalServiceProperties() {
        AzureOpenAITestProperties properties = new AzureOpenAITestProperties();
        properties.setEndpoint(ENDPOINT);
        return properties;
    }

    @Override
    protected OpenAIClientBuilderFactoryTestsExt createClientBuilderFactoryWithMockBuilder(AzureOpenAITestProperties properties) {
        return new OpenAIClientBuilderFactoryTestsExt(properties);
    }

    @Override
    protected void verifyServicePropertiesConfigured() {
        AzureOpenAITestProperties properties = new AzureOpenAITestProperties();
        properties.setEndpoint(ENDPOINT);
        properties.setServiceVersion(OpenAIServiceVersion.V2022_12_01);

        final OpenAIClientBuilderFactoryTestsExt factoryExt = new OpenAIClientBuilderFactoryTestsExt(properties);
        final OpenAIClientBuilder builder = factoryExt.build();

        verify(builder, times(1)).endpoint(ENDPOINT);
        verify(builder, times(1)).serviceVersion(OpenAIServiceVersion.V2022_12_01);
    }

    @Override
    protected void verifyCredentialCalled(OpenAIClientBuilder builder,
                                          Class<? extends TokenCredential> tokenCredentialClass,
                                          VerificationMode mode) {
        verify(builder, mode).credential(any(tokenCredentialClass));
    }

    @Override
    protected HttpClientOptions getHttpClientOptions(OpenAIClientBuilderFactoryTestsExt builderFactory) {
        return builderFactory.getHttpClientOptions();
    }

    @Override
    protected List<HttpPipelinePolicy> getHttpPipelinePolicies(OpenAIClientBuilderFactoryTestsExt builderFactory) {
        return builderFactory.getHttpPipelinePolicies();
    }

    @Override
    protected void verifyHttpClientCalled(OpenAIClientBuilder builder, VerificationMode mode) {
        verify(builder, mode).httpClient(any(TestHttpClient.class));
    }

    @Override
    protected void verifyRetryOptionsCalled(OpenAIClientBuilder builder, AzureOpenAITestProperties properties,
                                            VerificationMode mode) {
        verify(builder, mode).retryPolicy(any(RetryPolicy.class));
    }

    static class OpenAIClientBuilderFactoryTestsExt extends OpenAIClientBuilderFactory {

        OpenAIClientBuilderFactoryTestsExt(OpenAIClientProperties openAIClientProperties) {
            super(openAIClientProperties);
        }

        @Override
        protected OpenAIClientBuilder createBuilderInstance() {
            return mock(OpenAIClientBuilder.class);
        }

        @Override
        public HttpClientOptions getHttpClientOptions() {
            return super.getHttpClientOptions();
        }

        @Override
        public List<HttpPipelinePolicy> getHttpPipelinePolicies() {
            return super.getHttpPipelinePolicies();
        }

    }
}
