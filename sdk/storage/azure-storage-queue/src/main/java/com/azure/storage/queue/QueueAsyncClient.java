// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.storage.queue;

import com.azure.core.http.HttpPipeline;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.http.rest.VoidResponse;
import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.common.credentials.SASTokenCredential;
import com.azure.storage.common.credentials.SharedKeyCredential;
import com.azure.storage.queue.implementation.AzureQueueStorageBuilder;
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
import com.azure.storage.queue.models.StorageErrorException;
import com.azure.storage.queue.models.UpdatedMessage;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * This class provides a client that contains all the operations for interacting with a queue in Azure Storage Queue.
 * Operations allowed by the client are creating and deleting the queue, retrieving and updating metadata and access
 * policies of the queue, and enqueuing, dequeuing, peeking, updating, and deleting messages.
 *
 * <p><strong>Instantiating an Asynchronous Queue Client</strong></p>
 *
 * {@codesnippet com.azure.storage.queue.queueAsyncClient.instantiation}
 *
 * <p>View {@link QueueClientBuilder this} for additional ways to construct the client.</p>
 *
 * @see QueueClientBuilder
 * @see QueueClient
 * @see SharedKeyCredential
 * @see SASTokenCredential
 */
public final class QueueAsyncClient {
    private static final ClientLogger LOGGER = new ClientLogger(QueueAsyncClient.class);
    private final AzureQueueStorageImpl client;
    private final String queueName;

    /**
     * Creates a QueueAsyncClient that sends requests to the storage queue service at {@code AzureQueueStorageImpl#getUrl() endpoint}.
     * Each service call goes through the {@link HttpPipeline pipeline} in the {@code AzureQueueStorageImpl client}.
     *
     * @param client Client that interacts with the service interfaces
     * @param queueName Name of the queue
     */
    QueueAsyncClient(AzureQueueStorageImpl client, String queueName) {
        this.queueName = queueName;

        this.client = new AzureQueueStorageBuilder().pipeline(client.getHttpPipeline())
            .url(client.getUrl())
            .version(client.getVersion())
            .build();
    }

    /**
     * Creates a QueueAsyncClient that sends requests to the storage queue service at {@code endpoint}.
     * Each service call goes through the {@code httpPipeline}.
     *
     * @param endpoint URL for the Storage Queue service
     * @param httpPipeline HttpPipeline that the HTTP requests and response flow through
     * @param queueName Name of the queue
     */
    QueueAsyncClient(URL endpoint, HttpPipeline httpPipeline, String queueName) {
        this.queueName = queueName;

        this.client = new AzureQueueStorageBuilder().pipeline(httpPipeline)
            .url(endpoint.toString())
            .build();
    }

    /**
     * @return the URL of the storage queue
     * @throws RuntimeException If the queue is using a malformed URL.
     */
    public URL getQueueUrl() {
        try {
            return new URL(client.getUrl());
        } catch (MalformedURLException ex) {
            LOGGER.error("Queue URL is malformed");
            throw new RuntimeException("Queue URL is malformed");
        }
    }

    /**
     * Creates a new queue.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Create a queue</p>
     *
     * {@codesnippet com.azure.storage.queue.queueAsyncClient.create}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/create-queue4">Azure Docs</a>.</p>
     *
     * @return A response that only contains headers and response status code
     * @throws StorageErrorException If a queue with the same name already exists in the queue service.
     */
    public Mono<VoidResponse> create() {
        return create(null);
    }

    /**
     * Creates a new queue.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Create a queue with metadata "queue:metadataMap"</p>
     *
     * {@codesnippet com.azure.storage.queue.queueAsyncClient.create#map}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/create-queue4">Azure Docs</a>.</p>
     *
     * @param metadata Metadata to associate with the queue
     * @return A response that only contains headers and response status code
     * @throws StorageErrorException If a queue with the same name and different metadata already exists in the queue service.
     */
    public Mono<VoidResponse> create(Map<String, String> metadata) {
        return client.queues().createWithRestResponseAsync(queueName, null, metadata, null, Context.NONE)
            .map(VoidResponse::new);
    }

