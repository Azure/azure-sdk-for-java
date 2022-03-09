// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.integration.test.support.reactor;

import com.azure.spring.integration.core.AzureHeaders;
import com.azure.spring.integration.core.api.PartitionSupplier;
import com.azure.spring.integration.core.api.reactor.DefaultMessageHandler;
import com.azure.spring.integration.core.api.reactor.SendOperation;
import org.junit.jupiter.api.Test;
import org.springframework.expression.Expression;
import org.springframework.integration.MessageTimeoutException;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.util.concurrent.ListenableFutureCallback;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.assertThrows;

public abstract class MessageHandlerTest<O extends SendOperation> {

    protected String destination = "dest";
    protected String dynamicDestination = "dynamicName";
    protected DefaultMessageHandler handler;
    protected Mono<Void> mono = Mono.empty();
    protected O sendOperation;
    private Message<?> message;
    private String payload = "payload";


    public MessageHandlerTest() {
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
        verify(this.sendOperation, times(1)).sendAsync(eq(destination), isA(Message.class),
            isA(PartitionSupplier.class));
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
        verify(this.sendOperation, times(1)).sendAsync(eq(dynamicDestination), isA(Message.class),
            isA(PartitionSupplier.class));
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
        when(this.sendOperation.sendAsync(eq(this.destination), isA(Message.class),
            isA(PartitionSupplier.class))).thenReturn(Mono.empty().timeout(Mono.empty()));
        this.handler.setSync(true);
        this.handler.setSendTimeout(1);

        assertThrows(MessageTimeoutException.class, () -> this.handler.handleMessage(this.message));
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
