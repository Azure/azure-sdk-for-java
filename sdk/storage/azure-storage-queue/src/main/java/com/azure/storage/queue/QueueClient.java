// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.storage.queue;

import com.azure.core.annotation.ServiceClient;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.storage.common.Utility;
import com.azure.storage.common.credentials.SharedKeyCredential;
import com.azure.storage.queue.models.DequeuedMessage;
import com.azure.storage.queue.models.EnqueuedMessage;
import com.azure.storage.queue.models.PeekedMessage;
import com.azure.storage.queue.models.QueueProperties;
import com.azure.storage.queue.models.SignedIdentifier;
import com.azure.storage.queue.models.StorageException;
import com.azure.storage.queue.models.UpdatedMessage;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * This class provides a client that contains all the operations for interacting with a queue in Azure Storage Queue.
 * Operations allowed by the client are creating and deleting the queue, retrieving and updating metadata and access
 * policies of the queue, and enqueuing, dequeuing, peeking, updating, and deleting messages.
 *
 * <p><strong>Instantiating an Synchronous Queue Client</strong></p>
 *
 * {@codesnippet com.azure.storage.queue.queueClient.instantiation}
 *
 * <p>View {@link QueueClientBuilder this} for additional ways to construct the client.</p>
 *
 * @see QueueClientBuilder
 * @see QueueAsyncClient
 * @see SharedKeyCredential
 */
@ServiceClient(builder = QueueClientBuilder.class)
public final class QueueClient {
    private final QueueAsyncClient client;

    /**
     * Creates a QueueClient that wraps a QueueAsyncClient and blocks requests.
     *
     * @param client QueueAsyncClient that is used to send requests
     */
    QueueClient(QueueAsyncClient client) {
        this.client = client;
    }

    /**
     * @return the URL of the storage queue.
     */
    public String getQueueUrl() {
        return client.getQueueUrl();
    }

    /**
     * Creates a new queue.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Create a queue</p>
     *
     * {@codesnippet com.azure.storage.queue.queueClient.create}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/create-queue4">Azure Docs</a>.</p>
     *
     * @throws StorageException If a queue with the same name already exists in the queue service.
     */
    public void create() {
        createWithResponse(null, null, Context.NONE);
    }

    /**
     * Creates a new queue.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Create a queue with metadata "queue:metadataMap"</p>
     *
     * {@codesnippet com.azure.storage.queue.queueClient.createWithResponse#map-duration-context}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/create-queue4">Azure Docs</a>.</p>
     *
     * @param metadata Metadata to associate with the queue
     * @param timeout An optional timeout applied to the operation. If a response is not returned before the timeout
     * concludes a {@link RuntimeException} will be thrown.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A response that only contains headers and response status code
     * @throws StorageException If a queue with the same name and different metadata already exists in the queue
     * service.
     * @throws RuntimeException if the operation doesn't complete before the timeout concludes.
     */
    public Response<Void> createWithResponse(Map<String, String> metadata, Duration timeout, Context context) {
        Mono<Response<Void>> response = client.createWithResponse(metadata, context);
        return Utility.blockWithOptionalTimeout(response, timeout);
    }

    /**
     * Permanently deletes the queue.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Delete a queue</p>
     *
     * {@codesnippet com.azure.storage.queue.queueClient.delete}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/delete-queue3">Azure Docs</a>.</p>
     *
     * @throws StorageException If the queue doesn't exist
     */
    public void delete() {
        deleteWithResponse(null, Context.NONE);
    }

