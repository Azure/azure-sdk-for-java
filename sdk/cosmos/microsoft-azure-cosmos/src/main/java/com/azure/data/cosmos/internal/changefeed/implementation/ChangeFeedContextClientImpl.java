// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.cosmos.internal.changefeed.implementation;

import com.azure.data.cosmos.internal.AsyncDocumentClient;
import com.azure.data.cosmos.ChangeFeedOptions;
import com.azure.data.cosmos.CosmosContainer;
import com.azure.data.cosmos.CosmosContainerProperties;
import com.azure.data.cosmos.CosmosContainerRequestOptions;
import com.azure.data.cosmos.CosmosContainerResponse;
import com.azure.data.cosmos.CosmosDatabase;
import com.azure.data.cosmos.CosmosDatabaseRequestOptions;
import com.azure.data.cosmos.CosmosDatabaseResponse;
import com.azure.data.cosmos.CosmosItem;
import com.azure.data.cosmos.CosmosItemProperties;
import com.azure.data.cosmos.CosmosItemRequestOptions;
import com.azure.data.cosmos.CosmosItemResponse;
import com.azure.data.cosmos.FeedOptions;
import com.azure.data.cosmos.FeedResponse;
import com.azure.data.cosmos.SqlQuerySpec;
import com.azure.data.cosmos.internal.PartitionKeyRange;
import com.azure.data.cosmos.internal.changefeed.ChangeFeedContextClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.net.URI;

import static com.azure.data.cosmos.CosmosBridgeInternal.getContextClient;

/**
 * Implementation for ChangeFeedDocumentClient.
 */
public class ChangeFeedContextClientImpl implements ChangeFeedContextClient {
    private final AsyncDocumentClient documentClient;
    private final CosmosContainer cosmosContainer;
    private Scheduler rxScheduler;

    /**
     * Initializes a new instance of the {@link ChangeFeedContextClient} interface.
     * @param cosmosContainer existing client.
     */
    public ChangeFeedContextClientImpl(CosmosContainer cosmosContainer)
    {
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
    public ChangeFeedContextClientImpl(CosmosContainer cosmosContainer, Scheduler rxScheduler)
    {
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
    public Flux<FeedResponse<CosmosItemProperties>> createDocumentChangeFeedQuery(CosmosContainer collectionLink, ChangeFeedOptions feedOptions) {
        return collectionLink.queryChangeFeedItems(feedOptions)
            .publishOn(this.rxScheduler);
    }

    @Override
    public Mono<CosmosDatabaseResponse> readDatabase(CosmosDatabase database, CosmosDatabaseRequestOptions options) {
        return database.read()
            .publishOn(this.rxScheduler);
    }

    @Override
    public Mono<CosmosContainerResponse> readContainer(CosmosContainer containerLink, CosmosContainerRequestOptions options) {
        return containerLink.read(options)
            .publishOn(this.rxScheduler);
    }

    @Override
    public Mono<CosmosItemResponse> createItem(CosmosContainer containerLink, Object document, CosmosItemRequestOptions options, boolean disableAutomaticIdGeneration) {
        if (options != null) {
            return containerLink.createItem(document, options)
                .publishOn(this.rxScheduler);
        } else {
            return containerLink.createItem(document)
                .publishOn(this.rxScheduler);
        }
    }

    @Override
    public Mono<CosmosItemResponse> deleteItem(CosmosItem itemLink, CosmosItemRequestOptions options) {
        return itemLink.delete(options)
            .publishOn(this.rxScheduler);
    }

    @Override
    public Mono<CosmosItemResponse> replaceItem(CosmosItem itemLink, Object document, CosmosItemRequestOptions options) {
        return itemLink.replace(document, options)
            .publishOn(this.rxScheduler);
    }

    @Override
    public Mono<CosmosItemResponse> readItem(CosmosItem itemLink, CosmosItemRequestOptions options) {
        return itemLink.read(options)
            .publishOn(this.rxScheduler);
    }

    @Override
    public Flux<FeedResponse<CosmosItemProperties>> queryItems(CosmosContainer containerLink, SqlQuerySpec querySpec, FeedOptions options) {
        return containerLink.queryItems(querySpec, options)
            .publishOn(this.rxScheduler);
    }

    @Override
    public URI getServiceEndpoint() {
        return documentClient.getServiceEndpoint();
    }

    @Override
    public Mono<CosmosContainerProperties> readContainerSettings(CosmosContainer containerLink, CosmosContainerRequestOptions options) {
        return containerLink.read(options)
            .map(cosmosContainerResponse -> cosmosContainerResponse.properties());
    }

    @Override
    public CosmosContainer getContainerClient() {
        return this.cosmosContainer;
    }

    @Override
    public CosmosDatabase getDatabaseClient() {
        return this.cosmosContainer.getDatabase();
    }

    @Override
    public void close() {

    }
}
