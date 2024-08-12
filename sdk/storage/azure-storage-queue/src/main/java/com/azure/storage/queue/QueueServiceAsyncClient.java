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

/**
 * This class provides a client that contains all the operations for interacting with a queue account in Azure Storage.
 * Operations allowed by the client are creating, listing, and deleting queues, retrieving and updating properties of
 * the account, and retrieving statistics of the account.
 *
 * <p><strong>Instantiating an Asynchronous Queue Service Client</strong></p>
 *
 * <!-- src_embed com.azure.storage.queue.queueServiceAsyncClient.instantiation -->
 * <pre>
 * QueueServiceAsyncClient client = new QueueServiceClientBuilder&#40;&#41;
 *     .connectionString&#40;&quot;connectionstring&quot;&#41;
 *     .endpoint&#40;&quot;endpoint&quot;&#41;
 *     .buildAsyncClient&#40;&#41;;
 * </pre>
 * <!-- end com.azure.storage.queue.queueServiceAsyncClient.instantiation -->
 *
 * <p>View {@link QueueServiceClientBuilder this} for additional ways to construct the client.</p>
 *
 * @see QueueServiceClientBuilder
 * @see QueueServiceClient
 * @see StorageSharedKeyCredential
 */
@ServiceClient(builder = QueueServiceClientBuilder.class, isAsync = true)
public final class QueueServiceAsyncClient {
    private static final ClientLogger LOGGER = new ClientLogger(QueueServiceAsyncClient.class);
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
     * Gets the URL of the storage queue.
     *
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
        QueueClient queueClient = new QueueClient(client, queueName, accountName, serviceVersion, messageEncoding,
            processMessageDecodingErrorAsyncHandler, processMessageDecodingErrorHandler, null);
        return new QueueAsyncClient(client, queueName, accountName, serviceVersion, messageEncoding,
            processMessageDecodingErrorAsyncHandler, processMessageDecodingErrorHandler, queueClient);
    }

    /**
     * Creates a queue in the storage account with the specified name and returns a QueueAsyncClient to interact with
     * it.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Create the queue "test"</p>
     *
     * <!-- src_embed com.azure.storage.queue.queueServiceAsyncClient.createQueue#string -->
     * <pre>
     * client.createQueue&#40;&quot;myqueue&quot;&#41;.subscribe&#40;
     *     response -&gt; &#123;
     *     &#125;,
     *     error -&gt; System.err.print&#40;error.toString&#40;&#41;&#41;,
     *     &#40;&#41; -&gt; System.out.println&#40;&quot;Complete creating the queue!&quot;&#41;
     * &#41;;
     * </pre>
     * <!-- end com.azure.storage.queue.queueServiceAsyncClient.createQueue#string -->
     *
     * @param queueName Name of the queue
     * @return The {@link QueueAsyncClient QueueAsyncClient}
     * @throws QueueStorageException If a queue with the same name and different metadata already exists
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<QueueAsyncClient> createQueue(String queueName) {
        return createQueueWithResponse(queueName, null).flatMap(FluxUtil::toMono);
    }

    /**
     * Creates a queue in the storage account with the specified name and metadata and returns a QueueAsyncClient to
     * interact with it.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Create the queue "test" with metadata "queue:metadata"</p>
     *
     * <!-- src_embed com.azure.storage.queue.queueServiceAsyncClient.createQueueWithResponse#string-map -->
     * <pre>
     * client.createQueueWithResponse&#40;&quot;myqueue&quot;, Collections.singletonMap&#40;&quot;queue&quot;, &quot;metadata&quot;&#41;&#41;
     *     .subscribe&#40;
     *         response -&gt; System.out.printf&#40;&quot;Creating the queue with status code %d&quot;, response.getStatusCode&#40;&#41;&#41;,
     *         error -&gt; System.err.print&#40;error.toString&#40;&#41;&#41;,
     *         &#40;&#41; -&gt; System.out.println&#40;&quot;Complete creating the queue!&quot;&#41;
     *     &#41;;
     * </pre>
     * <!-- end com.azure.storage.queue.queueServiceAsyncClient.createQueueWithResponse#string-map -->
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
            QueueAsyncClient queueAsyncClient = getQueueAsyncClient(queueName);
            return queueAsyncClient.createWithResponse(metadata)
                .map(response -> new SimpleResponse<>(response, queueAsyncClient));
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    /**
     * Deletes a queue in the storage account
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Delete the queue "test"</p>
     *
     * <!-- src_embed com.azure.storage.queue.queueServiceAsyncClient.deleteQueue#string -->
     * <pre>
     * client.deleteQueue&#40;&quot;myshare&quot;&#41;.subscribe&#40;
     *     response -&gt; System.out.println&#40;&quot;Deleting the queue completed.&quot;&#41;
     * &#41;;
     * </pre>
     * <!-- end com.azure.storage.queue.queueServiceAsyncClient.deleteQueue#string -->
     *
     * @param queueName Name of the queue
     * @return An empty response
     * @throws QueueStorageException If the queue doesn't exist
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> deleteQueue(String queueName) {
        return deleteQueueWithResponse(queueName).flatMap(FluxUtil::toMono);
    }

    /**
     * Deletes a queue in the storage account
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Delete the queue "test"</p>
     *
     * <!-- src_embed com.azure.storage.queue.queueServiceAsyncClient.deleteQueueWithResponse#string -->
     * <pre>
     * client.deleteQueueWithResponse&#40;&quot;myshare&quot;&#41;.subscribe&#40;
     *     response -&gt; System.out.println&#40;&quot;Deleting the queue completed with status code: &quot; + response.getStatusCode&#40;&#41;&#41;
     * &#41;;
     * </pre>
     * <!-- end com.azure.storage.queue.queueServiceAsyncClient.deleteQueueWithResponse#string -->
     *
     * @param queueName Name of the queue
     * @return A response that only contains headers and response status code
     * @throws QueueStorageException If the queue doesn't exist
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> deleteQueueWithResponse(String queueName) {
        try {
            return getQueueAsyncClient(queueName).deleteWithResponse();
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    /**
     * Lists all queues in the storage account without their metadata.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>List all queues in the account</p>
     *
     * <!-- src_embed com.azure.storage.queue.queueServiceAsyncClient.listQueues -->
     * <pre>
     * client.listQueues&#40;&#41;.subscribe&#40;
     *     queueItem -&gt; System.out.printf&#40;&quot;Queue %s exists in the account&quot;, queueItem.getName&#40;&#41;&#41;,
     *     error -&gt; System.err.print&#40;error.toString&#40;&#41;&#41;,
     *     &#40;&#41; -&gt; System.out.println&#40;&quot;Complete listing the queues!&quot;&#41;
     * &#41;;
     * </pre>
     * <!-- end com.azure.storage.queue.queueServiceAsyncClient.listQueues -->
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
            return pagedFluxError(LOGGER, ex);
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
     * <!-- src_embed com.azure.storage.queue.queueServiceAsyncClient.listQueues#queueSergmentOptions -->
     * <pre>
     * client.listQueues&#40;new QueuesSegmentOptions&#40;&#41;.setPrefix&#40;&quot;azure&quot;&#41;&#41;.subscribe&#40;
     *     queueItem -&gt; System.out.printf&#40;&quot;Queue %s exists in the account and has metadata %s&quot;,
     *         queueItem.getName&#40;&#41;, queueItem.getMetadata&#40;&#41;&#41;,
     *     error -&gt; System.err.print&#40;error.toString&#40;&#41;&#41;,
     *     &#40;&#41; -&gt; System.out.println&#40;&quot;Complete listing the queues!&quot;&#41;
     * &#41;;
     * </pre>
     * <!-- end com.azure.storage.queue.queueServiceAsyncClient.listQueues#queueSergmentOptions -->
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
            return pagedFluxError(LOGGER, ex);
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
     * <!-- src_embed com.azure.storage.queue.queueServiceAsyncClient.getProperties -->
     * <pre>
     * client.getProperties&#40;&#41;
     *     .subscribe&#40;properties -&gt; &#123;
     *         System.out.printf&#40;&quot;Hour metrics enabled: %b, Minute metrics enabled: %b&quot;,
     *             properties.getHourMetrics&#40;&#41;.isEnabled&#40;&#41;, properties.getMinuteMetrics&#40;&#41;.isEnabled&#40;&#41;&#41;;
     *     &#125;&#41;;
     * </pre>
     * <!-- end com.azure.storage.queue.queueServiceAsyncClient.getProperties -->
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/get-queue-service-properties">Azure
     * Docs</a>.</p>
     *
     * @return Storage account {@link QueueServiceProperties Queue service properties}
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<QueueServiceProperties> getProperties() {
        return getPropertiesWithResponse().flatMap(FluxUtil::toMono);
    }

    /**
     * Retrieves the properties of the storage account's Queue service. The properties range from storage analytics and
     * metric to CORS (Cross-Origin Resource Sharing).
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Retrieve Queue service properties</p>
     *
     * <!-- src_embed com.azure.storage.queue.queueServiceAsyncClient.getPropertiesWithResponse -->
     * <pre>
     * client.getPropertiesWithResponse&#40;&#41;
     *     .subscribe&#40;response -&gt; &#123;
     *         QueueServiceProperties properties = response.getValue&#40;&#41;;
     *         System.out.printf&#40;&quot;Hour metrics enabled: %b, Minute metrics enabled: %b&quot;,
     *             properties.getHourMetrics&#40;&#41;.isEnabled&#40;&#41;, properties.getMinuteMetrics&#40;&#41;.isEnabled&#40;&#41;&#41;;
     *     &#125;&#41;;
     * </pre>
     * <!-- end com.azure.storage.queue.queueServiceAsyncClient.getPropertiesWithResponse -->
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
            return monoError(LOGGER, ex);
        }
    }

    Mono<Response<QueueServiceProperties>> getPropertiesWithResponse(Context context) {
        context = context == null ? Context.NONE : context;
        return client.getServices().getPropertiesWithResponseAsync(null, null, context)
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
     * <!-- src_embed com.azure.storage.queue.queueServiceAsyncClient.setProperties#QueueServiceProperties -->
     * <pre>
     * QueueServiceProperties properties = client.getProperties&#40;&#41;.block&#40;&#41;;
     * client.setProperties&#40;properties&#41;
     *     .doOnSuccess&#40;response -&gt; System.out.println&#40;&quot;Setting Queue service properties completed.&quot;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.queue.queueServiceAsyncClient.setProperties#QueueServiceProperties -->
     *
     * <p>Enable Minute and Hour Metrics</p>
     *
     * <!-- src_embed com.azure.storage.queue.queueServiceAsyncClient.setPropertiesEnableMetrics#QueueServiceProperties -->
     * <pre>
     * QueueServiceProperties properties = client.getProperties&#40;&#41;.block&#40;&#41;;
     * properties.getMinuteMetrics&#40;&#41;.setEnabled&#40;true&#41;;
     * properties.getHourMetrics&#40;&#41;.setEnabled&#40;true&#41;;
     * client.setProperties&#40;properties&#41;.subscribe&#40;
     *     response -&gt; System.out.println&#40;&quot;Setting Queue service properties completed.&quot;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.queue.queueServiceAsyncClient.setPropertiesEnableMetrics#QueueServiceProperties -->
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
        return setPropertiesWithResponse(properties).flatMap(FluxUtil::toMono);
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
     * <!-- src_embed com.azure.storage.queue.queueServiceAsyncClient.setPropertiesWithResponse#QueueServiceProperties -->
     * <pre>
     * QueueServiceProperties properties = client.getProperties&#40;&#41;.block&#40;&#41;;
     * client.setPropertiesWithResponse&#40;properties&#41;
     *     .subscribe&#40;response -&gt; System.out.printf&#40;&quot;Setting Queue service properties completed with status code %d&quot;,
     *         response.getStatusCode&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.queue.queueServiceAsyncClient.setPropertiesWithResponse#QueueServiceProperties -->
     *
     * <p>Enable Minute and Hour Metrics</p>
     *
     * <!-- src_embed com.azure.storage.queue.queueServiceAsyncClient.setPropertiesWithResponseEnableMetrics#QueueServiceProperties -->
     * <pre>
     * QueueServiceProperties properties = client.getProperties&#40;&#41;.block&#40;&#41;;
     * properties.getMinuteMetrics&#40;&#41;.setEnabled&#40;true&#41;;
     * properties.getHourMetrics&#40;&#41;.setEnabled&#40;true&#41;;
     * client.setPropertiesWithResponse&#40;properties&#41;
     *     .subscribe&#40;response -&gt; System.out.printf&#40;&quot;Setting Queue service properties completed with status code %d&quot;,
     *         response.getStatusCode&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.queue.queueServiceAsyncClient.setPropertiesWithResponseEnableMetrics#QueueServiceProperties -->
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
            return monoError(LOGGER, ex);
        }
    }

    Mono<Response<Void>> setPropertiesWithResponse(QueueServiceProperties properties, Context context) {
        context = context == null ? Context.NONE : context;
        return client.getServices().setPropertiesNoCustomHeadersWithResponseAsync(properties, null, null, context);
    }

    /**
     * Retrieves the geo replication information about the Queue service.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Retrieve the geo replication information</p>
     *
     * <!-- src_embed com.azure.storage.queue.queueServiceAsyncClient.getStatistics -->
     * <pre>
     * client.getStatistics&#40;&#41;
     *     .subscribe&#40;stats -&gt; &#123;
     *         System.out.printf&#40;&quot;Geo replication status: %s, Last synced: %s&quot;,
     *             stats.getGeoReplication&#40;&#41;.getStatus&#40;&#41;, stats.getGeoReplication&#40;&#41;.getLastSyncTime&#40;&#41;&#41;;
     *     &#125;&#41;;
     * </pre>
     * <!-- end com.azure.storage.queue.queueServiceAsyncClient.getStatistics -->
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/get-queue-service-stats">Azure Docs</a>.</p>
     *
     * @return The geo replication information about the Queue service
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<QueueServiceStatistics> getStatistics() {
        return getStatisticsWithResponse().flatMap(FluxUtil::toMono);
    }

    /**
     * Retrieves the geo replication information about the Queue service.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Retrieve the geo replication information</p>
     *
     * <!-- src_embed com.azure.storage.queue.queueServiceAsyncClient.getStatisticsWithResponse -->
     * <pre>
     * client.getStatisticsWithResponse&#40;&#41;
     *     .subscribe&#40;response -&gt; &#123;
     *         QueueServiceStatistics stats = response.getValue&#40;&#41;;
     *         System.out.printf&#40;&quot;Geo replication status: %s, Last synced: %s&quot;,
     *             stats.getGeoReplication&#40;&#41;.getStatus&#40;&#41;, stats.getGeoReplication&#40;&#41;.getLastSyncTime&#40;&#41;&#41;;
     *     &#125;&#41;;
     * </pre>
     * <!-- end com.azure.storage.queue.queueServiceAsyncClient.getStatisticsWithResponse -->
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
            return monoError(LOGGER, ex);
        }
    }

    Mono<Response<QueueServiceStatistics>> getStatisticsWithResponse(Context context) {
        context = context == null ? Context.NONE : context;
        return client.getServices().getStatisticsWithResponseAsync(null, null, context)
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
     * <!-- src_embed com.azure.storage.queue.QueueServiceAsyncClient.generateAccountSas#AccountSasSignatureValues -->
     * <pre>
     * AccountSasPermission permissions = new AccountSasPermission&#40;&#41;
     *     .setListPermission&#40;true&#41;
     *     .setReadPermission&#40;true&#41;;
     * AccountSasResourceType resourceTypes = new AccountSasResourceType&#40;&#41;.setContainer&#40;true&#41;.setObject&#40;true&#41;;
     * AccountSasService services = new AccountSasService&#40;&#41;.setQueueAccess&#40;true&#41;.setFileAccess&#40;true&#41;;
     * OffsetDateTime expiryTime = OffsetDateTime.now&#40;&#41;.plus&#40;Duration.ofDays&#40;2&#41;&#41;;
     *
     * AccountSasSignatureValues sasValues =
     *     new AccountSasSignatureValues&#40;expiryTime, permissions, services, resourceTypes&#41;;
     *
     * &#47;&#47; Client must be authenticated via StorageSharedKeyCredential
     * String sas = queueServiceAsyncClient.generateAccountSas&#40;sasValues&#41;;
     * </pre>
     * <!-- end com.azure.storage.queue.QueueServiceAsyncClient.generateAccountSas#AccountSasSignatureValues -->
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
     * <!-- src_embed com.azure.storage.queue.QueueServiceAsyncClient.generateAccountSas#AccountSasSignatureValues-Context -->
     * <pre>
     * AccountSasPermission permissions = new AccountSasPermission&#40;&#41;
     *     .setListPermission&#40;true&#41;
     *     .setReadPermission&#40;true&#41;;
     * AccountSasResourceType resourceTypes = new AccountSasResourceType&#40;&#41;.setContainer&#40;true&#41;.setObject&#40;true&#41;;
     * AccountSasService services = new AccountSasService&#40;&#41;.setQueueAccess&#40;true&#41;.setFileAccess&#40;true&#41;;
     * OffsetDateTime expiryTime = OffsetDateTime.now&#40;&#41;.plus&#40;Duration.ofDays&#40;2&#41;&#41;;
     *
     * AccountSasSignatureValues sasValues =
     *     new AccountSasSignatureValues&#40;expiryTime, permissions, services, resourceTypes&#41;;
     *
     * &#47;&#47; Client must be authenticated via StorageSharedKeyCredential
     * String sas = queueServiceAsyncClient.generateAccountSas&#40;sasValues, new Context&#40;&quot;key&quot;, &quot;value&quot;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.queue.QueueServiceAsyncClient.generateAccountSas#AccountSasSignatureValues-Context -->
     *
     * @param accountSasSignatureValues {@link AccountSasSignatureValues}
     * @param context Additional context that is passed through the code when generating a SAS.
     *
     * @return A {@code String} representing the SAS query parameters.
     */
    public String generateAccountSas(AccountSasSignatureValues accountSasSignatureValues, Context context) {
        return generateAccountSas(accountSasSignatureValues, null, context);
    }

    /**
     * Generates an account SAS for the Azure Storage account using the specified {@link AccountSasSignatureValues}.
     * <p>Note : The client must be authenticated via {@link StorageSharedKeyCredential}
     * <p>See {@link AccountSasSignatureValues} for more information on how to construct an account SAS.</p>
     *
     * @param accountSasSignatureValues {@link AccountSasSignatureValues}
     * @param stringToSignHandler For debugging purposes only. Returns the string to sign that was used to generate the
     * signature.
     * @param context Additional context that is passed through the code when generating a SAS.
     *
     * @return A {@code String} representing the SAS query parameters.
     */
    public String generateAccountSas(AccountSasSignatureValues accountSasSignatureValues,
        Consumer<String> stringToSignHandler, Context context) {
        return new AccountSasImplUtil(accountSasSignatureValues, null)
            .generateSas(SasImplUtils.extractSharedKeyCredential(getHttpPipeline()), stringToSignHandler, context);
    }
}