    /**
     * Permanently deletes the queue.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Delete a queue</p>
     *
     * {@codesnippet com.azure.storage.queue.queueClient.deleteWithResponse#duration-context}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/delete-queue3">Azure Docs</a>.</p>
     *
     * @param timeout An optional timeout applied to the operation. If a response is not returned before the timeout
     * concludes a {@link RuntimeException} will be thrown.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A response that only contains headers and response status code
     * @throws StorageException If the queue doesn't exist
     * @throws RuntimeException if the operation doesn't complete before the timeout concludes.
     */
    public Response<Void> deleteWithResponse(Duration timeout, Context context) {
        Mono<Response<Void>> response = client.deleteWithResponse(context);
        return Utility.blockWithOptionalTimeout(response, timeout);
    }

    /**
     * Retrieves metadata and approximate message count of the queue.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Get the properties of the queue</p>
     *
     * {@codesnippet com.azure.storage.queue.queueClient.getProperties}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/get-queue-metadata">Azure Docs</a>.</p>
     *
     * @return A response containing a {@link QueueProperties} value which contains the metadata and approximate
     * messages count of the queue.
     * @throws StorageException If the queue doesn't exist
     */
    public QueueProperties getProperties() {
        return getPropertiesWithResponse(null, Context.NONE).getValue();
    }

