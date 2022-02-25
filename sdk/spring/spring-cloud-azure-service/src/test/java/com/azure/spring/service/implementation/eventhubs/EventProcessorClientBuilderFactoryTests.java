// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.service.implementation.eventhubs;

import com.azure.messaging.eventhubs.EventProcessorClientBuilder;
import com.azure.spring.service.eventhubs.consumer.EventHubsMessageListener;
import com.azure.spring.service.implementation.AzureServiceClientBuilderFactoryBaseTests;
import com.azure.spring.service.implementation.eventhubs.factory.EventProcessorClientBuilderFactory;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class EventProcessorClientBuilderFactoryTests extends AzureServiceClientBuilderFactoryBaseTests<EventProcessorClientBuilder,
    TestAzureEventHubsProperties, EventProcessorClientBuilderFactory> {

    @Override
    protected TestAzureEventHubsProperties createMinimalServiceProperties() {
        return new TestAzureEventHubsProperties();
    }

    @Test
    void customPrefetchCount() {
        TestAzureEventHubsProperties properties = createMinimalServiceProperties();
        properties.getProcessor().setPrefetchCount(150);
        final TestEventProcessorClientBuilderFactory builderFactory =
            new TestEventProcessorClientBuilderFactory(properties);
        final EventProcessorClientBuilder builder = builderFactory.build();
        verify(builder, times(1)).prefetchCount(150);
    }

    static class TestEventProcessorClientBuilderFactory extends EventProcessorClientBuilderFactory {

        TestEventProcessorClientBuilderFactory(TestAzureEventHubsProperties properties) {
            super(properties.getProcessor(), null, mock(EventHubsMessageListener.class), errorContext -> { });
        }

        @Override
        public EventProcessorClientBuilder createBuilderInstance() {
            return mock(EventProcessorClientBuilder.class);
        }
    }
}
