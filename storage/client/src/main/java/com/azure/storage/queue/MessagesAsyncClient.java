// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.storage.queue;

import com.azure.core.http.HttpPipeline;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.http.rest.VoidResponse;
import com.azure.core.util.Context;
import com.azure.storage.queue.implementation.AzureQueueStorageImpl;
import com.azure.storage.queue.models.DequeuedMessage;
import com.azure.storage.queue.models.EnqueuedMessage;
import com.azure.storage.queue.models.PeekedMessage;
import com.azure.storage.queue.models.QueueMessage;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URL;
import java.time.Duration;

public final class MessagesAsyncClient {
    private final AzureQueueStorageImpl client;

    MessagesAsyncClient(AzureQueueStorageImpl client) {
        this.client = new AzureQueueStorageImpl(client.httpPipeline())
            .withUrl(client.url() + "/messages")
            .withVersion(client.version());
    }

    MessagesAsyncClient(URL endpoint, HttpPipeline httpPipeline) {
        this.client = new AzureQueueStorageImpl(httpPipeline)
            .withUrl(endpoint.toString());
    }

    public static MessagesAsyncClientBuilder builder() {
        return new MessagesAsyncClientBuilder();
    }

    public String url() {
        return client.url();
    }

    public MessageIdAsyncClient getMessageIdAsyncRawClient(String messageId) {
        return new MessageIdAsyncClient(messageId, client);
    }

    public Mono<Response<EnqueuedMessage>> enqueue(String messageText) {
        return enqueue(messageText, null);
    }

    Mono<Response<EnqueuedMessage>> enqueue(String messageText, Duration timeout) {
        QueueMessage message = new QueueMessage().messageText(messageText);
        if (timeout == null) {
            return client.messages().enqueueWithRestResponseAsync(message, Context.NONE)
                .map(response -> new SimpleResponse<>(response.request(), response.statusCode(), response.headers(), response.value().get(0)));
        } else {
            return client.messages().enqueueWithRestResponseAsync(message, Context.NONE)
                .timeout(timeout)
                .map(response -> new SimpleResponse<>(response.request(), response.statusCode(), response.headers(), response.value().get(0)));
        }
    }

    public Flux<DequeuedMessage> dequeue(int numberOfMessages) {
        return dequeue(numberOfMessages, null);
    }

    Flux<DequeuedMessage> dequeue(int numberOfMessages, Duration timeout) {
        if (timeout == null) {
            return client.messages().dequeueWithRestResponseAsync(numberOfMessages, null, null, null, Context.NONE)
                .flatMapMany(response -> Flux.fromIterable(response.value()));
        } else {
            return client.messages().dequeueWithRestResponseAsync(numberOfMessages, null, null, null, Context.NONE)
                .timeout(timeout)
                .flatMapMany(response -> Flux.fromIterable(response.value()));
        }
    }

    public Flux<PeekedMessage> peek(int numberOfMessages) {
        return peek(numberOfMessages, null);
    }

    Flux<PeekedMessage> peek(int numberOfMessages, Duration timeout) {
        if (timeout == null) {
            return client.messages().peekWithRestResponseAsync(numberOfMessages, null, null, Context.NONE)
                .flatMapMany(response -> Flux.fromIterable(response.value()));
        } else {
            return client.messages().peekWithRestResponseAsync(numberOfMessages, null, null, Context.NONE)
                .timeout(timeout)
                .flatMapMany(response -> Flux.fromIterable(response.value()));
        }

    }

    public Mono<VoidResponse> clear() {
        return clear(null);
    }

    Mono<VoidResponse> clear(Duration timeout) {
        if (timeout == null) {
            return client.messages().clearWithRestResponseAsync(Context.NONE)
                .map(VoidResponse::new);
        } else {
            return client.messages().clearWithRestResponseAsync(Context.NONE)
                .timeout(timeout)
                .map(VoidResponse::new);
        }
    }
}
