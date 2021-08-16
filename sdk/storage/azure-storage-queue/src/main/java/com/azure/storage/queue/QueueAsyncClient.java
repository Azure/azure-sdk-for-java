// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.storage.queue;

import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedResponse;
import com.azure.core.http.rest.PagedResponseBase;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Context;
import com.azure.core.util.FluxUtil;
import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.common.StorageSharedKeyCredential;
import com.azure.storage.common.implementation.SasImplUtils;
import com.azure.storage.common.implementation.StorageImplUtils;
import com.azure.storage.queue.implementation.AzureQueueStorageImpl;
import com.azure.storage.queue.implementation.models.MessageIdsUpdateHeaders;
import com.azure.storage.queue.implementation.models.MessageIdsUpdateResponse;
import com.azure.storage.queue.implementation.models.MessagesDequeueHeaders;
import com.azure.storage.queue.implementation.models.MessagesDequeueResponse;
import com.azure.storage.queue.implementation.models.MessagesPeekHeaders;
import com.azure.storage.queue.implementation.models.MessagesPeekResponse;
import com.azure.storage.queue.implementation.models.PeekedMessageItemInternal;
import com.azure.storage.queue.implementation.models.QueueMessage;
import com.azure.storage.queue.implementation.models.QueueMessageItemInternal;
import com.azure.storage.queue.implementation.models.QueuesGetPropertiesHeaders;
import com.azure.storage.queue.implementation.models.QueuesGetPropertiesResponse;
import com.azure.storage.queue.implementation.util.QueueSasImplUtil;
import com.azure.storage.queue.models.PeekedMessageItem;
import com.azure.storage.queue.models.QueueMessageDecodingError;
import com.azure.storage.queue.models.QueueMessageItem;
import com.azure.storage.queue.models.QueueProperties;
import com.azure.storage.queue.models.QueueSignedIdentifier;
import com.azure.storage.queue.models.QueueStorageException;
import com.azure.storage.queue.models.SendMessageResult;
import com.azure.storage.queue.models.UpdateMessageResult;
import com.azure.storage.queue.sas.QueueServiceSasSignatureValues;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Spliterators;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static com.azure.core.util.FluxUtil.monoError;
import static com.azure.core.util.FluxUtil.pagedFluxError;
import static com.azure.core.util.FluxUtil.withContext;
import static com.azure.core.util.tracing.Tracer.AZ_TRACING_NAMESPACE_KEY;
import static com.azure.storage.common.Utility.STORAGE_TRACING_NAMESPACE_VALUE;


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
    private final QueueMessageEncoding messageEncoding;
    private final Function<QueueMessageDecodingError, Mono<Void>> processMessageDecodingErrorAsyncHandler;
    private final Consumer<QueueMessageDecodingError> processMessageDecodingErrorHandler;

    /**
     * Creates a QueueAsyncClient that sends requests to the storage queue service at {@link #getQueueUrl() endpoint}.
     * Each service call goes through the {@link HttpPipeline pipeline}.
     *
     * @param client Client that interacts with the service interfaces
     * @param queueName Name of the queue
     */
    QueueAsyncClient(AzureQueueStorageImpl client, String queueName, String accountName,
        QueueServiceVersion serviceVersion, QueueMessageEncoding messageEncoding,
        Function<QueueMessageDecodingError, Mono<Void>> processMessageDecodingErrorAsyncHandler,
        Consumer<QueueMessageDecodingError> processMessageDecodingErrorHandler) {
        Objects.requireNonNull(queueName, "'queueName' cannot be null.");
        this.queueName = queueName;
        this.client = client;
        this.accountName = accountName;
        this.serviceVersion = serviceVersion;
        this.messageEncoding = messageEncoding;
        this.processMessageDecodingErrorAsyncHandler = processMessageDecodingErrorAsyncHandler;
        this.processMessageDecodingErrorHandler = processMessageDecodingErrorHandler;
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
     * Gets the message encoding the client is using.
     *
     * @return the message encoding the client is using.
     */
    public QueueMessageEncoding getMessageEncoding() {
        return messageEncoding;
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
     * <a href="https://docs.microsoft.com/rest/api/storageservices/create-queue4">Azure Docs</a>.</p>
     *
     * @return An empty response
     * @throws QueueStorageException If a queue with the same name already exists in the queue service.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
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
     * <a href="https://docs.microsoft.com/rest/api/storageservices/create-queue4">Azure Docs</a>.</p>
     *
     * @param metadata Metadata to associate with the queue. If there is leading or trailing whitespace in any
     * metadata key or value, it must be removed or encoded.
     * @return A response that only contains headers and response status code
     * @throws QueueStorageException If a queue with the same name and different metadata already exists in the queue
     * service.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> createWithResponse(Map<String, String> metadata) {
        try {
            return withContext(context -> createWithResponse(metadata, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<Void>> createWithResponse(Map<String, String> metadata, Context context) {
        context = context == null ? Context.NONE : context;
        return client.getQueues().createWithResponseAsync(queueName, null, metadata, null,
            context.addData(AZ_TRACING_NAMESPACE_KEY, STORAGE_TRACING_NAMESPACE_VALUE))
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
     * <a href="https://docs.microsoft.com/rest/api/storageservices/delete-queue3">Azure Docs</a>.</p>
     *
     * @return An empty response
     * @throws QueueStorageException If the queue doesn't exist
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
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
     * <a href="https://docs.microsoft.com/rest/api/storageservices/delete-queue3">Azure Docs</a>.</p>
     *
     * @return A response that only contains headers and response status code
     * @throws QueueStorageException If the queue doesn't exist
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> deleteWithResponse() {
        try {
            return withContext(this::deleteWithResponse);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<Void>> deleteWithResponse(Context context) {
        context = context == null ? Context.NONE : context;
        return client.getQueues().deleteWithResponseAsync(queueName, null, null,
            context.addData(AZ_TRACING_NAMESPACE_KEY, STORAGE_TRACING_NAMESPACE_VALUE))
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
     * <a href="https://docs.microsoft.com/rest/api/storageservices/get-queue-metadata">Azure Docs</a>.</p>
     *
     * @return A response containing a {@link QueueProperties} value which contains the metadata and approximate
     * messages count of the queue.
     * @throws QueueStorageException If the queue doesn't exist
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
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
     * <a href="https://docs.microsoft.com/rest/api/storageservices/get-queue-metadata">Azure Docs</a>.</p>
     *
     * @return A response containing a {@link QueueProperties} value which contains the metadata and approximate
     * messages count of the queue.
     * @throws QueueStorageException If the queue doesn't exist
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<QueueProperties>> getPropertiesWithResponse() {
        try {
            return withContext(this::getPropertiesWithResponse);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<QueueProperties>> getPropertiesWithResponse(Context context) {
        context = context == null ? Context.NONE : context;
        return client.getQueues().getPropertiesWithResponseAsync(queueName, null, null,
            context.addData(AZ_TRACING_NAMESPACE_KEY, STORAGE_TRACING_NAMESPACE_VALUE))
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
     * <a href="https://docs.microsoft.com/rest/api/storageservices/set-queue-metadata">Azure Docs</a>.</p>
     *
     * @param metadata Metadata to set on the queue
     * @return A response that only contains headers and response status code
     * @throws QueueStorageException If the queue doesn't exist
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
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
     * <a href="https://docs.microsoft.com/rest/api/storageservices/set-queue-metadata">Azure Docs</a>.</p>
     *
     * @param metadata Metadata to set on the queue
     * @return A response that only contains headers and response status code
     * @throws QueueStorageException If the queue doesn't exist
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> setMetadataWithResponse(Map<String, String> metadata) {
        try {
            return withContext(context -> setMetadataWithResponse(metadata, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<Void>> setMetadataWithResponse(Map<String, String> metadata, Context context) {
        context = context == null ? Context.NONE : context;
        return client.getQueues()
            .setMetadataWithResponseAsync(queueName, null, metadata, null,
                context.addData(AZ_TRACING_NAMESPACE_KEY, STORAGE_TRACING_NAMESPACE_VALUE))
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
     * <a href="https://docs.microsoft.com/rest/api/storageservices/get-queue-acl">Azure Docs</a>.</p>
     *
     * @return The stored access policies specified on the queue.
     * @throws QueueStorageException If the queue doesn't exist
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<QueueSignedIdentifier> getAccessPolicy() {
        try {
            Function<String, Mono<PagedResponse<QueueSignedIdentifier>>> retriever =
                marker -> this.client.getQueues()
                    .getAccessPolicyWithResponseAsync(queueName, null, null, Context.NONE)
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
     * <a href="https://docs.microsoft.com/rest/api/storageservices/set-queue-acl">Azure Docs</a>.</p>
     *
     * @param permissions Access policies to set on the queue
     * @return An empty response
     * @throws QueueStorageException If the queue doesn't exist, a stored access policy doesn't have all fields filled
     * out, or the queue will have more than five policies.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
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
     * <a href="https://docs.microsoft.com/rest/api/storageservices/set-queue-acl">Azure Docs</a>.</p>
     *
     * @param permissions Access policies to set on the queue
     * @return A response that only contains headers and response status code
     * @throws QueueStorageException If the queue doesn't exist, a stored access policy doesn't have all fields filled
     * out, or the queue will have more than five policies.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> setAccessPolicyWithResponse(Iterable<QueueSignedIdentifier> permissions) {
        try {
            return withContext(context -> setAccessPolicyWithResponse(permissions, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<Void>> setAccessPolicyWithResponse(Iterable<QueueSignedIdentifier> permissions, Context context) {
        context = context == null ? Context.NONE : context;
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

        return client.getQueues()
            .setAccessPolicyWithResponseAsync(queueName, null, null, permissionsList,
                context.addData(AZ_TRACING_NAMESPACE_KEY, STORAGE_TRACING_NAMESPACE_VALUE))
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
     * <a href="https://docs.microsoft.com/rest/api/storageservices/clear-messages">Azure Docs</a>.</p>
     *
     * @return An empty response
     * @throws QueueStorageException If the queue doesn't exist
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
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
     * <a href="https://docs.microsoft.com/rest/api/storageservices/clear-messages">Azure Docs</a>.</p>
     *
     * @return A response that only contains headers and response status code
     * @throws QueueStorageException If the queue doesn't exist
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> clearMessagesWithResponse() {
        try {
            return withContext(this::clearMessagesWithResponse);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<Void>> clearMessagesWithResponse(Context context) {
        context = context == null ? Context.NONE : context;
        return client.getMessages().clearWithResponseAsync(queueName, null, null,
            context.addData(AZ_TRACING_NAMESPACE_KEY, STORAGE_TRACING_NAMESPACE_VALUE))
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
     * <a href="https://docs.microsoft.com/rest/api/storageservices/put-message">Azure Docs</a>.</p>
     *
     * @param messageText Message text
     * @return A {@link SendMessageResult} value that contains the {@link SendMessageResult#getMessageId() messageId}
     * and {@link SendMessageResult#getPopReceipt() popReceipt} that are used to interact with the message
     * and other metadata about the enqueued message.
     * @throws QueueStorageException If the queue doesn't exist
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<SendMessageResult> sendMessage(String messageText) {
        try {
            return sendMessageWithResponse(messageText, null, null).flatMap(FluxUtil::toMono);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Enqueues a message that has a time-to-live of 7 days and is instantly visible.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Enqueue a message of "Hello, Azure"</p>
     *
     * {@codesnippet com.azure.storage.queue.queueAsyncClient.sendMessage#BinaryData}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/put-message">Azure Docs</a>.</p>
     *
     * @param message Message content
     * @return A {@link SendMessageResult} value that contains the {@link SendMessageResult#getMessageId() messageId}
     * and {@link SendMessageResult#getPopReceipt() popReceipt} that are used to interact with the message
     * and other metadata about the enqueued message.
     * @throws QueueStorageException If the queue doesn't exist
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<SendMessageResult> sendMessage(BinaryData message) {
        return sendMessageWithResponse(message, null, null).flatMap(FluxUtil::toMono);
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
     * <a href="https://docs.microsoft.com/rest/api/storageservices/put-message">Azure Docs</a>.</p>
     *
     * @param messageText Message text
     * @param visibilityTimeout Optional. The timeout period for how long the message is invisible in the queue. If
     * unset the value will default to 0 and the message will be instantly visible. The timeout must be between 0
     * seconds and 7 days.
     * @param timeToLive Optional. How long the message will stay alive in the queue. If unset the value will default to
     * 7 days, if {@code Duration.ofSeconds(-1)} is passed the message will not expire.
     * The time to live must be {@code Duration.ofSeconds(-1)} or any positive number of seconds.
     * @return A {@link SendMessageResult} value that contains the {@link SendMessageResult#getMessageId() messageId}
     * and {@link SendMessageResult#getPopReceipt() popReceipt} that are used to interact with the message
     * and other metadata about the enqueued message.
     * @throws QueueStorageException If the queue doesn't exist or the {@code visibilityTimeout} or {@code timeToLive}
     * are outside of the allowed limits.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<SendMessageResult>> sendMessageWithResponse(String messageText, Duration visibilityTimeout,
                                                                   Duration timeToLive) {
        try {
            return withContext(context ->
                sendMessageWithResponse(BinaryData.fromString(messageText), visibilityTimeout, timeToLive, context));
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
     * {@codesnippet com.azure.storage.queue.queueAsyncClient.sendMessageWithResponse#BinaryData-duration-duration}
     *
     * <p>Add a message of "Goodbye, Azure" that has a time to live of 5 seconds</p>
     *
     * {@codesnippet com.azure.storage.queue.QueueAsyncClient.sendMessageWithResponse-liveTime#BinaryData-Duration-Duration}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/put-message">Azure Docs</a>.</p>
     *
     * @param message Message content.
     * @param visibilityTimeout Optional. The timeout period for how long the message is invisible in the queue. If
     * unset the value will default to 0 and the message will be instantly visible. The timeout must be between 0
     * seconds and 7 days.
     * @param timeToLive Optional. How long the message will stay alive in the queue. If unset the value will default to
     * 7 days, if {@code Duration.ofSeconds(-1)} is passed the message will not expire.
     * The time to live must be {@code Duration.ofSeconds(-1)} or any positive number of seconds.
     * @return A {@link SendMessageResult} value that contains the {@link SendMessageResult#getMessageId() messageId}
     * and {@link SendMessageResult#getPopReceipt() popReceipt} that are used to interact with the message
     * and other metadata about the enqueued message.
     * @throws QueueStorageException If the queue doesn't exist or the {@code visibilityTimeout} or {@code timeToLive}
     * are outside of the allowed limits.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<SendMessageResult>> sendMessageWithResponse(BinaryData message, Duration visibilityTimeout,
                                                                     Duration timeToLive) {
        try {
            return withContext(context -> sendMessageWithResponse(message, visibilityTimeout, timeToLive, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<SendMessageResult>> sendMessageWithResponse(BinaryData message, Duration visibilityTimeout,
                                                              Duration timeToLive, Context context) {
        Integer visibilityTimeoutInSeconds = (visibilityTimeout == null) ? null : (int) visibilityTimeout.getSeconds();
        Integer timeToLiveInSeconds = (timeToLive == null) ? null : (int) timeToLive.getSeconds();
        context = context == null ? Context.NONE : context;
        Context finalContext = context.addData(AZ_TRACING_NAMESPACE_KEY, STORAGE_TRACING_NAMESPACE_VALUE);
        return encodeMessage(message)
            .flatMap(messageText -> {
                QueueMessage queueMessage = new QueueMessage().setMessageText(messageText);
                return client.getMessages()
                    .enqueueWithResponseAsync(queueName, queueMessage, visibilityTimeoutInSeconds, timeToLiveInSeconds,
                        null, null, finalContext)
                    .map(response -> new SimpleResponse<>(response, response.getValue().get(0)));
            });

    }

    private Mono<String> encodeMessage(BinaryData message) {
        Objects.requireNonNull(message, "'message' cannot be null.");
        switch (messageEncoding) {
            case NONE:
                return Mono.just(message.toString());
            case BASE64:
                return Mono.just(Base64.getEncoder().encodeToString(message.toBytes()));
            default:
                return FluxUtil.monoError(
                    logger, new IllegalArgumentException("Unsupported message encoding=" + messageEncoding));
        }
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
     * <a href="https://docs.microsoft.com/rest/api/storageservices/get-messages">Azure Docs</a>.</p>
     *
     * @return The first {@link QueueMessageItem} in the queue, it contains {@link QueueMessageItem#getMessageId()
     * messageId} and {@link QueueMessageItem#getPopReceipt() popReceipt} used to interact with the message,
     * additionally it contains other metadata about the message.
     * @throws QueueStorageException If the queue doesn't exist
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<QueueMessageItem> receiveMessage() {
        try {
            return receiveMessagesWithOptionalTimeout(1, null, null, Context.NONE).singleOrEmpty();
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
     * <a href="https://docs.microsoft.com/rest/api/storageservices/get-messages">Azure Docs</a>.</p>
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
    @ServiceMethod(returns = ReturnType.COLLECTION)
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
     * <a href="https://docs.microsoft.com/rest/api/storageservices/get-messages">Azure Docs</a>.</p>
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
    @ServiceMethod(returns = ReturnType.COLLECTION)
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
            marker -> StorageImplUtils.applyOptionalTimeout(this.client.getMessages()
                .dequeueWithResponseAsync(queueName, maxMessages, visibilityTimeoutInSeconds,
                    null, null, context), timeout)
                .flatMap(this::transformMessagesDequeueResponse);

        return new PagedFlux<>(() -> retriever.apply(null), retriever);
    }

    private Mono<PagedResponseBase<MessagesDequeueHeaders, QueueMessageItem>> transformMessagesDequeueResponse(
        MessagesDequeueResponse response) {
        List<QueueMessageItemInternal> queueMessageInternalItems = response.getValue();
        if (queueMessageInternalItems == null) {
            queueMessageInternalItems = Collections.emptyList();
        }
        return Flux.fromIterable(queueMessageInternalItems)
            .flatMapSequential(queueMessageItemInternal ->
                transformQueueMessageItemInternal(queueMessageItemInternal, messageEncoding)
                .onErrorResume(IllegalArgumentException.class, e -> {
                    if (processMessageDecodingErrorAsyncHandler != null) {
                        return transformQueueMessageItemInternal(
                            queueMessageItemInternal, QueueMessageEncoding.NONE)
                            .flatMap(messageItem -> processMessageDecodingErrorAsyncHandler.apply(
                                new QueueMessageDecodingError(
                                    this, new QueueClient(this),
                                    messageItem, null, e)))
                            .then(Mono.empty());
                    } else if (processMessageDecodingErrorHandler != null) {
                        return transformQueueMessageItemInternal(
                            queueMessageItemInternal, QueueMessageEncoding.NONE)
                            .flatMap(messageItem -> {
                                try {
                                    processMessageDecodingErrorHandler.accept(
                                        new QueueMessageDecodingError(
                                            this, new QueueClient(this),
                                            messageItem, null, e));
                                    return Mono.<QueueMessageItem>empty();
                                } catch (RuntimeException re) {
                                    return FluxUtil.<QueueMessageItem>monoError(logger, re);
                                }
                            })
                            .subscribeOn(Schedulers.boundedElastic());
                    } else {
                        return FluxUtil.monoError(logger, e);
                    }
                }))
            .collectList()
            .map(queueMessageItems -> new PagedResponseBase<>(response.getRequest(),
                response.getStatusCode(),
                response.getHeaders(),
                queueMessageItems,
                null,
                response.getDeserializedHeaders()));
    }

    private Mono<QueueMessageItem> transformQueueMessageItemInternal(
        QueueMessageItemInternal queueMessageItemInternal, QueueMessageEncoding messageEncoding) {
        QueueMessageItem queueMessageItem = new QueueMessageItem()
            .setMessageId(queueMessageItemInternal.getMessageId())
            .setDequeueCount(queueMessageItemInternal.getDequeueCount())
            .setExpirationTime(queueMessageItemInternal.getExpirationTime())
            .setInsertionTime(queueMessageItemInternal.getInsertionTime())
            .setPopReceipt(queueMessageItemInternal.getPopReceipt())
            .setTimeNextVisible(queueMessageItemInternal.getTimeNextVisible());
        return decodeMessageBody(queueMessageItemInternal.getMessageText(), messageEncoding)
            .map(queueMessageItem::setBody)
            .switchIfEmpty(Mono.just(queueMessageItem));
    }

    private Mono<BinaryData> decodeMessageBody(String messageText, QueueMessageEncoding messageEncoding) {
        if (messageText == null) {
            return Mono.empty();
        }

        switch (messageEncoding) {
            case NONE:
                return Mono.just(BinaryData.fromString(messageText));
            case BASE64:
                try {
                    return Mono.just(BinaryData.fromBytes(Base64.getDecoder().decode(messageText)));
                } catch (IllegalArgumentException e) {
                    return FluxUtil.monoError(logger, e);
                }
            default:
                return FluxUtil.monoError(
                    logger, new IllegalArgumentException("Unsupported message encoding=" + messageEncoding));
        }
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
     * <a href="https://docs.microsoft.com/rest/api/storageservices/peek-messages">Azure Docs</a>.</p>
     *
     * @return A {@link PeekedMessageItem} that contains metadata about the message.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<PeekedMessageItem> peekMessage() {
        try {
            return peekMessages(null).singleOrEmpty();
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
     * <a href="https://docs.microsoft.com/rest/api/storageservices/peek-messages">Azure Docs</a>.</p>
     *
     * @param maxMessages Optional. Maximum number of messages to peek, if there are less messages exist in the queue
     * than requested all the messages will be peeked. If left empty only 1 message will be peeked, the allowed range is
     * 1 to 32 messages.
     * @return Up to {@code maxMessages} {@link PeekedMessageItem PeekedMessages} from the queue. Each PeekedMessage
     * contains metadata about the message.
     * @throws QueueStorageException If the queue doesn't exist or {@code maxMessages} is outside of the allowed bounds
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
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
            marker -> StorageImplUtils.applyOptionalTimeout(this.client.getMessages()
                .peekWithResponseAsync(queueName, maxMessages, null, null, context), timeout)
                .flatMap(this::transformMessagesPeekResponse);

        return new PagedFlux<>(() -> retriever.apply(null), retriever);
    }

    private Mono<PagedResponseBase<MessagesPeekHeaders, PeekedMessageItem>> transformMessagesPeekResponse(
        MessagesPeekResponse response) {
        List<PeekedMessageItemInternal> peekedMessageInternalItems = response.getValue();
        if (peekedMessageInternalItems == null) {
            peekedMessageInternalItems = Collections.emptyList();
        }
        return Flux.fromIterable(peekedMessageInternalItems)
            .flatMapSequential(peekedMessageItemInternal ->
                transformPeekedMessageItemInternal(peekedMessageItemInternal, messageEncoding)
                    .onErrorResume(IllegalArgumentException.class, e -> {
                        if (processMessageDecodingErrorAsyncHandler != null) {
                            return transformPeekedMessageItemInternal(
                                peekedMessageItemInternal, QueueMessageEncoding.NONE)
                                .flatMap(messageItem -> processMessageDecodingErrorAsyncHandler.apply(
                                    new QueueMessageDecodingError(
                                        this,  new QueueClient(this),
                                        null, messageItem, e)))
                                .then(Mono.empty());
                        } else if (processMessageDecodingErrorHandler != null) {
                            return transformPeekedMessageItemInternal(
                                peekedMessageItemInternal, QueueMessageEncoding.NONE)
                                .flatMap(messageItem -> {
                                    try {
                                        processMessageDecodingErrorHandler.accept(
                                            new QueueMessageDecodingError(
                                                this,  new QueueClient(this),
                                                null, messageItem, e));
                                        return Mono.<PeekedMessageItem>empty();
                                    } catch (RuntimeException re) {
                                        return FluxUtil.<PeekedMessageItem>monoError(logger, re);
                                    }
                                })
                                .subscribeOn(Schedulers.boundedElastic());
                        } else {
                            return FluxUtil.monoError(logger, e);
                        }
                    }))
            .collectList()
            .map(peekedMessageItems -> new PagedResponseBase<>(response.getRequest(),
                response.getStatusCode(),
                response.getHeaders(),
                peekedMessageItems,
                null,
                response.getDeserializedHeaders()));
    }

    private Mono<PeekedMessageItem> transformPeekedMessageItemInternal(
        PeekedMessageItemInternal peekedMessageItemInternal, QueueMessageEncoding messageEncoding) {
        PeekedMessageItem peekedMessageItem = new PeekedMessageItem()
            .setMessageId(peekedMessageItemInternal.getMessageId())
            .setDequeueCount(peekedMessageItemInternal.getDequeueCount())
            .setExpirationTime(peekedMessageItemInternal.getExpirationTime())
            .setInsertionTime(peekedMessageItemInternal.getInsertionTime());
        return decodeMessageBody(peekedMessageItemInternal.getMessageText(), messageEncoding)
            .map(peekedMessageItem::setBody)
            .switchIfEmpty(Mono.just(peekedMessageItem));
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
     * <a href="https://docs.microsoft.com/rest/api/storageservices/update-message">Azure Docs</a>.</p>
     *
     * @param messageId Id of the message to update
     * @param popReceipt Unique identifier that must match for the message to be updated
     * @param messageText Updated value for the message
     * @param visibilityTimeout The timeout period for how long the message is invisible in the queue in seconds. The
     * timeout period must be between 1 second and 7 days. The default value is Duration.ZERO.
     * @return A {@link UpdateMessageResult} that contains the new
     * {@link UpdateMessageResult#getPopReceipt() popReceipt} to interact with the message,
     * additionally contains the updated metadata about the message.
     * @throws QueueStorageException If the queue or messageId don't exist, the popReceipt doesn't match on the message,
     * or the {@code visibilityTimeout} is outside the allowed bounds
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
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
     * <a href="https://docs.microsoft.com/rest/api/storageservices/update-message">Azure Docs</a>.</p>
     *
     * @param messageId Id of the message to update
     * @param popReceipt Unique identifier that must match for the message to be updated
     * @param messageText Updated value for the message
     * @param visibilityTimeout The timeout period for how long the message is invisible in the queue in seconds. The
     * timeout period must be between 1 second and 7 days. The default value is Duration.ZERO.
     * @return A {@link UpdateMessageResult} that contains the new
     * {@link UpdateMessageResult#getPopReceipt() popReceipt} to interact with the message,
     * additionally contains the updated metadata about the message.
     * @throws QueueStorageException If the queue or messageId don't exist, the popReceipt doesn't match on the message,
     * or the {@code visibilityTimeout} is outside the allowed bounds
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<UpdateMessageResult>> updateMessageWithResponse(String messageId, String popReceipt,
            String messageText, Duration visibilityTimeout) {
        try {
            return withContext(context -> updateMessageWithResponse(messageId, popReceipt, messageText,
                visibilityTimeout, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<UpdateMessageResult>> updateMessageWithResponse(String messageId, String popReceipt,
        String messageText, Duration visibilityTimeout, Context context) {
        QueueMessage message = messageText == null ? null : new QueueMessage().setMessageText(messageText);
        context = context == null ? Context.NONE : context;
        visibilityTimeout = visibilityTimeout == null ? Duration.ZERO : visibilityTimeout;
        return client.getMessageIds().updateWithResponseAsync(queueName, messageId, popReceipt,
                (int) visibilityTimeout.getSeconds(), null, null, message,
            context.addData(AZ_TRACING_NAMESPACE_KEY, STORAGE_TRACING_NAMESPACE_VALUE))
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
     * <a href="https://docs.microsoft.com/rest/api/storageservices/delete-message2">Azure Docs</a>.</p>
     *
     * @param messageId Id of the message to deleted
     * @param popReceipt Unique identifier that must match for the message to be deleted
     * @return An empty response
     * @throws QueueStorageException If the queue or messageId don't exist or the popReceipt doesn't match on the
     * message.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
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
     * <a href="https://docs.microsoft.com/rest/api/storageservices/delete-message2">Azure Docs</a>.</p>
     *
     * @param messageId Id of the message to deleted
     * @param popReceipt Unique identifier that must match for the message to be deleted
     * @return A response that only contains headers and response status code
     * @throws QueueStorageException If the queue or messageId don't exist or the popReceipt doesn't match on the
     * message.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> deleteMessageWithResponse(String messageId, String popReceipt) {
        try {
            return withContext(context -> deleteMessageWithResponse(messageId, popReceipt,
                context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<Void>> deleteMessageWithResponse(String messageId, String popReceipt, Context context) {
        context = context == null ? Context.NONE : context;
        return client.getMessageIds().deleteWithResponseAsync(queueName, messageId, popReceipt, null, null,
            context.addData(AZ_TRACING_NAMESPACE_KEY, STORAGE_TRACING_NAMESPACE_VALUE))
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
     * <p>Note : The client must be authenticated via {@link StorageSharedKeyCredential}
     * <p>See {@link QueueServiceSasSignatureValues} for more information on how to construct a service SAS.</p>
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.queue.QueueAsyncClient.generateSas#QueueServiceSasSignatureValues}
     *
     * @param queueServiceSasSignatureValues {@link QueueServiceSasSignatureValues}
     *
     * @return A {@code String} representing the SAS query parameters.
     */
    public String generateSas(QueueServiceSasSignatureValues queueServiceSasSignatureValues) {
        return generateSas(queueServiceSasSignatureValues, Context.NONE);
    }

    /**
     * Generates a service sas for the queue using the specified {@link QueueServiceSasSignatureValues}
     * <p>Note : The client must be authenticated via {@link StorageSharedKeyCredential}
     * <p>See {@link QueueServiceSasSignatureValues} for more information on how to construct a service SAS.</p>
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.queue.QueueAsyncClient.generateSas#QueueServiceSasSignatureValues-Context}
     *
     * @param queueServiceSasSignatureValues {@link QueueServiceSasSignatureValues}
     * @param context Additional context that is passed through the code when generating a SAS.
     *
     * @return A {@code String} representing the SAS query parameters.
     */
    public String generateSas(QueueServiceSasSignatureValues queueServiceSasSignatureValues, Context context) {
        return new QueueSasImplUtil(queueServiceSasSignatureValues, getQueueName())
            .generateSas(SasImplUtils.extractSharedKeyCredential(getHttpPipeline()), context);
    }

    /*
     * Maps the HTTP headers returned from the service to the expected response type
     * @param response Service response
     * @return Mapped response
     */
    private Response<QueueProperties> getQueuePropertiesResponse(QueuesGetPropertiesResponse response) {
        QueuesGetPropertiesHeaders propertiesHeaders = response.getDeserializedHeaders();
        QueueProperties properties = new QueueProperties(propertiesHeaders.getXMsMeta(),
            propertiesHeaders.getXMsApproximateMessagesCount());
        return new SimpleResponse<>(response, properties);
    }

    /*
     * Maps the HTTP headers returned from the service to the expected response type
     * @param response Service response
     * @return Mapped response
     */
    private Response<UpdateMessageResult> getUpdatedMessageResponse(MessageIdsUpdateResponse response) {
        MessageIdsUpdateHeaders headers = response.getDeserializedHeaders();
        UpdateMessageResult updateMessageResult = new UpdateMessageResult(headers.getXMsPopreceipt(),
            headers.getXMsTimeNextVisible());
        return new SimpleResponse<>(response, updateMessageResult);
    }
}
