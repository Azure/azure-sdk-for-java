// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.storage.queue;

import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.VoidResponse;
import com.azure.storage.queue.models.QueueItem;
import com.azure.storage.queue.models.QueuesSegmentOptions;
import com.azure.storage.queue.models.StorageErrorException;
import com.azure.storage.queue.models.StorageServiceProperties;
import com.azure.storage.queue.models.StorageServiceStats;

import java.util.Map;

/**
 * Queue service client
 */
public final class QueueServiceClient {
    private final QueueServiceAsyncClient client;

    QueueServiceClient(QueueServiceAsyncClient client) {
        this.client = client;
    }

    /**
     * @return a new client builder instance
     */
    public static QueueServiceClientBuilder builder() {
        return new QueueServiceClientBuilder();
    }

    /**
     * @return URL of the storage account queue endpoint
     */
    public String url() {
        return client.url();
    }

    /**
     * Gets a QueueClient that is targeting the specified queue
     * @param queueName Name of the queue
     * @return QueueClient that interacts with the specified queue
     */
    public QueueClient getQueueClient(String queueName) {
        return new QueueClient(client.getQueueAsyncClient(queueName));
    }

    /**
     * Creates a new queue and returns the client to interact with it
     * @param queueName Name of the queue
     * @return the client to interact with the new queue
     */
    public QueueClient createQueue(String queueName) {
        return createQueue(queueName, null);
    }

    /**
     * Creates a new queue and returns the client to interact with it
     * @param queueName Name of the queue
     * @param metadata Metadata to set on the queue
     * @return the client to interact with the new queue
     * @throws StorageErrorException If the queue fails to be created
     */
    public QueueClient createQueue(String queueName, Map<String, String> metadata) {
        return new QueueClient(client.createQueue(queueName, metadata));
    }

    /**
     * Deletes a queue in the storage account
     * @param queueName Name of the queue
     * @throws StorageErrorException If the queue fails to be deleted
     */
    public void deleteQueue(String queueName) {
        client.deleteQueue(queueName);
    }

    /**
     * Lists the queues in the storage account
     * @param marker Starting point to list the queues
     * @param options Filter for queue selection
     * @return Queues in the storage account that passed the filter and metadata to continue listing more queues
     */
    public Iterable<QueueItem> listQueuesSegment(String marker, QueuesSegmentOptions options) {
        return client.listQueuesSegment(marker, options).collectList().block();
    }

    /**
     * @return Global queue properties in the storage account
     */
    public Response<StorageServiceProperties> getProperties() {
        return client.getProperties().block();
    }

    /**
     * Sets global queue properties for the storage account
     * @param properties Queue properties
     * @return an empty response
     */
    public VoidResponse setProperties(StorageServiceProperties properties) {
        return client.setProperties(properties).block();
    }

    /**
     * @return global statics about queues in the storage account
     */
    public Response<StorageServiceStats> getStatistics() {
        return client.getStatistics().block();
    }
}
