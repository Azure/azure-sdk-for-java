// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.storage.queue;

import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedResponse;
import com.azure.core.http.rest.PagedResponseBase;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.ResponseBase;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Context;
import com.azure.core.util.FluxUtil;
import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.common.StorageSharedKeyCredential;
import com.azure.storage.common.implementation.SasImplUtils;
import com.azure.storage.common.implementation.StorageImplUtils;
import com.azure.storage.queue.implementation.AzureQueueStorageImpl;
import com.azure.storage.queue.implementation.models.MessagesDequeueHeaders;
import com.azure.storage.queue.implementation.models.MessagesPeekHeaders;
import com.azure.storage.queue.implementation.models.PeekedMessageItemInternal;
import com.azure.storage.queue.implementation.models.QueueMessage;
import com.azure.storage.queue.implementation.models.QueueMessageItemInternal;
import com.azure.storage.queue.implementation.util.ModelHelper;
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


/**
 * This class provides a client that contains all the operations for interacting with a queue in Azure Storage Queue.
 * Operations allowed by the client are creating and deleting the queue, retrieving and updating metadata and access
 * policies of the queue, and enqueuing, dequeuing, peeking, updating, and deleting messages.
 *
 * <p><strong>Instantiating an Asynchronous Queue Client</strong></p>
 *
 * <!-- src_embed com.azure.storage.queue.queueAsyncClient.instantiation -->
 * <pre>
 * QueueAsyncClient client = new QueueClientBuilder&#40;&#41;
 *     .connectionString&#40;&quot;connectionstring&quot;&#41;
 *     .endpoint&#40;&quot;endpoint&quot;&#41;
 *     .buildAsyncClient&#40;&#41;;
 * </pre>
 * <!-- end com.azure.storage.queue.queueAsyncClient.instantiation -->
 *
 * <p>View {@link QueueClientBuilder this} for additional ways to construct the client.</p>
 *
 * @see QueueClientBuilder
 * @see QueueClient
 * @see StorageSharedKeyCredential
 */
@ServiceClient(builder = QueueClientBuilder.class, isAsync = true)
public final class QueueAsyncClient {

    private static final ClientLogger LOGGER = new ClientLogger(QueueAsyncClient.class);
    private final AzureQueueStorageImpl client;
    private final String queueName;
    private final String accountName;
    private final QueueServiceVersion serviceVersion;
    private final QueueMessageEncoding messageEncoding;
    private final Function<QueueMessageDecodingError, Mono<Void>> processMessageDecodingErrorAsyncHandler;
    private final Consumer<QueueMessageDecodingError> processMessageDecodingErrorHandler;
    private final QueueClient queueClient;

    /**
     * Creates a QueueAsyncClient that sends requests to the storage queue service at {@link #getQueueUrl() endpoint}.
     * Each service call goes through the {@link HttpPipeline pipeline}.
     *
     * @param client Client that interacts with the service interfaces
     * @param queueName Name of the queue
     * @param accountName Name of the account.
     * @param serviceVersion the {@link QueueServiceVersion}.
     * @param messageEncoding the {@link QueueMessageEncoding}.
     * @param processMessageDecodingErrorAsyncHandler the asynchronous handler that performs the tasks needed when a
     * message is received or peaked from the queue but cannot be decoded.
     * @param processMessageDecodingErrorHandler the synchronous handler that performs the tasks needed when a
     * message is received or peaked from the queue but cannot be decoded.
     * @param queueClient the {@link QueueClient} associated with this client.
     */
    QueueAsyncClient(AzureQueueStorageImpl client, String queueName, String accountName,
        QueueServiceVersion serviceVersion, QueueMessageEncoding messageEncoding,
        Function<QueueMessageDecodingError, Mono<Void>> processMessageDecodingErrorAsyncHandler,
        Consumer<QueueMessageDecodingError> processMessageDecodingErrorHandler, QueueClient queueClient) {
        Objects.requireNonNull(queueName, "'queueName' cannot be null.");
        this.queueName = queueName;
        this.client = client;
        this.accountName = accountName;
        this.serviceVersion = serviceVersion;
        this.messageEncoding = messageEncoding;
        this.processMessageDecodingErrorAsyncHandler = processMessageDecodingErrorAsyncHandler;
        this.processMessageDecodingErrorHandler = processMessageDecodingErrorHandler;
        this.queueClient = queueClient;
    }

