// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.stream.binder.eventhubs;

import com.azure.messaging.eventhubs.checkpointstore.blob.BlobCheckpointStore;
import com.azure.messaging.eventhubs.models.ErrorContext;
import com.azure.messaging.eventhubs.models.EventBatchContext;
import com.azure.messaging.eventhubs.models.EventContext;
import com.azure.spring.cloud.stream.binder.eventhubs.properties.EventHubsBindingProperties;
import com.azure.spring.cloud.stream.binder.eventhubs.properties.EventHubsConsumerProperties;
import com.azure.spring.cloud.stream.binder.eventhubs.properties.EventHubsExtendedBindingProperties;
import com.azure.spring.cloud.stream.binder.eventhubs.properties.EventHubsProducerProperties;
import com.azure.spring.cloud.stream.binder.eventhubs.provisioning.EventHubsChannelProvisioner;
import com.azure.spring.eventhubs.core.EventHubsProcessorContainer;
import com.azure.spring.eventhubs.core.EventHubsTemplate;
import com.azure.spring.eventhubs.core.producer.DefaultEventHubsNamespaceProducerFactory;
import com.azure.spring.integration.eventhubs.inbound.health.EventHusProcessorInstrumentation;
import com.azure.spring.integration.instrumentation.Instrumentation;
import com.azure.spring.integration.instrumentation.InstrumentationManager;
import com.azure.spring.messaging.checkpoint.CheckpointConfig;
import com.azure.spring.messaging.checkpoint.CheckpointMode;
import com.azure.spring.service.eventhubs.processor.BatchEventProcessingListener;
import com.azure.spring.service.eventhubs.processor.EventProcessingListener;
import com.azure.spring.service.eventhubs.processor.RecordEventProcessingListener;
import com.azure.spring.service.eventhubs.processor.consumer.ErrorContextConsumer;
import com.azure.spring.service.eventhubs.properties.EventHubsProcessorDescriptor;
import com.azure.storage.blob.BlobContainerAsyncClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;
import org.springframework.cloud.stream.binder.BinderHeaders;
import org.springframework.cloud.stream.binder.ExtendedConsumerProperties;
import org.springframework.cloud.stream.binder.ExtendedProducerProperties;
import org.springframework.cloud.stream.binder.HeaderMode;
import org.springframework.cloud.stream.provisioning.ConsumerDestination;
import org.springframework.cloud.stream.provisioning.ProducerDestination;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.messaging.MessageChannel;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Duration;
import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

public class EventHubsHealthIndicatorTests {

    @Mock
    private ConsumerDestination consumerDestination;

    @Mock
    private ProducerDestination producerDestination;

    @Mock
    private MessageChannel errorChannel;

    @Mock
    private BlobContainerAsyncClient blobContainerAsyncClient;

    private EventHubsHealthIndicator healthIndicator;

    private ExtendedProducerProperties<EventHubsProducerProperties> producerProperties;
    private ExtendedConsumerProperties<EventHubsConsumerProperties> consumerProperties;

    private final EventHubsExtendedBindingProperties extendedBindingProperties =
        new EventHubsExtendedBindingProperties();
    private final EventHubsProducerProperties eventHubsProducerProperties = new EventHubsProducerProperties();
    private final EventHubsConsumerProperties eventHubsConsumerProperties = new EventHubsConsumerProperties();

    private static final String PRODUCER_NAME = "producer-test";
    private static final String CONSUMER_NAME = "consumer-test";
    private static final String CONSUMER_GROUP_NAME = "consumer-group";
    private static final String NAMESPACE_NAME = "eventhub-namespace";
    private static final String CONNECTION_STRING = "Endpoint=sb://test.servicebus.windows.net/;SharedAccessKeyName=RootManageSharedAccessKey;SharedAccessKey=key";

    private TestEventHubsMessageChannelBinder binder =
        new TestEventHubsMessageChannelBinder(BinderHeaders.STANDARD_HEADERS,
            new EventHubsChannelProvisioner(), null, null);

    @BeforeEach
    public void init() {
        MockitoAnnotations.openMocks(this);
        GenericApplicationContext context = new GenericApplicationContext();
        binder.setApplicationContext(context);
        healthIndicator = new EventHubsHealthIndicator(binder);
    }

    @Test
    public void testNoEventHubsInUse() {
        final Health health = healthIndicator.health();
        assertThat(health.getStatus()).isEqualTo(Status.UNKNOWN);
    }

