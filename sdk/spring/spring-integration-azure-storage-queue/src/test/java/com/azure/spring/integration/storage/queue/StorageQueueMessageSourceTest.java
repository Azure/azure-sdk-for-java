// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.integration.storage.queue;

import com.azure.spring.integration.storage.queue.inbound.StorageQueueMessageSource;
import com.azure.spring.storage.queue.core.StorageQueueOperation;
import com.azure.spring.storage.queue.core.StorageQueueRuntimeException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.GenericMessage;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class StorageQueueMessageSourceTest {

    @Mock
    private StorageQueueOperation mockOperation;
    private Message<?> message;

    private String destination = "test-destination";
    private StorageQueueMessageSource messageSource;
    private AutoCloseable closeable;

    public StorageQueueMessageSourceTest() {
        Map<String, Object> values = new HashMap<>(2);
        values.put("key1", "value1");
        values.put("key2", "value2");
        message = new GenericMessage<>("testPayload", values);
    }

    @BeforeAll
    public void init() {
        this.closeable = MockitoAnnotations.openMocks(this);
    }

    @BeforeEach
    public void setup() {
        messageSource = new StorageQueueMessageSource(destination, mockOperation);
    }

    @AfterAll
    public void close() throws Exception {
        this.closeable.close();
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
