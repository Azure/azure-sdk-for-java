// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.integration.eventhub.outbound;

import com.azure.spring.integration.core.AzureHeaders;
import com.azure.spring.integration.core.api.PartitionSupplier;
import com.azure.spring.integration.core.api.reactor.DefaultMessageHandler;
import com.azure.spring.integration.eventhub.api.EventHubOperation;
import com.azure.spring.integration.test.support.reactor.MessageHandlerTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.integration.expression.FunctionExpression;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class EventHubMessageHandlerTest extends MessageHandlerTest<EventHubOperation> {

    private static final ExpressionParser EXPRESSION_PARSER = new SpelExpressionParser();
    private static final String TO_PARTITION_SUPPLIER_METHOD_NAME = "toPartitionSupplier";
    @Mock
    private ConfigurableListableBeanFactory beanFactory;
    private Expression partitionIdExpression;
    private Expression partitionKeyExpression;
    private String payload = "payload";
    private byte[] payloadBytes;
    private AutoCloseable closeable;

    @BeforeEach
    @Override
    @SuppressWarnings("unchecked")
    public void setUp() {
        this.closeable = MockitoAnnotations.openMocks(this);
        this.sendOperation = mock(EventHubOperation.class);
        when(this.sendOperation.sendAsync(eq(this.destination), isA(Message.class),
                                          isA(PartitionSupplier.class))).thenReturn(mono);
        when(
            this.sendOperation.sendAsync(eq(this.dynamicDestination), isA(Message.class), isA(PartitionSupplier.class)))
            .thenReturn(mono);
        this.handler = new DefaultMessageHandler(this.destination, this.sendOperation);
    }

    @AfterEach
    public void close() throws Exception {
        closeable.close();
    }

    @Test
    public void testToPartitionSupplierReturnPartitionIdFromHeader() {
        setPropertiesForPartitionSupplier();

        Map<String, Integer> headers = new HashMap<>(1);
        headers.put(AzureHeaders.PARTITION_ID, 1);
        // set partition id with integer type
        Message<?> message = new GenericMessage<>(payloadBytes, Collections.unmodifiableMap(headers));
        PartitionSupplier partitionSupplier = ReflectionTestUtils.invokeMethod(this.handler,
                                                                               DefaultMessageHandler.class,
                                                                               TO_PARTITION_SUPPLIER_METHOD_NAME,
                                                                               message);
        assertThat(partitionSupplier.getPartitionId()).isEqualTo("1");
        assertThat(partitionSupplier.getPartitionKey()).isNull();

        Map<String, String> headersv2 = new HashMap<>(1);
        headersv2.put(AzureHeaders.PARTITION_ID, "2");
        // set partition id with string type
        message = new GenericMessage<>(payloadBytes, Collections.unmodifiableMap(headersv2));
        this.handler.setPartitionIdExpression(partitionIdExpression);
        partitionSupplier = ReflectionTestUtils.invokeMethod(this.handler, DefaultMessageHandler.class,
                                                             TO_PARTITION_SUPPLIER_METHOD_NAME, message);
        assertThat(partitionSupplier.getPartitionId()).isEqualTo("2");
        assertThat(partitionSupplier.getPartitionKey()).isNull();
    }

    @Test
    public void testToPartitionSupplierReturnPartitionIdFromHeaderPriority() {
        setPropertiesForPartitionSupplier();

        Map<String, Integer> headers = new HashMap<>(2);
        headers.put(AzureHeaders.PARTITION_ID, 2);
        headers.put("scst_partition", 3);
        // set header azure_partition_id and expression partitionIdExpression
        Message<?> message = new GenericMessage<>(payloadBytes, Collections.unmodifiableMap(headers));
        this.handler.setPartitionIdExpression(partitionIdExpression);
        PartitionSupplier partitionSupplier = ReflectionTestUtils.invokeMethod(this.handler,
                                                                               DefaultMessageHandler.class,
                                                                               TO_PARTITION_SUPPLIER_METHOD_NAME,
                                                                               message);
        assertThat(partitionSupplier.getPartitionId()).isEqualTo("2");
        assertThat(partitionSupplier.getPartitionKey()).isNull();
    }

    @Test
    public void testToPartitionSupplierReturnPartitionIdFromPartitionIdExpression() {
        setPropertiesForPartitionSupplier();

        Map<String, Integer> headers = new HashMap<>(1);
        headers.put("scst_partition", 3);

        this.handler.setPartitionIdExpression(partitionIdExpression);
        Message<?> message = new GenericMessage<>(payloadBytes, Collections.unmodifiableMap(headers));
        PartitionSupplier partitionSupplier = ReflectionTestUtils.invokeMethod(this.handler,
                                                                               DefaultMessageHandler.class,
                                                                               TO_PARTITION_SUPPLIER_METHOD_NAME,
                                                                               message);
        assertThat(partitionSupplier.getPartitionId()).isEqualTo("3");
        assertThat(partitionSupplier.getPartitionKey()).isNull();
    }

    @Test
    public void testToPartitionSupplierReturnPartitionKeyFromHeader() {
        setPropertiesForPartitionSupplier();

        Map<String, String> headers = new HashMap<>(1);
        headers.put(AzureHeaders.PARTITION_KEY, "key1");

        // set header azure_partition_key
        Message<?> message = new GenericMessage<>(payloadBytes, Collections.unmodifiableMap(headers));
        PartitionSupplier partitionSupplier = ReflectionTestUtils.invokeMethod(this.handler,
                                                                               DefaultMessageHandler.class,
                                                                               TO_PARTITION_SUPPLIER_METHOD_NAME,
                                                                               message);
        assertThat(partitionSupplier.getPartitionId()).isNull();
        assertThat(partitionSupplier.getPartitionKey()).isEqualTo("key1");
    }

    @Test
    public void testToPartitionSupplierReturnPartitionKeyFromHeaderPriority() {
        setPropertiesForPartitionSupplier();

        Map<String, String> headers = new HashMap<>(1);
        headers.put(AzureHeaders.PARTITION_KEY, "key2");

        // set header azure_partition_key and key expression
        Message<?> message = new GenericMessage<>(payloadBytes, Collections.unmodifiableMap(headers));
        this.handler.setPartitionKeyExpression(partitionKeyExpression);
        PartitionSupplier partitionSupplier = ReflectionTestUtils.invokeMethod(this.handler,
                                                                               DefaultMessageHandler.class,
                                                                               TO_PARTITION_SUPPLIER_METHOD_NAME,
                                                                               message);
        assertThat(partitionSupplier.getPartitionId()).isNull();
        assertThat(partitionSupplier.getPartitionKey()).isEqualTo("key2");
    }

    @Test
    public void testToPartitionSupplierReturnPartitionKeyFromPartitionKeyExpression() {
        setPropertiesForPartitionSupplier();

        this.handler.setPartitionKeyExpression(partitionKeyExpression);
        Message<?> message = new GenericMessage<>(payloadBytes);
        PartitionSupplier partitionSupplier = ReflectionTestUtils.invokeMethod(this.handler,
                                                                               DefaultMessageHandler.class,
                                                                               TO_PARTITION_SUPPLIER_METHOD_NAME,
                                                                               message);
        assertThat(partitionSupplier.getPartitionId()).isNull();
        assertThat(partitionSupplier.getPartitionKey()).isEqualTo(String.valueOf(payloadBytes.hashCode()));
    }

    /**
     * This is only for creating PartitionSupply setting
     */
    private void setPropertiesForPartitionSupplier() {
        partitionIdExpression = EXPRESSION_PARSER.parseExpression("headers['scst_partition']");
        partitionKeyExpression = new FunctionExpression<Message<?>>(m -> m.getPayload().hashCode());
        payloadBytes = payload.getBytes(StandardCharsets.UTF_8);

        this.handler.setBeanFactory(beanFactory);
        this.handler.afterPropertiesSet();
    }
}
