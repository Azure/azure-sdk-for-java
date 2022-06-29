// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.integration.storage.queue.inbound;

import com.azure.spring.messaging.storage.queue.core.StorageQueueTemplate;
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

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class StorageQueueMessageSourceTests {

    @Mock
    private StorageQueueTemplate mockTemplate;
    private Message<?> message;

    private String destination = "test-destination";
    private Duration visibilityTimeout = Duration.ofMinutes(1);
    private StorageQueueMessageSource messageSource;
    private AutoCloseable closeable;

    public StorageQueueMessageSourceTests() {
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
        messageSource = new StorageQueueMessageSource(destination, mockTemplate, visibilityTimeout);
    }

    @AfterAll
    public void close() throws Exception {
        this.closeable.close();
    }

    @Test
    public void testDoReceiveWhenHaveNoMessage() {
        when(this.mockTemplate.receiveAsync(eq(destination), eq(visibilityTimeout))).thenReturn(Mono.empty());
        assertNull(messageSource.doReceive());
    }

    @Test
    public void testDoReceiveSuccess() {
        when(this.mockTemplate.receiveAsync(eq(destination), eq(visibilityTimeout))).thenReturn(Mono.just(message));
        Message<?> receivedMessage = (Message<?>) messageSource.doReceive();
        assertEquals(message, receivedMessage);
    }

    @Test
    public void testDoReceiveSuccessWithDefaultVisibilityTimeout() {
        final StorageQueueMessageSource messageSourceWithDefaultTimeout =
            new StorageQueueMessageSource(destination, mockTemplate);
        when(this.mockTemplate.receiveAsync(eq(destination), isNull())).thenReturn(Mono.just(message));
        Message<?> receivedMessage = (Message<?>) messageSourceWithDefaultTimeout.doReceive();
        assertEquals(message, receivedMessage);
    }
}