    /**
     * Permanently deletes the queue.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Delete a queue</p>
     *
     * {@codesnippet com.azure.storage.queue.queueAsyncClient.delete}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/delete-queue3">Azure Docs</a>.</p>
     *
     * @return A response that only contains headers and response status code
     * @throws StorageErrorException If the queue doesn't exist
     */
    public Mono<VoidResponse> delete() {
        return client.queues().deleteWithRestResponseAsync(queueName, Context.NONE)
            .map(VoidResponse::new);
    }

    /**
     * Retrieves metadata and approximate message count of the queue.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Get the properties of the queue</p>
     *
     * {@codesnippet com.azure.storage.queue.queueAsyncClient.getProperties}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/get-queue-metadata">Azure Docs</a>.</p>
     *
     * @return A response containing a {@link QueueProperties} value which contains the metadata and approximate
     * messages count of the queue.
     * @throws StorageErrorException If the queue doesn't exist
     */
    public Mono<Response<QueueProperties>> getProperties() {
        return client.queues().getPropertiesWithRestResponseAsync(queueName, Context.NONE)
            .map(this::getQueuePropertiesResponse);
    }

    /**
     * Sets the metadata of the queue.
     *
     * Passing in a {@code null} value for metadata will clear the metadata associated with the queue.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Set the queue's metadata to "queue:metadataMap"</p>
     *
     * {@codesnippet com.azure.storage.queue.queueAsyncClient.setMetadata#map}
     *
     * <p>Clear the queue's metadata</p>
     *
     * {@codesnippet com.azure.storage.queue.queueAsyncClient.clearMetadata#map}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/set-queue-metadata">Azure Docs</a>.</p>
     *
     * @param metadata Metadata to set on the queue
     * @return A response that only contains headers and response status code
     * @throws StorageErrorException If the queue doesn't exist
     */
    public Mono<VoidResponse> setMetadata(Map<String, String> metadata) {
        return client.queues().setMetadataWithRestResponseAsync(queueName, null, metadata, null, Context.NONE)
            .map(VoidResponse::new);
    }

    /**
     * Retrieves stored access policies specified on the queue.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>List the stored access policies</p>
     *
     * {@codesnippet com.azure.storage.queue.queueAsyncClient.getAccessPolicy}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/get-queue-acl">Azure Docs</a>.</p>
     *
     * @return The stored access policies specified on the queue.
     * @throws StorageErrorException If the queue doesn't exist
     */
    public Flux<SignedIdentifier> getAccessPolicy() {
        return client.queues().getAccessPolicyWithRestResponseAsync(queueName, Context.NONE)
            .flatMapMany(response -> Flux.fromIterable(response.value()));
    }

    /**
     * Sets stored access policies on the queue.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Set a read only stored access policy</p>
     *
     * {@codesnippet com.azure.storage.queue.queueAsyncClient.setAccessPolicy}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/set-queue-acl">Azure Docs</a>.</p>
     *
     * @param permissions Access policies to set on the queue
     * @return A response that only contains headers and response status code
     * @throws StorageErrorException If the queue doesn't exist, a stored access policy doesn't have all fields filled out,
     * or the queue will have more than five policies.
     */
    public Mono<VoidResponse> setAccessPolicy(List<SignedIdentifier> permissions) {
        return client.queues().setAccessPolicyWithRestResponseAsync(queueName, permissions, null,  null, Context.NONE)
            .map(VoidResponse::new);
    }

