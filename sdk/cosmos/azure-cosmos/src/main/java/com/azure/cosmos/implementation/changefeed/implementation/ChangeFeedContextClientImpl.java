// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.changefeed.implementation;

import com.azure.cosmos.ChangeFeedOptions;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosAsyncContainerResponse;
import com.azure.cosmos.CosmosAsyncDatabase;
import com.azure.cosmos.CosmosAsyncDatabaseResponse;
import com.azure.cosmos.CosmosAsyncItemResponse;
import com.azure.cosmos.CosmosContainerProperties;
import com.azure.cosmos.CosmosContainerRequestOptions;
import com.azure.cosmos.CosmosDatabaseRequestOptions;
import com.azure.cosmos.CosmosItemProperties;
import com.azure.cosmos.CosmosItemRequestOptions;
import com.azure.cosmos.FeedOptions;
import com.azure.cosmos.FeedResponse;
import com.azure.cosmos.PartitionKey;
import com.azure.cosmos.SqlQuerySpec;
import com.azure.cosmos.implementation.AsyncDocumentClient;
import com.azure.cosmos.implementation.PartitionKeyRange;
import com.azure.cosmos.implementation.changefeed.ChangeFeedContextClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.net.URI;

import static com.azure.cosmos.CosmosBridgeInternal.getContextClient;

/**
 * Implementation for ChangeFeedDocumentClient.
 */
public class ChangeFeedContextClientImpl implements ChangeFeedContextClient {
    private final Logger logger = LoggerFactory.getLogger(ChangeFeedContextClientImpl.class);

    private final AsyncDocumentClient documentClient;
    private final CosmosAsyncContainer cosmosContainer;
    private Scheduler rxScheduler;

    /**
     * Initializes a new instance of the {@link ChangeFeedContextClient} interface.
     * @param cosmosContainer existing client.
     */
    public ChangeFeedContextClientImpl(CosmosAsyncContainer cosmosContainer) {
        if (cosmosContainer == null) {
            throw new IllegalArgumentException("cosmosContainer");
        }

        this.cosmosContainer = cosmosContainer;
        this.documentClient = getContextClient(cosmosContainer);
        this.rxScheduler = Schedulers.elastic();
    }

    /**
     * Initializes a new instance of the {@link ChangeFeedContextClient} interface.
     * @param cosmosContainer existing client.
     * @param rxScheduler the RX Java scheduler to observe on.
     */
    public ChangeFeedContextClientImpl(CosmosAsyncContainer cosmosContainer, Scheduler rxScheduler) {
        if (cosmosContainer == null) {
            throw new IllegalArgumentException("cosmosContainer");
        }

        this.cosmosContainer = cosmosContainer;
        this.documentClient = getContextClient(cosmosContainer);
        this.rxScheduler = rxScheduler;

    }

    @Override
    public Flux<FeedResponse<PartitionKeyRange>> readPartitionKeyRangeFeed(String partitionKeyRangesOrCollectionLink, FeedOptions feedOptions) {
        return this.documentClient.readPartitionKeyRanges(partitionKeyRangesOrCollectionLink, feedOptions)
            .publishOn(this.rxScheduler);
    }

    @Override
    public Flux<FeedResponse<CosmosItemProperties>> createDocumentChangeFeedQuery(CosmosAsyncContainer collectionLink, ChangeFeedOptions feedOptions) {
        return collectionLink.queryChangeFeedItems(feedOptions)
            .publishOn(this.rxScheduler);
    }

    @Override
    public Mono<CosmosAsyncDatabaseResponse> readDatabase(CosmosAsyncDatabase database, CosmosDatabaseRequestOptions options) {
        return database.read()
            .publishOn(this.rxScheduler);
    }

    @Override
    public Mono<CosmosAsyncContainerResponse> readContainer(CosmosAsyncContainer containerLink, CosmosContainerRequestOptions options) {
        return containerLink.read(options)
            .publishOn(this.rxScheduler);
    }

    @Override
    public <T> Mono<CosmosAsyncItemResponse<T>> createItem(CosmosAsyncContainer containerLink, T document,
                                                     CosmosItemRequestOptions options, boolean disableAutomaticIdGeneration) {
        if (options != null) {
            return containerLink.createItem(document, options)
                .publishOn(this.rxScheduler);
        } else {
            return containerLink.createItem(document)
                .publishOn(this.rxScheduler);
        }
    }

    @Override
    public Mono<CosmosAsyncItemResponse> deleteItem(String itemId, PartitionKey partitionKey,
                                                    CosmosItemRequestOptions options) {
        return cosmosContainer.deleteItem(itemId, partitionKey, options)
            .publishOn(this.rxScheduler);
    }

    @Override
    public <T> Mono<CosmosAsyncItemResponse<T>> replaceItem(String itemId, PartitionKey partitionKey, T document,
                                                     CosmosItemRequestOptions options) {
        return cosmosContainer.replaceItem(document, itemId, partitionKey, options)
            .publishOn(this.rxScheduler);
    }

    @Override
    public <T> Mono<CosmosAsyncItemResponse<T>> readItem(String itemId, PartitionKey partitionKey, 
                                                  CosmosItemRequestOptions options, Class<T> itemType) {
        return cosmosContainer.readItem(itemId, partitionKey, options, itemType)
            .publishOn(this.rxScheduler);
    }

    @Override
    public <T> Flux<FeedResponse<T>> queryItems(CosmosAsyncContainer containerLink, SqlQuerySpec querySpec,
                                                FeedOptions options, Class<T> klass) {
        return containerLink.queryItems(querySpec, options, klass)
                            .byPage()
                            .publishOn(this.rxScheduler);
    }

    @Override
    public URI getServiceEndpoint() {
        return documentClient.getServiceEndpoint();
    }

    @Override
    public Mono<CosmosContainerProperties> readContainerSettings(CosmosAsyncContainer containerLink, CosmosContainerRequestOptions options) {
        return containerLink.read(options)
            .map(cosmosContainerResponse -> cosmosContainerResponse.getProperties());
    }

    @Override
    public CosmosAsyncContainer getContainerClient() {
        return this.cosmosContainer;
    }

    @Override
    public CosmosAsyncDatabase getDatabaseClient() {
        return this.cosmosContainer.getDatabase();
    }

    @Override
    public void close() {

    }
}
