// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.changefeed.common;

import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosAsyncDatabase;
import com.azure.cosmos.CosmosBridgeInternal;
import com.azure.cosmos.implementation.AsyncDocumentClient;
import com.azure.cosmos.implementation.Document;
import com.azure.cosmos.implementation.PartitionKeyRange;
import com.azure.cosmos.implementation.changefeed.ChangeFeedContextClient;
import com.azure.cosmos.models.CosmosChangeFeedRequestOptions;
import com.azure.cosmos.models.CosmosContainerProperties;
import com.azure.cosmos.models.CosmosContainerRequestOptions;
import com.azure.cosmos.models.CosmosContainerResponse;
import com.azure.cosmos.models.CosmosDatabaseRequestOptions;
import com.azure.cosmos.models.CosmosDatabaseResponse;
import com.azure.cosmos.models.CosmosItemRequestOptions;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.FeedResponse;
import com.azure.cosmos.models.ModelBridgeInternal;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.models.SqlQuerySpec;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

import static com.azure.cosmos.CosmosBridgeInternal.getContextClient;

/**
 * Implementation for ChangeFeedDocumentClient.
 */
public class ChangeFeedContextClientImpl implements ChangeFeedContextClient {
    private final AsyncDocumentClient documentClient;
    private final CosmosAsyncContainer cosmosContainer;
    private Scheduler scheduler;

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
        this.scheduler = Schedulers.boundedElastic();
    }

    /**
     * Initializes a new instance of the {@link ChangeFeedContextClient} interface.
     * @param cosmosContainer existing client.
     * @param scheduler the RX Java scheduler to observe on.
     */
    public ChangeFeedContextClientImpl(CosmosAsyncContainer cosmosContainer, Scheduler scheduler) {
        if (cosmosContainer == null) {
            throw new IllegalArgumentException("cosmosContainer");
        }

        this.cosmosContainer = cosmosContainer;
        this.documentClient = getContextClient(cosmosContainer);
        this.scheduler = scheduler;

    }

    @Override
    public Scheduler getScheduler() {
        return this.scheduler;
    }

    @Override
    public void setScheduler(Scheduler scheduler) {
        this.scheduler = scheduler;
    }

    @Override
    public Flux<FeedResponse<PartitionKeyRange>> readPartitionKeyRangeFeed(String partitionKeyRangesOrCollectionLink, CosmosQueryRequestOptions cosmosQueryRequestOptions) {
        return this.documentClient.readPartitionKeyRanges(partitionKeyRangesOrCollectionLink, cosmosQueryRequestOptions)
            .publishOn(this.scheduler);
    }

    @Override
    public <T> Flux<FeedResponse<T>> createDocumentChangeFeedQuery(CosmosAsyncContainer collectionLink,
                                                                   CosmosChangeFeedRequestOptions changeFeedRequestOptions,
                                                                   Class<T> klass) {

        // ChangeFeed processor relies on getting GoneException signals
        // to handle split of leases - so we need to suppress the split-proofing
        // in the underlying fetcher/pipeline for the change feed processor.
        CosmosChangeFeedRequestOptions effectiveRequestOptions =
            ModelBridgeInternal.disableSplitHandling(changeFeedRequestOptions);

        AsyncDocumentClient clientWrapper =
            CosmosBridgeInternal.getAsyncDocumentClient(collectionLink.getDatabase());
        Flux<FeedResponse<T>> feedResponseFlux =
            clientWrapper
                .getCollectionCache()
                .resolveByNameAsync(
                    null,
                    BridgeInternal.extractContainerSelfLink(collectionLink),
                    null)
                .flatMapMany((collection) -> {
                    if (collection == null) {
                        throw new IllegalStateException("Collection cannot be null");
                    }

                    return clientWrapper
                        .queryDocumentChangeFeed(collection, effectiveRequestOptions, Document.class)
                        .map(response -> {
                            List<T> results = response.getResults()
                                                             .stream()
                                                             .map(document ->
                                                                 ModelBridgeInternal.toObjectFromJsonSerializable(
                                                                     document,
                                                                     klass))
                                                             .collect(Collectors.toList());
                            return BridgeInternal.toFeedResponsePage(
                                results,
                                response.getResponseHeaders(),
                                false,
                                response.getCosmosDiagnostics());
                        });
                });
        return feedResponseFlux.publishOn(this.scheduler);
    }

    @Override
    public Mono<CosmosDatabaseResponse> readDatabase(CosmosAsyncDatabase database, CosmosDatabaseRequestOptions options) {
        return database.read()
            .publishOn(this.scheduler);
    }

    @Override
    public Mono<CosmosContainerResponse> readContainer(CosmosAsyncContainer containerLink, CosmosContainerRequestOptions options) {
        return containerLink.read(options)
            .publishOn(this.scheduler);
    }

    @Override
    public <T> Mono<CosmosItemResponse<T>> createItem(CosmosAsyncContainer containerLink, T document,
                                                      CosmosItemRequestOptions options, boolean disableAutomaticIdGeneration) {
        if (options != null) {
            return containerLink.createItem(document, options)
                .publishOn(this.scheduler);
        } else {
            return containerLink.createItem(document)
                .publishOn(this.scheduler);
        }
    }

    @Override
    public Mono<CosmosItemResponse<Object>> deleteItem(String itemId, PartitionKey partitionKey,
                                                       CosmosItemRequestOptions options) {
        return cosmosContainer.deleteItem(itemId, partitionKey, options)
            .publishOn(this.scheduler);
    }

    @Override
    public <T> Mono<CosmosItemResponse<T>> replaceItem(String itemId, PartitionKey partitionKey, T document,
                                                       CosmosItemRequestOptions options) {
        return cosmosContainer.replaceItem(document, itemId, partitionKey, options)
            .publishOn(this.scheduler);
    }

    @Override
    public <T> Mono<CosmosItemResponse<T>> readItem(String itemId, PartitionKey partitionKey,
                                                    CosmosItemRequestOptions options, Class<T> itemType) {
        return cosmosContainer.readItem(itemId, partitionKey, options, itemType)
            .publishOn(this.scheduler);
    }

    @Override
    public <T> Flux<FeedResponse<T>> queryItems(CosmosAsyncContainer containerLink, SqlQuerySpec querySpec,
                                                CosmosQueryRequestOptions options, Class<T> klass) {
        return containerLink.queryItems(querySpec, options, klass)
                            .byPage()
                            .publishOn(this.scheduler);
    }

    @Override
    public URI getServiceEndpoint() {
        return documentClient.getServiceEndpoint();
    }

    @Override
    public Mono<CosmosContainerProperties> readContainerSettings(CosmosAsyncContainer containerLink, CosmosContainerRequestOptions options) {
        return containerLink.read(options)
            .map(CosmosContainerResponse::getProperties);
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
