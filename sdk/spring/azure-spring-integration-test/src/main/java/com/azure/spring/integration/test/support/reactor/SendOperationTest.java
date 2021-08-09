// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.integration.test.support.reactor;

import com.azure.spring.integration.core.api.reactor.SendOperation;
import org.junit.jupiter.api.Test;
import org.springframework.core.NestedRuntimeException;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.GenericMessage;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Fail.fail;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

public abstract class SendOperationTest<O extends SendOperation> {

    protected String consumerGroup = "consumer-group";
    protected String destination = "event-hub";
    protected Message<?> message;
    protected Mono<Void> mono = Mono.empty();
    protected String payload = "payload";
    protected O sendOperation;

    public SendOperationTest() {
        Map<String, Object> valueMap = new HashMap<>(2);
        valueMap.put("key1", "value1");
        valueMap.put("key2", "value2");
        message = new GenericMessage<>("testPayload", valueMap);
    }

    protected abstract void setupError(String errorMessage);


    @Test
    public void testSend() {
        final Mono<Void> mono = this.sendOperation.sendAsync(destination, message, null);

        assertNull(mono.block());
        verifySendCalled(1);
    }

    @Test
    public void testSendCreateSenderFailure() {
        whenSendWithException();

        assertThrows(NestedRuntimeException.class, () -> this.sendOperation.sendAsync(destination, this.message,
            null).block());
    }

    @Test
    public void testSendFailure() {
        String errorMessage = "Send failed.";
        setupError(errorMessage);
        Mono<Void> mono = this.sendOperation.sendAsync(destination, this.message, null);

        try {
            mono.block();
            fail("Test should fail.");
        } catch (Exception e) {
            assertEquals(errorMessage, e.getMessage());
        }
    }

    protected abstract void verifyGetClientCreator(int times);

    protected abstract void verifySendCalled(int times);

    protected abstract void whenSendWithException();

    public String getConsumerGroup() {
        return consumerGroup;
    }

    public void setConsumerGroup(String consumerGroup) {
        this.consumerGroup = consumerGroup;
    }

    public Mono<Void> getMono() {
        return mono;
    }

    public void setMono(Mono<Void> mono) {
        this.mono = mono;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public O getSendOperation() {
        return sendOperation;
    }

    public void setSendOperation(O sendOperation) {
        this.sendOperation = sendOperation;
    }
}
