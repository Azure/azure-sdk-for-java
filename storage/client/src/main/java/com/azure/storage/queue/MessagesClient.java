// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.storage.queue;

import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.VoidResponse;
import com.azure.core.util.Context;
import com.azure.storage.queue.models.DequeuedMessageItem;
import com.azure.storage.queue.models.EnqueuedMessage;
import com.azure.storage.queue.models.PeekedMessageItem;

import java.time.Duration;
import java.util.List;

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

    public Response<EnqueuedMessage> enqueue(String messageText, Duration timeout, Context context) {
        return client.enqueue(messageText, timeout, context).block();
    }

    public List<DequeuedMessageItem> dequeue(int numberOfMessages, Duration timeout, Context context) {
        return client.dequeue(numberOfMessages, timeout, context).collectList().block();
    }

    public List<PeekedMessageItem> peek(int numberOfMessages, Duration timeout, Context context) {
        return client.peek(numberOfMessages, timeout, context).collectList().block();
    }

    public VoidResponse clear(Duration timeout, Context context) {
        return client.clear(timeout, context).block();
    }
}
