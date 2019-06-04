// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.storage.queue;

import com.azure.core.util.Context;
import com.azure.storage.queue.models.ListQueuesSegmentResponse;
import com.azure.storage.queue.models.QueuesSegmentOptions;
import com.azure.storage.queue.models.StorageServiceProperties;
import com.azure.storage.queue.models.StorageServiceStats;

public final class QueueServiceClient {
    private final QueueServiceRawClient client;

    QueueServiceClient(QueueServiceRawClient client) {
        this.client = client;
    }

    public static QueueServiceClientBuilder builder() {
        return new QueueServiceClientBuilder();
    }

    public String url() {
        return client.url();
    }

    public QueueServiceRawClient getRawClient() {
        return client;
    }

    public QueueClient getQueueClient(String queueName) {
        return new QueueClient(client.getQueueServiceRawClient(queueName));
    }

    public ListQueuesSegmentResponse listQueuesSegment(String marker, QueuesSegmentOptions options) {
        return client.listQueuesSegment(marker, options, Context.NONE).value();
    }

    public StorageServiceProperties getProperties() {
        return client.getProperties(Context.NONE).value();
    }

    public void setProperties(StorageServiceProperties properties) {
        client.setProperties(properties, Context.NONE);
    }

    public StorageServiceStats getStatistics() {
        return client.getStatistics(Context.NONE).value();
    }
}
