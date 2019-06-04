// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.storage.queue;

import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.VoidResponse;
import com.azure.core.util.Context;
import com.azure.storage.queue.models.ListQueuesSegmentResponse;
import com.azure.storage.queue.models.QueuesSegmentOptions;
import com.azure.storage.queue.models.StorageServiceProperties;
import com.azure.storage.queue.models.StorageServiceStats;

final class QueueServiceRawClient {
    private final QueueServiceAsyncRawClient client;

    QueueServiceRawClient(QueueServiceAsyncRawClient client) {
        this.client = client;
    }

    public QueueRawClient getQueueServiceRawClient(String queueName) {
        return new QueueRawClient(client.getQueueAsyncRawClient(queueName));
    }

    public String url() {
        return client.url();
    }

    public QueueRawClient getQueueRawClient(String queueName) {
        return new QueueRawClient(client.getQueueAsyncRawClient(queueName));
    }

    public Response<ListQueuesSegmentResponse> listQueuesSegment(String marker, QueuesSegmentOptions options, Context context) {
        return client.listQueuesSegment(marker, options, context).block();
    }

    public Response<StorageServiceProperties> getProperties(Context context) {
        return client.getProperties(context).block();
    }

    public VoidResponse setProperties(StorageServiceProperties properties, Context context) {
        return client.setProperties(properties, context).block();
    }

    public Response<StorageServiceStats> getStatistics(Context context) {
        return client.getStatistics(context).block();
    }
}
