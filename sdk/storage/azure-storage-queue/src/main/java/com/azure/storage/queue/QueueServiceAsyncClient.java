// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.storage.queue;

import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedResponse;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.util.Context;
import com.azure.core.util.FluxUtil;
import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.common.StorageSharedKeyCredential;
import com.azure.storage.common.implementation.AccountSasImplUtil;
import com.azure.storage.common.implementation.SasImplUtils;
import com.azure.storage.common.implementation.StorageImplUtils;
import com.azure.storage.common.sas.AccountSasSignatureValues;
import com.azure.storage.queue.implementation.AzureQueueStorageImpl;
import com.azure.storage.queue.models.QueueCorsRule;
import com.azure.storage.queue.models.QueueItem;
import com.azure.storage.queue.models.QueueMessageDecodingError;
import com.azure.storage.queue.models.QueueServiceProperties;
import com.azure.storage.queue.models.QueueServiceStatistics;
import com.azure.storage.queue.models.QueueStorageException;
import com.azure.storage.queue.models.QueuesSegmentOptions;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

import static com.azure.core.util.FluxUtil.monoError;
import static com.azure.core.util.FluxUtil.pagedFluxError;
import static com.azure.core.util.FluxUtil.withContext;
import static com.azure.core.util.tracing.Tracer.AZ_TRACING_NAMESPACE_KEY;
import static com.azure.storage.common.Utility.STORAGE_TRACING_NAMESPACE_VALUE;


/**
 * This class provides a client that contains all the operations for interacting with a queue account in Azure Storage.
 * Operations allowed by the client are creating, listing, and deleting queues, retrieving and updating properties of
 * the account, and retrieving statistics of the account.
 *
 * <p><strong>Instantiating an Asynchronous Queue Service Client</strong></p>
 *
 * {@codesnippet com.azure.storage.queue.queueServiceAsyncClient.instantiation}
 *
 * <p>View {@link QueueServiceClientBuilder this} for additional ways to construct the client.</p>
 *
 * @see QueueServiceClientBuilder
 * @see QueueServiceClient
 * @see StorageSharedKeyCredential
 */
@ServiceClient(builder = QueueServiceClientBuilder.class, isAsync = true)
public final class QueueServiceAsyncClient {
    private final ClientLogger logger = new ClientLogger(QueueServiceAsyncClient.class);
    private final AzureQueueStorageImpl client;
    private final String accountName;
    private final QueueServiceVersion serviceVersion;
    private final QueueMessageEncoding messageEncoding;
    private final Function<QueueMessageDecodingError, Mono<Void>> processMessageDecodingErrorAsyncHandler;
    private final Consumer<QueueMessageDecodingError> processMessageDecodingErrorHandler;

    /**
     * Creates a QueueServiceAsyncClient from the passed {@link AzureQueueStorageImpl implementation client}.
     *
     * @param azureQueueStorage Client that interacts with the service interfaces.
     */
    QueueServiceAsyncClient(AzureQueueStorageImpl azureQueueStorage, String accountName,
        QueueServiceVersion serviceVersion, QueueMessageEncoding messageEncoding,
        Function<QueueMessageDecodingError, Mono<Void>> processMessageDecodingErrorAsyncHandler,
        Consumer<QueueMessageDecodingError> processMessageDecodingErrorHandler) {
        this.client = azureQueueStorage;
        this.accountName = accountName;
        this.serviceVersion = serviceVersion;
        this.messageEncoding = messageEncoding;
        this.processMessageDecodingErrorAsyncHandler = processMessageDecodingErrorAsyncHandler;
        this.processMessageDecodingErrorHandler = processMessageDecodingErrorHandler;
    }

