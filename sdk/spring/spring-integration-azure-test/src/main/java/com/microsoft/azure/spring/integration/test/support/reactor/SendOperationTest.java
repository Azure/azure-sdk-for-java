/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.integration.test.support.reactor;

import com.google.common.collect.ImmutableMap;
import com.microsoft.azure.spring.integration.core.api.reactor.SendOperation;
import org.junit.Test;
import org.springframework.core.NestedRuntimeException;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.GenericMessage;
import reactor.core.publisher.Mono;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

public abstract class SendOperationTest<O extends SendOperation> {

    protected O sendOperation;
    protected String payload = "payload";
    protected Mono<Void> mono = Mono.empty();
    protected String destination = "event-hub";
    protected String consumerGroup = "consumer-group";
    protected Message<?> message =
            new GenericMessage<>("testPayload", ImmutableMap.of("key1", "value1", "key2", "value2"));

    @Test
    public void testSend() {
        final Mono<Void> mono = this.sendOperation.sendAsync(destination, message, null);

        assertNull(mono.block());
        verifySendCalled(1);
    }

    @Test(expected = NestedRuntimeException.class)
    public void testSendCreateSenderFailure() {
        whenSendWithException();

        this.sendOperation.sendAsync(destination, this.message, null).block();
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

    protected abstract void setupError(String errorMessage);

    protected abstract void verifySendCalled(int times);

    protected abstract void whenSendWithException();

    protected abstract void verifyGetClientCreator(int times);

}
