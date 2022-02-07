// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.eventhub.stream.binder;

import com.azure.messaging.eventhubs.models.EventContext;
import com.azure.messaging.eventhubs.models.PartitionContext;
import com.azure.spring.eventhub.stream.binder.properties.EventHubConsumerProperties;
import com.azure.spring.eventhub.stream.binder.properties.EventHubProducerProperties;
import com.azure.spring.integration.core.api.StartPosition;
import com.azure.spring.integration.eventhub.api.EventHubClientFactory;
import com.azure.spring.integration.eventhub.support.EventHubTestOperation;
import com.azure.spring.servicebus.stream.binder.test.AzurePartitionBinderTests;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.cloud.stream.binder.ExtendedConsumerProperties;
import org.springframework.cloud.stream.binder.ExtendedProducerProperties;
import org.springframework.cloud.stream.binder.HeaderMode;
import reactor.core.publisher.Mono;

import static org.mockito.Mockito.when;

/**
 * Azure EventHubs Binder test
 */
public class EventHubPartitionBinderTests extends
    AzurePartitionBinderTests<EventHubTestBinder, ExtendedConsumerProperties<EventHubConsumerProperties>,
        ExtendedProducerProperties<EventHubProducerProperties>> {

    @Mock
    EventHubClientFactory clientFactory;

    @Mock
    EventContext eventContext;

    @Mock
    PartitionContext partitionContext;

    private EventHubTestBinder binder;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        when(this.eventContext.updateCheckpointAsync()).thenReturn(Mono.empty());
        when(this.eventContext.getPartitionContext()).thenReturn(this.partitionContext);
        when(this.partitionContext.getPartitionId()).thenReturn("1");

        this.binder = new EventHubTestBinder(new EventHubTestOperation(clientFactory, () -> eventContext));
    }

    @Override
    protected String getClassUnderTestName() {
        return EventHubTestBinder.class.getSimpleName();
    }

    @Override
    protected EventHubTestBinder getBinder() throws Exception {
        return this.binder;
    }

    @Override
    protected ExtendedConsumerProperties<EventHubConsumerProperties> createConsumerProperties() {
        ExtendedConsumerProperties<EventHubConsumerProperties> properties =
            new ExtendedConsumerProperties<>(new EventHubConsumerProperties());
        properties.setHeaderMode(HeaderMode.embeddedHeaders);
        properties.getExtension().setStartPosition(StartPosition.EARLIEST);
        return properties;
    }

    @Override
    protected ExtendedProducerProperties<EventHubProducerProperties> createProducerProperties(TestInfo testInfo) {
        ExtendedProducerProperties<EventHubProducerProperties> properties =
            new ExtendedProducerProperties<>(new EventHubProducerProperties());
        properties.setHeaderMode(HeaderMode.embeddedHeaders);
        return properties;
    }
}
