// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.storage.queue;

import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.VoidResponse;
import com.azure.storage.queue.models.ListQueuesSegmentResponse;
import com.azure.storage.queue.models.QueuesSegmentOptions;
import com.azure.storage.queue.models.StorageServiceProperties;
import com.azure.storage.queue.models.StorageServiceStats;

public final class QueueServiceClient {
    private final QueueServiceAsyncClient client;

    QueueServiceClient(QueueServiceAsyncClient client) {
        this.client = client;
    }

    public static QueueServiceClientBuilder builder() {
        return new QueueServiceClientBuilder();
    }

    public String url() {
        return client.url();
    }

    public QueueClient getQueueClient(String queueName) {
        return new QueueClient(client.getQueueAsyncClient(queueName));
    }

    public Response<ListQueuesSegmentResponse> listQueuesSegment(String marker, QueuesSegmentOptions options) {
        return client.listQueuesSegment(marker, options).block();
    }

    public Response<StorageServiceProperties> getProperties() {
        return client.getProperties().block();
    }

    public VoidResponse setProperties(StorageServiceProperties properties) {
        return client.setProperties(properties).block();
    }

    public Response<StorageServiceStats> getStatistics() {
        return client.getStatistics().block();
    }
}
