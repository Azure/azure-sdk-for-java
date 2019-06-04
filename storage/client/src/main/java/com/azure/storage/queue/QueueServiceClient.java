// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.storage.queue;

public final class QueueServiceClient {
    private final QueueServiceRawClient client;

    QueueServiceClient(QueueServiceRawClient client) {
        this.client = client;
    }

    public static QueueServiceClientBuilder builder() {
        return new QueueServiceClientBuilder();
    }

    public QueueServiceRawClient getRawClient() {
        return client;
    }

    public QueueClient getQueueClient(String queueName) {
        return new QueueClient(client.getQueueServiceRawClient(queueName));
    }
}
