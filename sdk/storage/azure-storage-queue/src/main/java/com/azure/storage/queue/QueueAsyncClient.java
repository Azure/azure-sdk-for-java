// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.storage.queue;

import static com.azure.core.implementation.util.FluxUtil.withContext;
import static com.azure.storage.queue.PostProcessor.postProcessResponse;

import com.azure.core.annotation.ServiceClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedResponse;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.implementation.http.PagedResponseBase;
import com.azure.core.implementation.util.FluxUtil;
import com.azure.core.util.Context;
import com.azure.storage.common.Utility;
import com.azure.storage.common.credentials.SharedKeyCredential;
import com.azure.storage.queue.implementation.AzureQueueStorageImpl;
import com.azure.storage.queue.implementation.models.MessageIdUpdateHeaders;
import com.azure.storage.queue.implementation.models.MessageIdsUpdateResponse;
import com.azure.storage.queue.implementation.models.QueueGetPropertiesHeaders;
import com.azure.storage.queue.implementation.models.QueuesGetPropertiesResponse;
import com.azure.storage.queue.models.DequeuedMessage;
import com.azure.storage.queue.models.EnqueuedMessage;
import com.azure.storage.queue.models.PeekedMessage;
import com.azure.storage.queue.models.QueueMessage;
import com.azure.storage.queue.models.QueueProperties;
import com.azure.storage.queue.models.SignedIdentifier;
import com.azure.storage.queue.models.StorageException;
import com.azure.storage.queue.models.UpdatedMessage;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import reactor.core.publisher.Mono;

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
 */
@ServiceClient(builder = QueueClientBuilder.class, isAsync = true)
public final class QueueAsyncClient {
    private final AzureQueueStorageImpl client;
    private final String queueName;
    private final String accountName;

    /**
     * Creates a QueueAsyncClient that sends requests to the storage queue service at {@link #getQueueUrl() endpoint}.
     * Each service call goes through the {@link HttpPipeline pipeline}.
     *
     * @param client Client that interacts with the service interfaces
     * @param queueName Name of the queue
     */
    QueueAsyncClient(AzureQueueStorageImpl client, String queueName, String accountName) {
        Objects.requireNonNull(queueName, "'queueName' cannot be null.");
        this.queueName = queueName;
        this.client = client;
        this.accountName = accountName;
    }