    /**
     * @return the URL of the storage queue
     */
    public String getQueueUrl() {
        return client.getUrl() + "/" + queueName;
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
     * <!-- src_embed com.azure.storage.queue.queueAsyncClient.create -->
     * <pre>
     * client.create&#40;&#41;.subscribe&#40;
     *     response -&gt; &#123;
     *     &#125;,
     *     error -&gt; System.err.print&#40;error.toString&#40;&#41;&#41;,
     *     &#40;&#41; -&gt; System.out.println&#40;&quot;Complete creating the queue!&quot;&#41;
     * &#41;;
     * </pre>
     * <!-- end com.azure.storage.queue.queueAsyncClient.create -->
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/create-queue4">Azure Docs</a>.</p>
     *
     * @return An empty response
     * @throws QueueStorageException If a queue with the same name already exists in the queue service.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
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
     * <!-- src_embed com.azure.storage.queue.queueAsyncClient.createWithResponse#map -->
     * <pre>
     * client.createWithResponse&#40;Collections.singletonMap&#40;&quot;queue&quot;, &quot;metadataMap&quot;&#41;&#41;.subscribe&#40;
     *     response -&gt; System.out.println&#40;&quot;Complete creating the queue with status code:&quot; + response.getStatusCode&#40;&#41;&#41;,
     *     error -&gt; System.err.print&#40;error.toString&#40;&#41;&#41;
     * &#41;;
     * </pre>
     * <!-- end com.azure.storage.queue.queueAsyncClient.createWithResponse#map -->
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
            return monoError(LOGGER, ex);
        }
    }

    Mono<Response<Void>> createWithResponse(Map<String, String> metadata, Context context) {
        context = context == null ? Context.NONE : context;
        return client.getQueues().createWithResponseAsync(queueName, null, metadata, null, context)
            .map(response -> new SimpleResponse<>(response, null));
    }

    /**
     * Creates a new queue.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Create a queue</p>
     *
     * <!-- src_embed com.azure.storage.queue.queueAsyncClient.createIfNotExists -->
     * <pre>
     * client.createIfNotExists&#40;&#41;.subscribe&#40;created -&gt; &#123;
     *     if &#40;created&#41; &#123;
     *         System.out.println&#40;&quot;Successfully created.&quot;&#41;;
     *     &#125; else &#123;
     *         System.out.println&#40;&quot;Already exists.&quot;&#41;;
     *     &#125;
     * &#125;&#41;;
     * </pre>
     * <!-- end com.azure.storage.queue.queueAsyncClient.createIfNotExists -->
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/create-queue4">Azure Docs</a>.</p>
     *
     * @return A reactive response signaling completion. {@code true} indicates a new queue was created,
     * {@code false} indicates the specified queue already existed.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Boolean> createIfNotExists() {
        return createIfNotExistsWithResponse(null).flatMap(FluxUtil::toMono);
    }

    /**
     * Creates a new queue.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Create a queue with metadata "queue:metadataMap"</p>
     *
     * <!-- src_embed com.azure.storage.queue.queueAsyncClient.createIfNotExistsWithResponse#map -->
     * <pre>
     * client.createIfNotExistsWithResponse&#40;Collections.singletonMap&#40;&quot;queue&quot;, &quot;metadataMap&quot;&#41;&#41;
     *     .subscribe&#40;response -&gt; &#123;
     *         if &#40;response.getStatusCode&#40;&#41; == 409&#41; &#123;
     *             System.out.println&#40;&quot;Already exists.&quot;&#41;;
     *         &#125; else &#123;
     *             System.out.println&#40;&quot;successfully created.&quot;&#41;;
     *         &#125;
     *     &#125;&#41;;
     * </pre>
     * <!-- end com.azure.storage.queue.queueAsyncClient.createIfNotExistsWithResponse#map -->
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/create-queue4">Azure Docs</a>.</p>
     *
     * @param metadata Metadata to associate with the queue. If there is leading or trailing whitespace in any
     * metadata key or value, it must be removed or encoded.
     * @return A reactive response signaling completion. If {@link Response}'s status code is 201, a new queue was
     * successfully created. If status code is 204 or 409, a queue already existed at this location.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Boolean>> createIfNotExistsWithResponse(Map<String, String> metadata) {
        try {
            return createIfNotExistsWithResponse(metadata, null);
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    Mono<Response<Boolean>> createIfNotExistsWithResponse(Map<String, String> metadata, Context context) {
        try {
            return createWithResponse(metadata, context)
                .map(response -> (Response<Boolean>) new SimpleResponse<>(response, true))
                .onErrorResume(t -> t instanceof QueueStorageException && ((QueueStorageException) t).getStatusCode() == 409,
                    t -> {
                        HttpResponse response = ((QueueStorageException) t).getResponse();
                        return Mono.just(new SimpleResponse<>(response.getRequest(), response.getStatusCode(),
                            response.getHeaders(), false));
                    });
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    /**
     * Permanently deletes the queue.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Delete a queue</p>
     *
     * <!-- src_embed com.azure.storage.queue.queueAsyncClient.delete -->
     * <pre>
     * client.delete&#40;&#41;.doOnSuccess&#40;
     *     response -&gt; System.out.println&#40;&quot;Deleting the queue completed.&quot;&#41;
     * &#41;;
     * </pre>
     * <!-- end com.azure.storage.queue.queueAsyncClient.delete -->
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/delete-queue3">Azure Docs</a>.</p>
     *
     * @return An empty response
     * @throws QueueStorageException If the queue doesn't exist
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
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
     * <!-- src_embed com.azure.storage.queue.queueAsyncClient.deleteWithResponse -->
     * <pre>
     * client.deleteWithResponse&#40;&#41;.subscribe&#40;
     *     response -&gt; System.out.println&#40;&quot;Deleting the queue completed with status code: &quot; + response.getStatusCode&#40;&#41;&#41;
     * &#41;;
     * </pre>
     * <!-- end com.azure.storage.queue.queueAsyncClient.deleteWithResponse -->
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
            return monoError(LOGGER, ex);
        }
    }

    Mono<Response<Void>> deleteWithResponse(Context context) {
        context = context == null ? Context.NONE : context;
        return client.getQueues().deleteWithResponseAsync(queueName, null, null, context)
            .map(response -> new SimpleResponse<>(response, null));
    }

    /**
     * Permanently deletes the queue if it exists.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Delete a queue</p>
     *
     * <!-- src_embed com.azure.storage.queue.queueAsyncClient.deleteIfExists -->
     * <pre>
     * client.deleteIfExists&#40;&#41;.subscribe&#40;deleted -&gt; &#123;
     *     if &#40;deleted&#41; &#123;
     *         System.out.println&#40;&quot;Successfully deleted.&quot;&#41;;
     *     &#125; else &#123;
     *         System.out.println&#40;&quot;Does not exist.&quot;&#41;;
     *     &#125;
     * &#125;&#41;;
     * </pre>
     * <!-- end com.azure.storage.queue.queueAsyncClient.deleteIfExists -->
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/delete-queue3">Azure Docs</a>.</p>
     *
     * @return a reactive response signaling completion. {@code true} indicates that the queue was successfully
     * deleted, {@code false} indicates that the queue did not exist.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Boolean> deleteIfExists() {
        return deleteIfExistsWithResponse().flatMap(FluxUtil::toMono);
    }

    /**
     * Permanently deletes the queue if it exists.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Delete a queue</p>
     *
     * <!-- src_embed com.azure.storage.queue.queueAsyncClient.deleteIfExistsWithResponse -->
     * <pre>
     * client.deleteIfExistsWithResponse&#40;&#41;.subscribe&#40;response -&gt; &#123;
     *     if &#40;response.getStatusCode&#40;&#41; == 404&#41; &#123;
     *         System.out.println&#40;&quot;Does not exist.&quot;&#41;;
     *     &#125; else &#123;
     *         System.out.println&#40;&quot;successfully deleted.&quot;&#41;;
     *     &#125;
     * &#125;&#41;;
     * </pre>
     * <!-- end com.azure.storage.queue.queueAsyncClient.deleteIfExistsWithResponse -->
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/delete-queue3">Azure Docs</a>.</p>
     *
     * @return A reactive response signaling completion. If {@link Response}'s status code is 204, the queue was
     * successfully deleted. If status code is 404, the queue does not exist.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Boolean>> deleteIfExistsWithResponse() {
        try {
            return withContext(this::deleteIfExistsWithResponse);
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    Mono<Response<Boolean>> deleteIfExistsWithResponse(Context context) {
        context = context == null ? Context.NONE : context;
        return deleteWithResponse(context)
            .map(response -> (Response<Boolean>) new SimpleResponse<>(response, true))
            .onErrorResume(t -> t instanceof QueueStorageException && ((QueueStorageException) t).getStatusCode() == 404,
                t -> {
                    HttpResponse response = ((QueueStorageException) t).getResponse();
                    return Mono.just(new SimpleResponse<>(response.getRequest(), response.getStatusCode(),
                        response.getHeaders(), false));
                });
    }

    /**
     * Retrieves metadata and approximate message count of the queue.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Get the properties of the queue</p>
     *
     * <!-- src_embed com.azure.storage.queue.queueAsyncClient.getProperties -->
     * <pre>
     * client.getProperties&#40;&#41;
     *     .subscribe&#40;properties -&gt; &#123;
     *         System.out.printf&#40;&quot;Metadata: %s, Approximate message count: %d&quot;, properties.getMetadata&#40;&#41;,
     *             properties.getApproximateMessagesCount&#40;&#41;&#41;;
     *     &#125;&#41;;
     * </pre>
     * <!-- end com.azure.storage.queue.queueAsyncClient.getProperties -->
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
        return getPropertiesWithResponse().flatMap(FluxUtil::toMono);
    }

    /**
     * Retrieves metadata and approximate message count of the queue.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Get the properties of the queue</p>
     *
     * <!-- src_embed com.azure.storage.queue.queueAsyncClient.getPropertiesWithResponse -->
     * <pre>
     * client.getPropertiesWithResponse&#40;&#41;
     *     .subscribe&#40;response -&gt; &#123;
     *         QueueProperties properties = response.getValue&#40;&#41;;
     *         System.out.printf&#40;&quot;Metadata: %s, Approximate message count: %d&quot;, properties.getMetadata&#40;&#41;,
     *             properties.getApproximateMessagesCount&#40;&#41;&#41;;
     *     &#125;&#41;;
     * </pre>
     * <!-- end com.azure.storage.queue.queueAsyncClient.getPropertiesWithResponse -->
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
            return monoError(LOGGER, ex);
        }
    }

    Mono<Response<QueueProperties>> getPropertiesWithResponse(Context context) {
        context = context == null ? Context.NONE : context;
        return client.getQueues().getPropertiesWithResponseAsync(queueName, null, null, context).map(
            response -> new SimpleResponse<>(response,
                ModelHelper.transformQueueProperties(response.getDeserializedHeaders())));
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
     * <!-- src_embed com.azure.storage.queue.queueAsyncClient.setMetadata#map -->
     * <pre>
     * client.setMetadata&#40;Collections.singletonMap&#40;&quot;queue&quot;, &quot;metadataMap&quot;&#41;&#41;
     *     .subscribe&#40;response -&gt; System.out.println&#40;&quot;Setting metadata completed.&quot;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.queue.queueAsyncClient.setMetadata#map -->
     *
     * <p>Clear the queue's metadata</p>
     *
     * <!-- src_embed com.azure.storage.queue.queueAsyncClient.clearMetadata#map -->
     * <pre>
     * client.setMetadata&#40;null&#41;
     *     .subscribe&#40;response -&gt; System.out.println&#40;&quot;Clearing metadata completed.&quot;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.queue.queueAsyncClient.clearMetadata#map -->
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
     * <!-- src_embed com.azure.storage.queue.queueAsyncClient.setMetadataWithResponse#map -->
     * <pre>
     * client.setMetadataWithResponse&#40;Collections.singletonMap&#40;&quot;queue&quot;, &quot;metadataMap&quot;&#41;&#41;
     *     .subscribe&#40;response -&gt; System.out.printf&#40;&quot;Setting metadata completed with status code %d&quot;,
     *         response.getStatusCode&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.queue.queueAsyncClient.setMetadataWithResponse#map -->
     *
     * <p>Clear the queue's metadata</p>
     *
     * <!-- src_embed com.azure.storage.queue.queueAsyncClient.clearMetadataWithResponse#map -->
     * <pre>
     * client.setMetadataWithResponse&#40;null&#41;
     *     .subscribe&#40;response -&gt; System.out.printf&#40;&quot;Clearing metadata completed with status code %d&quot;,
     *         response.getStatusCode&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.queue.queueAsyncClient.clearMetadataWithResponse#map -->
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
            return monoError(LOGGER, ex);
        }
    }

    Mono<Response<Void>> setMetadataWithResponse(Map<String, String> metadata, Context context) {
        context = context == null ? Context.NONE : context;
        return client.getQueues()
            .setMetadataWithResponseAsync(queueName, null, metadata, null, context)
            .map(response -> new SimpleResponse<>(response, null));
    }

    /**
     * Retrieves stored access policies specified on the queue.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>List the stored access policies</p>
     *
     * <!-- src_embed com.azure.storage.queue.queueAsyncClient.getAccessPolicy -->
     * <pre>
     * client.getAccessPolicy&#40;&#41;
     *     .subscribe&#40;result -&gt; System.out.printf&#40;&quot;Access policy %s allows these permissions: %s&quot;,
     *         result.getId&#40;&#41;, result.getAccessPolicy&#40;&#41;.getPermissions&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.queue.queueAsyncClient.getAccessPolicy -->
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
            return pagedFluxError(LOGGER, ex);
        }
    }

    /**
     * Sets stored access policies on the queue.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Set a read only stored access policy</p>
     *
     * <!-- src_embed com.azure.storage.queue.QueueAsyncClient.setAccessPolicy#Iterable -->
     * <pre>
     * QueueAccessPolicy accessPolicy = new QueueAccessPolicy&#40;&#41;.setPermissions&#40;&quot;r&quot;&#41;
     *     .setStartsOn&#40;OffsetDateTime.now&#40;ZoneOffset.UTC&#41;&#41;
     *     .setExpiresOn&#40;OffsetDateTime.now&#40;ZoneOffset.UTC&#41;.plusDays&#40;10&#41;&#41;;
     *
     * QueueSignedIdentifier permission = new QueueSignedIdentifier&#40;&#41;.setId&#40;&quot;mypolicy&quot;&#41;.setAccessPolicy&#40;accessPolicy&#41;;
     * client.setAccessPolicy&#40;Collections.singletonList&#40;permission&#41;&#41;
     *     .subscribe&#40;response -&gt; System.out.println&#40;&quot;Setting access policies completed.&quot;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.queue.QueueAsyncClient.setAccessPolicy#Iterable -->
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
        return setAccessPolicyWithResponse(permissions).flatMap(FluxUtil::toMono);
    }

    /**
     * Sets stored access policies on the queue.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Set a read only stored access policy</p>
     *
     * <!-- src_embed com.azure.storage.queue.QueueAsyncClient.setAccessPolicyWithResponse#Iterable -->
     * <pre>
     * QueueAccessPolicy accessPolicy = new QueueAccessPolicy&#40;&#41;.setPermissions&#40;&quot;r&quot;&#41;
     *     .setStartsOn&#40;OffsetDateTime.now&#40;ZoneOffset.UTC&#41;&#41;
     *     .setExpiresOn&#40;OffsetDateTime.now&#40;ZoneOffset.UTC&#41;.plusDays&#40;10&#41;&#41;;
     *
     * QueueSignedIdentifier permission = new QueueSignedIdentifier&#40;&#41;.setId&#40;&quot;mypolicy&quot;&#41;.setAccessPolicy&#40;accessPolicy&#41;;
     * client.setAccessPolicyWithResponse&#40;Collections.singletonList&#40;permission&#41;&#41;
     *     .subscribe&#40;response -&gt; System.out.printf&#40;&quot;Setting access policies completed with status code %d&quot;,
     *         response.getStatusCode&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.queue.QueueAsyncClient.setAccessPolicyWithResponse#Iterable -->
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
            return monoError(LOGGER, ex);
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
            .setAccessPolicyWithResponseAsync(queueName, null, null, permissionsList, context)
            .map(response -> new SimpleResponse<>(response, null));
    }

    /**
     * Deletes all messages in the queue.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Clear the messages</p>
     *
     * <!-- src_embed com.azure.storage.queue.queueAsyncClient.clearMessages -->
     * <pre>
     * client.clearMessages&#40;&#41;.subscribe&#40;
     *     response -&gt; System.out.println&#40;&quot;Clearing messages completed.&quot;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.queue.queueAsyncClient.clearMessages -->
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/clear-messages">Azure Docs</a>.</p>
     *
     * @return An empty response
     * @throws QueueStorageException If the queue doesn't exist
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
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
     * <!-- src_embed com.azure.storage.queue.queueAsyncClient.clearMessagesWithResponse -->
     * <pre>
     * client.clearMessagesWithResponse&#40;&#41;.doOnSuccess&#40;
     *     response -&gt; System.out.println&#40;&quot;Clearing messages completed with status code: &quot; + response.getStatusCode&#40;&#41;&#41;
     * &#41;;
     * </pre>
     * <!-- end com.azure.storage.queue.queueAsyncClient.clearMessagesWithResponse -->
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
            return monoError(LOGGER, ex);
        }
    }

    Mono<Response<Void>> clearMessagesWithResponse(Context context) {
        context = context == null ? Context.NONE : context;
        return client.getMessages().clearWithResponseAsync(queueName, null, null, context)
            .map(response -> new SimpleResponse<>(response, null));
    }

    /**
     * Enqueues a message that has a time-to-live of 7 days and is instantly visible.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Enqueue a message of "Hello, Azure"</p>
     *
     * <!-- src_embed com.azure.storage.queue.queueAsyncClient.sendMessage#string -->
     * <pre>
     * client.sendMessage&#40;&quot;Hello, Azure&quot;&#41;.subscribe&#40;
     *     response -&gt; &#123;
     *     &#125;,
     *     error -&gt; System.err.print&#40;error.toString&#40;&#41;&#41;,
     *     &#40;&#41; -&gt; System.out.println&#40;&quot;Complete enqueuing the message!&quot;&#41;
     * &#41;;
     * </pre>
     * <!-- end com.azure.storage.queue.queueAsyncClient.sendMessage#string -->
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
        return sendMessageWithResponse(messageText, null, null).flatMap(FluxUtil::toMono);
    }

    /**
     * Enqueues a message that has a time-to-live of 7 days and is instantly visible.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Enqueue a message of "Hello, Azure"</p>
     *
     * <!-- src_embed com.azure.storage.queue.queueAsyncClient.sendMessage#BinaryData -->
     * <pre>
     * client.sendMessage&#40;BinaryData.fromString&#40;&quot;Hello, Azure&quot;&#41;&#41;.subscribe&#40;
     *         response -&gt; &#123;
     *         &#125;,
     *         error -&gt; System.err.print&#40;error.toString&#40;&#41;&#41;,
     *         &#40;&#41; -&gt; System.out.println&#40;&quot;Complete enqueuing the message!&quot;&#41;
     * &#41;;
     * </pre>
     * <!-- end com.azure.storage.queue.queueAsyncClient.sendMessage#BinaryData -->
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
     * <!-- src_embed com.azure.storage.queue.queueAsyncClient.sendMessageWithResponse#string-duration-duration -->
     * <pre>
     * client.sendMessageWithResponse&#40;&quot;Hello, Azure&quot;,
     *     Duration.ofSeconds&#40;5&#41;, null&#41;.subscribe&#40;
     *         response -&gt; System.out.printf&#40;&quot;Message %s expires at %s&quot;, response.getValue&#40;&#41;.getMessageId&#40;&#41;,
     *             response.getValue&#40;&#41;.getExpirationTime&#40;&#41;&#41;,
     *         error -&gt; System.err.print&#40;error.toString&#40;&#41;&#41;,
     *         &#40;&#41; -&gt; System.out.println&#40;&quot;Complete enqueuing the message!&quot;&#41;
     * &#41;;
     * </pre>
     * <!-- end com.azure.storage.queue.queueAsyncClient.sendMessageWithResponse#string-duration-duration -->
     *
     * <p>Add a message of "Goodbye, Azure" that has a time to live of 5 seconds</p>
     *
     * <!-- src_embed com.azure.storage.queue.QueueAsyncClient.sendMessageWithResponse-liveTime#String-Duration-Duration -->
     * <pre>
     * client.sendMessageWithResponse&#40;&quot;Goodbye, Azure&quot;,
     *     null, Duration.ofSeconds&#40;5&#41;&#41;.subscribe&#40;
     *         response -&gt; System.out.printf&#40;&quot;Message %s expires at %s&quot;, response.getValue&#40;&#41;.getMessageId&#40;&#41;,
     *             response.getValue&#40;&#41;.getExpirationTime&#40;&#41;&#41;,
     *         error -&gt; System.err.print&#40;error.toString&#40;&#41;&#41;,
     *         &#40;&#41; -&gt; System.out.println&#40;&quot;Complete enqueuing the message!&quot;&#41;
     * &#41;;
     * </pre>
     * <!-- end com.azure.storage.queue.QueueAsyncClient.sendMessageWithResponse-liveTime#String-Duration-Duration -->
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
     * are outside the allowed limits.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<SendMessageResult>> sendMessageWithResponse(String messageText, Duration visibilityTimeout,
                                                                   Duration timeToLive) {
        return sendMessageWithResponse(BinaryData.fromString(messageText), visibilityTimeout, timeToLive);
    }

    /**
     * Enqueues a message with a given time-to-live and a timeout period where the message is invisible in the queue.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Add a message of "Hello, Azure" that has a timeout of 5 seconds</p>
     *
     * <!-- src_embed com.azure.storage.queue.queueAsyncClient.sendMessageWithResponse#BinaryData-duration-duration -->
     * <pre>
     * client.sendMessageWithResponse&#40;BinaryData.fromString&#40;&quot;Hello, Azure&quot;&#41;,
     *         Duration.ofSeconds&#40;5&#41;, null&#41;.subscribe&#40;
     *         response -&gt; System.out.printf&#40;&quot;Message %s expires at %s&quot;, response.getValue&#40;&#41;.getMessageId&#40;&#41;,
     *             response.getValue&#40;&#41;.getExpirationTime&#40;&#41;&#41;,
     *         error -&gt; System.err.print&#40;error.toString&#40;&#41;&#41;,
     *         &#40;&#41; -&gt; System.out.println&#40;&quot;Complete enqueuing the message!&quot;&#41;
     * &#41;;
     * </pre>
     * <!-- end com.azure.storage.queue.queueAsyncClient.sendMessageWithResponse#BinaryData-duration-duration -->
     *
     * <p>Add a message of "Goodbye, Azure" that has a time to live of 5 seconds</p>
     *
     * <!-- src_embed com.azure.storage.queue.QueueAsyncClient.sendMessageWithResponse-liveTime#BinaryData-Duration-Duration -->
     * <pre>
     * client.sendMessageWithResponse&#40;BinaryData.fromString&#40;&quot;Goodbye, Azure&quot;&#41;,
     *         null, Duration.ofSeconds&#40;5&#41;&#41;.subscribe&#40;
     *         response -&gt; System.out.printf&#40;&quot;Message %s expires at %s&quot;, response.getValue&#40;&#41;.getMessageId&#40;&#41;,
     *             response.getValue&#40;&#41;.getExpirationTime&#40;&#41;&#41;,
     *         error -&gt; System.err.print&#40;error.toString&#40;&#41;&#41;,
     *         &#40;&#41; -&gt; System.out.println&#40;&quot;Complete enqueuing the message!&quot;&#41;
     * &#41;;
     * </pre>
     * <!-- end com.azure.storage.queue.QueueAsyncClient.sendMessageWithResponse-liveTime#BinaryData-Duration-Duration -->
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
     * are outside the allowed limits.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<SendMessageResult>> sendMessageWithResponse(BinaryData message, Duration visibilityTimeout,
                                                                     Duration timeToLive) {
        try {
            return withContext(context -> sendMessageWithResponse(message, visibilityTimeout, timeToLive, context));
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    Mono<Response<SendMessageResult>> sendMessageWithResponse(BinaryData message, Duration visibilityTimeout,
                                                              Duration timeToLive, Context context) {
        Integer visibilityTimeoutInSeconds = (visibilityTimeout == null) ? null : (int) visibilityTimeout.getSeconds();
        Integer timeToLiveInSeconds = (timeToLive == null) ? null : (int) timeToLive.getSeconds();
        Context finalContext  = context == null ? Context.NONE : context;
        return Mono.fromCallable(() -> ModelHelper.encodeMessage(message, messageEncoding))
            .flatMap(messageText -> {
                QueueMessage queueMessage = new QueueMessage().setMessageText(messageText);
                return client.getMessages()
                    .enqueueWithResponseAsync(queueName, queueMessage, visibilityTimeoutInSeconds, timeToLiveInSeconds,
                        null, null, finalContext)
                    .map(response -> new SimpleResponse<>(response, response.getValue().get(0)));
            });

    }

    /**
     * Retrieves the first message in the queue and hides it from other operations for 30 seconds.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Dequeue a message</p>
     *
     * <!-- src_embed com.azure.storage.queue.queueAsyncClient.receiveMessage -->
     * <pre>
     * client.receiveMessage&#40;&#41;.subscribe&#40;
     *     message -&gt; System.out.println&#40;&quot;The message got from getMessages operation: &quot;
     *         + message.getBody&#40;&#41;.toString&#40;&#41;&#41;,
     *     error -&gt; System.err.print&#40;error.toString&#40;&#41;&#41;,
     *     &#40;&#41; -&gt; System.out.println&#40;&quot;Complete receiving the message!&quot;&#41;
     * &#41;;
     * </pre>
     * <!-- end com.azure.storage.queue.queueAsyncClient.receiveMessage -->
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
            return monoError(LOGGER, ex);
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
     * <!-- src_embed com.azure.storage.queue.queueAsyncClient.receiveMessages#integer -->
     * <pre>
     * client.receiveMessages&#40;5&#41;.subscribe&#40;
     *     message -&gt; System.out.println&#40;&quot;The message got from getMessages operation: &quot;
     *         + message.getBody&#40;&#41;.toString&#40;&#41;&#41;,
     *     error -&gt; System.err.print&#40;error.toString&#40;&#41;&#41;,
     *     &#40;&#41; -&gt; System.out.println&#40;&quot;Complete receiving the message!&quot;&#41;
     * &#41;;
     * </pre>
     * <!-- end com.azure.storage.queue.queueAsyncClient.receiveMessages#integer -->
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/get-messages">Azure Docs</a>.</p>
     *
     * @param maxMessages Optional. Maximum number of messages to get, if there are fewer messages exist in the queue
     * than requested all the messages will be returned. If left empty only 1 message will be retrieved, the allowed
     * range is 1 to 32 messages.
     * @return Up to {@code maxMessages} {@link QueueMessageItem ReceiveMessageItem} from the queue.
     * Each DequeuedMessage contains {@link QueueMessageItem#getMessageId() messageId} and
     * {@link QueueMessageItem#getPopReceipt() popReceipt} used to interact with the message and
     * other metadata about the message.
     * @throws QueueStorageException If the queue doesn't exist or {@code maxMessages} is outside the allowed bounds
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<QueueMessageItem> receiveMessages(Integer maxMessages) {
        return receiveMessages(maxMessages, null);
    }

    /**
     * Retrieves up to the maximum number of messages from the queue and hides them from other operations for the
     * timeout period.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Dequeue up to 5 messages and give them a 60 second timeout period</p>
     *
     * <!-- src_embed com.azure.storage.queue.queueAsyncClient.receiveMessages#integer-duration -->
     * <pre>
     * client.receiveMessages&#40;5, Duration.ofSeconds&#40;60&#41;&#41;
     *     .subscribe&#40;
     *         message -&gt; System.out.println&#40;&quot;The message got from getMessages operation: &quot;
     *             + message.getBody&#40;&#41;.toString&#40;&#41;&#41;,
     *         error -&gt; System.err.print&#40;error.toString&#40;&#41;&#41;,
     *         &#40;&#41; -&gt; System.out.println&#40;&quot;Complete receiving the message!&quot;&#41;
     *     &#41;;
     * </pre>
     * <!-- end com.azure.storage.queue.queueAsyncClient.receiveMessages#integer-duration -->
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/get-messages">Azure Docs</a>.</p>
     *
     * @param maxMessages Optional. Maximum number of messages to get, if there are fewer messages exist in the queue
     * than requested all the messages will be returned. If left empty only 1 message will be retrieved, the allowed
     * range is 1 to 32 messages.
     * @param visibilityTimeout Optional. The timeout period for how long the message is invisible in the queue. If left
     * empty the dequeued messages will be invisible for 30 seconds. The timeout must be between 1 second and 7 days.
     * @return Up to {@code maxMessages} {@link QueueMessageItem DequeuedMessages} from the queue. Each DeqeuedMessage
     * contains {@link QueueMessageItem#getMessageId() messageId} and
     * {@link QueueMessageItem#getPopReceipt() popReceipt}
     * used to interact with the message and other metadata about the message.
     * @throws QueueStorageException If the queue doesn't exist or {@code maxMessages} or {@code visibilityTimeout} is
     * outside the allowed bounds
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<QueueMessageItem> receiveMessages(Integer maxMessages, Duration visibilityTimeout) {
        try {
            return receiveMessagesWithOptionalTimeout(maxMessages, visibilityTimeout, null, Context.NONE);
        } catch (RuntimeException ex) {
            return pagedFluxError(LOGGER, ex);
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
        ResponseBase<MessagesDequeueHeaders, List<QueueMessageItemInternal>> response) {
        List<QueueMessageItemInternal> queueMessageInternalItems = response.getValue();
        if (queueMessageInternalItems == null) {
            queueMessageInternalItems = Collections.emptyList();
        }

        return Flux.fromIterable(queueMessageInternalItems)
            .flatMapSequential(queueMessageItemInternal -> Mono.fromCallable(() ->
                        ModelHelper.transformQueueMessageItemInternal(queueMessageItemInternal, messageEncoding))
                .onErrorResume(IllegalArgumentException.class, e -> {
                    if (processMessageDecodingErrorAsyncHandler != null) {
                        return Mono.fromCallable(() ->
                                ModelHelper.transformQueueMessageItemInternal(queueMessageItemInternal,
                                    QueueMessageEncoding.NONE))
                            .flatMap(messageItem -> processMessageDecodingErrorAsyncHandler.apply(
                                new QueueMessageDecodingError(this, queueClient, messageItem, null, e)))
                            .then(Mono.empty());
                    } else if (processMessageDecodingErrorHandler != null) {
                        return Mono.fromCallable(() -> ModelHelper.transformQueueMessageItemInternal(
                            queueMessageItemInternal, QueueMessageEncoding.NONE))
                            .flatMap(messageItem -> {
                                try {
                                    return Mono.fromRunnable(() -> processMessageDecodingErrorHandler.accept(
                                        new QueueMessageDecodingError(this, queueClient, messageItem, null, e)))
                                        .subscribeOn(Schedulers.boundedElastic())
                                        .then(Mono.empty());
                                } catch (RuntimeException re) {
                                    return FluxUtil.<QueueMessageItem>monoError(LOGGER, re);
                                }
                            })
                            .subscribeOn(Schedulers.boundedElastic());
                    } else {
                        return FluxUtil.monoError(LOGGER, e);
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
     * <!-- src_embed com.azure.storage.queue.queueAsyncClient.peekMessage -->
     * <pre>
     * client.peekMessage&#40;&#41;.subscribe&#40;
     *     peekMessages -&gt; System.out.println&#40;&quot;The message got from peek operation: &quot;
     *         + peekMessages.getBody&#40;&#41;.toString&#40;&#41;&#41;,
     *     error -&gt; System.err.print&#40;error.toString&#40;&#41;&#41;,
     *     &#40;&#41; -&gt; System.out.println&#40;&quot;Complete peeking the message!&quot;&#41;
     * &#41;;
     * </pre>
     * <!-- end com.azure.storage.queue.queueAsyncClient.peekMessage -->
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/peek-messages">Azure Docs</a>.</p>
     *
     * @return A {@link PeekedMessageItem} that contains metadata about the message.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<PeekedMessageItem> peekMessage() {
        return peekMessages(null).singleOrEmpty();
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
     * <!-- src_embed com.azure.storage.queue.queueAsyncClient.peekMessages#integer -->
     * <pre>
     * client.peekMessages&#40;5&#41;.subscribe&#40;
     *     peekMessage -&gt; System.out.printf&#40;&quot;Peeked message %s has been received %d times&quot;,
     *         peekMessage.getMessageId&#40;&#41;, peekMessage.getDequeueCount&#40;&#41;&#41;,
     *     error -&gt; System.err.print&#40;error.toString&#40;&#41;&#41;,
     *     &#40;&#41; -&gt; System.out.println&#40;&quot;Complete peeking the message!&quot;&#41;
     * &#41;;
     * </pre>
     * <!-- end com.azure.storage.queue.queueAsyncClient.peekMessages#integer -->
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/peek-messages">Azure Docs</a>.</p>
     *
     * @param maxMessages Optional. Maximum number of messages to peek, if there are fewer messages exist in the queue
     * than requested all the messages will be peeked. If left empty only 1 message will be peeked, the allowed range is
     * 1 to 32 messages.
     * @return Up to {@code maxMessages} {@link PeekedMessageItem PeekedMessages} from the queue. Each PeekedMessage
     * contains metadata about the message.
     * @throws QueueStorageException If the queue doesn't exist or {@code maxMessages} is outside the allowed bounds
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<PeekedMessageItem> peekMessages(Integer maxMessages) {
        try {
            return peekMessagesWithOptionalTimeout(maxMessages, null, Context.NONE);
        } catch (RuntimeException ex) {
            return pagedFluxError(LOGGER, ex);
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
        ResponseBase<MessagesPeekHeaders, List<PeekedMessageItemInternal>> response) {
        List<PeekedMessageItemInternal> peekedMessageInternalItems = response.getValue();
        if (peekedMessageInternalItems == null) {
            peekedMessageInternalItems = Collections.emptyList();
        }

        return Flux.fromIterable(peekedMessageInternalItems)
            .flatMapSequential(peekedMessageItemInternal ->
                Mono.fromCallable(() ->
                        ModelHelper.transformPeekedMessageItemInternal(peekedMessageItemInternal, messageEncoding))
                    .onErrorResume(IllegalArgumentException.class, e -> {
                        if (processMessageDecodingErrorAsyncHandler != null) {
                            return Mono.fromCallable(() ->
                                    ModelHelper.transformPeekedMessageItemInternal(peekedMessageItemInternal,
                                        QueueMessageEncoding.NONE))

                                .flatMap(messageItem -> processMessageDecodingErrorAsyncHandler.apply(
                                    new QueueMessageDecodingError(this, queueClient, null, messageItem, e)))
                                .then(Mono.empty());
                        } else if (processMessageDecodingErrorHandler != null) {
                            return Mono.fromCallable(() ->
                                    ModelHelper.transformPeekedMessageItemInternal(peekedMessageItemInternal,
                                        QueueMessageEncoding.NONE))

                                .flatMap(messageItem -> {
                                    try {
                                        return Mono.fromRunnable(() -> processMessageDecodingErrorHandler.accept(
                                                new QueueMessageDecodingError(this, queueClient, null, messageItem, e)))
                                            .subscribeOn(Schedulers.boundedElastic())
                                            .then(Mono.empty());
                                    } catch (RuntimeException re) {
                                        return FluxUtil.<PeekedMessageItem>monoError(LOGGER, re);
                                    }
                                })
                                .subscribeOn(Schedulers.boundedElastic());
                        } else {
                            return FluxUtil.monoError(LOGGER, e);
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

    /**
     * Updates the specific message in the queue with a new message and resets the visibility timeout.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Dequeue the first message and update it to "Hello again, Azure" and hide it for 5 seconds</p>
     *
     * <!-- src_embed com.azure.storage.queue.QueueAsyncClient.updateMessage#String-String-String-Duration -->
     * <pre>
     * client.receiveMessage&#40;&#41;.subscribe&#40;
     *     message -&gt; &#123;
     *         client.updateMessage&#40;&quot;newText&quot;, message.getMessageId&#40;&#41;,
     *             message.getPopReceipt&#40;&#41;, null&#41;.subscribe&#40;
     *                 response -&gt; &#123;
     *                 &#125;,
     *                 updateError -&gt; System.err.print&#40;updateError.toString&#40;&#41;&#41;,
     *                 &#40;&#41; -&gt; System.out.println&#40;&quot;Complete updating the message!&quot;&#41;
     *         &#41;;
     *     &#125;,
     *     error -&gt; System.err.print&#40;error.toString&#40;&#41;&#41;,
     *     &#40;&#41; -&gt; System.out.println&#40;&quot;Complete receiving the message!&quot;&#41;
     * &#41;;
     * </pre>
     * <!-- end com.azure.storage.queue.QueueAsyncClient.updateMessage#String-String-String-Duration -->
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/update-message">Azure Docs</a>.</p>
     *
     * @param messageId ID of the message to update
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
        return updateMessageWithResponse(messageId, popReceipt, messageText, visibilityTimeout)
            .flatMap(FluxUtil::toMono);
    }

    /**
     * Updates the specific message in the queue with a new message and resets the visibility timeout.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Dequeue the first message and update it to "Hello again, Azure" and hide it for 5 seconds</p>
     *
     * <!-- src_embed com.azure.storage.queue.QueueAsyncClient.updateMessageWithResponse#String-String-String-Duration -->
     * <pre>
     *
     * client.receiveMessage&#40;&#41;.subscribe&#40;
     *     message -&gt; &#123;
     *         client.updateMessageWithResponse&#40;message.getMessageId&#40;&#41;, message.getPopReceipt&#40;&#41;, &quot;newText&quot;,
     *             null&#41;.subscribe&#40;
     *                 response -&gt; System.out.println&#40;&quot;Complete updating the message with status code:&quot;
     *                     + response.getStatusCode&#40;&#41;&#41;,
     *                 updateError -&gt; System.err.print&#40;updateError.toString&#40;&#41;&#41;,
     *                 &#40;&#41; -&gt; System.out.println&#40;&quot;Complete updating the message!&quot;&#41;
     *         &#41;;
     *     &#125;,
     *     error -&gt; System.err.print&#40;error.toString&#40;&#41;&#41;,
     *     &#40;&#41; -&gt; System.out.println&#40;&quot;Complete receiving the message!&quot;&#41;
     * &#41;;
     * </pre>
     * <!-- end com.azure.storage.queue.QueueAsyncClient.updateMessageWithResponse#String-String-String-Duration -->
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/update-message">Azure Docs</a>.</p>
     *
     * @param messageId ID of the message to update
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
            return monoError(LOGGER, ex);
        }
    }

    Mono<Response<UpdateMessageResult>> updateMessageWithResponse(String messageId, String popReceipt,
        String messageText, Duration visibilityTimeout, Context context) {
        QueueMessage message = messageText == null ? null : new QueueMessage().setMessageText(messageText);
        context = context == null ? Context.NONE : context;
        visibilityTimeout = visibilityTimeout == null ? Duration.ZERO : visibilityTimeout;
        return client.getMessageIds().updateWithResponseAsync(queueName, messageId, popReceipt,
                (int) visibilityTimeout.getSeconds(), null, null, message, context)
            .map(response -> {
                UpdateMessageResult result = new UpdateMessageResult(
                    response.getDeserializedHeaders().getXMsPopreceipt(),
                    response.getDeserializedHeaders().getXMsTimeNextVisible());

                return new SimpleResponse<>(response, result);
            });
    }

    /**
     * Deletes the specified message in the queue
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Delete the first message</p>
     *
     * <!-- src_embed com.azure.storage.queue.QueueAsyncClient.deleteMessage#String-String -->
     * <pre>
     * client.receiveMessage&#40;&#41;.subscribe&#40;
     *     message -&gt; &#123;
     *         client.deleteMessage&#40;message.getMessageId&#40;&#41;, message.getPopReceipt&#40;&#41;&#41;.subscribe&#40;
     *             response -&gt; &#123;
     *             &#125;,
     *             deleteError -&gt; System.err.print&#40;deleteError.toString&#40;&#41;&#41;,
     *             &#40;&#41; -&gt; System.out.println&#40;&quot;Complete deleting the message!&quot;&#41;
     *         &#41;;
     *     &#125;,
     *     error -&gt; System.err.print&#40;error.toString&#40;&#41;&#41;,
     *     &#40;&#41; -&gt; System.out.println&#40;&quot;Complete receiving the message!&quot;&#41;
     * &#41;;
     * </pre>
     * <!-- end com.azure.storage.queue.QueueAsyncClient.deleteMessage#String-String -->
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/delete-message2">Azure Docs</a>.</p>
     *
     * @param messageId ID of the message to deleted
     * @param popReceipt Unique identifier that must match for the message to be deleted
     * @return An empty response
     * @throws QueueStorageException If the queue or messageId don't exist or the popReceipt doesn't match on the
     * message.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
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
     * <!-- src_embed com.azure.storage.queue.QueueAsyncClient.deleteMessageWithResponse#String-String -->
     * <pre>
     * client.receiveMessage&#40;&#41;.subscribe&#40;
     *     message -&gt; &#123;
     *         client.deleteMessageWithResponse&#40;message.getMessageId&#40;&#41;, message.getPopReceipt&#40;&#41;&#41;
     *             .subscribe&#40;
     *                 response -&gt; System.out.println&#40;&quot;Complete deleting the message with status code: &quot;
     *                     + response.getStatusCode&#40;&#41;&#41;,
     *                 deleteError -&gt; System.err.print&#40;deleteError.toString&#40;&#41;&#41;,
     *                 &#40;&#41; -&gt; System.out.println&#40;&quot;Complete deleting the message!&quot;&#41;
     *             &#41;;
     *     &#125;,
     *     error -&gt; System.err.print&#40;error.toString&#40;&#41;&#41;,
     *     &#40;&#41; -&gt; System.out.println&#40;&quot;Complete receiving the message!&quot;&#41;
     * &#41;;
     * </pre>
     * <!-- end com.azure.storage.queue.QueueAsyncClient.deleteMessageWithResponse#String-String -->
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/delete-message2">Azure Docs</a>.</p>
     *
     * @param messageId ID of the message to deleted
     * @param popReceipt Unique identifier that must match for the message to be deleted
     * @return A response that only contains headers and response status code
     * @throws QueueStorageException If the queue or messageId don't exist or the popReceipt doesn't match on the
     * message.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> deleteMessageWithResponse(String messageId, String popReceipt) {
        try {
            return withContext(context -> deleteMessageWithResponse(messageId, popReceipt, context));
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    Mono<Response<Void>> deleteMessageWithResponse(String messageId, String popReceipt, Context context) {
        context = context == null ? Context.NONE : context;
        return client.getMessageIds().deleteWithResponseAsync(queueName, messageId, popReceipt, null, null, context)
            .map(response -> new SimpleResponse<>(response, null));
    }

    /**
     * Get the queue name of the client.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.queue.queueAsyncClient.getQueueName -->
     * <pre>
     * String queueName = client.getQueueName&#40;&#41;;
     * System.out.println&#40;&quot;The name of the queue is &quot; + queueName&#41;;
     * </pre>
     * <!-- end com.azure.storage.queue.queueAsyncClient.getQueueName -->
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
     * <!-- src_embed com.azure.storage.queue.QueueAsyncClient.generateSas#QueueServiceSasSignatureValues -->
     * <pre>
     * OffsetDateTime expiryTime = OffsetDateTime.now&#40;&#41;.plusDays&#40;1&#41;;
     * QueueSasPermission permission = new QueueSasPermission&#40;&#41;.setReadPermission&#40;true&#41;;
     *
     * QueueServiceSasSignatureValues values = new QueueServiceSasSignatureValues&#40;expiryTime, permission&#41;
     *     .setStartTime&#40;OffsetDateTime.now&#40;&#41;&#41;;
     *
     * client.generateSas&#40;values&#41;; &#47;&#47; Client must be authenticated via StorageSharedKeyCredential
     * </pre>
     * <!-- end com.azure.storage.queue.QueueAsyncClient.generateSas#QueueServiceSasSignatureValues -->
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
     * <!-- src_embed com.azure.storage.queue.QueueAsyncClient.generateSas#QueueServiceSasSignatureValues-Context -->
     * <pre>
     * OffsetDateTime expiryTime = OffsetDateTime.now&#40;&#41;.plusDays&#40;1&#41;;
     * QueueSasPermission permission = new QueueSasPermission&#40;&#41;.setReadPermission&#40;true&#41;;
     *
     * QueueServiceSasSignatureValues values = new QueueServiceSasSignatureValues&#40;expiryTime, permission&#41;
     *     .setStartTime&#40;OffsetDateTime.now&#40;&#41;&#41;;
     *
     * &#47;&#47; Client must be authenticated via StorageSharedKeyCredential
     * client.generateSas&#40;values, new Context&#40;&quot;key&quot;, &quot;value&quot;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.queue.QueueAsyncClient.generateSas#QueueServiceSasSignatureValues-Context -->
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

    AzureQueueStorageImpl getAzureQueueStorage() {
        return this.client;
    }
}
