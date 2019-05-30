package com.azure.storage.queue;

import com.azure.core.http.HttpPipeline;
import com.azure.core.http.rest.VoidResponse;
import com.azure.core.util.Context;
import com.azure.storage.queue.implementation.AzureQueueStorageImpl;
import com.azure.storage.queue.models.DequeuedMessageItem;
import com.azure.storage.queue.models.EnqueuedMessage;
import com.azure.storage.queue.models.PeekedMessageItem;
import com.azure.storage.queue.models.QueueMessage;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URL;

final class MessagesAsyncRawClient {
    private final AzureQueueStorageImpl generateClient;

    MessagesAsyncRawClient(AzureQueueStorageImpl generateClient) {
        this.generateClient = generateClient;
    }

    MessagesAsyncRawClient(URL endpoint, HttpPipeline httpPipeline) {
        this.generateClient = new AzureQueueStorageImpl(httpPipeline).withUrl(endpoint.toString());
    }

    public MessageIdAsyncRawClient getMessageIdClient(String messageId) {
        return null;
    }

    public Flux<EnqueuedMessage> enqueue(QueueMessage queueMessage, Context context) {
        return enqueue(queueMessage, null, context);
    }

    Flux<EnqueuedMessage> enqueue(QueueMessage queueMessage, Integer timeout, Context context) {
        return generateClient.messages().enqueueWithRestResponseAsync(queueMessage, null, null, timeout, null, context)
            .flatMapMany(response -> Flux.fromIterable(response.value()));
    }

    public Flux<DequeuedMessageItem> dequeue(int numberOfMessages, Context context) {
        return dequeue(numberOfMessages, null, context);
    }

    Flux<DequeuedMessageItem> dequeue(int numberOfMessages, Integer timeout, Context context) {
        return generateClient.messages().dequeueWithRestResponseAsync(numberOfMessages, null, timeout, null, context)
            .flatMapMany(response -> Flux.fromIterable(response.value()));
    }

    public Flux<PeekedMessageItem> peek(int numberOfMessages, Context context) {
        return peek(numberOfMessages, null, context);
    }

    Flux<PeekedMessageItem> peek(int numberOfMessages, Integer timeout, Context context) {
        return generateClient.messages().peekWithRestResponseAsync(numberOfMessages, timeout, null, context)
            .flatMapMany(response -> Flux.fromIterable(response.value()));
    }

    public Mono<VoidResponse> clear(Context context) {
        return clear(null, context);
    }

    Mono<VoidResponse> clear(Integer timeout, Context context) {
        return generateClient.messages().clearWithRestResponseAsync(timeout, null, context)
            .map(VoidResponse::new);
    }
}