    /**
     * Retrieves metadata and approximate message count of the queue.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Get the properties of the queue</p>
     *
     * {@codesnippet com.azure.storage.queue.queueClient.getPropertiesWithResponse#duration-context}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/get-queue-metadata">Azure Docs</a>.</p>
     *
     * @param timeout An optional timeout applied to the operation. If a response is not returned before the timeout
     * concludes a {@link RuntimeException} will be thrown.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A response containing a {@link QueueProperties} value which contains the metadata and approximate
     * messages count of the queue.
     * @throws StorageException If the queue doesn't exist
     * @throws RuntimeException if the operation doesn't complete before the timeout concludes.
     */
    public Response<QueueProperties> getPropertiesWithResponse(Duration timeout, Context context) {
        Mono<Response<QueueProperties>> response = client.getPropertiesWithResponse(context);
        return Utility.blockWithOptionalTimeout(response, timeout);
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
     * {@codesnippet com.azure.storage.queue.queueClient.setMetadata#map}
     *
     * <p>Clear the queue's metadata</p>
     *
     * {@codesnippet com.azure.storage.queue.queueClient.clearMetadata#map}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/set-queue-metadata">Azure Docs</a>.</p>
     *
     * @param metadata Metadata to set on the queue
     * @throws StorageException If the queue doesn't exist
     */
    public void setMetadata(Map<String, String> metadata) {
        setMetadataWithResponse(metadata, null, Context.NONE);
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
     * {@codesnippet com.azure.storage.queue.queueClient.setMetadataWithResponse#map-duration-context}
     *
     * <p>Clear the queue's metadata</p>
     *
     * {@codesnippet com.azure.storage.queue.queueClient.clearMetadataWithResponse#map-duration-context}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/set-queue-metadata">Azure Docs</a>.</p>
     *
     * @param metadata Metadata to set on the queue
     * @param timeout An optional timeout applied to the operation. If a response is not returned before the timeout
     * concludes a {@link RuntimeException} will be thrown.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A response that only contains headers and response status code
     * @throws StorageException If the queue doesn't exist
     * @throws RuntimeException if the operation doesn't complete before the timeout concludes.
     */
    public Response<Void> setMetadataWithResponse(Map<String, String> metadata, Duration timeout, Context context) {
        Mono<Response<Void>> response = client.setMetadataWithResponse(metadata, context);
        return Utility.blockWithOptionalTimeout(response, timeout);
    }

    /**
     * Retrieves stored access policies specified on the queue.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>List the stored access policies</p>
     *
     * {@codesnippet com.azure.storage.queue.queueClient.getAccessPolicy}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/get-queue-acl">Azure Docs</a>.</p>
     *
     * @return The stored access policies specified on the queue.
     * @throws StorageException If the queue doesn't exist
     */
    public PagedIterable<SignedIdentifier> getAccessPolicy() {
        return new PagedIterable<>(client.getAccessPolicy());
    }

    /**
     * Sets stored access policies on the queue.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Set a read only stored access policy</p>
     *
     * {@codesnippet com.azure.storage.queue.QueueClient.setAccessPolicy#List}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/set-queue-acl">Azure Docs</a>.</p>
     *
     * @param permissions Access policies to set on the queue
     * @throws StorageException If the queue doesn't exist, a stored access policy doesn't have all fields filled out,
     * or the queue will have more than five policies.
     */
    public void setAccessPolicy(List<SignedIdentifier> permissions) {
        setAccessPolicyWithResponse(permissions, null, Context.NONE);
    }

    /**
     * Sets stored access policies on the queue.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Set a read only stored access policy</p>
     *
     * {@codesnippet com.azure.storage.queue.queueClient.setAccessPolicyWithResponse#List-Duration-Context}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/set-queue-acl">Azure Docs</a>.</p>
     *
     * @param permissions Access policies to set on the queue
     * @param timeout An optional timeout applied to the operation. If a response is not returned before the timeout
     * concludes a {@link RuntimeException} will be thrown.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A response that only contains headers and response status code
     * @throws StorageException If the queue doesn't exist, a stored access policy doesn't have all fields filled out,
     * or the queue will have more than five policies.
     * @throws RuntimeException if the operation doesn't complete before the timeout concludes.
     */
    public Response<Void> setAccessPolicyWithResponse(List<SignedIdentifier> permissions, Duration timeout,
        Context context) {
        Mono<Response<Void>> response = client.setAccessPolicyWithResponse(permissions, context);
        return Utility.blockWithOptionalTimeout(response, timeout);
    }

    /**
     * Deletes all messages in the queue.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Clear the messages</p>
     *
     * {@codesnippet com.azure.storage.queue.queueClient.clearMessages}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/clear-messages">Azure Docs</a>.</p>
     *
     * @throws StorageException If the queue doesn't exist
     */
    public void clearMessages() {
        clearMessagesWithResponse(null, Context.NONE);
    }

    /**
     * Deletes all messages in the queue.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Clear the messages</p>
     *
     * {@codesnippet com.azure.storage.queue.queueClient.clearMessagesWithResponse#duration-context}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/clear-messages">Azure Docs</a>.</p>
     *
     * @param timeout An optional timeout applied to the operation. If a response is not returned before the timeout
     * concludes a {@link RuntimeException} will be thrown.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A response that only contains headers and response status code
     * @throws StorageException If the queue doesn't exist
     * @throws RuntimeException if the operation doesn't complete before the timeout concludes.
     */
    public Response<Void> clearMessagesWithResponse(Duration timeout, Context context) {
        Mono<Response<Void>> response = client.clearMessagesWithResponse(context);
        return Utility.blockWithOptionalTimeout(response, timeout);
    }

    /**
     * Enqueues a message that has a time-to-live of 7 days and is instantly visible.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Enqueue a message of "Hello, Azure"</p>
     *
     * {@codesnippet com.azure.storage.queue.queueClient.enqueueMessage#string}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/put-message">Azure Docs</a>.</p>
     *
     * @param messageText Message text
     * @return A {@link EnqueuedMessage} value that contains the {@link EnqueuedMessage#getMessageId() messageId} and
     * {@link EnqueuedMessage#getPopReceipt() popReceipt} that are used to interact with the message and other metadata
     * about the enqueued message.
     * @throws StorageException If the queue doesn't exist
     */
    public EnqueuedMessage enqueueMessage(String messageText) {
        return enqueueMessageWithResponse(messageText, null, null, null, Context.NONE).getValue();
    }

    /**
     * Enqueues a message with a given time-to-live and a timeout period where the message is invisible in the queue.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Add a message of "Hello, Azure" that has a timeout of 5 seconds</p>
     *
     * {@codesnippet com.azure.storage.queue.QueueClient.enqueueMessageWithResponse#String-Duration-Duration-Duration-Context1}
     *
     * <p>Add a message of "Goodbye, Azure" that has a time to live of 5 seconds</p>
     *
     * {@codesnippet com.azure.storage.queue.QueueClient.enqueueMessageWithResponse#String-Duration-Duration-Duration-Context2}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/put-message">Azure Docs</a>.</p>
     *
     * @param messageText Message text
     * @param visibilityTimeout Optional. The timeout period for how long the message is invisible in the queue. If
     * unset the value will default to 0 and the message will be instantly visible. The timeout must be between 0
     * seconds and 7 days.
     * @param timeToLive Optional. How long the message will stay alive in the queue. If unset the value will default to
     * 7 days, if -1 is passed the message will not expire. The time to live must be -1 or any positive number.
     * @param timeout An optional timeout applied to the operation. If a response is not returned before the timeout
     * concludes a {@link RuntimeException} will be thrown.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A response containing the {@link EnqueuedMessage} value that contains the {@link
     * EnqueuedMessage#getMessageId() messageId} and {@link EnqueuedMessage#getPopReceipt() popReceipt} that are used to
     * interact with the message and other metadata about the enqueued message.
     * @throws StorageException If the queue doesn't exist or the {@code visibilityTimeout} or {@code timeToLive} are
     * outside of the allowed limits.
     * @throws RuntimeException if the operation doesn't complete before the timeout concludes.
     */
    public Response<EnqueuedMessage> enqueueMessageWithResponse(String messageText, Duration visibilityTimeout,
        Duration timeToLive, Duration timeout, Context context) {
        Mono<Response<EnqueuedMessage>> response = client.enqueueMessageWithResponse(messageText,
            visibilityTimeout, timeToLive, context);
        return Utility.blockWithOptionalTimeout(response, timeout);
    }

    /**
     * Retrieves the first message in the queue and hides it from other operations for 30 seconds.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Dequeue a message</p>
     *
     * {@codesnippet com.azure.storage.queue.queueClient.dequeueMessages}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/get-messages">Azure Docs</a>.</p>
     *
     * @return The first {@link DequeuedMessage} in the queue, it contains {@link DequeuedMessage#getMessageId()
     * messageId} and {@link DequeuedMessage#getPopReceipt() popReceipt} used to interact with the message, additionally
     * it contains other metadata about the message.
     * @throws StorageException If the queue doesn't exist
     */
    public PagedIterable<DequeuedMessage> dequeueMessages() {
        return dequeueMessages(1, Duration.ofSeconds(30), null, Context.NONE);
    }

    /**
     * Retrieves up to the maximum number of messages from the queue and hides them from other operations for 30
     * seconds.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Dequeue up to 5 messages</p>
     *
     * {@codesnippet com.azure.storage.queue.queueClient.dequeueMessages#integer}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/get-messages">Azure Docs</a>.</p>
     *
     * @param maxMessages Optional. Maximum number of messages to get, if there are less messages exist in the queue
     * than requested all the messages will be returned. If left empty only 1 message will be retrieved, the allowed
     * range is 1 to 32 messages.
     * @return Up to {@code maxMessages} {@link DequeuedMessage DequeuedMessages} from the queue. Each DequeuedMessage
     * contains {@link DequeuedMessage#getMessageId() messageId} and {@link DequeuedMessage#getPopReceipt() popReceipt}
     * used to interact with the message and other metadata about the message.
     * @throws StorageException If the queue doesn't exist or {@code maxMessages} is outside of the allowed bounds
     */
    public PagedIterable<DequeuedMessage> dequeueMessages(Integer maxMessages) {
        return dequeueMessages(maxMessages, Duration.ofSeconds(30), null, Context.NONE);
    }

    /**
     * Retrieves up to the maximum number of messages from the queue and hides them from other operations for the
     * timeout period.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Dequeue up to 5 messages and give them a 60 second timeout period</p>
     *
     * {@codesnippet com.azure.storage.queue.queueClient.dequeueMessages#integer-duration-duration-context}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/get-messages">Azure Docs</a>.</p>
     *
     * @param maxMessages Optional. Maximum number of messages to get, if there are less messages exist in the queue
     * than requested all the messages will be returned. If left empty only 1 message will be retrieved, the allowed
     * range is 1 to 32 messages.
     * @param visibilityTimeout Optional. The timeout period for how long the message is invisible in the queue. If left
     * empty the dequeued messages will be invisible for 30 seconds. The timeout must be between 1 second and 7 days.
     * @param timeout An optional timeout applied to the operation. If a response is not returned before the timeout
     * concludes a {@link RuntimeException} will be thrown.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return Up to {@code maxMessages} {@link DequeuedMessage DequeuedMessages} from the queue. Each DeqeuedMessage
     * contains {@link DequeuedMessage#getMessageId() messageId} and {@link DequeuedMessage#getPopReceipt() popReceipt}
     * used to interact with the message and other metadata about the message.
     * @throws StorageException If the queue doesn't exist or {@code maxMessages} or {@code visibilityTimeout} is
     * outside of the allowed bounds
     * @throws RuntimeException if the operation doesn't complete before the timeout concludes.
     */
    public PagedIterable<DequeuedMessage> dequeueMessages(Integer maxMessages, Duration visibilityTimeout,
        Duration timeout, Context context) {
        return new PagedIterable<>(
            client.dequeueMessagesWithOptionalTimeout(maxMessages, visibilityTimeout, timeout, context));
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
     * {@codesnippet com.azure.storage.queue.queueClient.peekMessages}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/peek-messages">Azure Docs</a>.</p>
     *
     * @return A {@link PeekedMessage} that contains metadata about the message.
     */
    public PagedIterable<PeekedMessage> peekMessages() {
        return peekMessages(null, null, Context.NONE);
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
     * {@codesnippet com.azure.storage.queue.queueClient.peekMessages#integer-duration-context}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/peek-messages">Azure Docs</a>.</p>
     *
     * @param maxMessages Optional. Maximum number of messages to peek, if there are less messages exist in the queue
     * than requested all the messages will be peeked. If left empty only 1 message will be peeked, the allowed range is
     * 1 to 32 messages.
     * @param timeout An optional timeout applied to the operation. If a response is not returned before the timeout
     * concludes a {@link RuntimeException} will be thrown.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return Up to {@code maxMessages} {@link PeekedMessage PeekedMessages} from the queue. Each PeekedMessage
     * contains metadata about the message.
     * @throws StorageException If the queue doesn't exist or {@code maxMessages} is outside of the allowed bounds
     * @throws RuntimeException if the operation doesn't complete before the timeout concludes.
     */
    public PagedIterable<PeekedMessage> peekMessages(Integer maxMessages, Duration timeout, Context context) {
        return new PagedIterable<>(client.peekMessagesWithOptionalTimeout(maxMessages, timeout, context));
    }

    /**
     * Updates the specific message in the queue with a new message and resets the visibility timeout.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Dequeue the first message and update it to "Hello again, Azure" and hide it for 5 seconds</p>
     *
     * {@codesnippet com.azure.storage.queue.QueueClient.updateMessage#String-String-String-Duration}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/update-message">Azure Docs</a>.</p>
     *
     * @param messageText Updated value for the message
     * @param messageId Id of the message to update
     * @param popReceipt Unique identifier that must match for the message to be updated
     * @param visibilityTimeout The timeout period for how long the message is invisible in the queue in seconds. The
     * timeout period must be between 1 second and 7 days.
     * @return A {@link UpdatedMessage} that contains the new {@link UpdatedMessage#getPopReceipt() popReceipt} to
     * interact with the message, additionally contains the updated metadata about the message.
     * @throws StorageException If the queue or messageId don't exist, the popReceipt doesn't match on the message, or
     * the {@code visibilityTimeout} is outside the allowed bounds
     */
    public UpdatedMessage updateMessage(String messageText, String messageId, String popReceipt,
        Duration visibilityTimeout) {
        return updateMessageWithResponse(messageText, messageId, popReceipt, visibilityTimeout, null, Context.NONE)
            .getValue();
    }

    /**
     * Updates the specific message in the queue with a new message and resets the visibility timeout.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Dequeue the first message and update it to "Hello again, Azure" and hide it for 5 seconds</p>
     *
     * {@codesnippet com.azure.storage.queue.QueueClient.updateMessageWithResponse#String-String-String-Duration-Duration-Context}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/update-message">Azure Docs</a>.</p>
     *
     * @param messageText Updated value for the message
     * @param messageId Id of the message to update
     * @param popReceipt Unique identifier that must match for the message to be updated
     * @param visibilityTimeout The timeout period for how long the message is invisible in the queue in seconds. The
     * timeout period must be between 1 second and 7 days.
     * @param timeout An optional timeout applied to the operation. If a response is not returned before the timeout
     * concludes a {@link RuntimeException} will be thrown.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A response containing the {@link UpdatedMessage} that contains the new {@link
     * UpdatedMessage#getPopReceipt() popReceipt} to interact with the message, additionally contains the updated
     * metadata about the message.
     * @throws StorageException If the queue or messageId don't exist, the popReceipt doesn't match on the message, or
     * the {@code visibilityTimeout} is outside the allowed bounds
     * @throws RuntimeException if the operation doesn't complete before the timeout concludes.
     */
    public Response<UpdatedMessage> updateMessageWithResponse(String messageText, String messageId, String popReceipt,
        Duration visibilityTimeout, Duration timeout, Context context) {
        Mono<Response<UpdatedMessage>> response = client.updateMessageWithResponse(messageText, messageId,
            popReceipt, visibilityTimeout, context);
        return Utility.blockWithOptionalTimeout(response, timeout);
    }

    /**
     * Deletes the specified message in the queue
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Delete the first message</p>
     *
     * {@codesnippet com.azure.storage.queue.QueueClient.deleteMessage#String-String}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/delete-message2">Azure Docs</a>.</p>
     *
     * @param messageId Id of the message to deleted
     * @param popReceipt Unique identifier that must match for the message to be deleted
     * @throws StorageException If the queue or messageId don't exist or the popReceipt doesn't match on the message
     */
    public void deleteMessage(String messageId, String popReceipt) {
        deleteMessageWithResponse(messageId, popReceipt, null, Context.NONE);
    }

    /**
     * Deletes the specified message in the queue
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Delete the first message</p>
     *
     * {@codesnippet com.azure.storage.queue.QueueClient.deleteMessageWithResponse#String-String-Duration-Context}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/delete-message2">Azure Docs</a>.</p>
     *
     * @param messageId Id of the message to deleted
     * @param popReceipt Unique identifier that must match for the message to be deleted
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @param timeout An optional timeout applied to the operation. If a response is not returned before the timeout
     * concludes a {@link RuntimeException} will be thrown.
     * @return A response that only contains headers and response status code
     * @throws StorageException If the queue or messageId don't exist or the popReceipt doesn't match on the message
     * @throws RuntimeException if the operation doesn't complete before the timeout concludes.
     */
    public Response<Void> deleteMessageWithResponse(String messageId, String popReceipt, Duration timeout,
        Context context) {
        Mono<Response<Void>> response = client.deleteMessageWithResponse(messageId, popReceipt, context);
        return Utility.blockWithOptionalTimeout(response, timeout);
    }

    /**
     * Get the queue name of the client.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.queue.queueClient.getQueueName}
     *
     * @return The name of the queue.
     */
    public String getQueueName() {
        return this.client.getQueueName();
    }
}
