// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.storage.queue;

import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.storage.queue.models.ListQueuesSegmentResponse;
import com.azure.storage.queue.models.QueuesSegmentOptions;
import com.azure.storage.queue.models.StorageServiceProperties;
import com.azure.storage.queue.models.StorageServiceStats;
import reactor.core.publisher.Mono;

public final class QueueServiceAsyncClient {
    private final QueueServiceAsyncRawClient client;

    QueueServiceAsyncClient(QueueServiceAsyncRawClient client) {
        this.client = client;
    }

    public static QueueServiceAsyncClientBuilder builder() {
        return new QueueServiceAsyncClientBuilder();
    }

    public QueueServiceAsyncRawClient getRawClient() {
        return client;
    }

    public QueueAsyncClient getQueueAsyncClient(String queueName) {
        return new QueueAsyncClient(client.getQueueAsyncRawClient(queueName));
    }

    public Mono<ListQueuesSegmentResponse> listQueuesSegment(String marker, QueuesSegmentOptions options) {
        return client.listQueuesSegment(marker, options, Context.NONE)
            .map(Response::value);
    }

    public Mono<StorageServiceProperties> getProperties() {
        return client.getProperties(Context.NONE)
            .map(Response::value);
    }

    public Mono<Void> setProperties(StorageServiceProperties properties) {
        return client.setProperties(properties, Context.NONE)
            .flatMap(response -> Mono.empty());
    }

    public Mono<StorageServiceStats> getStatistics() {
        return client.getStatistics(Context.NONE)
            .map(Response::value);
    }
}
