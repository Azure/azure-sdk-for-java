// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.storage.queue;

import com.azure.core.http.HttpPipeline;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.http.rest.VoidResponse;
import com.azure.core.util.Context;
import com.azure.storage.queue.implementation.AzureQueueStorageImpl;
import com.azure.storage.queue.models.QueueGetPropertiesHeaders;
import com.azure.storage.queue.models.QueueProperties;
import com.azure.storage.queue.models.QueuesGetPropertiesResponse;
import com.azure.storage.queue.models.SignedIdentifier;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URL;
import java.time.Duration;
import java.util.List;
import java.util.Map;

public final class QueueAsyncClient {
    private final AzureQueueStorageImpl client;

    QueueAsyncClient(String queueName, AzureQueueStorageImpl generateClient) {
        this.client = new AzureQueueStorageImpl(generateClient.httpPipeline())
            .withUrl(generateClient.url() + "/" + queueName)
            .withVersion(generateClient.version());

        this.create(null, null);
    }

    QueueAsyncClient(URL endpoint, HttpPipeline httpPipeline) {
        this.client = new AzureQueueStorageImpl(httpPipeline).withUrl(endpoint.toString());
    }

    public static QueueAsyncClientBuilder builder() {
        return new QueueAsyncClientBuilder();
    }

    public String url() {
        return client.url();
    }

    public MessagesAsyncClient getMessagesAsyncClient() {
        return new MessagesAsyncClient(client);
    }

    public Mono<VoidResponse> create(Map<String, String> metadata) {
        return create(metadata, null);
    }

    Mono<VoidResponse> create(Map<String, String> metdata, Duration timeout) {
        if (timeout == null) {
            return client.queues().createWithRestResponseAsync(null, metdata, null, Context.NONE)
                .map(VoidResponse::new);
        } else {
            return client.queues().createWithRestResponseAsync(null, metdata, null, Context.NONE)
                .timeout(timeout)
                .map(VoidResponse::new);
        }

    }

    public Mono<VoidResponse> delete() {
        return delete(null);
    }

    Mono<VoidResponse> delete(Duration timeout) {
        if (timeout == null) {
            return client.queues().deleteWithRestResponseAsync(Context.NONE)
                .map(VoidResponse::new);
        } else {
            return client.queues().deleteWithRestResponseAsync(Context.NONE)
                .timeout(timeout)
                .map(VoidResponse::new);
        }
    }

    public Mono<Response<QueueProperties>> getProperties() {
        return getProperties(null);
    }

    Mono<Response<QueueProperties>> getProperties(Duration timeout) {
        if (timeout == null) {
            return client.queues().getPropertiesWithRestResponseAsync(Context.NONE)
                .map(this::getQueuePropertiesResponse);
        } else {
            return client.queues().getPropertiesWithRestResponseAsync(Context.NONE)
                .timeout(timeout)
                .map(this::getQueuePropertiesResponse);
        }
    }

    public Mono<VoidResponse> setMetadata(Map<String, String> metadata) {
        return setMetadata(metadata, null);
    }

    Mono<VoidResponse> setMetadata(Map<String, String> metadata, Duration timeout) {
        if (timeout == null) {
            return client.queues().setMetadataWithRestResponseAsync(null, metadata, null, Context.NONE)
                .map(VoidResponse::new);
        } else {
            return client.queues().setMetadataWithRestResponseAsync(null, metadata, null, Context.NONE)
                .timeout(timeout)
                .map(VoidResponse::new);
        }
    }

    public Flux<SignedIdentifier> getAccessPolicy() {
        return getAccessPolicy(null);
    }

    Flux<SignedIdentifier> getAccessPolicy(Duration timeout) {
        if (timeout == null) {
            return client.queues().getAccessPolicyWithRestResponseAsync(Context.NONE)
                .flatMapMany(response -> Flux.fromIterable(response.value()));
        } else {
            return client.queues().getAccessPolicyWithRestResponseAsync(Context.NONE)
                .timeout(timeout)
                .flatMapMany(response -> Flux.fromIterable(response.value()));
        }
    }

    public Mono<VoidResponse> setAccessPolicy(List<SignedIdentifier> permissions) {
        return setAccessPolicy(permissions, null);
    }

    Mono<VoidResponse> setAccessPolicy(List<SignedIdentifier> permissions, Duration timeout) {
        if (timeout == null) {
            return client.queues().setAccessPolicyWithRestResponseAsync(permissions, null, null, Context.NONE)
                .map(VoidResponse::new);
        } else {
            return client.queues().setAccessPolicyWithRestResponseAsync(permissions, null, null, Context.NONE)
                .timeout(timeout)
                .map(VoidResponse::new);
        }
    }

    private Response<QueueProperties> getQueuePropertiesResponse(QueuesGetPropertiesResponse response) {
        QueueGetPropertiesHeaders propertiesHeaders = response.deserializedHeaders();
        QueueProperties properties = new QueueProperties(propertiesHeaders.metadata(), propertiesHeaders.approximateMessagesCount());
        return new SimpleResponse<>(response.request(), response.statusCode(), response.headers(), properties);
    }
}
