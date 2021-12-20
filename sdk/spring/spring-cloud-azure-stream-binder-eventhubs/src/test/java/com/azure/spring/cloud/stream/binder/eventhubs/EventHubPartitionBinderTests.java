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
import com.azure.spring.integration.eventhubs.inbound.EventHubsInboundChannelAdapter;
import com.azure.spring.integration.handler.DefaultMessageHandler;
import com.azure.spring.messaging.PropertiesSupplier;
import com.azure.spring.messaging.checkpoint.CheckpointConfig;
import com.azure.spring.service.eventhubs.processor.RecordEventProcessingListener;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

/**
 * Test cases are defined in super class
 *
 * @author Warren Zhu
 */
@Disabled("Not finished yet")
public class EventHubPartitionBinderTests extends
    AzurePartitionBinderTests<EventHubsTestBinder, ExtendedConsumerProperties<EventHubsConsumerProperties>,
        ExtendedProducerProperties<EventHubsProducerProperties>> {
    //TODO (Xiaobing Zhu): It is currently impossible to upgrade JUnit 4 to JUnit 5 due to the inheritance of Spring unit tests.

    @Mock
    EventContext eventContext;

    @Mock
    PartitionContext partitionContext;

    DefaultMessageHandler messageHandler;

    MessageProducer messageProducer;

    EventHubsProducerFactory producerFactory;

    private EventHubsTestBinder binder;

    private static final String DESTINATION = "testDestination";
//    private final static String EVENTHUBS = "output";
    private static final String CONSUMER = "test1";

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        when(this.eventContext.getPartitionContext()).thenReturn(this.partitionContext);
        when(this.partitionContext.getPartitionId()).thenReturn("1");

        NamespaceProperties namespaceProperties = new NamespaceProperties();
        this.producerFactory = spy(new DefaultEventHubsNamespaceProducerFactory(namespaceProperties));
        //        EventHubProducerAsyncClient producerAsyncClient = mock(EventHubProducerAsyncClient.class);
        //        doReturn(producerAsyncClient).when(this.producerFactory).createProducer(anyString());
        //        EventDataBatch eventDataBatch = mock(EventDataBatch.class);
        //        Mono<EventDataBatch> dataBatchMono = Mono.just(eventDataBatch);
        //        when(producerAsyncClient.createBatch(any(CreateBatchOptions.class))).thenReturn(dataBatchMono);
        //        when(producerAsyncClient.send(any(EventDataBatch.class))).thenReturn(Mono.empty());

        this.messageHandler = spy(new DefaultMessageHandler(DESTINATION, new EventHubsTestTemplate(producerFactory, new TestEventProcessorListener())));

        doNothing().when(this.messageHandler).afterPropertiesSet();

        CheckpointStore checkpointStore = new TestCheckpointStore();
        @SuppressWarnings("unchecked") EventHubsProcessorFactory processorFactory =
            spy(new DefaultEventHubsNamespaceProcessorFactory(checkpointStore,
                namespaceProperties, mock(PropertiesSupplier.class)));
        EventProcessorClient processorClient = mock(EventProcessorClient.class);
        doReturn(processorClient).when(processorFactory).createProcessor(anyString(), anyString(), any(TestEventProcessorListener.class));
        //        when(processorFactory.createProcessor(anyString(), anyString(), any(TestEventProcessorListener.class))).thenReturn(processorClient);
        EventHubsProcessorContainer processorContainer = spy(new EventHubsProcessorContainer(processorFactory));
        this.messageProducer = spy(new EventHubsInboundChannelAdapter(processorContainer,
            DESTINATION, CONSUMER, new CheckpointConfig()));
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

        }
    }
}
