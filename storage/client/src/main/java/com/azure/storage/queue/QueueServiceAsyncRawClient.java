// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.storage.queue;

import com.azure.core.http.HttpPipeline;
import com.azure.storage.queue.implementation.AzureQueueStorageImpl;

import java.net.URL;

final class QueueServiceAsyncRawClient {
    private final AzureQueueStorageImpl client;

    QueueServiceAsyncRawClient(URL endpoint, HttpPipeline httpPipeline) {
        this.client = new AzureQueueStorageImpl(httpPipeline).withUrl(endpoint.toString());
    }

    public QueueAsyncRawClient getQueueAsyncRawClient(String queueName) {
        return new QueueAsyncRawClient(queueName, client);
    }
}
