// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.stream.binder.eventhubs;

import com.azure.messaging.eventhubs.checkpointstore.blob.BlobCheckpointStore;
import com.azure.spring.cloud.service.eventhubs.properties.EventBatchProperties;
import com.azure.spring.cloud.stream.binder.eventhubs.core.properties.EventHubsBindingProperties;
import com.azure.spring.cloud.stream.binder.eventhubs.core.properties.EventHubsConsumerProperties;
import com.azure.spring.cloud.stream.binder.eventhubs.core.properties.EventHubsExtendedBindingProperties;
import com.azure.spring.cloud.stream.binder.eventhubs.core.properties.EventHubsProducerProperties;
import com.azure.spring.cloud.stream.binder.eventhubs.core.provisioning.EventHubsChannelProvisioner;
import com.azure.spring.integration.eventhubs.inbound.EventHubsInboundChannelAdapter;
import com.azure.spring.messaging.eventhubs.core.checkpoint.CheckpointConfig;
import com.azure.spring.messaging.eventhubs.core.checkpoint.CheckpointMode;
import com.azure.spring.messaging.eventhubs.core.EventHubsTemplate;
import com.azure.spring.messaging.eventhubs.core.DefaultEventHubsNamespaceProducerFactory;
import com.azure.storage.blob.BlobContainerAsyncClient;
import org.junit.jupiter.api.Assertions;
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
    private static final String CONNECTION_STRING = "Endpoint=sb://test.servicebus.windows.net/;"
        + "SharedAccessKeyName=RootManageSharedAccessKey;SharedAccessKey=key";

    private final EventHubsMessageChannelTestBinder binder = new EventHubsMessageChannelTestBinder(
        BinderHeaders.STANDARD_HEADERS, new EventHubsChannelProvisioner(), null, null);

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
        Assertions.assertNotNull(eventHubsTemplate);

        DefaultEventHubsNamespaceProducerFactory producerFactory = (DefaultEventHubsNamespaceProducerFactory)
            ReflectionTestUtils.getField(eventHubsTemplate, "producerFactory");
        Assertions.assertNotNull(producerFactory);
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
        Assertions.assertNotNull(eventHubsTemplate);

        DefaultEventHubsNamespaceProducerFactory producerFactory = (DefaultEventHubsNamespaceProducerFactory)
            ReflectionTestUtils.getField(eventHubsTemplate, "producerFactory");
        Assertions.assertNotNull(producerFactory);

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
        EventHubsInboundChannelAdapter consumerEndpoint = (EventHubsInboundChannelAdapter) binder.createConsumerEndpoint(consumerDestination, CONSUMER_GROUP_NAME,
            consumerProperties);

        consumerEndpoint.afterPropertiesSet();
        consumerEndpoint.doStart();

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

        binder.addProcessorDownInstrumentation();

        final Health health = healthIndicator.health();
        assertThat(health.getStatus()).isEqualTo(Status.DOWN);
    }

    @Test
    public void processorBatchEventHealthIndicatorIsUp() {
        prepareConsumerProperties();

        CheckpointConfig checkpoint = eventHubsConsumerProperties.getCheckpoint();
        checkpoint.setMode(CheckpointMode.BATCH);

        EventBatchProperties batch = eventHubsConsumerProperties.getBatch();
        batch.setMaxSize(10);
        batch.setMaxWaitTime(Duration.ofMillis(1));

        consumerProperties.setBatchMode(true);

        when(consumerDestination.getName()).thenReturn(CONSUMER_NAME);

        binder.setCheckpointStore(new BlobCheckpointStore(blobContainerAsyncClient));
        EventHubsInboundChannelAdapter consumerEndpoint = (EventHubsInboundChannelAdapter) binder.createConsumerEndpoint(consumerDestination, CONSUMER_GROUP_NAME,
            consumerProperties);

        consumerEndpoint.afterPropertiesSet();
        consumerEndpoint.doStart();

        final Health health = healthIndicator.health();
        assertThat(health.getStatus()).isEqualTo(Status.UP);
    }

    @Test
    public void processorBatchEventHealthIndicatorIsDown() {
        prepareConsumerProperties();
        CheckpointConfig checkpoint = eventHubsConsumerProperties.getCheckpoint();
        checkpoint.setMode(CheckpointMode.BATCH);
        EventBatchProperties batch = eventHubsConsumerProperties.getBatch();
        batch.setMaxSize(10);
        batch.setMaxWaitTime(Duration.ofMillis(1));
        when(consumerDestination.getName()).thenReturn(CONSUMER_NAME);
        binder.setCheckpointStore(new BlobCheckpointStore(blobContainerAsyncClient));
        binder.createConsumerEndpoint(consumerDestination, CONSUMER_GROUP_NAME, consumerProperties);

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

}
