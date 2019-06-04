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

final class QueueServiceAsyncRawClient {
    private final AzureQueueStorageImpl client;

    QueueServiceAsyncRawClient(URL endpoint, HttpPipeline httpPipeline) {
        this.client = new AzureQueueStorageImpl(httpPipeline).withUrl(endpoint.toString());
    }

    public QueueAsyncRawClient getQueueAsyncRawClient(String queueName) {
        return new QueueAsyncRawClient(queueName, client);
    }

    public Mono<Response<ListQueuesSegmentResponse>> listQueuesSegment(String marker, QueuesSegmentOptions options, Context context) {
        return client.services().listQueuesSegmentWithRestResponseAsync(options.prefix(), marker, options.maxResults(), options.includes(), null, null, context)
            .map(response -> new SimpleResponse<>(response.request(), response.statusCode(), response.headers(), response.value()));
    }

    public Mono<Response<StorageServiceProperties>> getProperties(Context context) {
        return client.services().getPropertiesWithRestResponseAsync(context)
            .map(response -> new SimpleResponse<>(response.request(), response.statusCode(), response.headers(), response.value()));
    }

    public Mono<VoidResponse> setProperties(StorageServiceProperties properties, Context context) {
        return client.services().setPropertiesWithRestResponseAsync(properties, context)
            .map(VoidResponse::new);
    }

    public Mono<Response<StorageServiceStats>> getStatistics(Context context) {
        return client.services().getStatisticsWithRestResponseAsync(context)
            .map(response -> new SimpleResponse<>(response.request(), response.statusCode(), response.headers(), response.value()));
    }
}
