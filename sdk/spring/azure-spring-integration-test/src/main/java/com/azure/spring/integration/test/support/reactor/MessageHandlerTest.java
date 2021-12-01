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

/**
 *
 * @param <O> The type that extends SendOperation.
 */
public abstract class MessageHandlerTest<O extends SendOperation> {

    /**
     * The destination.
     */
    protected String destination = "dest";

    /**
     * The dynamic destination.
     */
    protected String dynamicDestination = "dynamicName";

    /**
     * The message handler.
     */
    protected DefaultMessageHandler handler;

    /**
     * The mono.
     */
    protected Mono<Void> mono = Mono.empty();

    /**
     * The send operation.
     */
    protected O sendOperation;

    private Message<?> message;
    private String payload = "payload";

    /**
     * Test message handler.
     */
    public MessageHandlerTest() {
        Map<String, Object> valueMap = new HashMap<>(2);
        valueMap.put("key1", "value1");
        valueMap.put("key2", "value2");
        message = new GenericMessage<>("testPayload", valueMap);
    }

    /**
     * Set up.
     */
    public abstract void setUp();

    /**
     * Test send.
     */
    @Test
    @SuppressWarnings("unchecked")
    public void testSend() {
        this.handler.handleMessage(this.message);
        verify(this.sendOperation, times(1)).sendAsync(eq(destination), isA(Message.class),
            isA(PartitionSupplier.class));
    }

    /**
     * Test send callback.
     */
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

    /**
     * Test send dynamic topic.
     */
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

    /**
     * Test send sync.
     */
    @Test
    public void testSendSync() {
        this.handler.setSync(true);
        Expression timeout = spy(this.handler.getSendTimeoutExpression());
        this.handler.setSendTimeoutExpression(timeout);

        this.handler.handleMessage(this.message);
        verify(timeout, times(1)).getValue(eq(null), eq(this.message), eq(Long.class));
    }

    /**
     * Test send timeout.
     */
    @Test
    @SuppressWarnings("unchecked")
    public void testSendTimeout() {
        when(this.sendOperation.sendAsync(eq(this.destination), isA(Message.class),
            isA(PartitionSupplier.class))).thenReturn(Mono.empty().timeout(Mono.empty()));
        this.handler.setSync(true);
        this.handler.setSendTimeout(1);

        assertThrows(MessageTimeoutException.class, () -> this.handler.handleMessage(this.message));
    }

    /**
     *
     * @return The mono.
     */
    public Mono<Void> getMono() {
        return mono;
    }

    /**
     *
     * @param mono The mono.
     */
    public void setMono(Mono<Void> mono) {
        this.mono = mono;
    }

    /**
     *
     * @return The handler.
     */
    public DefaultMessageHandler getHandler() {
        return handler;
    }

    /**
     *
     * @param handler The handler.
     */
    public void setHandler(DefaultMessageHandler handler) {
        this.handler = handler;
    }

    /**
     *
     * @return The sendOperation.
     */
    public O getSendOperation() {
        return sendOperation;
    }

    /**
     *
     * @param sendOperation The sendOperation.
     */
    public void setSendOperation(O sendOperation) {
        this.sendOperation = sendOperation;
    }

}