    @Test
    public void producerHealthIndicatorIsUp() {
        prepareProducerProperties();
        when(producerDestination.getName()).thenReturn(PRODUCER_NAME);
        binder.createProducerMessageHandler(producerDestination, producerProperties, errorChannel);
        EventHubsTemplate eventHubsTemplate =
            (EventHubsTemplate) ReflectionTestUtils.getField(binder, "eventHubsTemplate");
        DefaultEventHubsNamespaceProducerFactory producerFactory =
            (DefaultEventHubsNamespaceProducerFactory) ReflectionTestUtils.getField(eventHubsTemplate, "producerFactory");
        producerFactory.createProducer(PRODUCER_NAME);
        final Health health = healthIndicator.health();
        assertThat(health.getStatus()).isEqualTo(Status.UP);
    }

    @Test
    public void producerHealthIndicatorIsDown() {
        prepareProducerProperties();
        when(producerDestination.getName()).thenReturn(PRODUCER_NAME);
        binder.createProducerMessageHandler(producerDestination, producerProperties, errorChannel);
        EventHubsTemplate eventHubsTemplate =
            (EventHubsTemplate) ReflectionTestUtils.getField(binder, "eventHubsTemplate");
        DefaultEventHubsNamespaceProducerFactory producerFactory =
            (DefaultEventHubsNamespaceProducerFactory) ReflectionTestUtils.getField(eventHubsTemplate, "producerFactory");
        producerFactory.createProducer(PRODUCER_NAME);
        binder.addProducerDownInstrumentation();
        final Health health = healthIndicator.health();
        assertThat(health.getStatus()).isEqualTo(Status.DOWN);
    }

    @Test
    public void processorRecordEventHealthIndicatorIsUp() {
        prepareConsumerProperties();
        CheckpointConfig checkpoint = eventHubsConsumerProperties.getCheckpoint();
        checkpoint.setMode(CheckpointMode.MANUAL);
        when(consumerDestination.getName()).thenReturn(CONSUMER_NAME);
        binder.setCheckpointStore(new BlobCheckpointStore(blobContainerAsyncClient));
        binder.createConsumerEndpoint(consumerDestination, CONSUMER_GROUP_NAME, consumerProperties);
        EventHubsProcessorContainer processorContainer =
            (EventHubsProcessorContainer) ReflectionTestUtils.getField(binder,
                "processorContainer");
        TestIntegrationRecordEventProcessingListener listener = new TestIntegrationRecordEventProcessingListener();
        processorContainer.subscribe(CONSUMER_NAME, CONSUMER_GROUP_NAME, listener);

        final Health health = healthIndicator.health();
        assertThat(health.getStatus()).isEqualTo(Status.UP);
    }

    @Test
    public void processorRecordEventHealthIndicatorIsDown() {
        prepareConsumerProperties();
        CheckpointConfig checkpoint = eventHubsConsumerProperties.getCheckpoint();
        checkpoint.setMode(CheckpointMode.MANUAL);
        when(consumerDestination.getName()).thenReturn(CONSUMER_NAME);
        binder.setCheckpointStore(new BlobCheckpointStore(blobContainerAsyncClient));
        binder.createConsumerEndpoint(consumerDestination, CONSUMER_GROUP_NAME, consumerProperties);
        EventHubsProcessorContainer processorContainer =
            (EventHubsProcessorContainer) ReflectionTestUtils.getField(binder,
                "processorContainer");
        TestIntegrationRecordEventProcessingListener listener = new TestIntegrationRecordEventProcessingListener();
        processorContainer.subscribe(CONSUMER_NAME, CONSUMER_GROUP_NAME, listener);
        binder.addProcessorDownInstrumentation();

        final Health health = healthIndicator.health();
        assertThat(health.getStatus()).isEqualTo(Status.DOWN);
    }

    @Test
    public void processorBatchEventHealthIndicatorIsUp() {
        prepareConsumerProperties();
        CheckpointConfig checkpoint = eventHubsConsumerProperties.getCheckpoint();
        checkpoint.setMode(CheckpointMode.BATCH);
        EventHubsProcessorDescriptor.Batch batch = eventHubsConsumerProperties.getBatch();
        batch.setMaxSize(10);
        batch.setMaxWaitTime(Duration.ofMillis(1));
        when(consumerDestination.getName()).thenReturn(CONSUMER_NAME);
        binder.setCheckpointStore(new BlobCheckpointStore(blobContainerAsyncClient));
        binder.createConsumerEndpoint(consumerDestination, CONSUMER_GROUP_NAME, consumerProperties);
        EventHubsProcessorContainer processorContainer =
            (EventHubsProcessorContainer) ReflectionTestUtils.getField(binder,
                "processorContainer");
        TestIntegrationBatchEventProcessingListener listener = new TestIntegrationBatchEventProcessingListener();
        processorContainer.subscribe(CONSUMER_NAME, CONSUMER_GROUP_NAME, listener);

        final Health health = healthIndicator.health();
        assertThat(health.getStatus()).isEqualTo(Status.UP);
    }

