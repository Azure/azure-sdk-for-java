// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.stream.binder.eventhubs;

import com.azure.spring.cloud.stream.binder.eventhubs.core.properties.EventHubsProducerProperties;
import com.azure.spring.cloud.stream.binder.eventhubs.core.provisioning.EventHubsChannelProvisioner;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.cloud.stream.binder.BinderHeaders;
import org.springframework.cloud.stream.binder.ExtendedProducerProperties;
import org.springframework.cloud.stream.provisioning.ProducerDestination;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;
import org.springframework.test.util.ReflectionTestUtils;

import static org.mockito.Mockito.when;

public class EventHubsMessageChannelBinderTests {

    @Mock
    private ProducerDestination producerDestination;

    @Mock
    private MessageChannel errorChannel;

    private static final String PARTITION_ID_EXPRESSION = "partitionIdExpression";
    private static final String PARTITION_KEY_EXPRESSION = "partitionKeyExpression";
    private final EventHubsProducerProperties eventHubsProducerProperties = new EventHubsProducerProperties();
    private ExtendedProducerProperties<EventHubsProducerProperties> producerProperties =
        new ExtendedProducerProperties<>(eventHubsProducerProperties);
    private EventHubsMessageChannelTestBinder binder =
        new EventHubsMessageChannelTestBinder(BinderHeaders.STANDARD_HEADERS, new EventHubsChannelProvisioner(),
            null, null);

    @BeforeEach
    public void init() {
        MockitoAnnotations.openMocks(this);
        when(producerDestination.getName()).thenReturn("producer-test");
        binder.setApplicationContext(new GenericApplicationContext());
    }

    @Test
    public void withoutPartitionConfig() {
        MessageHandler handler = binder.createProducerMessageHandler(producerDestination,
            producerProperties, errorChannel);

        Assertions.assertNull(ReflectionTestUtils.getField(handler, PARTITION_ID_EXPRESSION));
        Assertions.assertNull(ReflectionTestUtils.getField(handler, PARTITION_KEY_EXPRESSION));
    }

    @Test
    public void withPartitionConfig() {
        producerProperties.setPartitionKeyExpression(new SpelExpressionParser().parseExpression("payload"));
        MessageHandler handler = binder.createProducerMessageHandler(producerDestination,
            producerProperties, errorChannel);

        Assertions.assertNotNull(ReflectionTestUtils.getField(handler, PARTITION_ID_EXPRESSION));
        Assertions.assertNull(ReflectionTestUtils.getField(handler, PARTITION_KEY_EXPRESSION));
    }

}
