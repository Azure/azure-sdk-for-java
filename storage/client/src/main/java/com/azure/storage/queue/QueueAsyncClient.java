// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.storage.queue;

import com.azure.core.http.HttpPipeline;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.http.rest.VoidResponse;
import com.azure.core.util.Context;
import com.azure.storage.queue.implementation.AzureQueueStorageImpl;
import com.azure.storage.queue.models.DequeuedMessage;
import com.azure.storage.queue.models.EnqueuedMessage;
import com.azure.storage.queue.models.MessageIdUpdateHeaders;
import com.azure.storage.queue.models.MessageIdsUpdateResponse;
import com.azure.storage.queue.models.PeekedMessage;
import com.azure.storage.queue.models.QueueGetPropertiesHeaders;
import com.azure.storage.queue.models.QueueMessage;
import com.azure.storage.queue.models.QueueProperties;
import com.azure.storage.queue.models.QueuesGetPropertiesResponse;
import com.azure.storage.queue.models.SignedIdentifier;
import com.azure.storage.queue.models.StorageErrorCode;
import com.azure.storage.queue.models.StorageErrorException;
import com.azure.storage.queue.models.UpdatedMessage;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URL;
import java.time.Duration;
import java.util.List;
import java.util.Map;

public final class QueueAsyncClient {
    private final AzureQueueStorageImpl client;
    private final String queueName;

    QueueAsyncClient(AzureQueueStorageImpl client, String queueName) {
        this.queueName = queueName;
        this.client = new AzureQueueStorageImpl(client.httpPipeline())
            .withUrl(client.url())
            .withVersion(client.version());

        try {
            create().block();
        } catch (StorageErrorException ex) {
            if (!StorageErrorCode.QUEUE_ALREADY_EXISTS.equals(ex.value().message())) {
                throw ex;
            }
        }
    }

    QueueAsyncClient(URL endpoint, HttpPipeline httpPipeline, String queueName) {
        this.queueName = queueName;
        this.client = new AzureQueueStorageImpl(httpPipeline)
            .withUrl(endpoint.toString());
    }

    /**
     * @return a client builder
     */
    public static QueueAsyncClientBuilder builder() {
        return new QueueAsyncClientBuilder();
    }

    /**
     * @return the URL of the queue
     */
    public String url() {
        return client.url();
    }

    /**
     * Creates a queue with no metadata
     * @return an empty response
     */
    public Mono<VoidResponse> create() {
        return create(null);
    }

    /**
     * Creates a queue with metadata
     * @param metadata Metadata to set on the queue
     * @return an empty response
     */
    public Mono<VoidResponse> create(Map<String, String> metadata) {
        return client.queues().createWithRestResponseAsync(queueName, Context.NONE)
            .map(VoidResponse::new);
    }

    /**
     * Deletes the queue
     * @return an empty response
     */
    public Mono<VoidResponse> delete() {
        return client.queues().deleteWithRestResponseAsync(queueName, Context.NONE)
            .map(VoidResponse::new);
    }

    /**
     * @return the properties of the queue
     */
    public Mono<Response<QueueProperties>> getProperties() {
        return client.queues().getPropertiesWithRestResponseAsync(queueName, Context.NONE)
            .map(this::getQueuePropertiesResponse);
    }

    /**
     * Sets the metadata of the queue
     * @param metadata Metadata to set on the queue
     * @return an empty response
     */
    public Mono<VoidResponse> setMetadata(Map<String, String> metadata) {
        return client.queues().setMetadataWithRestResponseAsync(queueName, Context.NONE)
            .map(VoidResponse::new);
    }

    /**
     * @return the access policies of the queue
     */
    public Flux<SignedIdentifier> getAccessPolicy() {
        return client.queues().getAccessPolicyWithRestResponseAsync(queueName, Context.NONE)
            .flatMapMany(response -> Flux.fromIterable(response.value()));
    }

    /**
     * Sets access policies of the queue
     * @param permissions Access policies to set on the queue
     * @return an empty response
     */
    public Mono<VoidResponse> setAccessPolicy(List<SignedIdentifier> permissions) {
        return client.queues().setAccessPolicyWithRestResponseAsync(queueName, permissions, null,  null, Context.NONE)
            .map(VoidResponse::new);
    }

    /**
     * Deletes all messages in the queue
     * @return an empty response
     */
    public Mono<VoidResponse> clearMessages() {
        return client.messages().clearWithRestResponseAsync(queueName, Context.NONE)
            .map(VoidResponse::new);
    }

    /**
     * Adds a message to the queue
     * @param messageText Message text
     * @return the enqueued message information
     */
    public Mono<Response<EnqueuedMessage>> enqueueMessage(String messageText) {
        return enqueueMessage(messageText, Duration.ofSeconds(0), Duration.ofDays(7));
    }

