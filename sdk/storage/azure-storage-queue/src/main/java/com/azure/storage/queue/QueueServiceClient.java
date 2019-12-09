// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.storage.queue;

import com.azure.core.annotation.ServiceClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.util.Context;
import com.azure.storage.common.StorageSharedKeyCredential;
import com.azure.storage.common.implementation.StorageImplUtils;
import com.azure.storage.common.sas.AccountSasSignatureValues;
import com.azure.storage.queue.models.QueueCorsRule;
import com.azure.storage.queue.models.QueueItem;
import com.azure.storage.queue.models.QueueServiceProperties;
import com.azure.storage.queue.models.QueueServiceStatistics;
import com.azure.storage.queue.models.QueuesSegmentOptions;
import com.azure.storage.queue.models.QueueStorageException;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Map;

/**
 * This class provides a client that contains all the operations for interacting with a queue account in Azure Storage.
 * Operations allowed by the client are creating, listing, and deleting queues, retrieving and updating properties of
 * the account, and retrieving statistics of the account.
 *
 * <p><strong>Instantiating an Synchronous Queue Service Client</strong></p>
 *
 * {@codesnippet com.azure.storage.queue.queueServiceClient.instantiation}
 *
 * <p>View {@link QueueServiceClientBuilder this} for additional ways to construct the client.</p>
 *
 * @see QueueServiceClientBuilder
 * @see QueueServiceAsyncClient
 * @see StorageSharedKeyCredential
 */