    /**
     * @return the URL of the storage queue
     */
    public String getQueueUrl() {
        return String.format("%s/%s", client.getUrl(), queueName);
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
     * @return An empty response
     * @throws StorageException If a queue with the same name already exists in the queue service.
     */
    public Mono<Void> create() {
        return createWithResponse(null).flatMap(FluxUtil::toMono);
    }

    /**
     * Creates a new queue.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Create a queue with metadata "queue:metadataMap"</p>
     *
     * {@codesnippet com.azure.storage.queue.queueAsyncClient.createWithResponse#map}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/create-queue4">Azure Docs</a>.</p>
     *
     * @param metadata Metadata to associate with the queue
     * @return A response that only contains headers and response status code
     * @throws StorageException If a queue with the same name and different metadata already exists in the queue
     * service.
     */
    public Mono<Response<Void>> createWithResponse(Map<String, String> metadata) {
        return withContext(context -> createWithResponse(metadata, context));
    }

    Mono<Response<Void>> createWithResponse(Map<String, String> metadata, Context context) {
        return postProcessResponse(client.queues()
            .createWithRestResponseAsync(queueName, null, metadata, null, context))
            .map(response -> new SimpleResponse<>(response, null));
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
     * @return An empty response
     * @throws StorageException If the queue doesn't exist
     */
    public Mono<Void> delete() {
        return deleteWithResponse().flatMap(FluxUtil::toMono);
    }

    /**
     * Permanently deletes the queue.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Delete a queue</p>
     *
     * {@codesnippet com.azure.storage.queue.queueAsyncClient.deleteWithResponse}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/delete-queue3">Azure Docs</a>.</p>
     *
     * @return A response that only contains headers and response status code
     * @throws StorageException If the queue doesn't exist
     */
    public Mono<Response<Void>> deleteWithResponse() {
        return withContext(this::deleteWithResponse);
    }

    Mono<Response<Void>> deleteWithResponse(Context context) {
        return postProcessResponse(client.queues().deleteWithRestResponseAsync(queueName, context))
            .map(response -> new SimpleResponse<>(response, null));
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
     * @throws StorageException If the queue doesn't exist
     */
    public Mono<QueueProperties> getProperties() {
        return getPropertiesWithResponse().flatMap(FluxUtil::toMono);
    }

    /**
     * Retrieves metadata and approximate message count of the queue.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Get the properties of the queue</p>
     *
     * {@codesnippet com.azure.storage.queue.queueAsyncClient.getPropertiesWithResponse}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/get-queue-metadata">Azure Docs</a>.</p>
     *
     * @return A response containing a {@link QueueProperties} value which contains the metadata and approximate
     * messages count of the queue.
     * @throws StorageException If the queue doesn't exist
     */
    public Mono<Response<QueueProperties>> getPropertiesWithResponse() {
        return withContext(this::getPropertiesWithResponse);
    }

    Mono<Response<QueueProperties>> getPropertiesWithResponse(Context context) {
        return postProcessResponse(client.queues().getPropertiesWithRestResponseAsync(queueName, context))
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
     * @throws StorageException If the queue doesn't exist
     */
    public Mono<Void> setMetadata(Map<String, String> metadata) {
        return setMetadataWithResponse(metadata).flatMap(FluxUtil::toMono);
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
     * {@codesnippet com.azure.storage.queue.queueAsyncClient.setMetadataWithResponse#map}
     *
     * <p>Clear the queue's metadata</p>
     *
     * {@codesnippet com.azure.storage.queue.queueAsyncClient.clearMetadataWithResponse#map}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/set-queue-metadata">Azure Docs</a>.</p>
     *
     * @param metadata Metadata to set on the queue
     * @return A response that only contains headers and response status code
     * @throws StorageException If the queue doesn't exist
     */
    public Mono<Response<Void>> setMetadataWithResponse(Map<String, String> metadata) {
        return withContext(context -> setMetadataWithResponse(metadata, context));
    }

    Mono<Response<Void>> setMetadataWithResponse(Map<String, String> metadata, Context context) {
        return postProcessResponse(client.queues()
            .setMetadataWithRestResponseAsync(queueName, null, metadata, null, context))
            .map(response -> new SimpleResponse<>(response, null));
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
     * @throws StorageException If the queue doesn't exist
     */
    public PagedFlux<SignedIdentifier> getAccessPolicy() {
        Function<String, Mono<PagedResponse<SignedIdentifier>>> retriever =
            marker -> postProcessResponse(this.client.queues()
                .getAccessPolicyWithRestResponseAsync(queueName, Context.NONE))
                .map(response -> new PagedResponseBase<>(response.getRequest(),
                    response.getStatusCode(),
                    response.getHeaders(),
                    response.getValue(),
                    null,
                    response.getDeserializedHeaders()));

        return new PagedFlux<>(() -> retriever.apply(null), retriever);
    }

    /**
     * Sets stored access policies on the queue.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Set a read only stored access policy</p>
     *
     * {@codesnippet com.azure.storage.queue.QueueAsyncClient.setAccessPolicy#List}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/set-queue-acl">Azure Docs</a>.</p>
     *
     * @param permissions Access policies to set on the queue
     * @return An empty response
     * @throws StorageException If the queue doesn't exist, a stored access policy doesn't have all fields filled out,
     * or the queue will have more than five policies.
     */
    public Mono<Void> setAccessPolicy(List<SignedIdentifier> permissions) {
        return setAccessPolicyWithResponse(permissions).flatMap(FluxUtil::toMono);
    }

    /**
     * Sets stored access policies on the queue.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Set a read only stored access policy</p>
     *
     * {@codesnippet com.azure.storage.queue.QueueAsyncClient.setAccessPolicyWithResponse#List}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/set-queue-acl">Azure Docs</a>.</p>
     *
     * @param permissions Access policies to set on the queue
     * @return A response that only contains headers and response status code
     * @throws StorageException If the queue doesn't exist, a stored access policy doesn't have all fields filled out,
     * or the queue will have more than five policies.
     */
    public Mono<Response<Void>> setAccessPolicyWithResponse(List<SignedIdentifier> permissions) {
        return withContext(context -> setAccessPolicyWithResponse(permissions, context));
    }

    Mono<Response<Void>> setAccessPolicyWithResponse(List<SignedIdentifier> permissions, Context context) {
        /*
        We truncate to seconds because the service only supports nanoseconds or seconds, but doing an
        OffsetDateTime.now will only give back milliseconds (more precise fields are zeroed and not serialized). This
        allows for proper serialization with no real detriment to users as sub-second precision on active time for
        signed identifiers is not really necessary.
         */
        if (permissions != null) {
            for (SignedIdentifier permission : permissions) {
                if (permission.getAccessPolicy() != null && permission.getAccessPolicy().getStart() != null) {
                    permission.getAccessPolicy().setStart(
                        permission.getAccessPolicy().getStart().truncatedTo(ChronoUnit.SECONDS));
                }
                if (permission.getAccessPolicy() != null && permission.getAccessPolicy().getExpiry() != null) {
                    permission.getAccessPolicy().setExpiry(
                        permission.getAccessPolicy().getExpiry().truncatedTo(ChronoUnit.SECONDS));
                }
            }
        }

        return postProcessResponse(client.queues()
            .setAccessPolicyWithRestResponseAsync(queueName, permissions, null, null, context))
            .map(response -> new SimpleResponse<>(response, null));
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
     * @return An empty response
     * @throws StorageException If the queue doesn't exist
     */
    public Mono<Void> clearMessages() {
        return clearMessagesWithResponse().flatMap(FluxUtil::toMono);
    }

    /**
     * Deletes all messages in the queue.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Clear the messages</p>
     *
     * {@codesnippet com.azure.storage.queue.queueAsyncClient.clearMessagesWithResponse}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/clear-messages">Azure Docs</a>.</p>
     *
     * @return A response that only contains headers and response status code
     * @throws StorageException If the queue doesn't exist
     */
    public Mono<Response<Void>> clearMessagesWithResponse() {
        return withContext(this::clearMessagesWithResponse);
    }

    Mono<Response<Void>> clearMessagesWithResponse(Context context) {
        return postProcessResponse(client.messages().clearWithRestResponseAsync(queueName, context))
            .map(response -> new SimpleResponse<>(response, null));
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
     * @return A {@link EnqueuedMessage} value that contains the {@link EnqueuedMessage#getMessageId() messageId} and
     * {@link EnqueuedMessage#getPopReceipt() popReceipt} that are used to interact with the message and other metadata
     * about the enqueued message.
     * @throws StorageException If the queue doesn't exist
     */
    public Mono<EnqueuedMessage> enqueueMessage(String messageText) {
        return enqueueMessageWithResponse(messageText, null, null).flatMap(FluxUtil::toMono);
    }

    /**
     * Enqueues a message with a given time-to-live and a timeout period where the message is invisible in the queue.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Add a message of "Hello, Azure" that has a timeout of 5 seconds</p>
     *
     * {@codesnippet com.azure.storage.queue.queueAsyncClient.enqueueMessageWithResponse#string-duration-duration}
     *
     * <p>Add a message of "Goodbye, Azure" that has a time to live of 5 seconds</p>
     *
     * {@codesnippet com.azure.storage.queue.QueueAsyncClient.enqueueMessageWithResponse-liveTime#String-Duration-Duration}
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
     * @return A {@link EnqueuedMessage} value that contains the {@link EnqueuedMessage#getMessageId() messageId} and
     * {@link EnqueuedMessage#getPopReceipt() popReceipt} that are used to interact with the message and other metadata
     * about the enqueued message.
     * @throws StorageException If the queue doesn't exist or the {@code visibilityTimeout} or {@code timeToLive} are
     * outside of the allowed limits.
     */
    public Mono<Response<EnqueuedMessage>> enqueueMessageWithResponse(String messageText, Duration visibilityTimeout,
        Duration timeToLive) {
        return withContext(context -> enqueueMessageWithResponse(messageText, visibilityTimeout, timeToLive, context));
    }

    Mono<Response<EnqueuedMessage>> enqueueMessageWithResponse(String messageText, Duration visibilityTimeout,
        Duration timeToLive, Context context) {
        Integer visibilityTimeoutInSeconds = (visibilityTimeout == null) ? null : (int) visibilityTimeout.getSeconds();
        Integer timeToLiveInSeconds = (timeToLive == null) ? null : (int) timeToLive.getSeconds();
        QueueMessage message = new QueueMessage().setMessageText(messageText);

        return postProcessResponse(client.messages()
            .enqueueWithRestResponseAsync(queueName, message, visibilityTimeoutInSeconds, timeToLiveInSeconds,
                null, null, context))
            .map(response -> new SimpleResponse<>(response, response.getValue().get(0)));
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
     * @return The first {@link DequeuedMessage} in the queue, it contains {@link DequeuedMessage#getMessageId()
     * messageId} and {@link DequeuedMessage#getPopReceipt() popReceipt} used to interact with the message, additionally
     * it contains other metadata about the message.
     * @throws StorageException If the queue doesn't exist
     */
    public PagedFlux<DequeuedMessage> dequeueMessages() {
        return dequeueMessagesWithOptionalTimeout(1, null, null, Context.NONE);
    }

    /**
     * Retrieves up to the maximum number of messages from the queue and hides them from other operations for 30
     * seconds.
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
     * @param maxMessages Optional. Maximum number of messages to get, if there are less messages exist in the queue
     * than requested all the messages will be returned. If left empty only 1 message will be retrieved, the allowed
     * range is 1 to 32 messages.
     * @return Up to {@code maxMessages} {@link DequeuedMessage DequeuedMessages} from the queue. Each DequeuedMessage
     * contains {@link DequeuedMessage#getMessageId() messageId} and {@link DequeuedMessage#getPopReceipt() popReceipt}
     * used to interact with the message and other metadata about the message.
     * @throws StorageException If the queue doesn't exist or {@code maxMessages} is outside of the allowed bounds
     */
    public PagedFlux<DequeuedMessage> dequeueMessages(Integer maxMessages) {
        return dequeueMessagesWithOptionalTimeout(maxMessages, null, null, Context.NONE);
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
     * @param maxMessages Optional. Maximum number of messages to get, if there are less messages exist in the queue
     * than requested all the messages will be returned. If left empty only 1 message will be retrieved, the allowed
     * range is 1 to 32 messages.
     * @param visibilityTimeout Optional. The timeout period for how long the message is invisible in the queue. If left
     * empty the dequeued messages will be invisible for 30 seconds. The timeout must be between 1 second and 7 days.
     * @return Up to {@code maxMessages} {@link DequeuedMessage DequeuedMessages} from the queue. Each DeqeuedMessage
     * contains {@link DequeuedMessage#getMessageId() messageId} and {@link DequeuedMessage#getPopReceipt() popReceipt}
     * used to interact with the message and other metadata about the message.
     * @throws StorageException If the queue doesn't exist or {@code maxMessages} or {@code visibilityTimeout} is
     * outside of the allowed bounds
     */
    public PagedFlux<DequeuedMessage> dequeueMessages(Integer maxMessages, Duration visibilityTimeout) {
        return dequeueMessagesWithOptionalTimeout(maxMessages, visibilityTimeout, null, Context.NONE);
    }

    PagedFlux<DequeuedMessage> dequeueMessagesWithOptionalTimeout(Integer maxMessages, Duration visibilityTimeout,
        Duration timeout, Context context) {
        Integer visibilityTimeoutInSeconds = (visibilityTimeout == null) ? null : (int) visibilityTimeout.getSeconds();
        Function<String, Mono<PagedResponse<DequeuedMessage>>> retriever =
            marker -> postProcessResponse(Utility.applyOptionalTimeout(this.client.messages()
                .dequeueWithRestResponseAsync(queueName, maxMessages, visibilityTimeoutInSeconds,
                    null, null, context), timeout)
                .map(response -> new PagedResponseBase<>(response.getRequest(),
                    response.getStatusCode(),
                    response.getHeaders(),
                    response.getValue(),
                    null,
                    response.getDeserializedHeaders())));

        return new PagedFlux<>(() -> retriever.apply(null), retriever);
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
    public PagedFlux<PeekedMessage> peekMessages() {
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
     * @param maxMessages Optional. Maximum number of messages to peek, if there are less messages exist in the queue
     * than requested all the messages will be peeked. If left empty only 1 message will be peeked, the allowed range is
     * 1 to 32 messages.
     * @return Up to {@code maxMessages} {@link PeekedMessage PeekedMessages} from the queue. Each PeekedMessage
     * contains metadata about the message.
     * @throws StorageException If the queue doesn't exist or {@code maxMessages} is outside of the allowed bounds
     */
    public PagedFlux<PeekedMessage> peekMessages(Integer maxMessages) {
        return peekMessagesWithOptionalTimeout(maxMessages, null, Context.NONE);
    }

    PagedFlux<PeekedMessage> peekMessagesWithOptionalTimeout(Integer maxMessages, Duration timeout, Context context) {
        Function<String, Mono<PagedResponse<PeekedMessage>>> retriever =
            marker -> postProcessResponse(Utility.applyOptionalTimeout(this.client.messages()
                .peekWithRestResponseAsync(queueName, maxMessages, null, null, context), timeout)
                .map(response -> new PagedResponseBase<>(response.getRequest(),
                    response.getStatusCode(),
                    response.getHeaders(),
                    response.getValue(),
                    null,
                    response.getDeserializedHeaders())));

        return new PagedFlux<>(() -> retriever.apply(null), retriever);
    }

    /**
     * Updates the specific message in the queue with a new message and resets the visibility timeout.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Dequeue the first message and update it to "Hello again, Azure" and hide it for 5 seconds</p>
     *
     * {@codesnippet com.azure.storage.queue.QueueAsyncClient.updateMessage#String-String-String-Duration}
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
    public Mono<UpdatedMessage> updateMessage(String messageText, String messageId, String popReceipt,
        Duration visibilityTimeout) {
        return updateMessageWithResponse(messageText, messageId, popReceipt, visibilityTimeout)
            .flatMap(FluxUtil::toMono);
    }

    /**
     * Updates the specific message in the queue with a new message and resets the visibility timeout.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Dequeue the first message and update it to "Hello again, Azure" and hide it for 5 seconds</p>
     *
     * {@codesnippet com.azure.storage.queue.QueueAsyncClient.updateMessageWithResponse#String-String-String-Duration}
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
    public Mono<Response<UpdatedMessage>> updateMessageWithResponse(String messageText, String messageId,
        String popReceipt, Duration visibilityTimeout) {
        return withContext(context ->
            updateMessageWithResponse(messageText, messageId, popReceipt, visibilityTimeout, context));
    }

    Mono<Response<UpdatedMessage>> updateMessageWithResponse(String messageText, String messageId, String popReceipt,
        Duration visibilityTimeout, Context context) {
        QueueMessage message = new QueueMessage().setMessageText(messageText);
        return postProcessResponse(client.messageIds()
            .updateWithRestResponseAsync(queueName, messageId, message, popReceipt,
                (int) visibilityTimeout.getSeconds(), context))
            .map(this::getUpdatedMessageResponse);
    }

    /**
     * Deletes the specified message in the queue
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Delete the first message</p>
     *
     * {@codesnippet com.azure.storage.queue.QueueAsyncClient.deleteMessage#String-String}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/delete-message2">Azure Docs</a>.</p>
     *
     * @param messageId Id of the message to deleted
     * @param popReceipt Unique identifier that must match for the message to be deleted
     * @return An empty response
     * @throws StorageException If the queue or messageId don't exist or the popReceipt doesn't match on the message
     */
    public Mono<Void> deleteMessage(String messageId, String popReceipt) {
        return deleteMessageWithResponse(messageId, popReceipt).flatMap(FluxUtil::toMono);
    }

    /**
     * Deletes the specified message in the queue
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Delete the first message</p>
     *
     * {@codesnippet com.azure.storage.queue.QueueAsyncClient.deleteMessageWithResponse#String-String}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/delete-message2">Azure Docs</a>.</p>
     *
     * @param messageId Id of the message to deleted
     * @param popReceipt Unique identifier that must match for the message to be deleted
     * @return A response that only contains headers and response status code
     * @throws StorageException If the queue or messageId don't exist or the popReceipt doesn't match on the message
     */
    public Mono<Response<Void>> deleteMessageWithResponse(String messageId, String popReceipt) {
        return withContext(context -> deleteMessageWithResponse(messageId, popReceipt, context));
    }

    Mono<Response<Void>> deleteMessageWithResponse(String messageId, String popReceipt, Context context) {
        return postProcessResponse(client.messageIds()
            .deleteWithRestResponseAsync(queueName, messageId, popReceipt, context))
            .map(response -> new SimpleResponse<>(response, null));
    }

    /**
     * Get the queue name of the client.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.queue.queueAsyncClient.getQueueName}
     *
     * @return The name of the queue.
     */
    public String getQueueName() {
        return queueName;
    }


    /**
     * Get associated account name.
     *
     * @return account name associated with this storage resource.
     */
    public String getAccountName() {
        return this.accountName;
    }

    /*
     * Maps the HTTP headers returned from the service to the expected response type
     * @param response Service response
     * @return Mapped response
     */
    private Response<QueueProperties> getQueuePropertiesResponse(QueuesGetPropertiesResponse response) {
        QueueGetPropertiesHeaders propertiesHeaders = response.getDeserializedHeaders();
        QueueProperties properties = new QueueProperties(propertiesHeaders.getMetadata(),
            propertiesHeaders.getApproximateMessagesCount());
        return new SimpleResponse<>(response, properties);
    }

    /*
     * Maps the HTTP headers returned from the service to the expected response type
     * @param response Service response
     * @return Mapped response
     */
    private Response<UpdatedMessage> getUpdatedMessageResponse(MessageIdsUpdateResponse response) {
        MessageIdUpdateHeaders headers = response.getDeserializedHeaders();
        UpdatedMessage updatedMessage = new UpdatedMessage(headers.getPopReceipt(), headers.getTimeNextVisible());
        return new SimpleResponse<>(response, updatedMessage);
    }
}
