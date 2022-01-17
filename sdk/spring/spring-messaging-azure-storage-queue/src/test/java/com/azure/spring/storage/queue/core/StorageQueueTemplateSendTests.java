// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.storage.queue.core;

import com.azure.spring.storage.queue.core.factory.StorageQueueClientFactory;
import com.azure.storage.queue.QueueAsyncClient;
import com.azure.storage.queue.models.SendMessageResult;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import reactor.core.publisher.Mono;

import static org.assertj.core.api.Fail.fail;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class StorageQueueTemplateSendTests {

    @Mock
    private StorageQueueClientFactory mockClientFactory;

    @Mock
    private QueueAsyncClient mockClient;

    private AutoCloseable closeable;
    protected String destination = "storage-queue";
    protected Message<?> message = MessageBuilder.withPayload("test").build();
    protected StorageQueueTemplate storageQueueTemplate;

    @BeforeEach
    public void init() {
        this.closeable = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    public void close() throws Exception {
        this.closeable.close();
    }

    @BeforeEach
    public void setup() {
        when(this.mockClientFactory.createQueueClient(eq(destination))).thenReturn(mockClient);
        when(this.mockClient.sendMessage(anyString())).thenReturn(Mono.just(new SendMessageResult()));

        this.storageQueueTemplate = new StorageQueueTemplate(mockClientFactory);
    }

    @Test
    public void testSend() {
        final Mono<Void> mono = this.storageQueueTemplate.sendAsync(destination, message);

        assertNull(mono.block());
        verifySendCalled(1);
    }

    @Test
    public void testSendFailure() {
        String errorMessage = "Send failed.";
        setupError(errorMessage);
        Mono<Void> mono = this.storageQueueTemplate.sendAsync(destination, this.message);

        try {
            mono.block();
            fail("Test should fail.");
        } catch (Exception e) {
            assertEquals(errorMessage, e.getMessage());
        }
    }

    protected void setupError(String errorMessage) {
        when(this.mockClient.sendMessage(any(String.class)))
            .thenReturn(Mono.error(new IllegalArgumentException(errorMessage)));
    }

    protected void verifySendCalled(int times) {
        verify(this.mockClient, times(times)).sendMessage(isA(String.class));
    }

    protected void verifyGetClientCreator(int times) {
        verify(this.mockClientFactory, times(times)).createQueueClient(this.destination);
    }

}
