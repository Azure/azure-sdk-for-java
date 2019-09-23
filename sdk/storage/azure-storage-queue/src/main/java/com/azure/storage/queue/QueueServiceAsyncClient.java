// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.storage.queue;

import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedResponse;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.http.rest.VoidResponse;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.implementation.http.PagedResponseBase;
import com.azure.core.implementation.util.FluxUtil;
import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.common.AccountSASPermission;
import com.azure.storage.common.AccountSASResourceType;
import com.azure.storage.common.AccountSASService;
import com.azure.storage.common.AccountSASSignatureValues;
import com.azure.storage.common.IPRange;
import com.azure.storage.common.SASProtocol;
import com.azure.storage.common.Utility;
import com.azure.storage.common.credentials.SASTokenCredential;
import com.azure.storage.common.credentials.SharedKeyCredential;
import com.azure.storage.queue.implementation.AzureQueueStorageImpl;
import com.azure.storage.queue.models.CorsRule;
import com.azure.storage.queue.models.ListQueuesIncludeType;
import com.azure.storage.queue.models.QueueItem;
import com.azure.storage.queue.models.QueuesSegmentOptions;
import com.azure.storage.queue.models.StorageException;
import com.azure.storage.queue.models.StorageServiceProperties;
import com.azure.storage.queue.models.StorageServiceStats;
import reactor.core.publisher.Mono;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

import static com.azure.core.implementation.util.FluxUtil.withContext;
import static com.azure.storage.queue.PostProcessor.postProcessResponse;

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
 * @see SharedKeyCredential
 * @see SASTokenCredential
 */
@ServiceClient(builder = QueueServiceClientBuilder.class, isAsync = true)
public final class QueueServiceAsyncClient {
    private final ClientLogger logger = new ClientLogger(QueueServiceAsyncClient.class);
    private final AzureQueueStorageImpl client;

    /**
     * Creates a QueueServiceAsyncClient from the passed {@link AzureQueueStorageImpl implementation client}.
     *
     * @param azureQueueStorage Client that interacts with the service interfaces.
     */
    QueueServiceAsyncClient(AzureQueueStorageImpl azureQueueStorage) {
        this.client = azureQueueStorage;
    }

