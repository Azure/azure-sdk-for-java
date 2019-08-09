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
import com.azure.storage.queue.models.CorsRule;
import com.azure.storage.queue.models.ListQueuesIncludeType;
import com.azure.storage.queue.models.ListQueuesSegmentResponse;
import com.azure.storage.queue.models.QueueItem;
import com.azure.storage.queue.models.QueuesSegmentOptions;
import com.azure.storage.queue.models.ServicesListQueuesSegmentResponse;
import com.azure.storage.queue.models.StorageErrorException;
import com.azure.storage.queue.models.StorageServiceProperties;
import com.azure.storage.queue.models.StorageServiceStats;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * This class provides a client that contains all the operations for interacting with a queue account in Azure Storage.
 * Operations allowed by the client are creating, listing, and deleting queues, retrieving and updating properties of the account,
 * and retrieving statistics of the account.
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
public final class QueueServiceAsyncClient {
    private static final ClientLogger LOGGER = new ClientLogger(QueueServiceAsyncClient.class);
    private final AzureQueueStorageImpl client;

    /**
     * Creates a QueueServiceAsyncClient that sends requests to the storage account at {@code endpoint}.
     * Each service call goes through the {@code httpPipeline}.
     *
     * @param endpoint URL for the Storage Queue service
     * @param httpPipeline HttpPipeline that the HTTP requests and response flow through
     */
    QueueServiceAsyncClient(URL endpoint, HttpPipeline httpPipeline) {
        this.client = new AzureQueueStorageBuilder().pipeline(httpPipeline)
            .url(endpoint.toString())
            .build();
    }

