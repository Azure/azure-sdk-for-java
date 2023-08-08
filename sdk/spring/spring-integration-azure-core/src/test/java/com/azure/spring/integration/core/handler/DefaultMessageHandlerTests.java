// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.integration.core.handler;

import com.azure.spring.messaging.AzureHeaders;
import com.azure.spring.messaging.core.SendOperation;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.integration.MessageTimeoutException;
import org.springframework.integration.expression.FunctionExpression;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.util.concurrent.ListenableFutureCallback;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public abstract class DefaultMessageHandlerTests<O extends SendOperation> {

    protected String destination = "dest";
    protected String dynamicDestination = "dynamicName";
    protected DefaultMessageHandler handler;
    protected Mono<Void> mono = Mono.empty();
    protected O sendOperation;
    private Message<?> message;
    private String payload = "payload";

    private Expression partitionIdExpression;
    private Expression partitionKeyExpression;
    private byte[] payloadBytes;
    private static final ExpressionParser EXPRESSION_PARSER = new SpelExpressionParser();
    private static final String GET_PARTITION_FROM_EXPRESSION_METHOD_NAME = "getPartitionFromExpression";
    private static final String CREATE_MUTABLE_MESSAGE_METHOD_NAME = "createMutableMessage";

    @Mock
    private ConfigurableListableBeanFactory beanFactory;


    public DefaultMessageHandlerTests() {
        Map<String, Object> valueMap = new HashMap<>(2);
        valueMap.put("key1", "value1");
        valueMap.put("key2", "value2");
        message = new GenericMessage<>("testPayload", valueMap);
    }
    public abstract void setUp();

    @Test
    @SuppressWarnings("unchecked")
    public void testSend() {
        this.handler.handleMessage(this.message);
        verify(this.sendOperation, times(1)).sendAsync(eq(destination), isA(Message.class));
    }

    @Test
    public void testSendCallback() {
        ListenableFutureCallback<Void> callbackSpy = spy(new ListenableFutureCallback<Void>() {
            @Override
            public void onFailure(Throwable ex) {
            }

            @Override
            public void onSuccess(Void v) {
            }
        });

        this.handler.setSendCallback(callbackSpy);

        this.handler.handleMessage(this.message);

        verify(callbackSpy, times(1)).onSuccess(eq(null));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testSendDynamicTopic() {
        Map<String, Object> headers = new HashMap<>(1);
        headers.put(AzureHeaders.NAME, dynamicDestination);
        Message<?> dynamicMessage = new GenericMessage<>(payload, headers);
        this.handler.handleMessage(dynamicMessage);
        verify(this.sendOperation, times(1)).sendAsync(eq(dynamicDestination), isA(Message.class));
    }

    @Test
    public void testSendSync() {
        this.handler.setSync(true);
        Expression timeout = spy(this.handler.getSendTimeoutExpression());
        this.handler.setSendTimeoutExpression(timeout);

        this.handler.handleMessage(this.message);
        verify(timeout, times(1)).getValue(eq(null), eq(this.message), eq(Long.class));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testSendTimeout() {
        when(this.sendOperation.sendAsync(eq(this.destination), isA(Message.class))).thenReturn(Mono.empty().timeout(Mono.empty()));
        this.handler.setSync(true);
        this.handler.setSendTimeout(1);

        assertThrows(MessageTimeoutException.class, () -> this.handler.handleMessage(this.message));
    }

    @Test
    public void testGetPartitionFromExpressionReturnPartitionId() {
        setPropertiesForPartitionExpression();

        Map<String, Integer> headers = new HashMap<>(1);
        headers.put("scst_partition", 3);

        this.handler.setPartitionIdExpression(partitionIdExpression);
        Message<?> message = new GenericMessage<>(payloadBytes, Collections.unmodifiableMap(headers));
        Map<String, String> partitionHeaders = ReflectionTestUtils.invokeMethod(this.handler,
            DefaultMessageHandler.class,
            GET_PARTITION_FROM_EXPRESSION_METHOD_NAME,
            message);
        assertThat(partitionHeaders.get(AzureHeaders.PARTITION_ID)).isEqualTo("3");
        assertThat(partitionHeaders.get(AzureHeaders.PARTITION_KEY)).isNull();
    }

    @Test
    public void testGetPartitionFromExpressionReturnPartitionKey() {
        setPropertiesForPartitionExpression();

        this.handler.setPartitionKeyExpression(partitionKeyExpression);
        Message<?> message = new GenericMessage<>(payloadBytes);
        Map<String, String> partitionHeaders = ReflectionTestUtils.invokeMethod(this.handler,
            DefaultMessageHandler.class,
            GET_PARTITION_FROM_EXPRESSION_METHOD_NAME,
            message);
        assertThat(partitionHeaders.get(AzureHeaders.PARTITION_ID)).isNull();
        assertThat(partitionHeaders.get(AzureHeaders.PARTITION_KEY)).isEqualTo(String.valueOf(payloadBytes.hashCode()));
    }

    @Test
    public void testMutableMessageHasTheSameHeaders() {

        Message<?> message = new GenericMessage<>(payload.getBytes(StandardCharsets.UTF_8));

        Map<String, String> expressionGeneratedHeaders = new HashMap<>();
        Message<?> mutableMessage = ReflectionTestUtils.invokeMethod(this.handler,
            DefaultMessageHandler.class,
            CREATE_MUTABLE_MESSAGE_METHOD_NAME,
            message,
            expressionGeneratedHeaders);

        assertThat(mutableMessage.getHeaders()).isEqualTo(message.getHeaders());
    }

    @Test
    public void testGetPartitionFromHeader() {

        Map<String, String> rawMessageHeaders = new HashMap<>();
        rawMessageHeaders.put(AzureHeaders.PARTITION_ID, "1");
        rawMessageHeaders.put(AzureHeaders.PARTITION_KEY, "key1");
        Message<?> message = new GenericMessage<>(payload.getBytes(StandardCharsets.UTF_8), Collections.unmodifiableMap(rawMessageHeaders));

        Map<String, String> expressionGeneratedHeaders = new HashMap<>(2);
        expressionGeneratedHeaders.put(AzureHeaders.PARTITION_ID, "2");
        expressionGeneratedHeaders.put(AzureHeaders.PARTITION_KEY, "key2");

        Message<?> mutableMessage = ReflectionTestUtils.invokeMethod(this.handler,
            DefaultMessageHandler.class,
            CREATE_MUTABLE_MESSAGE_METHOD_NAME,
            message,
            expressionGeneratedHeaders);
        assertThat(mutableMessage.getHeaders().get(AzureHeaders.PARTITION_ID)).isEqualTo("1");
        assertThat(mutableMessage.getHeaders().get(AzureHeaders.PARTITION_KEY)).isEqualTo("key1");
    }

    @Test
    public void testGetPartitionFromExpression() {

        Message<?> message = new GenericMessage<>(payload.getBytes(StandardCharsets.UTF_8));
        Map<String, String> expressionGeneratedHeaders = new HashMap<>();
        expressionGeneratedHeaders.put(AzureHeaders.PARTITION_ID, "2");
        expressionGeneratedHeaders.put(AzureHeaders.PARTITION_KEY, "key2");
        Message<?> mutableMessage = ReflectionTestUtils.invokeMethod(this.handler,
            DefaultMessageHandler.class,
            CREATE_MUTABLE_MESSAGE_METHOD_NAME,
            message,
            expressionGeneratedHeaders);
        assertThat(mutableMessage.getHeaders().get(AzureHeaders.PARTITION_ID)).isEqualTo("2");
        assertThat(mutableMessage.getHeaders().get(AzureHeaders.PARTITION_KEY)).isEqualTo("key2");
    }

    @Test
    public void testGetPartition() {

        Message<?> message = new GenericMessage<>(payload.getBytes(StandardCharsets.UTF_8));
        Map<String, String> expressionGeneratedHeaders = new HashMap<>();
        Message<?> mutableMessage = ReflectionTestUtils.invokeMethod(this.handler,
            DefaultMessageHandler.class,
            CREATE_MUTABLE_MESSAGE_METHOD_NAME,
            message,
            expressionGeneratedHeaders);
        assertThat(mutableMessage.getHeaders().get(AzureHeaders.PARTITION_ID)).isNull();
        assertThat(mutableMessage.getHeaders().get(AzureHeaders.PARTITION_KEY)).isNull();
    }

    /**
     * This is only for creating PartitionSupply setting
     */
    private void setPropertiesForPartitionExpression() {
        partitionIdExpression = EXPRESSION_PARSER.parseExpression("headers['scst_partition']");
        partitionKeyExpression = new FunctionExpression<Message<?>>(m -> m.getPayload().hashCode());
        payloadBytes = payload.getBytes(StandardCharsets.UTF_8);

        this.handler.setBeanFactory(beanFactory);
        this.handler.afterPropertiesSet();
    }

    public Mono<Void> getMono() {
        return mono;
    }

    public void setMono(Mono<Void> mono) {
        this.mono = mono;
    }

    public DefaultMessageHandler getHandler() {
        return handler;
    }

    public void setHandler(DefaultMessageHandler handler) {
        this.handler = handler;
    }

    public O getSendOperation() {
        return sendOperation;
    }

    public void setSendOperation(O sendOperation) {
        this.sendOperation = sendOperation;
    }

}
