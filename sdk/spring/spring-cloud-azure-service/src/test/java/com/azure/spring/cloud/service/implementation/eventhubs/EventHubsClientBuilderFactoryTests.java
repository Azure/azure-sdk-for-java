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
import com.azure.spring.cloud.service.implementation.eventhubs.properties.EventHubClientCommonProperties;
import org.junit.jupiter.api.Test;
import org.mockito.verification.VerificationMode;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class EventHubsClientBuilderFactoryTests extends AzureAmqpClientBuilderFactoryBaseTests<
    EventHubClientBuilder,
    AzureEventHubsTestProperties,
    EventHubsClientBuilderFactoryTests.EventHubClientBuilderFactoryExt> {

    @Test
    void consumerClientPropertiesConfigured() {
        AzureEventHubsTestProperties.Consumer properties = new AzureEventHubsTestProperties.Consumer();
        properties.setNamespace("test-namespace");
        properties.setEventHubName("test-event-hub");
        properties.setCustomEndpointAddress("test-custom-endpoint-address");
        properties.setConsumerGroup("test-consumer-group");
        properties.setPrefetchCount(10);

        EventHubClientBuilderFactoryExt builderFactory = new EventHubClientBuilderFactoryExt(properties);
        EventHubClientBuilder builder = builderFactory.build();

        verify(builder, times(1)).fullyQualifiedNamespace(properties.getFullyQualifiedNamespace());
        verify(builder, times(1)).eventHubName("test-event-hub");
        verify(builder, times(1)).customEndpointAddress("test-custom-endpoint-address");
        verify(builder, times(1)).consumerGroup("test-consumer-group");
        verify(builder, times(1)).prefetchCount(10);
    }

    @Test
    void producerClientPropertiesConfigured() {
        AzureEventHubsTestProperties.Producer properties = new AzureEventHubsTestProperties.Producer();
        properties.setNamespace("test-namespace");
        properties.setEventHubName("test-event-hub");
        properties.setCustomEndpointAddress("test-custom-endpoint-address");

        EventHubClientBuilderFactoryExt builderFactory = new EventHubClientBuilderFactoryExt(properties);
        EventHubClientBuilder builder = builderFactory.build();

        verify(builder, times(1)).fullyQualifiedNamespace(properties.getFullyQualifiedNamespace());
        verify(builder, times(1)).eventHubName("test-event-hub");
        verify(builder, times(1)).customEndpointAddress("test-custom-endpoint-address");
    }

    @Override
    protected AzureEventHubsTestProperties createMinimalServiceProperties() {
        AzureEventHubsTestProperties properties = new AzureEventHubsTestProperties();
        properties.setNamespace("test-namespace");
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
        verify(builder, mode).credential(any(tokenCredentialClass));
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

    @Override
    protected void verifyServicePropertiesConfigured() {
        AzureEventHubsTestProperties properties = new AzureEventHubsTestProperties();
        properties.setNamespace("test-namespace");
        properties.setEventHubName("test-event-hub");
        properties.setCustomEndpointAddress("test-custom-endpoint-address");
        properties.setSharedConnection(true);

        EventHubClientBuilderFactoryExt builderFactory = new EventHubClientBuilderFactoryExt(properties);
        EventHubClientBuilder builder = builderFactory.build();

        verify(builder, times(1)).fullyQualifiedNamespace(properties.getFullyQualifiedNamespace());
        verify(builder, times(1)).eventHubName("test-event-hub");
        verify(builder, times(1)).customEndpointAddress("test-custom-endpoint-address");
        verify(builder, times(1)).shareConnection();
    }

    static class EventHubClientBuilderFactoryExt extends EventHubClientBuilderFactory {

        EventHubClientBuilderFactoryExt(EventHubClientCommonProperties properties) {
            super(properties);
        }

        @Override
        public EventHubClientBuilder createBuilderInstance() {
            return mock(EventHubClientBuilder.class);
        }
    }
}
