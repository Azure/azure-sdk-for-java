// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.service.implementation.eventhubs;

import com.azure.messaging.eventhubs.EventProcessorClientBuilder;
import com.azure.spring.cloud.service.eventhubs.consumer.EventHubsRecordMessageListener;
import com.azure.spring.cloud.service.implementation.AzureServiceClientBuilderFactoryBaseTests;
import com.azure.spring.cloud.service.implementation.eventhubs.factory.EventProcessorClientBuilderFactory;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class EventProcessorClientBuilderFactoryTests extends AzureServiceClientBuilderFactoryBaseTests<EventProcessorClientBuilder,
    AzureEventHubsTestProperties, EventProcessorClientBuilderFactory> {

    @Override
    protected AzureEventHubsTestProperties createMinimalServiceProperties() {
        return new AzureEventHubsTestProperties();
    }

    @Test
    void customPrefetchCount() {
        AzureEventHubsTestProperties properties = createMinimalServiceProperties();
        properties.getProcessor().setPrefetchCount(150);
        final TestEventProcessorClientBuilderFactory builderFactory =
            new TestEventProcessorClientBuilderFactory(properties);
        final EventProcessorClientBuilder builder = builderFactory.build();
        verify(builder, times(1)).prefetchCount(150);
    }

    static class TestEventProcessorClientBuilderFactory extends EventProcessorClientBuilderFactory {

        TestEventProcessorClientBuilderFactory(AzureEventHubsTestProperties properties) {
            super(properties.getProcessor(), null, mock(EventHubsRecordMessageListener.class), errorContext -> { });
        }

        @Override
        public EventProcessorClientBuilder createBuilderInstance() {
            return mock(EventProcessorClientBuilder.class);
        }
    }
}
