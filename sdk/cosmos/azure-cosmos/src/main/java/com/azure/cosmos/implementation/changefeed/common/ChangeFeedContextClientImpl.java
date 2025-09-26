// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.changefeed.common;

import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosAsyncDatabase;
import com.azure.cosmos.CosmosBridgeInternal;
import com.azure.cosmos.implementation.AsyncDocumentClient;
import com.azure.cosmos.implementation.ImplementationBridgeHelpers;
import com.azure.cosmos.implementation.PartitionKeyRange;
import com.azure.cosmos.implementation.changefeed.ChangeFeedContextClient;
import com.azure.cosmos.implementation.routing.Range;
import com.azure.cosmos.models.CosmosBulkOperationResponse;
import com.azure.cosmos.models.CosmosBulkOperations;
import com.azure.cosmos.models.CosmosChangeFeedRequestOptions;
import com.azure.cosmos.models.CosmosContainerProperties;
import com.azure.cosmos.models.CosmosContainerRequestOptions;
import com.azure.cosmos.models.CosmosContainerResponse;
import com.azure.cosmos.models.CosmosDatabaseRequestOptions;
import com.azure.cosmos.models.CosmosDatabaseResponse;
import com.azure.cosmos.models.CosmosItemIdentity;
import com.azure.cosmos.models.CosmosItemOperation;
import com.azure.cosmos.models.CosmosItemRequestOptions;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.FeedResponse;
import com.azure.cosmos.models.ModelBridgeInternal;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.models.SqlQuerySpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import static com.azure.cosmos.CosmosBridgeInternal.getContextClient;
import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

/**
 * Implementation for ChangeFeedDocumentClient.
 */
public class ChangeFeedContextClientImpl implements ChangeFeedContextClient {
    private static final Logger logger = LoggerFactory.getLogger(ChangeFeedContextClientImpl.class);

    private final AsyncDocumentClient documentClient;
    private final CosmosAsyncContainer cosmosContainer;
    private Scheduler scheduler;
   private static final ImplementationBridgeHelpers.CosmosAsyncDatabaseHelper.CosmosAsyncDatabaseAccessor cosmosAsyncDatabaseAccessor =
        ImplementationBridgeHelpers.CosmosAsyncDatabaseHelper.getCosmosAsyncDatabaseAccessor();

    /**
     * Initializes a new instance of the {@link ChangeFeedContextClient} interface.
     * @param cosmosContainer existing client.
     */
    public ChangeFeedContextClientImpl(CosmosAsyncContainer cosmosContainer) {
        this(cosmosContainer, Schedulers.boundedElastic());
    }

    /**
     * Initializes a new instance of the {@link ChangeFeedContextClient} interface.
     * @param cosmosContainer existing client.
     * @param scheduler the RX Java scheduler to observe on.
     */
    public ChangeFeedContextClientImpl(CosmosAsyncContainer cosmosContainer, Scheduler scheduler) {
        checkNotNull(cosmosContainer, "Argument 'cosmosContainer' can not be null");

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
    public Mono<List<PartitionKeyRange>> getOverlappingRanges(Range<String> range, boolean forceRefresh) {
        AsyncDocumentClient clientWrapper =
                CosmosBridgeInternal.getAsyncDocumentClient(this.cosmosContainer.getDatabase());
        StringBuilder sb = new StringBuilder();
        sb.append("ChangeFeedContextClientImpl.getOverlappingRanges").append(",");

        return clientWrapper
                .getCollectionCache()
                .resolveByNameAsync(null, BridgeInternal.extractContainerSelfLink(this.cosmosContainer), null)
                .flatMap(collection -> {
                    return clientWrapper.getPartitionKeyRangeCache().tryGetOverlappingRangesAsync(
                            null,
                            collection.getResourceId(),
                            range,
                            forceRefresh,
                            null,
                        sb);
                })
                .flatMap(pkRangesValueHolder -> {
                    if (pkRangesValueHolder == null || pkRangesValueHolder.v == null) {
                        logger.warn("There are no overlapping ranges found for range {}", range);
                        return Mono.just(new ArrayList<PartitionKeyRange>());
                    }

                    return Mono.just(pkRangesValueHolder.v);
                })
                .publishOn(this.scheduler);
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

        return this.createDocumentChangeFeedQuery(collectionLink, changeFeedRequestOptions, klass, true);
    }

    @Override
    public  <T> Flux<FeedResponse<T>> createDocumentChangeFeedQuery(CosmosAsyncContainer collectionLink,
                                                                    CosmosChangeFeedRequestOptions changeFeedRequestOptions,
                                                                    Class<T> klass,
                                                                    boolean isSplitHandlingDisabled) {

        // Case 1: when split handling should be disabled
        // ChangeFeed processor relies on getting GoneException signals
        // to handle split of leases - so we need to suppress the split-proofing
        // in the underlying fetcher/pipeline for the change feed processor.
        // Case 2: when split handling should be enabled
        // A ChangeFeedProcessor instance which is backed by a client with a stale
        // PKRange cache will run into 410/1002s (PartitionKeyRangeGone) if disable split handling is true
        // in getCurrentState and getEstimatedLag scenarios therefore disable split handling should explicitly be set to false
        if (isSplitHandlingDisabled) {
            ModelBridgeInternal.disableSplitHandling(changeFeedRequestOptions);
        }
        return collectionLink
            .queryChangeFeed(changeFeedRequestOptions, klass)
            .byPage().take(1, true)
            .publishOn(this.scheduler);
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
    public Flux<CosmosBulkOperationResponse<Object>> deleteAllItems(List<CosmosItemIdentity> cosmosItemIdentities) {
        List<CosmosItemOperation> operations = new ArrayList<>();
        for (CosmosItemIdentity cosmosItemIdentity : cosmosItemIdentities) {
            operations.add(CosmosBulkOperations.getDeleteItemOperation(cosmosItemIdentity.getId(), cosmosItemIdentity.getPartitionKey()));
        }

        return cosmosContainer.executeBulkOperations(Flux.fromIterable(operations))
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
