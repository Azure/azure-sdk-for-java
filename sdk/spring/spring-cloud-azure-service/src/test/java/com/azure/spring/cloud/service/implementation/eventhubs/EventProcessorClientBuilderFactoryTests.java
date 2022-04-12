// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.service.implementation.eventhubs;

import com.azure.core.credential.TokenCredential;
import com.azure.messaging.eventhubs.EventProcessorClientBuilder;
import com.azure.spring.cloud.service.eventhubs.consumer.EventHubsRecordMessageListener;
import com.azure.spring.cloud.service.implementation.AzureServiceClientBuilderFactoryBaseTests;
import com.azure.spring.cloud.service.implementation.eventhubs.factory.EventProcessorClientBuilderFactory;
import org.junit.jupiter.api.Test;
import org.mockito.verification.VerificationMode;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class EventProcessorClientBuilderFactoryTests extends AzureServiceClientBuilderFactoryBaseTests<
    EventProcessorClientBuilder,
    AzureEventHubsTestProperties.Processor,
    EventProcessorClientBuilderFactoryTests.EventProcessorClientBuilderFactoryExt> {

    @Override
    protected AzureEventHubsTestProperties.Processor createMinimalServiceProperties() {
        AzureEventHubsTestProperties.Processor properties = new AzureEventHubsTestProperties.Processor();
        properties.setNamespace("test-namespace");
        properties.setEventHubName("test-eventhub");
        return properties;
    }

    @Override
    protected EventProcessorClientBuilderFactoryExt createClientBuilderFactoryWithMockBuilder(AzureEventHubsTestProperties.Processor properties) {
        return new EventProcessorClientBuilderFactoryExt(properties);
    }

    @Override
    protected void buildClient(EventProcessorClientBuilder builder) {
        builder.buildEventProcessorClient();
    }

    @Override
    protected void verifyCredentialCalled(EventProcessorClientBuilder builder,
                                          Class<? extends TokenCredential> tokenCredentialClass,
                                          VerificationMode mode) {
        verify(builder, mode).credential(any(String.class), any(String.class), any(tokenCredentialClass));
    }

    @Test
    void customPrefetchCount() {
        AzureEventHubsTestProperties.Processor properties = createMinimalServiceProperties();
        properties.setPrefetchCount(150);
        final EventProcessorClientBuilderFactoryExt builderFactory = createClientBuilderFactoryWithMockBuilder(properties);
        final EventProcessorClientBuilder builder = builderFactory.build();
        verify(builder, times(1)).prefetchCount(150);
    }

    static class EventProcessorClientBuilderFactoryExt extends EventProcessorClientBuilderFactory {

        EventProcessorClientBuilderFactoryExt(AzureEventHubsTestProperties.Processor properties) {
            super(properties, null, mock(EventHubsRecordMessageListener.class), errorContext -> { });
        }

        @Override
        public EventProcessorClientBuilder createBuilderInstance() {
            return mock(EventProcessorClientBuilder.class);
        }
    }
}
