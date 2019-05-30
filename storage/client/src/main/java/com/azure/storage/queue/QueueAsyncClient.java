// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.storage.queue;

import com.azure.core.util.Context;
import com.azure.storage.queue.models.QueueProperties;
import com.azure.storage.queue.models.SignedIdentifier;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

public final class QueueAsyncClient {
    private final QueueAsyncRawClient client;

    QueueAsyncClient(QueueAsyncRawClient client) {
        this.client = client;
    }

    /**
     * Creates a builder that can configure options for the SecretAsyncClient before creating an instance of it.
     * @return A new builder to create a SecretAsyncClient from.
     */
    public static QueueAsyncClientBuilder builder() {
        return new QueueAsyncClientBuilder();
    }

    public QueueAsyncRawClient getRawClient() {
        return client;
    }

    public MessagesAsyncClient getMessagesClient() {
        return new MessagesAsyncClient(client.getMessagesClient());
    }

    public Mono<Void> create() {
        return client.create(null, Context.NONE)
            .flatMap(response -> Mono.empty());
    }

    public Mono<Void> delete() {
        return client.delete(null, Context.NONE)
            .flatMap(response -> Mono.empty());
    }

    public Mono<QueueProperties> getProperties() {
        return client.getProperties(null, Context.NONE)
            .flatMap(response -> Mono.just(response.value()));
    }

    public Mono<Void> setMetadata(Map<String, String> metadata) {
        return client.setMetadata(metadata, null, Context.NONE)
            .flatMap(response -> Mono.empty());
    }

    public Flux<SignedIdentifier> getAccessPolicy() {
        return client.getAccessPolicy(null, Context.NONE);
    }

    public Mono<Void> setAccessPolicy(List<SignedIdentifier> permissions) {
        return client.setAccessPolicy(permissions, null, Context.NONE)
            .flatMap(response -> Mono.empty());
    }
}
