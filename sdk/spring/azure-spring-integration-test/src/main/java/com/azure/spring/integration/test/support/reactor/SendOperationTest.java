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

/**
 *
 * @param <O> The type that extends SendOperation.
 */
public abstract class SendOperationTest<O extends SendOperation> {

    /**
     * The consumer group
     */
    protected String consumerGroup = "consumer-group";

    /**
     * The destination.
     */
    protected String destination = "event-hub";

    /**
     * The message.
     */
    protected Message<?> message;

    /**
     * The mono.
     */
    protected Mono<Void> mono = Mono.empty();

    /**
     * The payload.
     */
    protected String payload = "payload";

    /**
     * The sendOperation.
     */
    protected O sendOperation;

    /**
     * Test sendOperation.
     */
    public SendOperationTest() {
        Map<String, Object> valueMap = new HashMap<>(2);
        valueMap.put("key1", "value1");
        valueMap.put("key2", "value2");
        message = new GenericMessage<>("testPayload", valueMap);
    }

    /**
     *
     * @param errorMessage The error message.
     */
    protected abstract void setupError(String errorMessage);

    /**
     * Test send.
     */
    @Test
    public void testSend() {
        final Mono<Void> mono = this.sendOperation.sendAsync(destination, message, null);

        assertNull(mono.block());
        verifySendCalled(1);
    }

    /**
     * Test send create sender failure.
     */
    @Test
    public void testSendCreateSenderFailure() {
        whenSendWithException();

        assertThrows(NestedRuntimeException.class, () -> this.sendOperation.sendAsync(destination, this.message,
            null).block());
    }

    /**
     * Test send failure.
     */
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

    /**
     *
     * @param times The times.
     */
    protected abstract void verifyGetClientCreator(int times);

    /**
     *
     * @param times The times.
     */
    protected abstract void verifySendCalled(int times);

    /**
     * When send with exception.
     */
    protected abstract void whenSendWithException();

    /**
     *
     * @return The consumerGroup.
     */
    public String getConsumerGroup() {
        return consumerGroup;
    }

    /**
     *
     * @param consumerGroup The consumerGroup.
     */
    public void setConsumerGroup(String consumerGroup) {
        this.consumerGroup = consumerGroup;
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
     * @return The payload.
     */
    public String getPayload() {
        return payload;
    }

    /**
     *
     * @param payload The payload.
     */
    public void setPayload(String payload) {
        this.payload = payload;
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
