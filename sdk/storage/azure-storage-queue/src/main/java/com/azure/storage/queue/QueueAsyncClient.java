// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.storage.queue;

import com.azure.core.annotation.ServiceClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedResponse;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.http.rest.PagedResponseBase;
import com.azure.core.util.FluxUtil;
import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.common.StorageSharedKeyCredential;
import com.azure.storage.common.implementation.SasImplUtils;
import com.azure.storage.common.implementation.StorageImplUtils;
import com.azure.storage.queue.implementation.AzureQueueStorageImpl;
import com.azure.storage.queue.implementation.models.MessageIdUpdateHeaders;
import com.azure.storage.queue.implementation.models.MessageIdsUpdateResponse;
import com.azure.storage.queue.implementation.models.QueueGetPropertiesHeaders;
import com.azure.storage.queue.implementation.models.QueueMessage;
import com.azure.storage.queue.implementation.models.QueuesGetPropertiesResponse;
import com.azure.storage.queue.implementation.util.QueueSasImplUtil;
import com.azure.storage.queue.models.PeekedMessageItem;
import com.azure.storage.queue.models.QueueMessageItem;
import com.azure.storage.queue.models.QueueProperties;
import com.azure.storage.queue.models.QueueSignedIdentifier;
import com.azure.storage.queue.models.QueueStorageException;
import com.azure.storage.queue.models.SendMessageResult;
import com.azure.storage.queue.models.UpdateMessageResult;
import com.azure.storage.queue.sas.QueueServiceSasSignatureValues;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Spliterators;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static com.azure.core.util.FluxUtil.monoError;
import static com.azure.core.util.FluxUtil.pagedFluxError;
import static com.azure.core.util.FluxUtil.withContext;

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
 * @see StorageSharedKeyCredential
 */
@ServiceClient(builder = QueueClientBuilder.class, isAsync = true)
public final class QueueAsyncClient {

    private final ClientLogger logger = new ClientLogger(QueueAsyncClient.class);
    private final AzureQueueStorageImpl client;
    private final String queueName;
    private final String accountName;
    private final QueueServiceVersion serviceVersion;

    /**
     * Creates a QueueAsyncClient that sends requests to the storage queue service at {@link #getQueueUrl() endpoint}.
     * Each service call goes through the {@link HttpPipeline pipeline}.
     *
     * @param client Client that interacts with the service interfaces
     * @param queueName Name of the queue
     */
    QueueAsyncClient(AzureQueueStorageImpl client, String queueName, String accountName,
        QueueServiceVersion serviceVersion) {
        Objects.requireNonNull(queueName, "'queueName' cannot be null.");
        this.queueName = queueName;
        this.client = client;
        this.accountName = accountName;
        this.serviceVersion = serviceVersion;
    }

    /**
     * @return the URL of the storage queue
     */
    public String getQueueUrl() {
        return String.format("%s/%s", client.getUrl(), queueName);
    }

    /**
     * Gets the service version the client is using.
     *
     * @return the service version the client is using.
     */
    public QueueServiceVersion getServiceVersion() {
        return serviceVersion;
    }