@ServiceClient(builder = QueueServiceClientBuilder.class)
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
     * @return the URL of the storage queue
     */
    public String getQueueServiceUrl() {
        return client.getQueueServiceUrl();
    }

    /**
     * Gets the service version the client is using.
     *
     * @return the service version the client is using.
     */
    public QueueServiceVersion getServiceVersion() {
        return client.getServiceVersion();
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
     * {@codesnippet com.azure.storage.queue.queueServiceClient.createQueue#string}
     *
     * @param queueName Name of the queue
     * @return A response containing the QueueClient and the status of creating the queue
     * @throws QueueStorageException If a queue with the same name and different metadata already exists
     */
    public QueueClient createQueue(String queueName) {
        return createQueueWithResponse(queueName, null, null, Context.NONE).getValue();
    }

    /**
     * Creates a queue in the storage account with the specified name and metadata and returns a QueueClient to interact
     * with it.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Create the queue "test" with metadata "queue:metadata"</p>
     *
     * {@codesnippet com.azure.storage.queue.queueServiceClient.createQueueWithResponse#string-map-duration-context}
     *
     * @param queueName Name of the queue
     * @param metadata Metadata to associate with the queue
     * @param timeout An optional timeout applied to the operation. If a response is not returned before the timeout
     * concludes a {@link RuntimeException} will be thrown.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A response containing the QueueClient and the status of creating the queue
     * @throws QueueStorageException If a queue with the same name and different metadata already exists
     * @throws RuntimeException if the operation doesn't complete before the timeout concludes.
     */
    public Response<QueueClient> createQueueWithResponse(String queueName, Map<String, String> metadata,
        Duration timeout, Context context) {

        Mono<Response<QueueAsyncClient>> asyncResponse = client.createQueueWithResponse(queueName, metadata, context);
        Response<QueueAsyncClient> response = StorageImplUtils.blockWithOptionalTimeout(asyncResponse, timeout);
        return new SimpleResponse<>(response, new QueueClient(response.getValue()));
    }

    /**
     * Deletes a queue in the storage account
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Delete the queue "test"</p>
     *
     * {@codesnippet com.azure.storage.queue.queueServiceClient.deleteQueue#string}
     *
     * @param queueName Name of the queue
     * @throws QueueStorageException If the queue doesn't exist
     */
    public void deleteQueue(String queueName) {
        deleteQueueWithResponse(queueName, null, Context.NONE);
    }

    /**
     * Deletes a queue in the storage account
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Delete the queue "test"</p>
     *
     * {@codesnippet com.azure.storage.queue.queueServiceClient.deleteQueueWithResponse#string-duration-context}
     *
     * @param queueName Name of the queue
     * @param timeout An optional timeout applied to the operation. If a response is not returned before the timeout
     * concludes a {@link RuntimeException} will be thrown.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A response containing the status of deleting the queue
     * @throws QueueStorageException If the queue doesn't exist
     * @throws RuntimeException if the operation doesn't complete before the timeout concludes.
     */
    public Response<Void> deleteQueueWithResponse(String queueName, Duration timeout, Context context) {
        Mono<Response<Void>> response = client.deleteQueueWithResponse(queueName, context);
        return StorageImplUtils.blockWithOptionalTimeout(response, timeout);
    }

    /**
     * Lists all queues in the storage account without their metadata.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>List all queues in the account</p>
     *
     * {@codesnippet com.azure.storage.queue.queueServiceClient.listQueues}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/list-queues1">Azure Docs</a>.</p>
     *
     * @return {@link QueueItem Queues} in the storage account
     */
    public PagedIterable<QueueItem> listQueues() {
        return listQueues(null, null, Context.NONE);
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
     * {@codesnippet com.azure.storage.queue.queueServiceClient.listQueues#queueSergmentOptions-duration-context}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/list-queues1">Azure Docs</a>.</p>
     *
     * @param options Options for listing queues
     * @param timeout An optional timeout applied to the operation. If a response is not returned before the timeout
     * concludes a {@link RuntimeException} will be thrown.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return {@link QueueItem Queues} in the storage account that satisfy the filter requirements
     * @throws RuntimeException if the operation doesn't complete before the timeout concludes.
     */
    public PagedIterable<QueueItem> listQueues(QueuesSegmentOptions options, Duration timeout, Context context) {
        return listQueues(null, options, timeout, context);
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
     * @throws RuntimeException if the operation doesn't complete before the timeout concludes.
     */
    PagedIterable<QueueItem> listQueues(String marker, QueuesSegmentOptions options, Duration timeout,
        Context context) {
        return new PagedIterable<>(client.listQueuesWithOptionalTimeout(marker, options, timeout, context));
    }

    /**
     * Retrieves the properties of the storage account's Queue service. The properties range from storage analytics and
     * metric to CORS (Cross-Origin Resource Sharing).
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Retrieve Queue service properties</p>
     *
     * {@codesnippet com.azure.storage.queue.queueServiceClient.getProperties}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/get-queue-service-properties">Azure
     * Docs</a>.</p>
     *
     * @return Storage account Queue service properties
     */
    public QueueServiceProperties getProperties() {
        return getPropertiesWithResponse(null, Context.NONE).getValue();
    }

    /**
     * Retrieves the properties of the storage account's Queue service. The properties range from storage analytics and
     * metric to CORS (Cross-Origin Resource Sharing).
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Retrieve Queue service properties</p>
     *
     * {@codesnippet com.azure.storage.queue.queueServiceClient.getPropertiesWithResponse#duration-context}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/get-queue-service-properties">Azure
     * Docs</a>.</p>
     *
     * @param timeout An optional timeout applied to the operation. If a response is not returned before the timeout
     * concludes a {@link RuntimeException} will be thrown.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A response containing the Storage account Queue service properties
     * @throws RuntimeException if the operation doesn't complete before the timeout concludes.
     */
    public Response<QueueServiceProperties> getPropertiesWithResponse(Duration timeout, Context context) {
        Mono<Response<QueueServiceProperties>> response = client.getPropertiesWithResponse(context);
        return StorageImplUtils.blockWithOptionalTimeout(response, timeout);
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
     * {@codesnippet com.azure.storage.queue.queueServiceClient.setProperties#QueueServiceProperties}
     *
     * <p>Enable Minute and Hour Metrics</p>
     *
     * {@codesnippet com.azure.storage.queue.queueServiceClient.setPropertiesEnableMetrics#QueueServiceProperties}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/set-queue-service-properties">Azure
     * Docs</a>.</p>
     *
     * @param properties Storage account Queue service properties
     * @throws QueueStorageException When one of the following is true
     * <ul>
     * <li>A CORS rule is missing one of its fields</li>
     * <li>More than five CORS rules will exist for the Queue service</li>
     * <li>Size of all CORS rules exceeds 2KB</li>
     * <li>
     * Length of {@link QueueCorsRule#getAllowedHeaders() allowed headers}, {@link QueueCorsRule#getExposedHeaders()
     * exposed headers}, or {@link QueueCorsRule#getAllowedOrigins() allowedOrigins() allowed origins} exceeds 256
     * characters.
     * </li>
     * <li>{@link QueueCorsRule#getAllowedMethods() Allowed methods} isn't DELETE, GET, HEAD, MERGE, POST, OPTIONS, or
     * PUT</li>
     * </ul>
     */
    public void setProperties(QueueServiceProperties properties) {
        setPropertiesWithResponse(properties, null, Context.NONE);
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
     * {@codesnippet com.azure.storage.queue.queueServiceClient.setPropertiesWithResponse#QueueServiceProperties-duration-context}
     *
     * <p>Enable Minute and Hour Metrics</p>
     *
     * {@codesnippet com.azure.storage.queue.queueServiceClient.setPropertiesWithResponseEnableMetrics#QueueServiceProperties-duration-context}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/set-queue-service-properties">Azure
     * Docs</a>.</p>
     *
     * @param properties Storage account Queue service properties
     * @param timeout An optional timeout applied to the operation. If a response is not returned before the timeout
     * concludes a {@link RuntimeException} will be thrown.
     * @param context Additional context that is passed through the Http pipeline during the service call.
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
     * @throws RuntimeException if the operation doesn't complete before the timeout concludes.
     */
    public Response<Void> setPropertiesWithResponse(QueueServiceProperties properties, Duration timeout,
        Context context) {
        Mono<Response<Void>> response = client.setPropertiesWithResponse(properties, context);
        return StorageImplUtils.blockWithOptionalTimeout(response, timeout);
    }

    /**
     * Retrieves the geo replication information about the Queue service.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Retrieve the geo replication information</p>
     *
     * {@codesnippet com.azure.storage.queue.queueServiceClient.getStatistics}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/get-queue-service-stats">Azure Docs</a>.</p>
     *
     * @return The geo replication information about the Queue service
     */
    public QueueServiceStatistics getStatistics() {
        return getStatisticsWithResponse(null, Context.NONE).getValue();
    }

    /**
     * Retrieves the geo replication information about the Queue service.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Retrieve the geo replication information</p>
     *
     * {@codesnippet com.azure.storage.queue.queueServiceClient.getStatisticsWithResponse#duration-context}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/get-queue-service-stats">Azure Docs</a>.</p>
     *
     * @param timeout An optional timeout applied to the operation. If a response is not returned before the timeout
     * concludes a {@link RuntimeException} will be thrown.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A response containing the geo replication information about the Queue service
     * @throws RuntimeException if the operation doesn't complete before the timeout concludes.
     */
    public Response<QueueServiceStatistics> getStatisticsWithResponse(Duration timeout, Context context) {
        Mono<Response<QueueServiceStatistics>> response = client.getStatisticsWithResponse(context);
        return StorageImplUtils.blockWithOptionalTimeout(response, timeout);
    }


    /**
     * Get associated account name.
     *
     * @return account name associated with this storage resource.
     */
    public String getAccountName() {
        return this.client.getAccountName();
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
     * Note : The client must be authenticated via {@link StorageSharedKeyCredential}
     * <p>See {@link AccountSasSignatureValues} for more information on how to construct an account SAS.</p>
     *
     * <p><strong>Generating an account SAS</strong></p>
     * <p>The snippet below generates an AccountSasSignatureValues object that lasts for two days and gives the user
     * read and list access to queue and file shares.</p>
     * {@codesnippet com.azure.storage.queue.QueueServiceClient.generateAccountSas#AccountSasSignatureValues}
     *
     * @param accountSasSignatureValues {@link AccountSasSignatureValues}
     *
     * @return A {@code String} representing all SAS query parameters.
     */
    public String generateAccountSas(AccountSasSignatureValues accountSasSignatureValues) {
        return this.client.generateAccountSas(accountSasSignatureValues);
    }
}