    /**
     * Deletes all messages in the queue.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Clear the messages</p>
     *
     * {@codesnippet com.azure.storage.queue.queueAsyncClient.clearMessages}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/clear-messages">Azure Docs</a>.</p>
     *
     * @return A response that only contains headers and response status code
     * @throws StorageErrorException If the queue doesn't exist
     */
    public Mono<VoidResponse> clearMessages() {
        return client.messages().clearWithRestResponseAsync(queueName, Context.NONE)
            .map(VoidResponse::new);
    }

    /**
     * Enqueues a message that has a time-to-live of 7 days and is instantly visible.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Enqueue a message of "Hello, Azure"</p>
     *
     * {@codesnippet com.azure.storage.queue.queueAsyncClient.enqueueMessage#string}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/put-message">Azure Docs</a>.</p>
     *
     * @param messageText Message text
     * @return A {@link EnqueuedMessage} value that contains the {@link EnqueuedMessage#messageId() messageId} and
     * {@link EnqueuedMessage#popReceipt() popReceipt} that are used to interact with the message and other metadata
     * about the enqueued message.
     * @throws StorageErrorException If the queue doesn't exist
     */
    public Mono<Response<EnqueuedMessage>> enqueueMessage(String messageText) {
        return enqueueMessage(messageText, Duration.ofSeconds(0), Duration.ofDays(7));
    }

    /**
     * Enqueues a message with a given time-to-live and a timeout period where the message is invisible in the queue.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Add a message of "Hello, Azure" that has a timeout of 5 seconds</p>
     *
     * {@codesnippet com.azure.storage.queue.queueAsyncClient.enqueueMessage#string-duration-duration}
     *
     * <p>Add a message of "Goodbye, Azure" that has a time to live of 5 seconds</p>
     *
     * {@codesnippet com.azure.storage.queue.queueAsyncClient.enqueueMessageLiveTime#string-duration-duration}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/put-message">Azure Docs</a>.</p>
     *
     * @param messageText Message text
     * @param visibilityTimeout Optional. The timeout period for how long the message is invisible in the queue in seconds.
     * If unset the value will default to 0 and the message will be instantly visible. The timeout must be between 0
     * seconds and 7 days.
     * @param timeToLive Optional. How long the message will stay alive in the queue in seconds. If unset the value will
     * default to 7 days, if -1 is passed the message will not expire. The time to live must be -1 or any positive number.
     * @return A {@link EnqueuedMessage} value that contains the {@link EnqueuedMessage#messageId() messageId} and
     * {@link EnqueuedMessage#popReceipt() popReceipt} that are used to interact with the message and other metadata
     * about the enqueued message.
     * @throws StorageErrorException If the queue doesn't exist or the {@code visibilityTimeout} or {@code timeToLive}
     * are outside of the allowed limits.
     */
    public Mono<Response<EnqueuedMessage>> enqueueMessage(String messageText, Duration visibilityTimeout, Duration timeToLive) {
        Integer visibilityTimeoutInSeconds = (visibilityTimeout == null) ? null : (int) visibilityTimeout.getSeconds();
        Integer timeToLiveInSeconds = (timeToLive == null) ? null : (int) timeToLive.getSeconds();
        QueueMessage message = new QueueMessage().messageText(messageText);

        return client.messages().enqueueWithRestResponseAsync(queueName, message, visibilityTimeoutInSeconds, timeToLiveInSeconds, null, null, Context.NONE)
            .map(response -> new SimpleResponse<>(response, response.value().get(0)));
    }

    /**
     * Retrieves the first message in the queue and hides it from other operations for 30 seconds.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Dequeue a message</p>
     *
     * {@codesnippet com.azure.storage.queue.queueAsyncClient.dequeueMessages}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/get-messages">Azure Docs</a>.</p>
     *
     * @return The first {@link DequeuedMessage} in the queue, it contains
     * {@link DequeuedMessage#messageId() messageId} and {@link DequeuedMessage#popReceipt() popReceipt} used to interact
     * with the message, additionally it contains other metadata about the message.
     * @throws StorageErrorException If the queue doesn't exist
     */
    public Flux<DequeuedMessage> dequeueMessages() {
        return dequeueMessages(1, Duration.ofSeconds(30));
    }

