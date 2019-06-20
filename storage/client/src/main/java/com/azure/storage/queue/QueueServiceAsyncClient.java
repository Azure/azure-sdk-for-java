// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.storage.queue;

import com.azure.core.http.HttpPipeline;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.http.rest.VoidResponse;
import com.azure.core.util.Context;
import com.azure.storage.queue.implementation.AzureQueueStorageBuilder;
import com.azure.storage.queue.implementation.AzureQueueStorageImpl;
import com.azure.storage.queue.models.ListQueuesIncludeType;
import com.azure.storage.queue.models.ListQueuesSegmentResponse;
import com.azure.storage.queue.models.QueueItem;
import com.azure.storage.queue.models.QueuesSegmentOptions;
import com.azure.storage.queue.models.ServicesListQueuesSegmentResponse;
import com.azure.storage.queue.models.StorageErrorException;
import com.azure.storage.queue.models.StorageServiceProperties;
import com.azure.storage.queue.models.StorageServiceStats;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Queue service async client
 */
public final class QueueServiceAsyncClient {
    private final AzureQueueStorageImpl client;

    /**
     * Constructor used by the builder
     * @param endpoint URL of the storage account queue endpoint
     * @param httpPipeline Http pipeline
     */
    QueueServiceAsyncClient(URL endpoint, HttpPipeline httpPipeline) {
        this.client = new AzureQueueStorageBuilder().pipeline(httpPipeline)
            .url(endpoint.toString())
            .build();
    }

    /**
     * @return a new client builder instance
     */
    public static QueueServiceAsyncClientBuilder builder() {
        return new QueueServiceAsyncClientBuilder();
    }

    /**
     * @return URL of the storage account queue endpoint
     */
    public URL getUrl() {
        try {
            return new URL(client.url());
        } catch (MalformedURLException ex) {
            throw new IllegalStateException("Storage account URL is malformed");
        }
    }

    /**
     * Gets a QueueAsyncClient that is targeting the specified queue
     * @param queueName Name of the queue
     * @return QueueAsyncClient that interacts with the specified queue
     */
    public QueueAsyncClient getQueueAsyncClient(String queueName) {
        return new QueueAsyncClient(client, queueName);
    }

    /**
     * Creates a new queue and returns the client to interact with it
     * @param queueName Name of the queue
     * @return the client to interact with the new queue
     */
    public Mono<Response<QueueAsyncClient>> createQueue(String queueName) {
        return createQueue(queueName, null);
    }

    /**
     * Creates a new queue and returns the client to interact with it
     * @param queueName Name of the queue
     * @param metadata Metadata to set on the queue
     * @return the client to interact with the new queue
     * @throws StorageErrorException If the queue fails to be created
     */
    public Mono<Response<QueueAsyncClient>> createQueue(String queueName, Map<String, String> metadata) {
        QueueAsyncClient queueAsyncClient = new QueueAsyncClient(client, queueName);

        return queueAsyncClient.create(metadata)
            .map(response -> new SimpleResponse<>(response, queueAsyncClient));
    }

    /**
     * Deletes a queue in the storage account
     * @param queueName Name of the queue
     * @throws StorageErrorException If the queue fails to be deleted
     */
    public Mono<VoidResponse> deleteQueue(String queueName) {
        return new QueueAsyncClient(client, queueName).delete();
    }

    /**
     * Lists the queues in the storage account
     * @return queues in the storage account
     */
    public Flux<QueueItem> listQueues() {
        return listQueues(null, null);
    }

    /**
     * Lists the queues in the storage account
     * @param options Filter for queue selection
     * @return queues in the storage account that satisfy the filter requirements
     */
    public Flux<QueueItem> listQueues(QueuesSegmentOptions options) {
        return listQueues(null, options);
    }

    /**
     * Lists the queues in the storage account
     * @param marker Starting point to list the queues
     * @param options Filter for queue selection
     * @return queues in the storage account that satisfy the filter requirements
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

    private Flux<QueueItem> listQueues(ServicesListQueuesSegmentResponse response, List<ListQueuesIncludeType> include, Context context) {
        ListQueuesSegmentResponse value = response.value();
        Mono<ServicesListQueuesSegmentResponse> result = client.services()
            .listQueuesSegmentWithRestResponseAsync(value.prefix(), value.marker(), value.maxResults(), include, null, null, context);

        return result.flatMapMany(r -> extractAndFetchQueues(r, include, context));
    }

    private Publisher<QueueItem> extractAndFetchQueues(ServicesListQueuesSegmentResponse response, List<ListQueuesIncludeType> include, Context context) {
        String nextPageLink = response.value().nextMarker();
        if (nextPageLink == null) {
            return Flux.fromIterable(response.value().queueItems());
        }

        return Flux.fromIterable(response.value().queueItems()).concatWith(listQueues(response, include, context));
    }

    /**
     * @return Global queue properties in the storage account
     */
    public Mono<Response<StorageServiceProperties>> getProperties() {
        return client.services().getPropertiesWithRestResponseAsync(Context.NONE)
            .map(response -> new SimpleResponse<>(response, response.value()));
    }

    /**
     * Sets global queue properties for the storage account
     * @param properties Queue properties
     * @return an empty response
     */
    public Mono<VoidResponse> setProperties(StorageServiceProperties properties) {
        return client.services().setPropertiesWithRestResponseAsync(properties, Context.NONE)
            .map(VoidResponse::new);
    }

    /**
     * @return global statics about queues in the storage account
     */
    public Mono<Response<StorageServiceStats>> getStatistics() {
        return client.services().getStatisticsWithRestResponseAsync(Context.NONE)
            .map(response -> new SimpleResponse<>(response, response.value()));
    }
}