    /**
     * @return the URL of the storage queue
     */
    public String getQueueServiceUrl() {
        return client.getUrl();
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
     * Constructs a QueueAsyncClient that interacts with the specified queue.
     *
     * This will not create the queue in the storage account if it doesn't exist.
     *
     * @param queueName Name of the queue
     * @return QueueAsyncClient that interacts with the specified queue
     */
    public QueueAsyncClient getQueueAsyncClient(String queueName) {
        return new QueueAsyncClient(client, queueName, accountName, serviceVersion,
            messageEncoding, processMessageDecodingErrorAsyncHandler, processMessageDecodingErrorHandler);
    }

    /**
     * Creates a queue in the storage account with the specified name and returns a QueueAsyncClient to interact with
     * it.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Create the queue "test"</p>
     *
     * {@codesnippet com.azure.storage.queue.queueServiceAsyncClient.createQueue#string}
     *
     * @param queueName Name of the queue
     * @return The {@link QueueAsyncClient QueueAsyncClient}
     * @throws QueueStorageException If a queue with the same name and different metadata already exists
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<QueueAsyncClient> createQueue(String queueName) {
        try {
            return createQueueWithResponse(queueName, null).flatMap(FluxUtil::toMono);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Creates a queue in the storage account with the specified name and metadata and returns a QueueAsyncClient to
     * interact with it.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Create the queue "test" with metadata "queue:metadata"</p>
     *
     * {@codesnippet com.azure.storage.queue.queueServiceAsyncClient.createQueueWithResponse#string-map}
     *
     * @param queueName Name of the queue
     * @param metadata Metadata to associate with the queue. If there is leading or trailing whitespace in any
     * metadata key or value, it must be removed or encoded.
     * @return A response containing the {@link QueueAsyncClient QueueAsyncClient} and the status of creating the queue
     * @throws QueueStorageException If a queue with the same name and different metadata already exists
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<QueueAsyncClient>> createQueueWithResponse(String queueName, Map<String, String> metadata) {
        try {
            Objects.requireNonNull(queueName, "'queueName' cannot be null.");
            return withContext(context -> createQueueWithResponse(queueName, metadata, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<QueueAsyncClient>> createQueueWithResponse(String queueName, Map<String, String> metadata,
        Context context) {
        QueueAsyncClient queueAsyncClient = new QueueAsyncClient(client, queueName, accountName,
            serviceVersion, messageEncoding, processMessageDecodingErrorAsyncHandler, processMessageDecodingErrorHandler);

        return queueAsyncClient.createWithResponse(metadata, context)
            .map(response -> new SimpleResponse<>(response, queueAsyncClient));
    }

    /**
     * Deletes a queue in the storage account
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Delete the queue "test"</p>
     *
     * {@codesnippet com.azure.storage.queue.queueServiceAsyncClient.deleteQueue#string}
     *
     * @param queueName Name of the queue
     * @return An empty response
     * @throws QueueStorageException If the queue doesn't exist
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> deleteQueue(String queueName) {
        try {
            return deleteQueueWithResponse(queueName).flatMap(FluxUtil::toMono);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Deletes a queue in the storage account
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Delete the queue "test"</p>
     *
     * {@codesnippet com.azure.storage.queue.queueServiceAsyncClient.deleteQueueWithResponse#string}
     *
     * @param queueName Name of the queue
     * @return A response that only contains headers and response status code
     * @throws QueueStorageException If the queue doesn't exist
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> deleteQueueWithResponse(String queueName) {
        try {
            return withContext(context -> deleteQueueWithResponse(queueName, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<Void>> deleteQueueWithResponse(String queueName, Context context) {
        return new QueueAsyncClient(client, queueName, accountName,
            serviceVersion, messageEncoding, processMessageDecodingErrorAsyncHandler, processMessageDecodingErrorHandler)
            .deleteWithResponse(context);
    }

    /**
     * Lists all queues in the storage account without their metadata.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>List all queues in the account</p>
     *
     * {@codesnippet com.azure.storage.queue.queueServiceAsyncClient.listQueues}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/list-queues1">Azure Docs</a>.</p>
     *
     * @return {@link QueueItem Queues} in the storage account
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<QueueItem> listQueues() {
        try {
            return listQueuesWithOptionalTimeout(null, null, null, Context.NONE);
        } catch (RuntimeException ex) {
            return pagedFluxError(logger, ex);
        }
    }

    /**
     * Lists the queues in the storage account that pass the filter.
     *
     * Pass true to {@link QueuesSegmentOptions#setIncludeMetadata(boolean) includeMetadata} to have metadata returned
     * for the queues.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>List all queues that begin with "azure"</p>
     *
     * {@codesnippet com.azure.storage.queue.queueServiceAsyncClient.listQueues#queueSergmentOptions}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/list-queues1">Azure Docs</a>.</p>
     *
     * @param options Options for listing queues
     * @return {@link QueueItem Queues} in the storage account that satisfy the filter requirements
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<QueueItem> listQueues(QueuesSegmentOptions options) {
        try {
            return listQueuesWithOptionalTimeout(null, options, null, Context.NONE);
        } catch (RuntimeException ex) {
            return pagedFluxError(logger, ex);
        }
    }

    /**
     * Lists the queues in the storage account that pass the filter starting at the specified marker.
     *
     * Pass true to {@link QueuesSegmentOptions#setIncludeMetadata(boolean) includeMetadata} to have metadata returned
     * for the queues.
     *
     * @param marker Starting point to list the queues
     * @param options Options for listing queues
     * @param timeout An optional timeout applied to the operation. If a response is not returned before the timeout
     * concludes a {@link RuntimeException} will be thrown.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return {@link QueueItem Queues} in the storage account that satisfy the filter requirements
     */
    PagedFlux<QueueItem> listQueuesWithOptionalTimeout(String marker, QueuesSegmentOptions options, Duration timeout,
        Context context) {
        final String prefix = (options != null) ? options.getPrefix() : null;
        final Integer maxResultsPerPage = (options != null) ? options.getMaxResultsPerPage() : null;
        final List<String> include = new ArrayList<>();

        if (options != null) {
            if (options.isIncludeMetadata()) {
                include.add("metadata");
            }
        }

        BiFunction<String, Integer, Mono<PagedResponse<QueueItem>>> retriever =
            (nextMarker, pageSize) -> StorageImplUtils.applyOptionalTimeout(this.client.getServices()
                .listQueuesSegmentSinglePageAsync(prefix, nextMarker,
                    pageSize == null ? maxResultsPerPage : pageSize, include,
                    null, null, context), timeout);

        return new PagedFlux<>(pageSize -> retriever.apply(marker, pageSize), retriever);
    }

    /**
     * Retrieves the properties of the storage account's Queue service. The properties range from storage analytics and
     * metric to CORS (Cross-Origin Resource Sharing).
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Retrieve Queue service properties</p>
     *
     * {@codesnippet com.azure.storage.queue.queueServiceAsyncClient.getProperties}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/get-queue-service-properties">Azure
     * Docs</a>.</p>
     *
     * @return Storage account {@link QueueServiceProperties Queue service properties}
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<QueueServiceProperties> getProperties() {
        try {
            return getPropertiesWithResponse().flatMap(FluxUtil::toMono);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Retrieves the properties of the storage account's Queue service. The properties range from storage analytics and
     * metric to CORS (Cross-Origin Resource Sharing).
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Retrieve Queue service properties</p>
     *
     * {@codesnippet com.azure.storage.queue.queueServiceAsyncClient.getPropertiesWithResponse}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/get-queue-service-properties">Azure
     * Docs</a>.</p>
     *
     * @return A response containing the Storage account {@link QueueServiceProperties Queue service properties}
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<QueueServiceProperties>> getPropertiesWithResponse() {
        try {
            return withContext(this::getPropertiesWithResponse);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<QueueServiceProperties>> getPropertiesWithResponse(Context context) {
        context = context == null ? Context.NONE : context;
        return client.getServices().getPropertiesWithResponseAsync(null, null,
            context.addData(AZ_TRACING_NAMESPACE_KEY, STORAGE_TRACING_NAMESPACE_VALUE))
            .map(response -> new SimpleResponse<>(response, response.getValue()));
    }

    /**
     * Sets the properties for the storage account's Queue service. The properties range from storage analytics and
     * metric to CORS (Cross-Origin Resource Sharing).
     *
     * To maintain the CORS in the Queue service pass a {@code null} value for {@link QueueServiceProperties#getCors()
     * CORS}. To disable all CORS in the Queue service pass an empty list for {@link QueueServiceProperties#getCors()
     * CORS}.
     *
     * <p><strong>Code Sample</strong></p>
     *
     * <p>Clear CORS in the Queue service</p>
     *
     * {@codesnippet com.azure.storage.queue.queueServiceAsyncClient.setProperties#QueueServiceProperties}
     *
     * <p>Enable Minute and Hour Metrics</p>
     *
     * {@codesnippet com.azure.storage.queue.queueServiceAsyncClient.setPropertiesEnableMetrics#QueueServiceProperties}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/set-queue-service-properties">Azure
     * Docs</a>.</p>
     *
     * @param properties Storage account Queue service properties
     * @return An empty response
     * @throws QueueStorageException When one of the following is true
     * <ul>
     * <li>A CORS rule is missing one of its fields</li>
     * <li>More than five CORS rules will exist for the Queue service</li>
     * <li>Size of all CORS rules exceeds 2KB</li>
     * <li>
     * Length of {@link QueueCorsRule#getAllowedHeaders() allowed headers}, {@link QueueCorsRule#getExposedHeaders()
     * exposed headers}, or {@link QueueCorsRule#getAllowedOrigins() allowed origins} exceeds 256 characters.
     * </li>
     * <li>{@link QueueCorsRule#getAllowedMethods() Allowed methods} isn't DELETE, GET, HEAD, MERGE, POST, OPTIONS, or
     * PUT</li>
     * </ul>
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> setProperties(QueueServiceProperties properties) {
        try {
            return setPropertiesWithResponse(properties).flatMap(FluxUtil::toMono);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Sets the properties for the storage account's Queue service. The properties range from storage analytics and
     * metric to CORS (Cross-Origin Resource Sharing).
     *
     * To maintain the CORS in the Queue service pass a {@code null} value for {@link QueueServiceProperties#getCors()
     * CORS}. To disable all CORS in the Queue service pass an empty list for {@link QueueServiceProperties#getCors()
     * CORS}.
     *
     * <p><strong>Code Sample</strong></p>
     *
     * <p>Clear CORS in the Queue service</p>
     *
     * {@codesnippet com.azure.storage.queue.queueServiceAsyncClient.setPropertiesWithResponse#QueueServiceProperties}
     *
     * <p>Enable Minute and Hour Metrics</p>
     *
     * {@codesnippet com.azure.storage.queue.queueServiceAsyncClient.setPropertiesWithResponseEnableMetrics#QueueServiceProperties}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/set-queue-service-properties">Azure
     * Docs</a>.</p>
     *
     * @param properties Storage account Queue service properties
     * @return A response that only contains headers and response status code
     * @throws QueueStorageException When one of the following is true
     * <ul>
     * <li>A CORS rule is missing one of its fields</li>
     * <li>More than five CORS rules will exist for the Queue service</li>
     * <li>Size of all CORS rules exceeds 2KB</li>
     * <li>
     * Length of {@link QueueCorsRule#getAllowedHeaders() allowed headers}, {@link QueueCorsRule#getExposedHeaders()
     * exposed headers}, or {@link QueueCorsRule#getAllowedOrigins() allowed origins} exceeds 256 characters.
     * </li>
     * <li>{@link QueueCorsRule#getAllowedMethods() Allowed methods} isn't DELETE, GET, HEAD, MERGE, POST, OPTIONS, or
     * PUT</li>
     * </ul>
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> setPropertiesWithResponse(QueueServiceProperties properties) {
        try {
            return withContext(context -> setPropertiesWithResponse(properties, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<Void>> setPropertiesWithResponse(QueueServiceProperties properties, Context context) {
        context = context == null ? Context.NONE : context;
        return client.getServices().setPropertiesWithResponseAsync(properties, null, null,
            context.addData(AZ_TRACING_NAMESPACE_KEY, STORAGE_TRACING_NAMESPACE_VALUE))
            .map(response -> new SimpleResponse<>(response, null));
    }

    /**
     * Retrieves the geo replication information about the Queue service.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Retrieve the geo replication information</p>
     *
     * {@codesnippet com.azure.storage.queue.queueServiceAsyncClient.getStatistics}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/get-queue-service-stats">Azure Docs</a>.</p>
     *
     * @return The geo replication information about the Queue service
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<QueueServiceStatistics> getStatistics() {
        try {
            return getStatisticsWithResponse().flatMap(FluxUtil::toMono);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Retrieves the geo replication information about the Queue service.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Retrieve the geo replication information</p>
     *
     * {@codesnippet com.azure.storage.queue.queueServiceAsyncClient.getStatisticsWithResponse}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/get-queue-service-stats">Azure Docs</a>.</p>
     *
     * @return A response containing the geo replication information about the Queue service
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<QueueServiceStatistics>> getStatisticsWithResponse() {
        try {
            return withContext(this::getStatisticsWithResponse);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<QueueServiceStatistics>> getStatisticsWithResponse(Context context) {
        context = context == null ? Context.NONE : context;
        return client.getServices().getStatisticsWithResponseAsync(null, null,
            context.addData(AZ_TRACING_NAMESPACE_KEY, STORAGE_TRACING_NAMESPACE_VALUE))
            .map(response -> new SimpleResponse<>(response, response.getValue()));
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
     * Gets the {@link HttpPipeline} powering this client.
     *
     * @return The pipeline.
     */
    public HttpPipeline getHttpPipeline() {
        return this.client.getHttpPipeline();
    }

    /**
     * Generates an account SAS for the Azure Storage account using the specified {@link AccountSasSignatureValues}.
     * <p>Note : The client must be authenticated via {@link StorageSharedKeyCredential}
     * <p>See {@link AccountSasSignatureValues} for more information on how to construct an account SAS.</p>
     *
     * <p>The snippet below generates a SAS that lasts for two days and gives the user read and list access to
     * queues and file shares.</p>
     * {@codesnippet com.azure.storage.queue.QueueServiceAsyncClient.generateAccountSas#AccountSasSignatureValues}
     *
     * @param accountSasSignatureValues {@link AccountSasSignatureValues}
     *
     * @return A {@code String} representing the SAS query parameters.
     */
    public String generateAccountSas(AccountSasSignatureValues accountSasSignatureValues) {
        return generateAccountSas(accountSasSignatureValues, Context.NONE);
    }

    /**
     * Generates an account SAS for the Azure Storage account using the specified {@link AccountSasSignatureValues}.
     * <p>Note : The client must be authenticated via {@link StorageSharedKeyCredential}
     * <p>See {@link AccountSasSignatureValues} for more information on how to construct an account SAS.</p>
     *
     * <p>The snippet below generates a SAS that lasts for two days and gives the user read and list access to
     * queues and file shares.</p>
     * {@codesnippet com.azure.storage.queue.QueueServiceAsyncClient.generateAccountSas#AccountSasSignatureValues-Context}
     *
     * @param accountSasSignatureValues {@link AccountSasSignatureValues}
     * @param context Additional context that is passed through the code when generating a SAS.
     *
     * @return A {@code String} representing the SAS query parameters.
     */
    public String generateAccountSas(AccountSasSignatureValues accountSasSignatureValues, Context context) {
        return new AccountSasImplUtil(accountSasSignatureValues, null)
            .generateSas(SasImplUtils.extractSharedKeyCredential(getHttpPipeline()), context);
    }

}
