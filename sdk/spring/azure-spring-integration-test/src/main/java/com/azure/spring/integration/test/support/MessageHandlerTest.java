// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.integration.test.support;

import com.azure.spring.integration.core.AzureHeaders;
import com.azure.spring.integration.core.DefaultMessageHandler;
import com.azure.spring.integration.core.api.PartitionSupplier;
import com.azure.spring.integration.core.api.SendOperation;
import org.junit.jupiter.api.Test;
import org.springframework.expression.Expression;
import org.springframework.integration.MessageTimeoutException;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.util.concurrent.ListenableFutureCallback;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.spy;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 *
 * @param <O> The type that extends SendOperation.
 */
public abstract class MessageHandlerTest<O extends SendOperation> {

    /**
     * Send Operation
     */
    @SuppressWarnings("unchecked")
    protected O sendOperation = null;

    /**
     * Default message handler.
     */
    protected DefaultMessageHandler handler = null;

    /**
     * Destination.
     */
    protected String destination = "dest";

    /**
     * Dynamic destination.
     */
    protected String dynamicDestination = "dynamicName";

    /**
     * Future.
     */
    @SuppressWarnings("unchecked")
    protected CompletableFuture<Void> future = new CompletableFuture<>();
    private Message<?> message;
    private String payload = "payload";

    /**
     * Set up.
     */
    public abstract void setUp();

    /**
     *
     * @return The sendOperation.
     */
    protected O getSendOperation() {
        return sendOperation;
    }

    /**
     *
     * @param sendOperation The sendOperation.
     */
    protected void setSendOperation(O sendOperation) {
        this.sendOperation = sendOperation;
    }

    /**
     *
     * @return The handler。
     */
    protected DefaultMessageHandler getHandler() {
        return handler;
    }

    /**
     *
     * @param handler The handler.
     */
    protected void setHandler(DefaultMessageHandler handler) {
        this.handler = handler;
    }

    /**
     *
     * @return The destination.
     */
    protected String getDestination() {
        return destination;
    }

    /**
     *
     * @param destination The destination.
     */
    protected void setDestination(String destination) {
        this.destination = destination;
    }

    /**
     *
     * @return The dynamicDestination.
     */
    protected String getDynamicDestination() {
        return dynamicDestination;
    }

    /**
     *
     * @param dynamicDestination The dynamicDestination.
     */
    protected void setDynamicDestination(String dynamicDestination) {
        this.dynamicDestination = dynamicDestination;
    }

    /**
     *
     * @return The future.
     */
    protected CompletableFuture<Void> getFuture() {
        return future;
    }

    /**
     *
     * @param future The future.
     */
    protected void setFuture(CompletableFuture<Void> future) {
        this.future = future;
    }

    /**
     * Test message handler.
     */
    public MessageHandlerTest() {
        Map<String, Object> valueMap = new HashMap<>(2);
        valueMap.put("key1", "value1");
        valueMap.put("key2", "value2");
        message = new GenericMessage<>("testPayload", valueMap);    }

    /**
     * Test send.
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testSend() {
        this.handler.handleMessage(this.message);
        verify(this.sendOperation, times(1))
            .sendAsync(eq(destination), isA(Message.class), isA(PartitionSupplier.class));
    }

    /**
     * Test send dynamic topic.
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testSendDynamicTopic() {
        Map<String, Object> headers = new HashMap<>(1);
        headers.put(AzureHeaders.NAME, dynamicDestination);
        Message<?> dynamicMessage = new GenericMessage<>(payload, headers);
        this.handler.handleMessage(dynamicMessage);
        verify(this.sendOperation, times(1))
            .sendAsync(eq(dynamicDestination), isA(Message.class), isA(PartitionSupplier.class));
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
    @SuppressWarnings("unchecked")
    @Test
    public void testSendTimeout() {
        when(this.sendOperation.sendAsync(eq(this.destination), isA(Message.class), isA(PartitionSupplier.class)))
            .thenReturn(new CompletableFuture<>());
        this.handler.setSync(true);
        this.handler.setSendTimeout(1);
        assertThrows(MessageTimeoutException.class, () -> this.handler.handleMessage(this.message));
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
}
