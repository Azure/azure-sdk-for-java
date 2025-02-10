// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.service.implementation.eventgrid;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.util.HttpClientOptions;
import com.azure.messaging.eventgrid.EventGridPublisherClientBuilder;
import com.azure.messaging.eventgrid.EventGridServiceVersion;
import com.azure.spring.cloud.service.implementation.AzureHttpClientBuilderFactoryBaseTests;
import com.azure.spring.cloud.service.implementation.core.http.TestHttpClient;
import com.azure.spring.cloud.service.implementation.eventgrid.factory.EventGridPublisherClientBuilderFactory;
import org.mockito.verification.VerificationMode;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class EventGridPublisherClientBuilderFactoryTests extends
    AzureHttpClientBuilderFactoryBaseTests<
        EventGridPublisherClientBuilder,
        AzureEventGridTestProperties,
        EventGridPublisherClientBuilderFactoryTests.EventGridPublisherClientBuilderFactoryExt> {

    private static final String ENDPOINT = "https://abc.somelocation.eventgrid.azure.net/api/eventseventgrid.azure.net/api/events";

    @Override
    protected AzureEventGridTestProperties createMinimalServiceProperties() {
        return new AzureEventGridTestProperties();
    }

    @Override
    protected EventGridPublisherClientBuilderFactoryExt createClientBuilderFactoryWithMockBuilder(AzureEventGridTestProperties properties) {
        return new EventGridPublisherClientBuilderFactoryExt(properties);
    }

    @Override
    protected void buildClient(EventGridPublisherClientBuilder builder) {
        builder.buildEventGridEventPublisherClient();
    }

    @Override
    protected void verifyCredentialCalled(EventGridPublisherClientBuilder builder, Class<? extends TokenCredential> tokenCredentialClass, VerificationMode mode) {
        verify(builder, mode).credential(any(tokenCredentialClass));
    }

    @Override
    protected void verifyHttpClientCalled(EventGridPublisherClientBuilder builder, VerificationMode mode) {
        verify(builder, mode).httpClient(any(TestHttpClient.class));
    }

    @Override
    protected void verifyRetryOptionsCalled(EventGridPublisherClientBuilder builder, AzureEventGridTestProperties properties, VerificationMode mode) {
        verify(builder, mode).retryPolicy(any(RetryPolicy.class));
    }

    @Override
    protected void verifyServicePropertiesConfigured() {
        AzureEventGridTestProperties properties = new AzureEventGridTestProperties();
        properties.setServiceVersion(EventGridServiceVersion.V2018_01_01);
        properties.setEndpoint(ENDPOINT);

        final EventGridPublisherClientBuilderFactoryExt factoryExt = new EventGridPublisherClientBuilderFactoryExt(properties);
        final EventGridPublisherClientBuilder builder = factoryExt.build();

        verify(builder, times(1)).endpoint(ENDPOINT);
        verify(builder, times(1)).serviceVersion(EventGridServiceVersion.V2018_01_01);
    }

    @Override
    protected HttpClientOptions getHttpClientOptions(EventGridPublisherClientBuilderFactoryExt builderFactory) {
        return builderFactory.getHttpClientOptions();
    }

    @Override
    protected List<HttpPipelinePolicy> getHttpPipelinePolicies(EventGridPublisherClientBuilderFactoryExt builderFactory) {
        return builderFactory.getHttpPipelinePolicies();
    }

    static class EventGridPublisherClientBuilderFactoryExt extends EventGridPublisherClientBuilderFactory {

        EventGridPublisherClientBuilderFactoryExt(AzureEventGridTestProperties eventGridProperties) {
            super(eventGridProperties);
        }

        @Override
        protected EventGridPublisherClientBuilder createBuilderInstance() {
            return mock(EventGridPublisherClientBuilder.class);
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
