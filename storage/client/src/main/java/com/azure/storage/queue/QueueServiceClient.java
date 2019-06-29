// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.storage.queue;

import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.http.rest.VoidResponse;
import com.azure.storage.common.credentials.SASTokenCredential;
import com.azure.storage.common.credentials.SharedKeyCredential;
import com.azure.storage.queue.models.CorsRule;
import com.azure.storage.queue.models.QueueItem;
import com.azure.storage.queue.models.QueuesSegmentOptions;
import com.azure.storage.queue.models.StorageErrorException;
import com.azure.storage.queue.models.StorageServiceProperties;
import com.azure.storage.queue.models.StorageServiceStats;
import java.net.URL;
import java.util.Map;

/**
 * This class provides a client that contains all the operations for interacting with a queue account in Azure Storage.
 * Operations allowed by the client are creating, listing, and deleting queues, retrieving and updating properties of the account,
 * and retrieving statistics of the account.
 *
 * <p><strong>Instantiating an Synchronous Queue Service Client</strong></p>
 *
 * <pre>
 * QueueServiceClient client = QueueServiceClient.builder()
 *     .connectionString(connectionString)
 *     .endpoint(endpoint)
 *     .build();
 * </pre>
 *
 * <p>View {@link QueueServiceClientBuilder this} for additional ways to construct the client.</p>
 *
 * @see QueueServiceClientBuilder
 * @see QueueServiceAsyncClient
 * @see SharedKeyCredential
 * @see SASTokenCredential
 */
public final class QueueServiceClient {
    private final QueueServiceAsyncClient client;

    /**
     * Creates a QueueServiceClient that wraps a QueueServiceAsyncClient and blocks requests.
     *
     * @param client QueueServiceAsyncClient that is used to send requests
     */
    QueueServiceClient(QueueServiceAsyncClient client) {
        this.client = client;
    }

    /**
     * Creates a builder that can configure options for the QueueServiceClient before creating an instance of it.
     *
     * @return A new {@link QueueServiceClientBuilder} used create QueueServiceClient instances.
     */
    public static QueueServiceClientBuilder builder() {
        return new QueueServiceClientBuilder();
    }

    /**
     * @return the URL of the storage queue
     */
    public URL getQueueServiceUrl() {
        return client.getQueueServiceUrl();
    }

    /**
     * Constructs a QueueClient that interacts with the specified queue.
     *
     * This will not create the queue in the storage account if it doesn't exist.
     *
     * @param queueName Name of the queue
     * @return QueueClient that interacts with the specified queue
     */
    public QueueClient getQueueClient(String queueName) {
        return new QueueClient(client.getQueueAsyncClient(queueName));
    }

    /**
     * Creates a queue in the storage account with the specified name and returns a QueueClient to interact with it.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Create the queue "test"</p>
     *
     * @codesnippet com.azure.storage.queue.queueServiceClient.createQueue#string
     *
     * @param queueName Name of the queue
     * @return A response containing the QueueClient and the status of creating the queue
     * @throws StorageErrorException If a queue with the same name and different metadata already exists
     */
    public Response<QueueClient> createQueue(String queueName) {
        return createQueue(queueName, null);
    }

    /**
     * Creates a queue in the storage account with the specified name and metadata and returns a QueueClient to
     * interact with it.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Create the queue "test" with metadata "queue:metadata"</p>
     *
     * <pre>
     * Response&lt;QueueClient&gt; = client.createQueue("test", Collections.singletonMap("queue", "metadata"));
     * System.out.printf("Creating the queue completed with status code %d", response.statusCode());
     * </pre>
     *
     * @param queueName Name of the queue
     * @param metadata Metadata to associate with the queue
     * @return A response containing the QueueClient and the status of creating the queue
     * @throws StorageErrorException If a queue with the same name and different metadata already exists
     */
    public Response<QueueClient> createQueue(String queueName, Map<String, String> metadata) {
        Response<QueueAsyncClient> response = client.createQueue(queueName, metadata).block();

        return new SimpleResponse<>(response, new QueueClient(response.value()));
    }

    /**
     * Deletes a queue in the storage account
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Delete the queue "test"</p>
     *
     * @codesnippet com.azure.storage.queue.queueServiceClient.deleteQueue#string
     *
     * @param queueName Name of the queue
     * @return A response containing the status of deleting the queue
     * @throws StorageErrorException If the queue doesn't exist
     */
    public VoidResponse deleteQueue(String queueName) {
        return client.deleteQueue(queueName).block();
    }

    /**
     * Lists all queues in the storage account without their metadata.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>List all queues in the account</p>
     *
     * @codesnippet com.azure.storage.queue.queueServiceClient.listQueues
     *
     * @return {@link QueueItem Queues} in the storage account
     */
    public Iterable<QueueItem> listQueues() {
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
     * <p>List all queues and their metadata in the account</p>
     *
     * <pre>
     * for (QueueItem queue : client.listQueues(new QueuesSegmentOptions().includeMetadata(true))) {
     *     System.out.printf("Queue %s exists in the account and has metadata %s", queue.name(), queue.metadata());
     * }
     * </pre>
     *
     * <p>List all queues that begin with "azure"</p>
     *
     * <pre>
     * for (QueueItem queue : client.listQueues(new QueuesSegmentOptions().prefix("azure"))) {
     *     System.out.printf("Queue %s exists in the account", queue.name());
     * }
     * </pre>
     *
     * @param options Options for listing queues
     * @return {@link QueueItem Queues} in the storage account that satisfy the filter requirements
     */
    public Iterable<QueueItem> listQueues(QueuesSegmentOptions options) {
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
    Iterable<QueueItem> listQueues(String marker, QueuesSegmentOptions options) {
        return client.listQueues(marker, options).toIterable();
    }

    /**
     * Retrieves the properties of the storage account's Queue service. The properties range from storage analytics and
     * metric to CORS (Cross-Origin Resource Sharing).
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Retrieve Queue service properties</p>
     *
     * <pre>
     * StorageServiceProperties properties = client.getProperties().value();
     * System.out.printf("Hour metrics enabled: %b, Minute metrics enabled: %b", properties.hourMetrics().enabled(), properties.minuteMetrics().enabled());
     * </pre>
     *
     * @return Storage account Queue service properties
     */
    public Response<StorageServiceProperties> getProperties() {
        return client.getProperties().block();
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
     * <pre>
     * StorageServiceProperties properties = client.getProperties().value();
     * properties.cors(Collections.emptyList());
     *
     * VoidResponse response = client.setProperties(properties);
     * System.out.printf("Setting Queue service properties completed with status code %d", response.statusCode());
     * </pre>
     *
     * <p>Enable Minute and Hour Metrics</p>
     *
     * <pre>
     * StorageServiceProperties properties = client.getProperties().value();
     * properties.minuteMetrics().enabled(true);
     * properties.hourMetrics().enabled(true);
     *
     * VoidResponse response = client.setProperties(properties);
     * System.out.printf("Setting Queue service properties completed with status code %d", response.statusCode());
     * </pre>
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
    public VoidResponse setProperties(StorageServiceProperties properties) {
        return client.setProperties(properties).block();
    }

    /**
     * Retrieves the geo replication information about the Queue service.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Retrieve the geo replication information</p>
     *
     * <pre>
     * StorageServiceStats stats = client.getStatistics().value();
     * System.out.printf("Geo replication status: %s, Last synced: %s", stats.geoReplication.status(), stats.geoReplication().lastSyncTime());
     * </pre>
     *
     * @return The geo replication information about the Queue service
     */
    public Response<StorageServiceStats> getStatistics() {
        return client.getStatistics().block();
    }
}
