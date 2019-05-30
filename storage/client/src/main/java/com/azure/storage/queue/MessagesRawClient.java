package com.azure.storage.queue;

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
        return null;
    }

    public List<EnqueuedMessage> enqueue(QueueMessage queueMessage, Duration timeout, Context context) {
        return client.enqueue(queueMessage, (int) timeout.getSeconds(), context).collectList().block();
    }

    public List<DequeuedMessageItem> dequeue(int numberOfMessages, Duration timeout, Context context) {
        return client.dequeue(numberOfMessages, (int) timeout.getSeconds(), context).collectList().block();
    }

    public List<PeekedMessageItem> peek(int numberOfMessages, Duration timeout, Context context) {
        return client.peek(numberOfMessages, (int) timeout.getSeconds(), context).collectList().block();
    }

    public VoidResponse clear(Duration timeout, Context context) {
        return client.clear((int) timeout.getSeconds(), context).block();
    }
}