    /**
     * Gets the {@link HttpPipeline} powering this client.
     *
     * @return The pipeline.
     */
    public HttpPipeline getHttpPipeline() {
        return client.getHttpPipeline();
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
     * @throws QueueStorageException If a queue with the same name already exists in the queue service.
     */
    public Mono<Void> create() {
        try {
            return createWithResponse(null).flatMap(FluxUtil::toMono);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
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
     * @throws QueueStorageException If a queue with the same name and different metadata already exists in the queue
     * service.
     */
    public Mono<Response<Void>> createWithResponse(Map<String, String> metadata) {
        try {
            return withContext(context -> createWithResponse(metadata, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<Void>> createWithResponse(Map<String, String> metadata, Context context) {
        return client.queues().createWithRestResponseAsync(queueName, null, metadata, null, context)
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
     * @throws QueueStorageException If the queue doesn't exist
     */
    public Mono<Void> delete() {
        try {
            return deleteWithResponse().flatMap(FluxUtil::toMono);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
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
     * @throws QueueStorageException If the queue doesn't exist
     */
    public Mono<Response<Void>> deleteWithResponse() {
        try {
            return withContext(this::deleteWithResponse);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<Void>> deleteWithResponse(Context context) {
        return client.queues().deleteWithRestResponseAsync(queueName, context)
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
     * @throws QueueStorageException If the queue doesn't exist
     */
    public Mono<QueueProperties> getProperties() {
        try {
            return getPropertiesWithResponse().flatMap(FluxUtil::toMono);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
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
     * @throws QueueStorageException If the queue doesn't exist
     */
    public Mono<Response<QueueProperties>> getPropertiesWithResponse() {
        try {
            return withContext(this::getPropertiesWithResponse);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<QueueProperties>> getPropertiesWithResponse(Context context) {
        return client.queues().getPropertiesWithRestResponseAsync(queueName, context)
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
     * @throws QueueStorageException If the queue doesn't exist
     */
    public Mono<Void> setMetadata(Map<String, String> metadata) {
        try {
            return setMetadataWithResponse(metadata).flatMap(FluxUtil::toMono);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
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
     * @throws QueueStorageException If the queue doesn't exist
     */
    public Mono<Response<Void>> setMetadataWithResponse(Map<String, String> metadata) {
        try {
            return withContext(context -> setMetadataWithResponse(metadata, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<Void>> setMetadataWithResponse(Map<String, String> metadata, Context context) {
        return client.queues()
            .setMetadataWithRestResponseAsync(queueName, null, metadata, null, context)
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
     * @throws QueueStorageException If the queue doesn't exist
     */
    public PagedFlux<QueueSignedIdentifier> getAccessPolicy() {
        try {
            Function<String, Mono<PagedResponse<QueueSignedIdentifier>>> retriever =
                marker -> this.client.queues()
                    .getAccessPolicyWithRestResponseAsync(queueName, Context.NONE)
                    .map(response -> new PagedResponseBase<>(response.getRequest(),
                        response.getStatusCode(),
                        response.getHeaders(),
                        response.getValue(),
                        null,
                        response.getDeserializedHeaders()));

            return new PagedFlux<>(() -> retriever.apply(null), retriever);
        } catch (RuntimeException ex) {
            return pagedFluxError(logger, ex);
        }
    }

    /**
     * Sets stored access policies on the queue.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Set a read only stored access policy</p>
     *
     * {@codesnippet com.azure.storage.queue.QueueAsyncClient.setAccessPolicy#Iterable}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/set-queue-acl">Azure Docs</a>.</p>
     *
     * @param permissions Access policies to set on the queue
     * @return An empty response
     * @throws QueueStorageException If the queue doesn't exist, a stored access policy doesn't have all fields filled
     * out, or the queue will have more than five policies.
     */
    public Mono<Void> setAccessPolicy(Iterable<QueueSignedIdentifier> permissions) {
        try {
            return setAccessPolicyWithResponse(permissions).flatMap(FluxUtil::toMono);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Sets stored access policies on the queue.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Set a read only stored access policy</p>
     *
     * {@codesnippet com.azure.storage.queue.QueueAsyncClient.setAccessPolicyWithResponse#Iterable}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/set-queue-acl">Azure Docs</a>.</p>
     *
     * @param permissions Access policies to set on the queue
     * @return A response that only contains headers and response status code
     * @throws QueueStorageException If the queue doesn't exist, a stored access policy doesn't have all fields filled
     * out, or the queue will have more than five policies.
     */
    public Mono<Response<Void>> setAccessPolicyWithResponse(Iterable<QueueSignedIdentifier> permissions) {
        try {
            return withContext(context -> setAccessPolicyWithResponse(permissions, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<Void>> setAccessPolicyWithResponse(Iterable<QueueSignedIdentifier> permissions, Context context) {
        /*
        We truncate to seconds because the service only supports nanoseconds or seconds, but doing an
        OffsetDateTime.now will only give back milliseconds (more precise fields are zeroed and not serialized). This
        allows for proper serialization with no real detriment to users as sub-second precision on active time for
        signed identifiers is not really necessary.
         */
        if (permissions != null) {
            for (QueueSignedIdentifier permission : permissions) {
                if (permission.getAccessPolicy() != null && permission.getAccessPolicy().getStartsOn() != null) {
                    permission.getAccessPolicy().setStartsOn(
                        permission.getAccessPolicy().getStartsOn().truncatedTo(ChronoUnit.SECONDS));
                }
                if (permission.getAccessPolicy() != null && permission.getAccessPolicy().getExpiresOn() != null) {
                    permission.getAccessPolicy().setExpiresOn(
                        permission.getAccessPolicy().getExpiresOn().truncatedTo(ChronoUnit.SECONDS));
                }
            }
        }
        List<QueueSignedIdentifier> permissionsList = StreamSupport.stream(
            permissions != null ? permissions.spliterator() : Spliterators.emptySpliterator(), false)
            .collect(Collectors.toList());

        return client.queues()
            .setAccessPolicyWithRestResponseAsync(queueName, permissionsList, null, null, context)
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
     * @throws QueueStorageException If the queue doesn't exist
     */
    public Mono<Void> clearMessages() {
        try {
            return clearMessagesWithResponse().flatMap(FluxUtil::toMono);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
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
     * @throws QueueStorageException If the queue doesn't exist
     */
    public Mono<Response<Void>> clearMessagesWithResponse() {
        try {
            return withContext(this::clearMessagesWithResponse);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<Void>> clearMessagesWithResponse(Context context) {
        return client.messages().clearWithRestResponseAsync(queueName, context)
            .map(response -> new SimpleResponse<>(response, null));
    }

    /**
     * Enqueues a message that has a time-to-live of 7 days and is instantly visible.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Enqueue a message of "Hello, Azure"</p>
     *
     * {@codesnippet com.azure.storage.queue.queueAsyncClient.sendMessage#string}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/put-message">Azure Docs</a>.</p>
     *
     * @param messageText Message text
     * @return A {@link SendMessageResult} value that contains the {@link SendMessageResult#getMessageId() messageId}
     * and {@link SendMessageResult#getPopReceipt() popReceipt} that are used to interact with the message
     * and other metadata about the enqueued message.
     * @throws QueueStorageException If the queue doesn't exist
     */
    public Mono<SendMessageResult> sendMessage(String messageText) {
        try {
            return sendMessageWithResponse(messageText, null, null).flatMap(FluxUtil::toMono);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Enqueues a message with a given time-to-live and a timeout period where the message is invisible in the queue.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Add a message of "Hello, Azure" that has a timeout of 5 seconds</p>
     *
     * {@codesnippet com.azure.storage.queue.queueAsyncClient.sendMessageWithResponse#string-duration-duration}
     *
     * <p>Add a message of "Goodbye, Azure" that has a time to live of 5 seconds</p>
     *
     * {@codesnippet com.azure.storage.queue.QueueAsyncClient.sendMessageWithResponse-liveTime#String-Duration-Duration}
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
     * @return A {@link SendMessageResult} value that contains the {@link SendMessageResult#getMessageId() messageId}
     * and {@link SendMessageResult#getPopReceipt() popReceipt} that are used to interact with the message
     * and other metadata about the enqueued message.
     * @throws QueueStorageException If the queue doesn't exist or the {@code visibilityTimeout} or {@code timeToLive}
     * are outside of the allowed limits.
     */
    public Mono<Response<SendMessageResult>> sendMessageWithResponse(String messageText, Duration visibilityTimeout,
                                                                   Duration timeToLive) {
        try {
            return withContext(
                context -> sendMessageWithResponse(messageText, visibilityTimeout, timeToLive, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<SendMessageResult>> sendMessageWithResponse(String messageText, Duration visibilityTimeout,
                                                              Duration timeToLive, Context context) {
        Integer visibilityTimeoutInSeconds = (visibilityTimeout == null) ? null : (int) visibilityTimeout.getSeconds();
        Integer timeToLiveInSeconds = (timeToLive == null) ? null : (int) timeToLive.getSeconds();
        QueueMessage message = new QueueMessage().setMessageText(messageText);

        return client.messages()
            .enqueueWithRestResponseAsync(queueName, message, visibilityTimeoutInSeconds, timeToLiveInSeconds,
                null, null, context)
            .map(response -> new SimpleResponse<>(response, response.getValue().get(0)));
    }

    /**
     * Retrieves the first message in the queue and hides it from other operations for 30 seconds.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Dequeue a message</p>
     *
     * {@codesnippet com.azure.storage.queue.queueAsyncClient.receiveMessage}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/get-messages">Azure Docs</a>.</p>
     *
     * @return The first {@link QueueMessageItem} in the queue, it contains {@link QueueMessageItem#getMessageId()
     * messageId} and {@link QueueMessageItem#getPopReceipt() popReceipt} used to interact with the message,
     * additionally it contains other metadata about the message.
     * @throws QueueStorageException If the queue doesn't exist
     */
    public Mono<QueueMessageItem> receiveMessage() {
        try {
            return receiveMessagesWithOptionalTimeout(1, null, null, Context.NONE)
                .single();
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Retrieves up to the maximum number of messages from the queue and hides them from other operations for 30
     * seconds.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Dequeue up to 5 messages</p>
     *
     * {@codesnippet com.azure.storage.queue.queueAsyncClient.receiveMessages#integer}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/get-messages">Azure Docs</a>.</p>
     *
     * @param maxMessages Optional. Maximum number of messages to get, if there are less messages exist in the queue
     * than requested all the messages will be returned. If left empty only 1 message will be retrieved, the allowed
     * range is 1 to 32 messages.
     * @return Up to {@code maxMessages} {@link QueueMessageItem ReceiveMessageItem} from the queue.
     * Each DequeuedMessage contains {@link QueueMessageItem#getMessageId() messageId} and
     * {@link QueueMessageItem#getPopReceipt() popReceipt} used to interact with the message and
     * other metadata about the message.
     * @throws QueueStorageException If the queue doesn't exist or {@code maxMessages} is outside of the allowed bounds
     */
    public PagedFlux<QueueMessageItem> receiveMessages(Integer maxMessages) {
        try {
            return receiveMessagesWithOptionalTimeout(maxMessages, null, null, Context.NONE);
        } catch (RuntimeException ex) {
            return pagedFluxError(logger, ex);
        }
    }

    /**
     * Retrieves up to the maximum number of messages from the queue and hides them from other operations for the
     * timeout period.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Dequeue up to 5 messages and give them a 60 second timeout period</p>
     *
     * {@codesnippet com.azure.storage.queue.queueAsyncClient.receiveMessages#integer-duration}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/get-messages">Azure Docs</a>.</p>
     *
     * @param maxMessages Optional. Maximum number of messages to get, if there are less messages exist in the queue
     * than requested all the messages will be returned. If left empty only 1 message will be retrieved, the allowed
     * range is 1 to 32 messages.
     * @param visibilityTimeout Optional. The timeout period for how long the message is invisible in the queue. If left
     * empty the dequeued messages will be invisible for 30 seconds. The timeout must be between 1 second and 7 days.
     * @return Up to {@code maxMessages} {@link QueueMessageItem DequeuedMessages} from the queue. Each DeqeuedMessage
     * contains {@link QueueMessageItem#getMessageId() messageId} and
     * {@link QueueMessageItem#getPopReceipt() popReceipt}
     * used to interact with the message and other metadata about the message.
     * @throws QueueStorageException If the queue doesn't exist or {@code maxMessages} or {@code visibilityTimeout} is
     * outside of the allowed bounds
     */
    public PagedFlux<QueueMessageItem> receiveMessages(Integer maxMessages, Duration visibilityTimeout) {
        try {
            return receiveMessagesWithOptionalTimeout(maxMessages, visibilityTimeout, null, Context.NONE);
        } catch (RuntimeException ex) {
            return pagedFluxError(logger, ex);
        }
    }

    PagedFlux<QueueMessageItem> receiveMessagesWithOptionalTimeout(Integer maxMessages, Duration visibilityTimeout,
        Duration timeout, Context context) {
        Integer visibilityTimeoutInSeconds = (visibilityTimeout == null) ? null : (int) visibilityTimeout.getSeconds();
        Function<String, Mono<PagedResponse<QueueMessageItem>>> retriever =
            marker -> StorageImplUtils.applyOptionalTimeout(this.client.messages()
                .dequeueWithRestResponseAsync(queueName, maxMessages, visibilityTimeoutInSeconds,
                    null, null, context), timeout)
                .map(response -> new PagedResponseBase<>(response.getRequest(),
                    response.getStatusCode(),
                    response.getHeaders(),
                    response.getValue(),
                    null,
                    response.getDeserializedHeaders()));

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
     * {@codesnippet com.azure.storage.queue.queueAsyncClient.peekMessage}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/peek-messages">Azure Docs</a>.</p>
     *
     * @return A {@link PeekedMessageItem} that contains metadata about the message.
     */
    public Mono<PeekedMessageItem> peekMessage() {
        try {
            return peekMessages(null).single();
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
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
     * @return Up to {@code maxMessages} {@link PeekedMessageItem PeekedMessages} from the queue. Each PeekedMessage
     * contains metadata about the message.
     * @throws QueueStorageException If the queue doesn't exist or {@code maxMessages} is outside of the allowed bounds
     */
    public PagedFlux<PeekedMessageItem> peekMessages(Integer maxMessages) {
        try {
            return peekMessagesWithOptionalTimeout(maxMessages, null, Context.NONE);
        } catch (RuntimeException ex) {
            return pagedFluxError(logger, ex);
        }
    }

    PagedFlux<PeekedMessageItem> peekMessagesWithOptionalTimeout(Integer maxMessages, Duration timeout,
        Context context) {
        Function<String, Mono<PagedResponse<PeekedMessageItem>>> retriever =
            marker -> StorageImplUtils.applyOptionalTimeout(this.client.messages()
                .peekWithRestResponseAsync(queueName, maxMessages, null, null, context), timeout)
                .map(response -> new PagedResponseBase<>(response.getRequest(),
                    response.getStatusCode(),
                    response.getHeaders(),
                    response.getValue(),
                    null,
                    response.getDeserializedHeaders()));

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
     * @param messageId Id of the message to update
     * @param popReceipt Unique identifier that must match for the message to be updated
     * @param messageText Updated value for the message
     * @param visibilityTimeout The timeout period for how long the message is invisible in the queue in seconds. The
     * timeout period must be between 1 second and 7 days.
     * @return A {@link UpdateMessageResult} that contains the new
     * {@link UpdateMessageResult#getPopReceipt() popReceipt} to interact with the message,
     * additionally contains the updated metadata about the message.
     * @throws QueueStorageException If the queue or messageId don't exist, the popReceipt doesn't match on the message,
     * or the {@code visibilityTimeout} is outside the allowed bounds
     */
    public Mono<UpdateMessageResult> updateMessage(String messageId, String popReceipt, String messageText,
        Duration visibilityTimeout) {
        try {
            return updateMessageWithResponse(messageId, popReceipt, messageText, visibilityTimeout)
                .flatMap(FluxUtil::toMono);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
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
     * @param messageId Id of the message to update
     * @param popReceipt Unique identifier that must match for the message to be updated
     * @param messageText Updated value for the message
     * @param visibilityTimeout The timeout period for how long the message is invisible in the queue in seconds. The
     * timeout period must be between 1 second and 7 days.
     * @return A {@link UpdateMessageResult} that contains the new
     * {@link UpdateMessageResult#getPopReceipt() popReceipt} to interact with the message,
     * additionally contains the updated metadata about the message.
     * @throws QueueStorageException If the queue or messageId don't exist, the popReceipt doesn't match on the message,
     * or the {@code visibilityTimeout} is outside the allowed bounds
     */
    public Mono<Response<UpdateMessageResult>> updateMessageWithResponse(String messageId, String popReceipt,
            String messageText, Duration visibilityTimeout) {
        try {
            return withContext(context ->
                updateMessageWithResponse(messageId, popReceipt, messageText, visibilityTimeout, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<UpdateMessageResult>> updateMessageWithResponse(String messageId, String popReceipt,
        String messageText, Duration visibilityTimeout, Context context) {
        QueueMessage message = new QueueMessage().setMessageText(messageText);
        return client.messageIds().updateWithRestResponseAsync(queueName, messageId, message, popReceipt,
                (int) visibilityTimeout.getSeconds(), context)
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
     * @throws QueueStorageException If the queue or messageId don't exist or the popReceipt doesn't match on the
     * message.
     */
    public Mono<Void> deleteMessage(String messageId, String popReceipt) {
        try {
            return deleteMessageWithResponse(messageId, popReceipt).flatMap(FluxUtil::toMono);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
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
     * @throws QueueStorageException If the queue or messageId don't exist or the popReceipt doesn't match on the
     * message.
     */
    public Mono<Response<Void>> deleteMessageWithResponse(String messageId, String popReceipt) {
        try {
            return withContext(context -> deleteMessageWithResponse(messageId, popReceipt, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<Void>> deleteMessageWithResponse(String messageId, String popReceipt, Context context) {
        return client.messageIds().deleteWithRestResponseAsync(queueName, messageId, popReceipt, context)
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

    /**
     * Generates a service sas for the queue using the specified {@link QueueServiceSasSignatureValues}
     * Note : The client must be authenticated via {@link StorageSharedKeyCredential}
     * <p>See {@link QueueServiceSasSignatureValues} for more information on how to construct a service SAS.</p>
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.queue.QueueAsyncClient.generateSas#QueueServiceSasSignatureValues}
     *
     * @param queueServiceSasSignatureValues {@link QueueServiceSasSignatureValues}
     *
     * @return A {@code String} representing all SAS query parameters.
     */
    public String generateSas(QueueServiceSasSignatureValues queueServiceSasSignatureValues) {
        return new QueueSasImplUtil(queueServiceSasSignatureValues, getQueueName())
            .generateSas(SasImplUtils.extractSharedKeyCredential(getHttpPipeline()));
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
    private Response<UpdateMessageResult> getUpdatedMessageResponse(MessageIdsUpdateResponse response) {
        MessageIdUpdateHeaders headers = response.getDeserializedHeaders();
        UpdateMessageResult updateMessageResult = new UpdateMessageResult(headers.getPopReceipt(),
            headers.getTimeNextVisible());
        return new SimpleResponse<>(response, updateMessageResult);
    }
}
