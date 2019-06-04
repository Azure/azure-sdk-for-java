// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.storage.queue;

final class QueueServiceRawClient {
    private final QueueServiceAsyncRawClient client;

    QueueServiceRawClient(QueueServiceAsyncRawClient client) {
        this.client = client;
    }

    public QueueRawClient getQueueServiceRawClient(String queueName) {
        return new QueueRawClient(client.getQueueAsyncRawClient(queueName));
    }
}
