// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.storage.queue;

import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.storage.queue.models.DequeuedMessageItem;
import com.azure.storage.queue.models.EnqueuedMessage;
import com.azure.storage.queue.models.PeekedMessageItem;
import com.azure.storage.queue.models.QueueMessage;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public final class MessagesAsyncClient {
    private final MessagesAsyncRawClient client;

    MessagesAsyncClient(MessagesAsyncRawClient client) {
        this.client = client;
    }

    public static MessagesAsyncClientBuilder builder() {
        return new MessagesAsyncClientBuilder();
    }

    public MessagesAsyncRawClient getRawClient() {
        return client;
    }

    public MessageIdAsyncClient getMessageIdAsyncClient(String messageId) {
        return new MessageIdAsyncClient(client.getMessageIdAsyncRawClient(messageId));
    }

    public Mono<EnqueuedMessage> enqueue(QueueMessage queueMessage) {
        return client.enqueue(queueMessage, null, Context.NONE)
            .map(Response::value);
    }

    public Flux<DequeuedMessageItem> dequeue(int numberOfMessages) {
        return client.dequeue(numberOfMessages, null, Context.NONE);
    }

    public Flux<PeekedMessageItem> peek(int numberOfMessages) {
        return client.peek(numberOfMessages, null, Context.NONE);
    }

    public Mono<Void> clear() {
        return client.clear(null, Context.NONE)
            .flatMap(response -> Mono.empty());
    }
}
