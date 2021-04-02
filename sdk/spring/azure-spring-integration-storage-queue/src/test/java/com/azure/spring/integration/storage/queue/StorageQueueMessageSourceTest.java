// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.integration.storage.queue;

import com.azure.spring.integration.storage.queue.inbound.StorageQueueMessageSource;
import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.GenericMessage;
import reactor.core.publisher.Mono;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class StorageQueueMessageSourceTest {

    @Mock
    private StorageQueueOperation mockOperation;
    private Message<?> message =
        new GenericMessage<>("testPayload", ImmutableMap.of("key1", "value1", "key2", "value2"));

    private String destination = "test-destination";
    private StorageQueueMessageSource messageSource;

    @BeforeEach
    public void setup() {
        messageSource = new StorageQueueMessageSource(destination, mockOperation);
    }

    @Test
    public void testDoReceiveWhenHaveNoMessage() {
        when(this.mockOperation.receiveAsync(eq(destination))).thenReturn(Mono.empty());
        assertNull(messageSource.doReceive());
    }

    @Test
    public void testReceiveFailure() {
        when(this.mockOperation.receiveAsync(eq(destination))).thenReturn(Mono.error(
            new StorageQueueRuntimeException("Failed to receive message.")));
        assertThrows(StorageQueueRuntimeException.class, () -> messageSource.doReceive());
    }

    @Test
    public void testDoReceiveSuccess() {
        when(this.mockOperation.receiveAsync(eq(destination))).thenReturn(Mono.just(message));
        Message<?> receivedMessage = (Message<?>) messageSource.doReceive();
        assertEquals(message, receivedMessage);
    }
}
