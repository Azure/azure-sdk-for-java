// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.storage.queue;

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
}
