// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.stream.binder.eventhubs;

import com.azure.spring.cloud.stream.binder.eventhubs.properties.EventHubsProducerProperties;
import com.azure.spring.cloud.stream.binder.eventhubs.provisioning.EventHubsProducerDestination;
import com.azure.spring.integration.handler.DefaultMessageHandler;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.cloud.stream.binder.ExtendedProducerProperties;
import org.springframework.cloud.stream.provisioning.ProducerDestination;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpression;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.messaging.MessageChannel;

import static org.springframework.cloud.stream.binder.BinderHeaders.PARTITION_HEADER;

public class EventHubsMessageChannelBinderTests {

    private EventHubsTestBinder binder;

    private String destination = "dest";

    @Mock
    private MessageChannel messageChannel;

    private ExtendedProducerProperties<EventHubsProducerProperties> producerProperties;

    private static final ExpressionParser EXPRESSION_PARSER = new SpelExpressionParser();

    @BeforeEach
    public void setUp() {
        this.binder = new EventHubsTestBinder(null, null);
        this.producerProperties = new ExtendedProducerProperties<>(new EventHubsProducerProperties());
    }

    @Test
    public void createProducerMessageHandlerWithSetPartitionIdExpression() {
        ProducerDestination producerDestination = new EventHubsProducerDestination(destination);
        Expression payloadExpression = EXPRESSION_PARSER.parseExpression("payload.test");
        producerProperties.setPartitionKeyExpression(payloadExpression);
        DefaultMessageHandler messageHandler =
            (DefaultMessageHandler) this.binder.getBinder()
                                               .createProducerMessageHandler(producerDestination,
                                                   producerProperties, messageChannel);
        Assertions.assertThat(messageHandler)
                  .extracting("partitionIdExpression")
                  .returns("headers['" + PARTITION_HEADER + "']",
                      exp -> ((SpelExpression) exp).getExpressionString());
        Assertions.assertThat(messageHandler).hasFieldOrPropertyWithValue("partitionKeyExpression", null);
    }

    @Test
    public void createProducerMessageHandlerWithSetPartitionKeyExpression() {
        ProducerDestination producerDestination = new EventHubsProducerDestination(destination);
        DefaultMessageHandler messageHandler =
            (DefaultMessageHandler) this.binder.getBinder()
                                               .createProducerMessageHandler(producerDestination,
                                                   producerProperties, messageChannel);
        Assertions.assertThat(messageHandler)
                  .extracting("partitionKeyExpression")
                  .returns(true, exp -> exp != null);
        Assertions.assertThat(messageHandler).hasFieldOrPropertyWithValue("partitionIdExpression", null);
    }
}