    /**
     * Adds a message to the queue
     * @param messageText Message text
     * @param visibilityTimeout How long the message is invisible in the queue in seconds, default is 0 seconds
     * @param timeToLive How long the message will stay in the queue in seconds, default is 7 days
     * @return the enqueued message information
     */
    public Mono<Response<EnqueuedMessage>> enqueueMessage(String messageText, Duration visibilityTimeout, Duration timeToLive) {
        Integer visibilityTimeoutInSeconds = (visibilityTimeout == null) ? null : (int) visibilityTimeout.getSeconds();
        Integer timeToLiveInSeconds = (timeToLive == null) ? null : (int) timeToLive.getSeconds();
        QueueMessage message = new QueueMessage().messageText(messageText);

        return client.messages().enqueueWithRestResponseAsync(queueName, message, visibilityTimeoutInSeconds, timeToLiveInSeconds, null, null, Context.NONE)
            .map(response -> mapResponse(response, response.value().get(0)));
    }

    /**
     * Retrieves a message from the queue
     * @return dequeued message information
     */
    public Flux<DequeuedMessage> dequeueMessages() {
        return dequeueMessages(1, Duration.ofSeconds(30));
    }

    /**
     * Retrieves messages from the queue
     * @param maxMessages Maximum number of messages to get, must be in the range (0, 32], default is 1
     * @param visibilityTimeout How long the message is invisible in the queue in seconds, default is 30 seconds
     * @return dequeued message information
     */
    public Flux<DequeuedMessage> dequeueMessages(Integer maxMessages, Duration visibilityTimeout) {
        Integer visibilityTimeoutInSeconds = (visibilityTimeout == null) ? null : (int) visibilityTimeout.getSeconds();
        return client.messages().dequeueWithRestResponseAsync(queueName, maxMessages, visibilityTimeoutInSeconds, null, null, Context.NONE)
            .flatMapMany(response -> Flux.fromIterable(response.value()));
    }

    /**
     * Peeks at messages in the queue
     * @return peeked message information
     */
    public Flux<PeekedMessage> peekMessages() {
        return peekMessages(1);
    }

    /**
     * Peeks at messages in the queue
     * @param maxMessages Maximum number of messages to peek, must be in the range (0, 32], default is 1
     * @return peeked message information
     */
    public Flux<PeekedMessage> peekMessages(Integer maxMessages) {
        return client.messages().peekWithRestResponseAsync(queueName, maxMessages, null, null, Context.NONE)
            .flatMapMany(response -> Flux.fromIterable(response.value()));
    }

    /**
     * Updates the message in the queue
     * @param messageId Id of the message
     * @param messageText Updated value for the message
     * @param popReceipt Unique identifier that must match the message for it to be updated
     * @param visibilityTimeout How long the message will be invisible in the queue in seconds
     * @return the updated message information
     */
    public Mono<Response<UpdatedMessage>> updateMessage(String messageId, String messageText, String popReceipt, Duration visibilityTimeout) {
        QueueMessage message = new QueueMessage().messageText(messageText);
        return client.messageIds().updateWithRestResponseAsync(queueName, messageId, message, popReceipt, (int) visibilityTimeout.getSeconds(), Context.NONE)
            .map(this::getUpdatedMessageResponse);
    }

    /**
     * Deletes the message from the queue
     * @param messageId Id of the message
     * @param popReceipt Unique identifier that must match the message for it to be deleted
     * @return an empty response
     */
    public Mono<VoidResponse> deleteMessage(String messageId, String popReceipt) {
        return client.messageIds().deleteWithRestResponseAsync(queueName, messageId, popReceipt, Context.NONE)
            .map(VoidResponse::new);
    }

    private Response<QueueProperties> getQueuePropertiesResponse(QueuesGetPropertiesResponse response) {
        QueueGetPropertiesHeaders propertiesHeaders = response.deserializedHeaders();
        QueueProperties properties = new QueueProperties(propertiesHeaders.metadata(), propertiesHeaders.approximateMessagesCount());
        return mapResponse(response, properties);
    }

    private Response<UpdatedMessage> getUpdatedMessageResponse(MessageIdsUpdateResponse response) {
        MessageIdUpdateHeaders headers = response.deserializedHeaders();
        UpdatedMessage updatedMessage = new UpdatedMessage(headers.popReceipt(), headers.timeNextVisible());
        return mapResponse(response, updatedMessage);
    }

    private <T> SimpleResponse<T> mapResponse(Response response, T value) {
        return new SimpleResponse<>(response.request(), response.statusCode(), response.headers(), value);
    }
}
