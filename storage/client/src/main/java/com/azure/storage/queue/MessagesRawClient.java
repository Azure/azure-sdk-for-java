// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.storage.queue;

import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.VoidResponse;
import com.azure.core.util.Context;
import com.azure.storage.queue.models.DequeuedMessageItem;
import com.azure.storage.queue.models.EnqueuedMessage;
import com.azure.storage.queue.models.PeekedMessageItem;
import com.azure.storage.queue.models.QueueMessage;

import java.time.Duration;
import java.util.List;

public final class MessagesRawClient {
    private final MessagesAsyncRawClient client;

    MessagesRawClient(MessagesAsyncRawClient client) {
        this.client = client;
    }

    public MessageIdRawClient getMessageIdRawClient(String messageId) {
        return new MessageIdRawClient(client.getMessageIdAsyncRawClient(messageId));
    }

    public Response<EnqueuedMessage> enqueue(QueueMessage queueMessage, Duration timeout, Context context) {
        return client.enqueue(queueMessage, timeout, context).block();
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
