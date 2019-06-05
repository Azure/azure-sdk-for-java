// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.storage.queue;

import com.azure.core.http.HttpPipeline;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.http.rest.VoidResponse;
import com.azure.core.util.Context;
import com.azure.storage.queue.implementation.AzureQueueStorageImpl;
import com.azure.storage.queue.models.ListQueuesSegmentResponse;
import com.azure.storage.queue.models.QueuesSegmentOptions;
import com.azure.storage.queue.models.StorageServiceProperties;
import com.azure.storage.queue.models.StorageServiceStats;
import reactor.core.publisher.Mono;

import java.net.URL;

public final class QueueServiceAsyncClient {
    private final AzureQueueStorageImpl client;

    QueueServiceAsyncClient(URL endpoint, HttpPipeline httpPipeline) {
        this.client = new AzureQueueStorageImpl(httpPipeline).withUrl(endpoint.toString());
    }

    public static QueueServiceAsyncClientBuilder builder() {
        return new QueueServiceAsyncClientBuilder();
    }

    public String url() {
        return client.url();
    }

    public QueueAsyncClient getQueueAsyncClient(String queueName) {
        return new QueueAsyncClient(queueName, client);
    }

    public Mono<Response<ListQueuesSegmentResponse>> listQueuesSegment(String marker, QueuesSegmentOptions options) {
        return client.services().listQueuesSegmentWithRestResponseAsync(options.prefix(), marker, options.maxResults(), options.includes(), null, null, Context.NONE)
            .map(response -> new SimpleResponse<>(response.request(), response.statusCode(), response.headers(), response.value()));
    }

    public Mono<Response<StorageServiceProperties>> getProperties() {
        return client.services().getPropertiesWithRestResponseAsync(Context.NONE)
            .map(response -> new SimpleResponse<>(response.request(), response.statusCode(), response.headers(), response.value()));
    }

    public Mono<VoidResponse> setProperties(StorageServiceProperties properties) {
        return client.services().setPropertiesWithRestResponseAsync(properties, Context.NONE)
            .map(VoidResponse::new);
    }

    public Mono<Response<StorageServiceStats>> getStatistics() {
        return client.services().getStatisticsWithRestResponseAsync(Context.NONE)
            .map(response -> new SimpleResponse<>(response.request(), response.statusCode(), response.headers(), response.value()));
    }
}
