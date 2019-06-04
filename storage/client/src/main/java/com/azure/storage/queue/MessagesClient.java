// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.storage.queue;

import com.azure.core.util.Context;
import com.azure.storage.queue.models.DequeuedMessageItem;
import com.azure.storage.queue.models.EnqueuedMessage;
import com.azure.storage.queue.models.PeekedMessageItem;
import com.azure.storage.queue.models.QueueMessage;

import java.util.List;

public final class MessagesClient {
    private final MessagesRawClient client;

    MessagesClient(MessagesRawClient client) {
        this.client = client;
    }

    public static MessagesClientBuilder builder() {
        return new MessagesClientBuilder();
    }

    public MessagesRawClient getRawClient() {
        return client;
    }

    public MessageIdClient getMassageIdClient(String messageId) {
        return new MessageIdClient(client.getMessageIdRawClient(messageId));
    }

    public EnqueuedMessage enqueue(QueueMessage queueMessage) {
        return client.enqueue(queueMessage, null, Context.NONE).value();
    }

    public List<DequeuedMessageItem> dequeue(int numberOfMessages) {
        return client.dequeue(numberOfMessages, null, Context.NONE);
    }

    public List<PeekedMessageItem> peek(int numberOfMessages) {
        return client.peek(numberOfMessages, null, Context.NONE);
    }

    public void clear() {
        client.clear(null, Context.NONE);
    }
}
