// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.stream.binder.eventhubs;

import com.azure.messaging.eventhubs.CheckpointStore;
import com.azure.messaging.eventhubs.EventProcessorClient;
import com.azure.messaging.eventhubs.models.Checkpoint;
import com.azure.messaging.eventhubs.models.EventContext;
import com.azure.messaging.eventhubs.models.PartitionContext;
import com.azure.messaging.eventhubs.models.PartitionOwnership;
import com.azure.spring.cloud.stream.binder.eventhubs.properties.EventHubsConsumerProperties;
import com.azure.spring.cloud.stream.binder.eventhubs.properties.EventHubsProducerProperties;
import com.azure.spring.cloud.stream.binder.servicebus.test.AzurePartitionBinderTests;
import com.azure.spring.eventhubs.core.EventHubsProcessorContainer;
import com.azure.spring.eventhubs.core.processor.DefaultEventHubsNamespaceProcessorFactory;
import com.azure.spring.eventhubs.core.processor.EventHubsProcessorFactory;
import com.azure.spring.eventhubs.core.producer.DefaultEventHubsNamespaceProducerFactory;
import com.azure.spring.eventhubs.core.producer.EventHubsProducerFactory;
import com.azure.spring.eventhubs.core.properties.NamespaceProperties;
import com.azure.spring.eventhubs.support.converter.EventHubsMessageConverter;
import com.azure.spring.integration.eventhubs.inbound.EventHubsInboundChannelAdapter;
import com.azure.spring.integration.handler.DefaultMessageHandler;
import com.azure.spring.messaging.PropertiesSupplier;
import com.azure.spring.messaging.checkpoint.CheckpointConfig;
import com.azure.spring.service.eventhubs.processor.RecordEventProcessingListener;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.cloud.stream.binder.ExtendedConsumerProperties;
import org.springframework.cloud.stream.binder.ExtendedProducerProperties;
import org.springframework.cloud.stream.binder.HeaderMode;
import org.springframework.integration.core.MessageProducer;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.function.Supplier;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

public class EventHubPartitionBinderTests extends
    AzurePartitionBinderTests<EventHubsTestBinder, ExtendedConsumerProperties<EventHubsConsumerProperties>,
        ExtendedProducerProperties<EventHubsProducerProperties>> {

    @Mock
    EventContext eventContext;

    @Mock
    PartitionContext partitionContext;

    DefaultMessageHandler messageHandler;

    MessageProducer messageProducer;

    EventHubsProducerFactory producerFactory;

    private EventHubsTestBinder binder;
    private Supplier<EventContext> eventContextSupplier;

    private static final EventHubsMessageConverter MESSAGE_CONVERTER = new EventHubsMessageConverter();

    private static final String DESTINATION = "testDestination";
    private static final String CONSUMER = "test1";

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        when(this.eventContext.getPartitionContext()).thenReturn(this.partitionContext);
        when(this.partitionContext.getPartitionId()).thenReturn("1");

        NamespaceProperties namespaceProperties = new NamespaceProperties();
        this.producerFactory = spy(new DefaultEventHubsNamespaceProducerFactory(namespaceProperties));
        TestEventProcessorListener processorListener = new TestEventProcessorListener();
        this.messageHandler = spy(new DefaultMessageHandler(DESTINATION,
            new EventHubsTestTemplate(producerFactory, processorListener)));

        doNothing().when(this.messageHandler).afterPropertiesSet();

        CheckpointStore checkpointStore = new TestCheckpointStore();
        @SuppressWarnings("unchecked") EventHubsProcessorFactory processorFactory =
            spy(new DefaultEventHubsNamespaceProcessorFactory(checkpointStore,
                namespaceProperties, mock(PropertiesSupplier.class)));
        EventProcessorClient processorClient = mock(EventProcessorClient.class);
        doReturn(processorClient).when(processorFactory).createProcessor(anyString(), anyString(), processorListener);
        EventHubsProcessorContainer processorContainer = spy(new EventHubsProcessorContainer(processorFactory));
        this.messageProducer = spy(new EventHubsInboundChannelAdapter(processorContainer,
            DESTINATION, CONSUMER, new CheckpointConfig()));
        this.eventContextSupplier = () -> eventContext;
        this.binder = new EventHubsTestBinder(this.messageHandler, this.messageProducer);
    }

    @Override
    protected String getClassUnderTestName() {
        return EventHubsTestBinder.class.getSimpleName();
    }

    @Override
    protected EventHubsTestBinder getBinder() {
        return this.binder;
    }

    @Override
    protected ExtendedConsumerProperties<EventHubsConsumerProperties> createConsumerProperties() {
        ExtendedConsumerProperties<EventHubsConsumerProperties> properties =
            new ExtendedConsumerProperties<>(new EventHubsConsumerProperties());
        properties.setHeaderMode(HeaderMode.embeddedHeaders);
        return properties;
    }

    @Override
    protected ExtendedProducerProperties<EventHubsProducerProperties> createProducerProperties(TestInfo testInfo) {
        ExtendedProducerProperties<EventHubsProducerProperties> properties =
            new ExtendedProducerProperties<>(new EventHubsProducerProperties());
        properties.setHeaderMode(HeaderMode.embeddedHeaders);
        return properties;
    }

    static class TestCheckpointStore implements CheckpointStore {

        @Override
        public Flux<PartitionOwnership> listOwnership(String s, String s1, String s2) {
            return null;
        }

        @Override
        public Flux<PartitionOwnership> claimOwnership(List<PartitionOwnership> list) {
            return null;
        }

        @Override
        public Flux<Checkpoint> listCheckpoints(String s, String s1, String s2) {
            return null;
        }

        @Override
        public Mono<Void> updateCheckpoint(Checkpoint checkpoint) {
            return null;
        }
    }

    static class TestEventProcessorListener implements RecordEventProcessingListener {

        @Override
        public void onEvent(EventContext eventContext) {
//            Message<?> message = MESSAGE_CONVERTER.toMessage(eventContext.getEventData(), new MessageHeaders(null), Message.class);
//            sendMessage(message);

        }
    }
}
