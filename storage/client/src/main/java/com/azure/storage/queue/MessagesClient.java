// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.storage.queue;

import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.VoidResponse;
import com.azure.storage.queue.models.DequeuedMessage;
import com.azure.storage.queue.models.EnqueuedMessage;
import com.azure.storage.queue.models.PeekedMessage;

import java.time.Duration;

public final class MessagesClient {
    private final MessagesAsyncClient client;

    MessagesClient(MessagesAsyncClient client) {
        this.client = client;
    }

    public static MessagesClientBuilder builder() {
        return new MessagesClientBuilder();
    }

    public String url() {
        return client.url();
    }

    public MessageIdClient getMessageIdRawClient(String messageId) {
        return new MessageIdClient(client.getMessageIdAsyncRawClient(messageId));
    }

    public Response<EnqueuedMessage> enqueue(String messageText, Duration timeout) {
        return client.enqueue(messageText, timeout).block();
    }

    public Iterable<DequeuedMessage> dequeue(int numberOfMessages, Duration timeout) {
        return client.dequeue(numberOfMessages, timeout).collectList().block();
    }

    public Iterable<PeekedMessage> peek(int numberOfMessages, Duration timeout) {
        return client.peek(numberOfMessages, timeout).collectList().block();
    }

    public VoidResponse clear(Duration timeout) {
        return client.clear(timeout).block();
    }
}
