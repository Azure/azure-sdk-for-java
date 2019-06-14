// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.storage.queue;

import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.VoidResponse;
import com.azure.storage.queue.models.DequeuedMessage;
import com.azure.storage.queue.models.EnqueuedMessage;
import com.azure.storage.queue.models.PeekedMessage;
import com.azure.storage.queue.models.QueueProperties;
import com.azure.storage.queue.models.SignedIdentifier;
import com.azure.storage.queue.models.UpdatedMessage;

import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * Queue client
 */
public final class QueueClient {
    private final QueueAsyncClient client;

    QueueClient(QueueAsyncClient client) {
        this.client = client;
    }

    /**
     * @return a new client builder instance
     */
    public static QueueClientBuilder builder() {
        return new QueueClientBuilder();
    }

    /**
     * @return the URL of the queue
     */
    public String getUrl() {
        return client.getUrl();
    }

    /**
     * Creates a queue with no metadata
     * @return an empty response
     */
    public VoidResponse create() {
        return create(null);
    }

    /**
     * Creates a queue with metadata
     * @param metadata Metadata to set on the queue
     * @return an empty response
     */
    public VoidResponse create(Map<String, String> metadata) {
        return client.create(metadata).block();
    }

    /**
     * Deletes the queue
     * @return an empty response
     */
    public VoidResponse delete() {
        return client.delete().block();
    }

    /**
     * @return the properties of the queue
     */
    public Response<QueueProperties> getProperties() {
        return client.getProperties().block();
    }

    /**
     * Sets the metadata of the queue
     * @param metadata Metadata to set on the queue
     * @return an empty response
     */
    public VoidResponse setMetadata(Map<String, String> metadata) {
        return client.setMetadata(metadata).block();
    }

    /**
     * @return the access policies of the queue
     */
    public Iterable<SignedIdentifier> getAccessPolicy() {
        return client.getAccessPolicy().toIterable();
    }

    /**
     * Sets access policies of the queue
     * @param permissions Access policies to set on the queue
     * @return an empty response
     */
    public VoidResponse setAccessPolicy(List<SignedIdentifier> permissions) {
        return client.setAccessPolicy(permissions).block();
    }

    /**
     * Deletes all messages in the queue
     * @return an empty response
     */
    public VoidResponse clearMessages() {
        return client.clearMessages().block();
    }

    /**
     * Adds a message to the queue
     * @param messageText Message text
     * @return the enqueued message information
     */
    public Response<EnqueuedMessage> enqueueMessage(String messageText) {
        return enqueueMessage(messageText, Duration.ofSeconds(0), Duration.ofDays(7));
    }

    /**
     * Adds a message to the queue
     * @param messageText Message text
     * @param visibilityTimeout How long the message is invisible in the queue in seconds, default is 0 seconds
     * @param timeToLive How long the message will stay in the queue in seconds, default is 7 days
     * @return the enqueued message information
     */
    public Response<EnqueuedMessage> enqueueMessage(String messageText, Duration visibilityTimeout, Duration timeToLive) {
        return client.enqueueMessage(messageText, visibilityTimeout, timeToLive).block();
    }

    /**
     * Retrieves a message from the queue
     * @return dequeued message information
     */
    public Iterable<DequeuedMessage> dequeueMessages() {
        return dequeueMessages(1, Duration.ofSeconds(30));
    }

    /**
     * Retrieves a maximum number of messages from the queue
     * @param maxMessages Maximum number of messages to get, must be in the range (0, 32], default is 1
     * @return dequeued message information
     */
    public Iterable<DequeuedMessage> dequeueMessages(Integer maxMessages) {
        return dequeueMessages(maxMessages, Duration.ofSeconds(30));
    }

    /**
     * Retrieves messages from the queue
     * @param maxMessages Maximum number of messages to get, must be in the range (0, 32], default is 1
     * @param visibilityTimeout How long the message is invisible in the queue in seconds, default is 30 seconds
     * @return dequeued message information
     */
    public Iterable<DequeuedMessage> dequeueMessages(Integer maxMessages, Duration visibilityTimeout) {
        return client.dequeueMessages(maxMessages, visibilityTimeout).toIterable();
    }

    /**
     * Peeks at messages in the queue
     * @return peeked message information
     */
    public Iterable<PeekedMessage> peekMessages() {
        return peekMessages(1);
    }

    /**
     * Peeks at messages in the queue
     * @param maxMessages Maximum number of messages to peek, must be in the range (0, 32], default is 1
     * @return peeked message information
     */
    public Iterable<PeekedMessage> peekMessages(Integer maxMessages) {
        return client.peekMessages(maxMessages).toIterable();
    }

    /**
     * Updates the message in the queue
     * @param messageText Updated value for the message
     * @param messageId Id of the message
     * @param popReceipt Unique identifier that must match the message for it to be updated
     * @param visibilityTimeout How long the message will be invisible in the queue in seconds
     * @return the updated message information
     */
    public Response<UpdatedMessage> updateMessage(String messageText, String messageId, String popReceipt, Duration visibilityTimeout) {
        return client.updateMessage(messageText, messageId, popReceipt, visibilityTimeout).block();
    }

    /**
     * Deletes the message from the queue
     * @param messageId Id of the message
     * @param popReceipt Unique identifier that must match the message for it to be deleted
     * @return an empty response
     */
    public VoidResponse deleteMessage(String messageId, String popReceipt) {
        return client.deleteMessage(messageId, popReceipt).block();
    }
}
