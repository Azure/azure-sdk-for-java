package com.azure.storage.queue;

import com.azure.core.http.HttpPipeline;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.http.rest.VoidResponse;
import com.azure.core.util.Context;
import com.azure.storage.queue.implementation.AzureQueueStorageImpl;
import com.azure.storage.queue.models.QueueProperties;
import com.azure.storage.queue.models.SignedIdentifier;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URL;
import java.util.List;
import java.util.Map;

final class QueueAsyncRawClient {
    private final AzureQueueStorageImpl client;

    QueueAsyncRawClient(URL endpoint, HttpPipeline httpPipeline) {
        this.client = new AzureQueueStorageImpl(httpPipeline).withUrl(endpoint.toString());
    }

    public MessagesAsyncRawClient getMessagesClient() {
        return new MessagesAsyncRawClient(client);
    }

    public Mono<VoidResponse> create(Context context) {
        return create(null, context);
    }

    Mono<VoidResponse> create(Integer timeout, Context context) {
        return client.queues().createWithRestResponseAsync(timeout, null, null, context)
            .map(response -> new VoidResponse(response.request(), response.statusCode(), response.headers()));
    }

    public Mono<VoidResponse> delete(Context context) {
        return delete(null, context);
    }

    Mono<VoidResponse> delete(Integer timeout, Context context) {
        return client.queues().deleteWithRestResponseAsync(timeout, null, context)
            .map(response -> new VoidResponse(response.request(), response.statusCode(), response.headers()));
    }

    public Mono<Response<QueueProperties>> getProperties(Context context) {
        return getProperties(null, context);
    }

    Mono<Response<QueueProperties>> getProperties(Integer timeout, Context context) {
        // QueueProperties is the box of things.
        return client.queues().getPropertiesWithRestResponseAsync(timeout, null, context)
            .map(response -> {
                QueueProperties properties = new QueueProperties(response.deserializedHeaders().metadata(), response.deserializedHeaders().approximateMessagesCount());
                return new SimpleResponse<>(response.request(), response.statusCode(), response.headers(), properties);
            });
    }

    public Mono<VoidResponse> setMetadata(Map<String, String> metadata, Context context) {
        return setMetadata(metadata, null, context);
    }

    Mono<VoidResponse> setMetadata(Map<String, String> metadata, Integer timeout, Context context) {
        return client.queues().setMetadataWithRestResponseAsync(timeout, metadata, null, context)
            .map(response -> new VoidResponse(response.request(), response.statusCode(), response.headers()));
    }

    public Flux<SignedIdentifier> getAccessPolicy(Context context) {
        return getAccessPolicy(null, context);
    }

    Flux<SignedIdentifier> getAccessPolicy(Integer timeout, Context context) {
        return client.queues().getAccessPolicyWithRestResponseAsync(timeout, null, context)
            .flatMapMany(response -> Flux.fromIterable(response.value()));
    }

    public Mono<VoidResponse> setAccessPolicy(List<SignedIdentifier> permissions, Context context) {
        return setAccessPolicy(permissions, null, context);
    }

    Mono<VoidResponse> setAccessPolicy(List<SignedIdentifier> permissions, Integer timeout, Context context) {
        return client.queues().setAccessPolicyWithRestResponseAsync(permissions, timeout, null, context)
            .map(response -> new VoidResponse(response.request(), response.statusCode(), response.headers()));
    }
}