    /**
     * @return the URL of the storage queue
     * @throws RuntimeException If the queue service is using a malformed URL.
     */
    public URL getQueueServiceUrl() {
        try {
            return new URL(client.getUrl());
        } catch (MalformedURLException ex) {
            LOGGER.error("Queue Service URL is malformed");
            throw new RuntimeException("Storage account URL is malformed");
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
     * Creates a queue in the storage account with the specified name and returns a QueueAsyncClient to interact
     * with it.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Create the queue "test"</p>
     *
     * {@codesnippet com.azure.storage.queue.queueServiceAsyncClient.createQueue#string}
     *
     * @param queueName Name of the queue
     * @return A response containing the QueueAsyncClient and the status of creating the queue
     * @throws StorageErrorException If a queue with the same name and different metadata already exists
     */
    public Mono<Response<QueueAsyncClient>> createQueue(String queueName) {
        return createQueue(queueName, null);
    }

    /**
     * Creates a queue in the storage account with the specified name and metadata and returns a QueueAsyncClient to
     * interact with it.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Create the queue "test" with metadata "queue:metadata"</p>
     *
     * {@codesnippet com.azure.storage.queue.queueServiceAsyncClient.createQueue#string-map}
     *
     * @param queueName Name of the queue
     * @param metadata Metadata to associate with the queue
     * @return A response containing the QueueAsyncClient and the status of creating the queue
     * @throws StorageErrorException If a queue with the same name and different metadata already exists
     */
    public Mono<Response<QueueAsyncClient>> createQueue(String queueName, Map<String, String> metadata) {
        QueueAsyncClient queueAsyncClient = new QueueAsyncClient(client, queueName);

        return queueAsyncClient.create(metadata)
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
     * @return A response that only contains headers and response status code
     * @throws StorageErrorException If the queue doesn't exist
     */
    public Mono<VoidResponse> deleteQueue(String queueName) {
        return new QueueAsyncClient(client, queueName).delete();
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
    public Flux<QueueItem> listQueues() {
        return listQueues(null, null);
    }

    /**
     * Lists the queues in the storage account that pass the filter.
     *
     * Pass true to {@link QueuesSegmentOptions#includeMetadata(boolean) includeMetadata} to have metadata returned for
     * the queues.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>List all queues that begin with "azure"</p>
     *
     * {@codesnippet com.azure.storage.queue.queueServiceClient.listQueues#queueSergmentOptions}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/list-queues1">Azure Docs</a>.</p>
     *
     * @param options Options for listing queues
     * @return {@link QueueItem Queues} in the storage account that satisfy the filter requirements
     */
    public Flux<QueueItem> listQueues(QueuesSegmentOptions options) {
        return listQueues(null, options);
    }

    /**
     * Lists the queues in the storage account that pass the filter starting at the specified marker.
     *
     * Pass true to {@link QueuesSegmentOptions#includeMetadata(boolean) includeMetadata} to have metadata returned for
     * the queues.
     *
     * @param marker Starting point to list the queues
     * @param options Options for listing queues
     * @return {@link QueueItem Queues} in the storage account that satisfy the filter requirements
     */
    Flux<QueueItem> listQueues(String marker, QueuesSegmentOptions options) {
        String prefix = null;
        Integer maxResults = null;
        final List<ListQueuesIncludeType> include = new ArrayList<>();

        if (options != null) {
            prefix = options.prefix();
            maxResults = options.maxResults();
            if (options.includeMetadata()) {
                include.add(ListQueuesIncludeType.fromString(ListQueuesIncludeType.METADATA.toString()));
            }
        }

        Mono<ServicesListQueuesSegmentResponse> result = client.services()
            .listQueuesSegmentWithRestResponseAsync(prefix, marker, maxResults, include, null, null, Context.NONE);

        return result.flatMapMany(response -> extractAndFetchQueues(response, include, Context.NONE));
    }

    /*
     * Helper function used to auto-enumerate through paged responses
     */
    private Flux<QueueItem> listQueues(ServicesListQueuesSegmentResponse response, List<ListQueuesIncludeType> include, Context context) {
        ListQueuesSegmentResponse value = response.value();
        Mono<ServicesListQueuesSegmentResponse> result = client.services()
            .listQueuesSegmentWithRestResponseAsync(value.prefix(), value.nextMarker(), value.maxResults(), include, null, null, context);

        return result.flatMapMany(r -> extractAndFetchQueues(r, include, context));
    }

    /*
     * Helper function used to auto-enumerate though paged responses
     */
    private Flux<QueueItem> extractAndFetchQueues(ServicesListQueuesSegmentResponse response, List<ListQueuesIncludeType> include, Context context) {
        String nextPageLink = response.value().nextMarker();
        if (nextPageLink == null) {
            return Flux.fromIterable(response.value().queueItems());
        }

        return Flux.fromIterable(response.value().queueItems()).concatWith(listQueues(response, include, context));
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
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/get-queue-service-properties">Azure Docs</a>.</p>
     *
     * @return Storage account Queue service properties
     */
    public Mono<Response<StorageServiceProperties>> getProperties() {
        return client.services().getPropertiesWithRestResponseAsync(Context.NONE)
            .map(response -> new SimpleResponse<>(response, response.value()));
    }

    /**
     * Sets the properties for the storage account's Queue service. The properties range from storage analytics and
     * metric to CORS (Cross-Origin Resource Sharing).
     *
     * To maintain the CORS in the Queue service pass a {@code null} value for {@link StorageServiceProperties#cors() CORS}.
     * To disable all CORS in the Queue service pass an empty list for {@link StorageServiceProperties#cors() CORS}.
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
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/set-queue-service-properties">Azure Docs</a>.</p>
     *
     * @param properties Storage account Queue service properties
     * @return A response that only contains headers and response status code
     * @throws StorageErrorException When one of the following is true
     * <ul>
     *     <li>A CORS rule is missing one of its fields</li>
     *     <li>More than five CORS rules will exist for the Queue service</li>
     *     <li>Size of all CORS rules exceeds 2KB</li>
     *     <li>
     *         Length of {@link CorsRule#allowedHeaders() allowed headers}, {@link CorsRule#exposedHeaders() exposed headers},
     *         or {@link CorsRule#allowedOrigins() allowed origins} exceeds 256 characters.
     *     </li>
     *     <li>{@link CorsRule#allowedMethods() Allowed methods} isn't DELETE, GET, HEAD, MERGE, POST, OPTIONS, or PUT</li>
     * </ul>
     */
    public Mono<VoidResponse> setProperties(StorageServiceProperties properties) {
        return client.services().setPropertiesWithRestResponseAsync(properties, Context.NONE)
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
    public Mono<Response<StorageServiceStats>> getStatistics() {
        return client.services().getStatisticsWithRestResponseAsync(Context.NONE)
            .map(response -> new SimpleResponse<>(response, response.value()));
    }
}
