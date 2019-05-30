package com.azure.storage.queue;

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

    public Flux<EnqueuedMessage> enqueue(QueueMessage queueMessage) {
        return client.enqueue(queueMessage, null, Context.NONE);
    }

    public Flux<DequeuedMessageItem> dequeue(int numberOfMessages) {
        return client.dequeue(numberOfMessages, null, Context.NONE);
    }

    public Flux<PeekedMessageItem> peek(int numberOfMessages) {
        return client.peek(numberOfMessages, Context.NONE);
    }

    public Mono<Void> clear() {
        return client.clear(null, Context.NONE)
            .flatMap(response -> Mono.empty());
    }
}
