// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.service.implementation.eventhubs;

import com.azure.core.amqp.AmqpRetryOptions;
import com.azure.core.amqp.AmqpTransportType;
import com.azure.core.amqp.ProxyOptions;
import com.azure.core.credential.TokenCredential;
import com.azure.messaging.eventhubs.EventHubClientBuilder;
import com.azure.spring.cloud.service.implementation.AzureAmqpClientBuilderFactoryBaseTests;
import com.azure.spring.cloud.service.implementation.eventhubs.factory.EventHubClientBuilderFactory;
import org.mockito.verification.VerificationMode;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class EventHubsClientBuilderFactoryTests extends AzureAmqpClientBuilderFactoryBaseTests<
    EventHubClientBuilder,
    AzureEventHubsTestProperties,
    EventHubsClientBuilderFactoryTests.EventHubClientBuilderFactoryExt> {

    @Override
    protected AzureEventHubsTestProperties createMinimalServiceProperties() {
        AzureEventHubsTestProperties properties = new AzureEventHubsTestProperties();
        properties.setNamespace("test-namespace");
        properties.setEventHubName("test-eventhub");
        return properties;
    }

    @Override
    protected EventHubClientBuilderFactoryExt createClientBuilderFactoryWithMockBuilder(AzureEventHubsTestProperties properties) {
        return new EventHubClientBuilderFactoryExt(properties);
    }

    @Override
    protected void buildClient(EventHubClientBuilder builder) {
        builder.buildConsumerClient();
    }

    @Override
    protected void verifyCredentialCalled(EventHubClientBuilder builder,
                                          Class<? extends TokenCredential> tokenCredentialClass,
                                          VerificationMode mode) {
        verify(builder, mode).credential(any(String.class), any(String.class), any(tokenCredentialClass));
    }

    @Override
    protected void verifyRetryOptionsCalled(EventHubClientBuilder builder, VerificationMode mode) {
        verify(builder, mode).retryOptions(any(AmqpRetryOptions.class));
    }

    @Override
    protected void verifyProxyOptionsCalled(EventHubClientBuilder builder, VerificationMode mode) {
        verify(builder, mode).proxyOptions(any(ProxyOptions.class));
    }

    @Override
    protected void verifyTransportTypeCalled(EventHubClientBuilder builder, VerificationMode mode) {
        verify(builder, mode).transportType(any(AmqpTransportType.class));
    }

    static class EventHubClientBuilderFactoryExt extends EventHubClientBuilderFactory {

        EventHubClientBuilderFactoryExt(AzureEventHubsTestProperties properties) {
            super(properties);
        }

        @Override
        public EventHubClientBuilder createBuilderInstance() {
            return mock(EventHubClientBuilder.class);
        }
    }
}