    /**
     * Retrieves up to the maximum number of messages from the queue and hides them from other operations for 30 seconds.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Dequeue up to 5 messages</p>
     *
     * {@codesnippet com.azure.storage.queue.queueAsyncClient.dequeueMessages#integer}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/get-messages">Azure Docs</a>.</p>
     *
     * @param maxMessages Optional. Maximum number of messages to get, if there are less messages exist in the queue than requested
     * all the messages will be returned. If left empty only 1 message will be retrieved, the allowed range is 1 to 32
     * messages.
     * @return Up to {@code maxMessages} {@link DequeuedMessage DequeuedMessages} from the queue. Each DequeuedMessage contains
     * {@link DequeuedMessage#messageId() messageId} and {@link DequeuedMessage#popReceipt() popReceipt} used to interact
     * with the message and other metadata about the message.
     * @throws StorageErrorException If the queue doesn't exist or {@code maxMessages} is outside of the allowed bounds
     */
    public Flux<DequeuedMessage> dequeueMessages(Integer maxMessages) {
        return dequeueMessages(maxMessages, Duration.ofSeconds(30));
    }

    /**
     * Retrieves up to the maximum number of messages from the queue and hides them from other operations for the
     * timeout period.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Dequeue up to 5 messages and give them a 60 second timeout period</p>
     *
     * {@codesnippet com.azure.storage.queue.queueAsyncClient.dequeueMessages#integer-duration}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/get-messages">Azure Docs</a>.</p>
     *
     * @param maxMessages Optional. Maximum number of messages to get, if there are less messages exist in the queue than requested
     * all the messages will be returned. If left empty only 1 message will be retrieved, the allowed range is 1 to 32
     * messages.
     * @param visibilityTimeout Optional. The timeout period for how long the message is invisible in the queue in seconds.
     * If left empty the dequeued messages will be invisible for 30 seconds. The timeout must be between 1 second and 7 days.
     * @return Up to {@code maxMessages} {@link DequeuedMessage DequeuedMessages} from the queue. Each DeqeuedMessage contains
     * {@link DequeuedMessage#messageId() messageId} and {@link DequeuedMessage#popReceipt() popReceipt} used to interact
     * with the message and other metadata about the message.
     * @throws StorageErrorException If the queue doesn't exist or {@code maxMessages} or {@code visibilityTimeout} is
     * outside of the allowed bounds
     */
    public Flux<DequeuedMessage> dequeueMessages(Integer maxMessages, Duration visibilityTimeout) {
        Integer visibilityTimeoutInSeconds = (visibilityTimeout == null) ? null : (int) visibilityTimeout.getSeconds();
        return client.messages().dequeueWithRestResponseAsync(queueName, maxMessages, visibilityTimeoutInSeconds, null, null, Context.NONE)
            .flatMapMany(response -> Flux.fromIterable(response.value()));
    }

    /**
     * Peeks the first message in the queue.
     *
     * Peeked messages don't contain the necessary information needed to interact with the message nor will it hide
     * messages from other operations on the queue.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Peek the first message</p>
     *
     * {@codesnippet com.azure.storage.queue.queueAsyncClient.peekMessages}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/peek-messages">Azure Docs</a>.</p>
     *
     * @return A {@link PeekedMessage} that contains metadata about the message.
     */
    public Flux<PeekedMessage> peekMessages() {
        return peekMessages(null);
    }

