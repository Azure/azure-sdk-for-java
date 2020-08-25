// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.spring.integration.storage.queue;

import com.google.common.collect.ImmutableMap;
import com.microsoft.azure.spring.integration.storage.queue.inbound.StorageQueueMessageSource;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.GenericMessage;
import reactor.core.publisher.Mono;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class StorageQueueMessageSourceTest {

    @Mock
    private StorageQueueOperation mockOperation;
    private Message<?> message =
        new GenericMessage<>("testPayload", ImmutableMap.of("key1", "value1", "key2", "value2"));

    private String destination = "test-destination";
    private StorageQueueMessageSource messageSource;

    @Before
    public void setup() {
        messageSource = new StorageQueueMessageSource(destination, mockOperation);
    }

    @Test
    public void testDoReceiveWhenHaveNoMessage() {
        when(this.mockOperation.receiveAsync(eq(destination))).thenReturn(Mono.empty());
        assertNull(messageSource.doReceive());
    }

    @Test(expected = StorageQueueRuntimeException.class)
    public void testReceiveFailure() {
        when(this.mockOperation.receiveAsync(eq(destination))).thenReturn(Mono.error(
            new StorageQueueRuntimeException("Failed to receive message.")));
        messageSource.doReceive();
    }

    @Test
    public void testDoReceiveSuccess() {
        when(this.mockOperation.receiveAsync(eq(destination))).thenReturn(Mono.just(message));
        Message<?> receivedMessage = (Message<?>) messageSource.doReceive();
        assertEquals(message, receivedMessage);
    }
}
