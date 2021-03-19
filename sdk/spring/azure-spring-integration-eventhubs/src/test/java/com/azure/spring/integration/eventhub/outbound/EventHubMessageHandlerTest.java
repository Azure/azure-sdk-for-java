// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.integration.eventhub.outbound;

import com.azure.spring.integration.core.AzureHeaders;
import com.azure.spring.integration.core.api.PartitionSupplier;
import com.azure.spring.integration.core.api.reactor.DefaultMessageHandler;
import com.azure.spring.integration.eventhub.api.EventHubOperation;
import com.azure.spring.integration.test.support.reactor.MessageHandlerTest;
import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.integration.expression.FunctionExpression;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class EventHubMessageHandlerTest extends MessageHandlerTest<EventHubOperation> {

    private static final String TO_PARTITION_SUPPLIER_METHOD_NAME = "toPartitionSupplier";
    private static final ExpressionParser EXPRESSION_PARSER = new SpelExpressionParser();
    private Expression partitionIdExpression;
    private Expression partitionKeyExpression;
    private byte[] payloadBytes;
    private String payload = "payload";

    @Mock
    private ConfigurableListableBeanFactory beanFactory;

    @Before
    @Override
    @SuppressWarnings("unchecked")
    public void setUp() {
        this.sendOperation = mock(EventHubOperation.class);
        when(this.sendOperation.sendAsync(eq(this.destination), isA(Message.class), isA(PartitionSupplier.class)))
            .thenReturn(mono);
        when(this.sendOperation
            .sendAsync(eq(this.dynamicDestination), isA(Message.class), isA(PartitionSupplier.class)))
            .thenReturn(mono);
        this.handler = new DefaultMessageHandler(this.destination, this.sendOperation);
    }

    private void initProperties() {
        partitionIdExpression = EXPRESSION_PARSER.parseExpression("headers['scst_partition']");
        partitionKeyExpression = new FunctionExpression<Message<?>>(m -> m.getPayload().hashCode());
        payloadBytes = payload.getBytes(StandardCharsets.UTF_8);

        this.handler.setBeanFactory(beanFactory);
        this.handler.afterPropertiesSet();
    }

    @Test
    public void testToPartitionSupplierReturnPartitionIdFromHeader() {
        initProperties();

        Message<?> message = new GenericMessage<>(payloadBytes, ImmutableMap.of(AzureHeaders.PARTITION_ID, 1));
        PartitionSupplier partitionSupplier = ReflectionTestUtils.invokeMethod(this.handler,
            DefaultMessageHandler.class, TO_PARTITION_SUPPLIER_METHOD_NAME, message);
        assertThat(partitionSupplier.getPartitionId()).isEqualTo("1");
        assertThat(partitionSupplier.getPartitionKey()).isNull();

        message = new GenericMessage<>(payloadBytes, ImmutableMap.of(AzureHeaders.PARTITION_ID, "2"));
        partitionSupplier = ReflectionTestUtils.invokeMethod(this.handler,
            DefaultMessageHandler.class, TO_PARTITION_SUPPLIER_METHOD_NAME, message);
        assertThat(partitionSupplier.getPartitionId()).isEqualTo("2");
        assertThat(partitionSupplier.getPartitionKey()).isNull();
    }

    @Test
    public void testToPartitionSupplierReturnPartitionIdFromPartitionIdExpression() {
        initProperties();

        this.handler.setPartitionIdExpression(partitionIdExpression);
        Message<?> message = new GenericMessage<>(payloadBytes, ImmutableMap.of("scst_partition", 3));
        PartitionSupplier partitionSupplier = ReflectionTestUtils.invokeMethod(this.handler,
            DefaultMessageHandler.class, TO_PARTITION_SUPPLIER_METHOD_NAME, message);
        assertThat(partitionSupplier.getPartitionId()).isEqualTo("3");
        assertThat(partitionSupplier.getPartitionKey()).isNull();
    }

    @Test
    public void testToPartitionSupplierReturnPartitionKeyFromPartitionKeyExpression() {
        initProperties();

        this.handler.setPartitionKeyExpression(partitionKeyExpression);
        Message<?> message = new GenericMessage<>(payloadBytes);
        PartitionSupplier partitionSupplier = ReflectionTestUtils.invokeMethod(this.handler,
            DefaultMessageHandler.class, TO_PARTITION_SUPPLIER_METHOD_NAME, message);
        assertThat(partitionSupplier.getPartitionId()).isNull();
        assertThat(partitionSupplier.getPartitionKey()).isEqualTo(String.valueOf(payloadBytes.hashCode()));
    }
}