    /**
     * Peek messages from the front of the queue up to the maximum number of messages.
     *
     * Peeked messages don't contain the necessary information needed to interact with the message nor will it hide
     * messages from other operations on the queue.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Peek up to the first five messages</p>
     *
     * {@codesnippet com.azure.storage.queue.queueAsyncClient.peekMessages#integer}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/peek-messages">Azure Docs</a>.</p>
     *
     * @param maxMessages Optional. Maximum number of messages to peek, if there are less messages exist in the queue than requested
     * all the messages will be peeked. If left empty only 1 message will be peeked, the allowed range is 1 to 32
     * messages.
     * @return Up to {@code maxMessages} {@link PeekedMessage PeekedMessages} from the queue. Each PeekedMessage contains
     * metadata about the message.
     * @throws StorageErrorException If the queue doesn't exist or {@code maxMessages} is outside of the allowed bounds
     */
    public Flux<PeekedMessage> peekMessages(Integer maxMessages) {
        return client.messages().peekWithRestResponseAsync(queueName, maxMessages, null, null, Context.NONE)
            .flatMapMany(response -> Flux.fromIterable(response.value()));
    }

    /**
     * Updates the specific message in the queue with a new message and resets the visibility timeout.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Dequeue the first message and update it to "Hello again, Azure" and hide it for 5 seconds</p>
     *
     * {@codesnippet com.azure.storage.queue.queueAsyncClient.updateMessage}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/update-message">Azure Docs</a>.</p>
     *
     * @param messageText Updated value for the message
     * @param messageId Id of the message to update
     * @param popReceipt Unique identifier that must match for the message to be updated
     * @param visibilityTimeout The timeout period for how long the message is invisible in the queue in seconds. The
     * timeout period must be between 1 second and 7 days.
     * @return A {@link UpdatedMessage} that contains the new {@link UpdatedMessage#popReceipt() popReceipt} to interact
     * with the message, additionally contains the updated metadata about the message.
     * @throws StorageErrorException If the queue or messageId don't exist, the popReceipt doesn't match on the message,
     * or the {@code visibilityTimeout} is outside the allowed bounds
     */
    public Mono<Response<UpdatedMessage>> updateMessage(String messageText, String messageId, String popReceipt, Duration visibilityTimeout) {
        QueueMessage message = new QueueMessage().messageText(messageText);
        return client.messageIds().updateWithRestResponseAsync(queueName, messageId, message, popReceipt, (int) visibilityTimeout.getSeconds(), Context.NONE)
            .map(this::getUpdatedMessageResponse);
    }

    /**
     * Deletes the specified message in the queue
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Delete the first message</p>
     *
     * {@codesnippet com.azure.storage.queue.queueAsyncClient.deleteMessage}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/delete-message2">Azure Docs</a>.</p>
     *
     * @param messageId Id of the message to deleted
     * @param popReceipt Unique identifier that must match for the message to be deleted
     * @return A response that only contains headers and response status code
     * @throws StorageErrorException If the queue or messageId don't exist or the popReceipt doesn't match on the message
     */
    public Mono<VoidResponse> deleteMessage(String messageId, String popReceipt) {
        return client.messageIds().deleteWithRestResponseAsync(queueName, messageId, popReceipt, Context.NONE)
            .map(VoidResponse::new);
    }

    /*
     * Maps the HTTP headers returned from the service to the expected response type
     * @param response Service response
     * @return Mapped response
     */
    private Response<QueueProperties> getQueuePropertiesResponse(QueuesGetPropertiesResponse response) {
        QueueGetPropertiesHeaders propertiesHeaders = response.deserializedHeaders();
        QueueProperties properties = new QueueProperties(propertiesHeaders.metadata(), propertiesHeaders.approximateMessagesCount());
        return new SimpleResponse<>(response, properties);
    }

    /*
     * Maps the HTTP headers returned from the service to the expected response type
     * @param response Service response
     * @return Mapped response
     */
    private Response<UpdatedMessage> getUpdatedMessageResponse(MessageIdsUpdateResponse response) {
        MessageIdUpdateHeaders headers = response.deserializedHeaders();
        UpdatedMessage updatedMessage = new UpdatedMessage(headers.popReceipt(), headers.timeNextVisible());
        return new SimpleResponse<>(response, updatedMessage);
    }
}