    @Test
    public void processorBatchEventHealthIndicatorIsDown() {
        prepareConsumerProperties();
        CheckpointConfig checkpoint = eventHubsConsumerProperties.getCheckpoint();
        checkpoint.setMode(CheckpointMode.BATCH);
        EventHubsProcessorDescriptor.Batch batch = eventHubsConsumerProperties.getBatch();
        batch.setMaxSize(10);
        batch.setMaxWaitTime(Duration.ofMillis(1));
        when(consumerDestination.getName()).thenReturn(CONSUMER_NAME);
        binder.setCheckpointStore(new BlobCheckpointStore(blobContainerAsyncClient));
        binder.createConsumerEndpoint(consumerDestination, CONSUMER_GROUP_NAME, consumerProperties);
        EventHubsProcessorContainer processorContainer =
            (EventHubsProcessorContainer) ReflectionTestUtils.getField(binder,
                "processorContainer");
        TestIntegrationBatchEventProcessingListener listener = new TestIntegrationBatchEventProcessingListener();
        processorContainer.subscribe(CONSUMER_NAME, CONSUMER_GROUP_NAME, listener);
        binder.addProcessorDownInstrumentation();

        final Health health = healthIndicator.health();
        assertThat(health.getStatus()).isEqualTo(Status.DOWN);
    }

    private void prepareProducerProperties() {
        eventHubsProducerProperties.setConnectionString(CONNECTION_STRING);

        EventHubsBindingProperties bindingProperties = new EventHubsBindingProperties();
        bindingProperties.setProducer(eventHubsProducerProperties);

        extendedBindingProperties.setBindings(new HashMap<String, EventHubsBindingProperties>() {
            {
                put(PRODUCER_NAME, bindingProperties);
            }
        });

        binder.setBindingProperties(extendedBindingProperties);

        producerProperties = new ExtendedProducerProperties<>(eventHubsProducerProperties);
        producerProperties.setHeaderMode(HeaderMode.embeddedHeaders);
    }

    private void prepareConsumerProperties() {
        eventHubsConsumerProperties.setNamespace(NAMESPACE_NAME);
        EventHubsBindingProperties bindingProperties = new EventHubsBindingProperties();
        bindingProperties.setConsumer(eventHubsConsumerProperties);

        extendedBindingProperties.setBindings(new HashMap<String, EventHubsBindingProperties>() {
            {
                put(CONSUMER_NAME, bindingProperties);
            }
        });
        binder.setBindingProperties(extendedBindingProperties);

        consumerProperties = new ExtendedConsumerProperties<>(eventHubsConsumerProperties);
        consumerProperties.setHeaderMode(HeaderMode.embeddedHeaders);
    }

    private interface TestInstrumentationEventProcessingListener extends EventProcessingListener {
        void setInstrumentationManager(InstrumentationManager instrumentationManager);
        void setInstrumentationId(String instrumentationId);
        default void updateInstrumentation(ErrorContext errorContext,
                                           InstrumentationManager instrumentationManager,
                                           String instrumentationId) {
            Instrumentation instrumentation = instrumentationManager.getHealthInstrumentation(instrumentationId);
            if (instrumentation != null) {
                if (instrumentation instanceof EventHusProcessorInstrumentation) {
                    ((EventHusProcessorInstrumentation) instrumentation).markError(errorContext);
                } else {
                    instrumentation.markDown(errorContext.getThrowable());
                }
            }
        }
    }

    private class TestIntegrationRecordEventProcessingListener implements
        TestInstrumentationEventProcessingListener, RecordEventProcessingListener {

        private InstrumentationManager instrumentationManager;
        private String instrumentationId;

        @Override
        public ErrorContextConsumer getErrorContextConsumer() {
            return errorContext -> {
                updateInstrumentation(errorContext, instrumentationManager, instrumentationId);
            };
        }

        @Override
        public void onEvent(EventContext eventContext) {

        }

        @Override
        public void setInstrumentationManager(InstrumentationManager instrumentationManager) {
            this.instrumentationManager = instrumentationManager;
        }

        @Override
        public void setInstrumentationId(String instrumentationId) {
            this.instrumentationId = instrumentationId;
        }
    }

    private class TestIntegrationBatchEventProcessingListener implements
        TestInstrumentationEventProcessingListener, BatchEventProcessingListener {

        private InstrumentationManager instrumentationManager;
        private String instrumentationId;

        @Override
        public ErrorContextConsumer getErrorContextConsumer() {
            return errorContext -> {
                updateInstrumentation(errorContext, instrumentationManager, instrumentationId);
            };
        }

        @Override
        public void onEventBatch(EventBatchContext eventBatchContext) {

        }

        @Override
        public void setInstrumentationManager(InstrumentationManager instrumentationManager) {
            this.instrumentationManager = instrumentationManager;
        }

        @Override
        public void setInstrumentationId(String instrumentationId) {
            this.instrumentationId = instrumentationId;
        }
    }
}
