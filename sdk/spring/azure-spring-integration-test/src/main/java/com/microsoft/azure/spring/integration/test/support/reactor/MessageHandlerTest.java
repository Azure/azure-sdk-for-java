// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.spring.integration.test.support.reactor;

import com.google.common.collect.ImmutableMap;
import com.microsoft.azure.spring.integration.core.AzureHeaders;
import com.microsoft.azure.spring.integration.core.api.PartitionSupplier;
import com.microsoft.azure.spring.integration.core.api.reactor.DefaultMessageHandler;
import com.microsoft.azure.spring.integration.core.api.reactor.SendOperation;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.expression.Expression;
import org.springframework.integration.MessageTimeoutException;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.util.concurrent.ListenableFutureCallback;
import reactor.core.publisher.Mono;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.spy;

@RunWith(MockitoJUnitRunner.class)
public abstract class MessageHandlerTest<O extends SendOperation> {

    protected O sendOperation;

    protected DefaultMessageHandler handler;
    protected String destination = "dest";
    protected String dynamicDestination = "dynamicName";
    protected Mono<Void> mono = Mono.empty();
    private Message<?> message = new GenericMessage<>("testPayload",
        ImmutableMap.of("key1", "value1", "key2", "value2"));
    private String payload = "payload";

    public abstract void setUp();

    public O getSendOperation() {
        return sendOperation;
    }

    public void setSendOperation(O sendOperation) {
        this.sendOperation = sendOperation;
    }

    public DefaultMessageHandler getHandler() {
        return handler;
    }

    public void setHandler(DefaultMessageHandler handler) {
        this.handler = handler;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public String getDynamicDestination() {
        return dynamicDestination;
    }

    public void setDynamicDestination(String dynamicDestination) {
        this.dynamicDestination = dynamicDestination;
    }

    public Mono<Void> getMono() {
        return mono;
    }

    public void setMono(Mono<Void> mono) {
        this.mono = mono;
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testSend() {
        this.handler.handleMessage(this.message);
        verify(this.sendOperation, times(1))
            .sendAsync(eq(destination), isA(Message.class), isA(PartitionSupplier.class));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testSendDynamicTopic() {
        Message<?> dynamicMessage =
            new GenericMessage<>(payload, ImmutableMap.of(AzureHeaders.NAME, dynamicDestination));
        this.handler.handleMessage(dynamicMessage);
        verify(this.sendOperation, times(1))
            .sendAsync(eq(dynamicDestination), isA(Message.class), isA(PartitionSupplier.class));
    }

    @Test
    public void testSendSync() {
        this.handler.setSync(true);
        Expression timeout = spy(this.handler.getSendTimeoutExpression());
        this.handler.setSendTimeoutExpression(timeout);

        this.handler.handleMessage(this.message);
        verify(timeout, times(1)).getValue(eq(null), eq(this.message), eq(Long.class));
    }

    @Test(expected = MessageTimeoutException.class)
    @SuppressWarnings("unchecked")
    public void testSendTimeout() {
        when(this.sendOperation.sendAsync(eq(this.destination), isA(Message.class), isA(PartitionSupplier.class)))
            .thenReturn(Mono.empty().timeout(Mono.empty()));
        this.handler.setSync(true);
        this.handler.setSendTimeout(1);

        this.handler.handleMessage(this.message);
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
}
