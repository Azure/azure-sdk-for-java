// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.servicebus.stream.binder;

import com.azure.spring.servicebus.stream.binder.properties.ServiceBusExtendedBindingProperties;
import com.azure.spring.servicebus.stream.binder.properties.ServiceBusProducerProperties;
import com.azure.spring.servicebus.stream.binder.provisioning.ServiceBusProducerDestination;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.cloud.stream.binder.ExtendedProducerProperties;
import org.springframework.cloud.stream.provisioning.ProducerDestination;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpression;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;

import static org.mockito.Mockito.mock;

public class ServiceBusMessageChannelBinderTest {

    @SuppressWarnings("unchecked")
    ServiceBusMessageChannelBinder<ServiceBusExtendedBindingProperties> serviceBusMessageChannelBinder =
        Mockito.mock(ServiceBusMessageChannelBinder.class, Mockito.CALLS_REAL_METHODS);

    private static final ExpressionParser EXPRESSION_PARSER = new SpelExpressionParser();

    private ExtendedProducerProperties<ServiceBusProducerProperties> producerProperties;

    private MessageChannel messageChannel = mock(MessageChannel.class);

    private GenericApplicationContext applicationContext = new GenericApplicationContext();

    private static final String PARTITION_HEADER = "headers['scst_partition']";

    private String destination = "dest";

    @Before
    public void setUp() {
        producerProperties = new ExtendedProducerProperties<>(new ServiceBusProducerProperties());
        serviceBusMessageChannelBinder.setApplicationContext(applicationContext);
    }

    @Test
    public void testCreateProducerMessageHandlerWithSetPartitionIdExpression() {
        ProducerDestination producerDestination = new ServiceBusProducerDestination(destination);
        Expression payloadExpression = EXPRESSION_PARSER.parseExpression("payload.test");
        producerProperties.setPartitionKeyExpression(payloadExpression);
        MessageHandler messageHandler = serviceBusMessageChannelBinder.
            createProducerMessageHandler(producerDestination, producerProperties, messageChannel);
        Assertions.assertThat(messageHandler)
            .extracting("partitionIdExpression1")
            .returns(PARTITION_HEADER,
                exp -> ((SpelExpression) exp).getExpressionString());
    }

    @Test
    public void testCreateProducerMessageHandlerWithSetPartitionKeyExpression() {
        ProducerDestination producerDestination = new ServiceBusProducerDestination(destination);
        MessageHandler messageHandler = serviceBusMessageChannelBinder.
            createProducerMessageHandler(producerDestination, producerProperties, messageChannel);
        Assertions.assertThat(messageHandler)
            .extracting("partitionKeyExpression")
            .returns(true, exp -> exp != null);
        Assertions.assertThat(messageHandler).hasFieldOrPropertyWithValue("partitionIdExpression", null);
    }

}
