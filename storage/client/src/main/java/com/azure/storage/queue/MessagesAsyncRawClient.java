// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.storage.queue;

import com.azure.core.http.HttpPipeline;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
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
import java.time.Duration;

final class MessagesAsyncRawClient {
    private final AzureQueueStorageImpl client;

    MessagesAsyncRawClient(AzureQueueStorageImpl generateClient) {
        this.client = generateClient;
    }

    MessagesAsyncRawClient(URL endpoint, HttpPipeline httpPipeline) {
        this.client = new AzureQueueStorageImpl(httpPipeline).withUrl(endpoint.toString());
    }

    public MessageIdAsyncRawClient getMessageIdAsyncRawClient(String messageId) {
        return new MessageIdAsyncRawClient(messageId, client);
    }

    public Mono<Response<EnqueuedMessage>> enqueue(QueueMessage queueMessage, Context context) {
        return enqueue(queueMessage, null, context);
    }

    Mono<Response<EnqueuedMessage>> enqueue(QueueMessage queueMessage, Duration timeout, Context context) {
        if (timeout == null) {
            return client.messages().enqueueWithRestResponseAsync(queueMessage, context)
                .map(response -> new SimpleResponse<>(response.request(), response.statusCode(), response.headers(), response.value().get(0)));
        } else {
            return client.messages().enqueueWithRestResponseAsync(queueMessage, context)
                .timeout(timeout)
                .map(response -> new SimpleResponse<>(response.request(), response.statusCode(), response.headers(), response.value().get(0)));
        }
    }

    public Flux<DequeuedMessageItem> dequeue(int numberOfMessages, Context context) {
        return dequeue(numberOfMessages, null, context);
    }

    Flux<DequeuedMessageItem> dequeue(int numberOfMessages, Duration timeout, Context context) {
        if (timeout == null) {
            return client.messages().dequeueWithRestResponseAsync(numberOfMessages, null, null, null, context)
                .flatMapMany(response -> Flux.fromIterable(response.value()));
        } else {
            return client.messages().dequeueWithRestResponseAsync(numberOfMessages, null, null, null, context)
                .timeout(timeout)
                .flatMapMany(response -> Flux.fromIterable(response.value()));
        }
    }

    public Flux<PeekedMessageItem> peek(int numberOfMessages, Context context) {
        return peek(numberOfMessages, null, context);
    }

    Flux<PeekedMessageItem> peek(int numberOfMessages, Duration timeout, Context context) {
        if (timeout == null) {
            return client.messages().peekWithRestResponseAsync(numberOfMessages, null, null, context)
                .flatMapMany(response -> Flux.fromIterable(response.value()));
        } else {
            return client.messages().peekWithRestResponseAsync(numberOfMessages, null, null, context)
                .timeout(timeout)
                .flatMapMany(response -> Flux.fromIterable(response.value()));
        }

    }

    public Mono<VoidResponse> clear(Context context) {
        return clear(null, context);
    }

    Mono<VoidResponse> clear(Duration timeout, Context context) {
        if (timeout == null) {
            return client.messages().clearWithRestResponseAsync(context)
                .map(VoidResponse::new);
        } else {
            return client.messages().clearWithRestResponseAsync(context)
                .timeout(timeout)
                .map(VoidResponse::new);
        }
    }
}