    /**
     * @return the URL of the storage queue
     * @throws RuntimeException If the queue service is using a malformed URL.
     */
    public URL getQueueServiceUrl() {
        try {
            return new URL(client.getUrl());
        } catch (MalformedURLException ex) {
            logger.error("Queue Service URL is malformed");
            throw logger.logExceptionAsError(new RuntimeException("Storage account URL is malformed"));
        }
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
        return new QueueAsyncClient(client, queueName);
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
     * @throws StorageException If a queue with the same name and different metadata already exists
     */
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
     * {@codesnippet com.azure.storage.queue.queueServiceAsyncClient.createQueueWithResponse#string-map}
     *
     * @param queueName Name of the queue
     * @param metadata Metadata to associate with the queue
     * @return A response containing the {@link QueueAsyncClient QueueAsyncClient} and the status of creating the queue
     * @throws StorageException If a queue with the same name and different metadata already exists
     */
    public Mono<Response<QueueAsyncClient>> createQueueWithResponse(String queueName, Map<String, String> metadata) {
        Objects.requireNonNull(queueName);
        return withContext(context -> createQueueWithResponse(queueName, metadata, context));
    }

    Mono<Response<QueueAsyncClient>> createQueueWithResponse(String queueName, Map<String, String> metadata,
        Context context) {
        QueueAsyncClient queueAsyncClient = new QueueAsyncClient(client, queueName);

        return postProcessResponse(queueAsyncClient.createWithResponse(metadata, context))
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
     * @throws StorageException If the queue doesn't exist
     */
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
     * {@codesnippet com.azure.storage.queue.queueServiceAsyncClient.deleteQueueWithResponse#string}
     *
     * @param queueName Name of the queue
     * @return A response that only contains headers and response status code
     * @throws StorageException If the queue doesn't exist
     */
    public Mono<VoidResponse> deleteQueueWithResponse(String queueName) {
        return withContext(context -> deleteQueueWithResponse(queueName, context));
    }

    Mono<VoidResponse> deleteQueueWithResponse(String queueName, Context context) {
        return new QueueAsyncClient(client, queueName).deleteWithResponse(context);
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
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/list-queues1">Azure Docs</a>.</p>
     *
     * @return {@link QueueItem Queues} in the storage account
     */
    public PagedFlux<QueueItem> listQueues() {
        return listQueuesWithOptionalTimeout(null, null, null, Context.NONE);
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
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/list-queues1">Azure Docs</a>.</p>
     *
     * @param options Options for listing queues
     * @return {@link QueueItem Queues} in the storage account that satisfy the filter requirements
     */
    public PagedFlux<QueueItem> listQueues(QueuesSegmentOptions options) {
        return listQueuesWithOptionalTimeout(null, options, null, Context.NONE);
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
        final Integer maxResults = (options != null) ? options.getMaxResults() : null;
        final List<ListQueuesIncludeType> include = new ArrayList<>();

        if (options != null) {
            if (options.isIncludeMetadata()) {
                include.add(ListQueuesIncludeType.fromString(ListQueuesIncludeType.METADATA.toString()));
            }
        }

        Function<String, Mono<PagedResponse<QueueItem>>> retriever =
            nextMarker -> postProcessResponse(Utility.applyOptionalTimeout(this.client.services()
                .listQueuesSegmentWithRestResponseAsync(prefix, nextMarker, maxResults, include,
                    null, null, context), timeout)
                .map(response -> new PagedResponseBase<>(response.getRequest(),
                    response.getStatusCode(),
                    response.getHeaders(),
                    response.getValue().getQueueItems(),
                    response.getValue().getNextMarker(),
                    response.getDeserializedHeaders())));

        return new PagedFlux<>(() -> retriever.apply(marker), retriever);
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
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/get-queue-service-properties">Azure
     * Docs</a>.</p>
     *
     * @return Storage account {@link StorageServiceProperties Queue service properties}
     */
    public Mono<StorageServiceProperties> getProperties() {
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
     * {@codesnippet com.azure.storage.queue.queueServiceAsyncClient.getPropertiesWithResponse}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/get-queue-service-properties">Azure
     * Docs</a>.</p>
     *
     * @return A response containing the Storage account {@link StorageServiceProperties Queue service properties}
     */
    public Mono<Response<StorageServiceProperties>> getPropertiesWithResponse() {
        return withContext(this::getPropertiesWithResponse);
    }

    Mono<Response<StorageServiceProperties>> getPropertiesWithResponse(Context context) {
        return postProcessResponse(client.services().getPropertiesWithRestResponseAsync(context))
            .map(response -> new SimpleResponse<>(response, response.getValue()));
    }

    /**
     * Sets the properties for the storage account's Queue service. The properties range from storage analytics and
     * metric to CORS (Cross-Origin Resource Sharing).
     *
     * To maintain the CORS in the Queue service pass a {@code null} value for {@link StorageServiceProperties#getCors()
     * CORS}. To disable all CORS in the Queue service pass an empty list for {@link StorageServiceProperties#getCors()
     * CORS}.
     *
     * <p><strong>Code Sample</strong></p>
     *
     * <p>Clear CORS in the Queue service</p>
     *
     * {@codesnippet com.azure.storage.queue.queueServiceAsyncClient.setProperties#storageServiceProperties}
     *
     * <p>Enable Minute and Hour Metrics</p>
     *
     * {@codesnippet com.azure.storage.queue.queueServiceAsyncClient.setPropertiesEnableMetrics#storageServiceProperties}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/set-queue-service-properties">Azure
     * Docs</a>.</p>
     *
     * @param properties Storage account Queue service properties
     * @return An empty response
     * @throws StorageException When one of the following is true
     * <ul>
     * <li>A CORS rule is missing one of its fields</li>
     * <li>More than five CORS rules will exist for the Queue service</li>
     * <li>Size of all CORS rules exceeds 2KB</li>
     * <li>
     * Length of {@link CorsRule#getAllowedHeaders() allowed headers}, {@link CorsRule#getExposedHeaders() exposed
     * headers}, or {@link CorsRule#getAllowedOrigins() allowed origins} exceeds 256 characters.
     * </li>
     * <li>{@link CorsRule#getAllowedMethods() Allowed methods} isn't DELETE, GET, HEAD, MERGE, POST, OPTIONS, or
     * PUT</li>
     * </ul>
     */
    public Mono<Void> setProperties(StorageServiceProperties properties) {
        return setPropertiesWithResponse(properties).flatMap(FluxUtil::toMono);
    }

    /**
     * Sets the properties for the storage account's Queue service. The properties range from storage analytics and
     * metric to CORS (Cross-Origin Resource Sharing).
     *
     * To maintain the CORS in the Queue service pass a {@code null} value for {@link StorageServiceProperties#getCors()
     * CORS}. To disable all CORS in the Queue service pass an empty list for {@link StorageServiceProperties#getCors()
     * CORS}.
     *
     * <p><strong>Code Sample</strong></p>
     *
     * <p>Clear CORS in the Queue service</p>
     *
     * {@codesnippet com.azure.storage.queue.queueServiceAsyncClient.setPropertiesWithResponse#storageServiceProperties}
     *
     * <p>Enable Minute and Hour Metrics</p>
     *
     * {@codesnippet com.azure.storage.queue.queueServiceAsyncClient.setPropertiesWithResponseEnableMetrics#storageServiceProperties}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/set-queue-service-properties">Azure
     * Docs</a>.</p>
     *
     * @param properties Storage account Queue service properties
     * @return A response that only contains headers and response status code
     * @throws StorageException When one of the following is true
     * <ul>
     * <li>A CORS rule is missing one of its fields</li>
     * <li>More than five CORS rules will exist for the Queue service</li>
     * <li>Size of all CORS rules exceeds 2KB</li>
     * <li>
     * Length of {@link CorsRule#getAllowedHeaders() allowed headers}, {@link CorsRule#getExposedHeaders() exposed
     * headers}, or {@link CorsRule#getAllowedOrigins() allowed origins} exceeds 256 characters.
     * </li>
     * <li>{@link CorsRule#getAllowedMethods() Allowed methods} isn't DELETE, GET, HEAD, MERGE, POST, OPTIONS, or
     * PUT</li>
     * </ul>
     */
    public Mono<VoidResponse> setPropertiesWithResponse(StorageServiceProperties properties) {
        return withContext(context -> setPropertiesWithResponse(properties, context));
    }

    Mono<VoidResponse> setPropertiesWithResponse(StorageServiceProperties properties, Context context) {
        return postProcessResponse(client.services().setPropertiesWithRestResponseAsync(properties, context))
            .map(VoidResponse::new);
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
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/get-queue-service-stats">Azure Docs</a>.</p>
     *
     * @return The geo replication information about the Queue service
     */
    public Mono<StorageServiceStats> getStatistics() {
        return getStatisticsWithResponse().flatMap(FluxUtil::toMono);
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
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/get-queue-service-stats">Azure Docs</a>.</p>
     *
     * @return A response containing the geo replication information about the Queue service
     */
    public Mono<Response<StorageServiceStats>> getStatisticsWithResponse() {
        return withContext(this::getStatisticsWithResponse);
    }

    Mono<Response<StorageServiceStats>> getStatisticsWithResponse(Context context) {
        return postProcessResponse(client.services().getStatisticsWithRestResponseAsync(context))
            .map(response -> new SimpleResponse<>(response, response.getValue()));
    }

    /**
     * Generates an account SAS token with the specified parameters
     *
     * @param accountSASService The {@code AccountSASService} services for the account SAS
     * @param accountSASResourceType An optional {@code AccountSASResourceType} resources for the account SAS
     * @param accountSASPermission The {@code AccountSASPermission} permission for the account SAS
     * @param expiryTime The {@code OffsetDateTime} expiry time for the account SAS
     * @return A string that represents the SAS token
     */
    public String generateAccountSAS(AccountSASService accountSASService, AccountSASResourceType accountSASResourceType,
        AccountSASPermission accountSASPermission, OffsetDateTime expiryTime) {
        return this.generateAccountSAS(accountSASService, accountSASResourceType, accountSASPermission, expiryTime,
            null /* startTime */, null /* version */, null /* ipRange */, null /* sasProtocol */);
    }

    /**
     * Generates an account SAS token with the specified parameters
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.queue.queueServiceAsyncClient.generateAccountSAS#AccountSASService-AccountSASResourceType-AccountSASPermission-OffsetDateTime-OffsetDateTime-String-IPRange-SASProtocol}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/create-account-sas">Azure Docs</a>.</p>
     *
     * @param accountSASService The {@code AccountSASService} services for the account SAS
     * @param accountSASResourceType An optional {@code AccountSASResourceType} resources for the account SAS
     * @param accountSASPermission The {@code AccountSASPermission} permission for the account SAS
     * @param expiryTime The {@code OffsetDateTime} expiry time for the account SAS
     * @param startTime The {@code OffsetDateTime} start time for the account SAS
     * @param version The {@code String} version for the account SAS
     * @param ipRange An optional {@code IPRange} ip address range for the SAS
     * @param sasProtocol An optional {@code SASProtocol} protocol for the SAS
     * @return A string that represents the SAS token
     */
    public String generateAccountSAS(AccountSASService accountSASService, AccountSASResourceType accountSASResourceType,
        AccountSASPermission accountSASPermission, OffsetDateTime expiryTime, OffsetDateTime startTime, String version,
        IPRange ipRange, SASProtocol sasProtocol) {

        SharedKeyCredential sharedKeyCredential = Utility.getSharedKeyCredential(this.client.getHttpPipeline());
        Utility.assertNotNull("sharedKeyCredential", sharedKeyCredential);

        return AccountSASSignatureValues.generateAccountSAS(sharedKeyCredential, accountSASService,
            accountSASResourceType, accountSASPermission, expiryTime, startTime, version, ipRange, sasProtocol);

    }
}
