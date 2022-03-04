// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.service.implementation.eventhubs;

import com.azure.core.amqp.AmqpRetryOptions;
import com.azure.messaging.eventhubs.EventHubClientBuilder;
import com.azure.messaging.eventhubs.EventHubConsumerClient;
import com.azure.spring.cloud.service.implementation.AzureServiceClientBuilderFactoryBaseTests;
import com.azure.spring.cloud.service.implementation.eventhubs.factory.EventHubClientBuilderFactory;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class EventHubsClientBuilderFactoryTests extends AzureServiceClientBuilderFactoryBaseTests<EventHubClientBuilder,
    AzureEventHubsTestProperties, EventHubClientBuilderFactory> {

    @Override
    protected AzureEventHubsTestProperties createMinimalServiceProperties() {
        return new AzureEventHubsTestProperties();
    }

    @Test
    void testRetryOptionsConfigured() {
        AzureEventHubsTestProperties properties = createMinimalServiceProperties();
        properties.getRetry().setMaxRetries(1);
        properties.getRetry().setBaseDelay(Duration.ofSeconds(2));
        properties.getRetry().setMaxDelay(Duration.ofSeconds(4));
        final EventHubClientBuilderFactoryExt builderFactory = new EventHubClientBuilderFactoryExt(properties);
        final EventHubClientBuilder builder = builderFactory.build();
        final EventHubConsumerClient client = builder.buildConsumerClient();
        verify(builder, times(1)).retry(any(AmqpRetryOptions.class));
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
