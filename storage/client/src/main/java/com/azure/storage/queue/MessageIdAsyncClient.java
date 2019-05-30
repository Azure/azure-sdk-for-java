package com.azure.storage.queue;

import com.azure.core.util.Context;
import com.azure.storage.queue.models.QueueMessage;
import com.azure.storage.queue.models.UpdatedMessage;
import reactor.core.publisher.Mono;

public final class MessageIdAsyncClient {
    private final MessageIdAsyncRawClient client;

    MessageIdAsyncClient(MessageIdAsyncRawClient client) {
        this.client = client;
    }

    public static MessageIdAsyncClientBuilder builder() {
        return new MessageIdAsyncClientBuilder();
    }

    public MessageIdAsyncRawClient getRawClient() {
        return client;
    }

    public Mono<UpdatedMessage> update(QueueMessage queueMessage, String popReceipt, int visibilityTimeout) {
        return client.update(queueMessage, popReceipt, visibilityTimeout, Context.NONE)
            .map(response -> response.value());
    }

    public Mono<Void> delete(String popReceipt) {
        return client.delete(popReceipt, Context.NONE)
            .flatMap(response -> Mono.empty());
    }
}
