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

final class QueueAsyncRawClient {
    private final AzureQueueStorageImpl client;

    QueueAsyncRawClient(String queueName, AzureQueueStorageImpl generateClient) {
        this.client = new AzureQueueStorageImpl(generateClient.httpPipeline())
            .withUrl(generateClient.url() + "/" + queueName)
            .withVersion(generateClient.version());

        this.create(Context.NONE);
    }

    QueueAsyncRawClient(URL endpoint, HttpPipeline httpPipeline) {
        this.client = new AzureQueueStorageImpl(httpPipeline).withUrl(endpoint.toString());
    }

    public MessagesAsyncRawClient getMessagesClient() {
        return new MessagesAsyncRawClient(client);
    }

    public Mono<VoidResponse> create(Context context) {
        return create(null, context);
    }

    Mono<VoidResponse> create(Duration timeout, Context context) {
        if (timeout == null) {
            return client.queues().createWithRestResponseAsync(context)
                .map(VoidResponse::new);
        } else {
            return client.queues().createWithRestResponseAsync(context)
                .timeout(timeout)
                .map(VoidResponse::new);
        }

    }

    public Mono<VoidResponse> delete(Context context) {
        return delete(null, context);
    }

    Mono<VoidResponse> delete(Duration timeout, Context context) {
        if (timeout == null) {
            return client.queues().deleteWithRestResponseAsync(context)
                .map(VoidResponse::new);
        } else {
            return client.queues().deleteWithRestResponseAsync(context)
                .timeout(timeout)
                .map(VoidResponse::new);
        }
    }

    public Mono<Response<QueueProperties>> getProperties(Context context) {
        return getProperties(null, context);
    }

    Mono<Response<QueueProperties>> getProperties(Duration timeout, Context context) {
        if (timeout == null) {
            return client.queues().getPropertiesWithRestResponseAsync(context)
                .map(this::getQueuePropertiesResponse);
        } else {
            return client.queues().getPropertiesWithRestResponseAsync(context)
                .timeout(timeout)
                .map(this::getQueuePropertiesResponse);
        }
    }

    public Mono<VoidResponse> setMetadata(Map<String, String> metadata, Context context) {
        return setMetadata(metadata, null, context);
    }

    Mono<VoidResponse> setMetadata(Map<String, String> metadata, Duration timeout, Context context) {
        if (timeout == null) {
            return client.queues().setMetadataWithRestResponseAsync(null, metadata, null, context)
                .map(VoidResponse::new);
        } else {
            return client.queues().setMetadataWithRestResponseAsync(null, metadata, null, context)
                .timeout(timeout)
                .map(VoidResponse::new);
        }
    }

    public Flux<SignedIdentifier> getAccessPolicy(Context context) {
        return getAccessPolicy(null, context);
    }

    Flux<SignedIdentifier> getAccessPolicy(Duration timeout, Context context) {
        if (timeout == null) {
            return client.queues().getAccessPolicyWithRestResponseAsync(context)
                .flatMapMany(response -> Flux.fromIterable(response.value()));
        } else {
            return client.queues().getAccessPolicyWithRestResponseAsync(context)
                .timeout(timeout)
                .flatMapMany(response -> Flux.fromIterable(response.value()));
        }
    }

    public Mono<VoidResponse> setAccessPolicy(List<SignedIdentifier> permissions, Context context) {
        return setAccessPolicy(permissions, null, context);
    }

    Mono<VoidResponse> setAccessPolicy(List<SignedIdentifier> permissions, Duration timeout, Context context) {
        if (timeout == null) {
            return client.queues().setAccessPolicyWithRestResponseAsync(permissions, null, null, context)
                .map(VoidResponse::new);
        } else {
            return client.queues().setAccessPolicyWithRestResponseAsync(permissions, null, null, context)
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
