// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.messaging.storage.queue.core;

import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedResponse;
import com.azure.core.util.BinaryData;
import com.azure.core.util.IterableStream;
import com.azure.spring.messaging.AzureHeaders;
import com.azure.spring.messaging.checkpoint.Checkpointer;
import com.azure.spring.messaging.storage.queue.core.factory.StorageQueueClientFactory;
import com.azure.storage.queue.QueueAsyncClient;
import com.azure.storage.queue.models.QueueMessageItem;
import com.azure.storage.queue.models.QueueStorageException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.messaging.Message;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class StorageQueueTemplateReceiveTests {

    private final String messageId = "1";
    private final String messageText = "test message";
    private final String popReceipt = "popReceipt";
    @Mock
    private StorageQueueClientFactory mockClientFactory;
    @Mock
    private QueueAsyncClient mockClient;
    private StorageQueueTemplate template;
    private QueueMessageItem queueMessage;
    private String destination = "queue";
    private AutoCloseable closeable;

    @BeforeEach
    public void setup() {
        this.closeable = MockitoAnnotations.openMocks(this);
        queueMessage = new QueueMessageItem();
        queueMessage.setBody(BinaryData.fromString(messageText));
        queueMessage.setMessageId(messageId);
        queueMessage.setPopReceipt(popReceipt);

        final PagedResponse<QueueMessageItem> pagedResponse = new PagedResponse<QueueMessageItem>() {

            @Override
            public String getContinuationToken() {
                return null;
            }

            @Override
            public int getStatusCode() {
                return 200;
            }

            @Override
            public HttpHeaders getHeaders() {
                return null;
            }

            @Override
            public HttpRequest getRequest() {
                return null;
            }

            @Override
            public void close() {

            }

            @Override
            public IterableStream<QueueMessageItem> getElements() {
                Flux<QueueMessageItem> flux = Flux.just(queueMessage);
                return new IterableStream<QueueMessageItem>(flux);
            }
        };
        when(this.mockClientFactory.createQueueClient(eq(destination))).thenReturn(this.mockClient);
        when(this.mockClient.receiveMessages(eq(1), any()))
            .thenReturn(new PagedFlux<>(() -> Mono.just(pagedResponse)));
        this.template = new StorageQueueTemplate(this.mockClientFactory);
    }

    @AfterEach
    public void close() throws Exception {
        this.closeable.close();
    }

    @Test
    public void testReceiveFailure() {
        when(this.mockClient.receiveMessages(eq(1), any()))
            .thenReturn(new PagedFlux<>(() -> Mono.error(new QueueStorageException("error happened", null, null))));

        final Mono<Message<?>> mono = this.template.receiveAsync(this.destination, any());
        verifyQueueStorageExceptionThrown(mono);
    }

    @Test
    public void testReceiveSuccessWithManualMode() {
        when(mockClient.deleteMessage(this.messageId, this.popReceipt)).thenReturn(Mono.empty());
        final Mono<Message<?>> mono = this.template.receiveAsync(destination, any());

        Map<String, Object> headers = mono.block().getHeaders();
        Checkpointer checkpointer = (Checkpointer) headers.get(AzureHeaders.CHECKPOINTER);
        Mono<Void> checkpointFuture = checkpointer.success();
        checkpointFuture.block();

        verify(this.mockClient, times(1)).deleteMessage(messageId, popReceipt);
    }

    private void verifyQueueStorageExceptionThrown(Mono<Message<?>> mono) {
        try {
            mono.block();
            fail("Test should fail.");
        } catch (Exception e) {
            assertEquals(QueueStorageException.class, e.getClass());
        }
    }
}
