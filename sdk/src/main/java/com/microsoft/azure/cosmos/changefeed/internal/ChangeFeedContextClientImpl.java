/*
 * The MIT License (MIT)
 * Copyright (c) 2018 Microsoft Corporation
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.microsoft.azure.cosmos.changefeed.internal;

import com.microsoft.azure.cosmos.CosmosClient;
import com.microsoft.azure.cosmos.CosmosContainer;
import com.microsoft.azure.cosmos.CosmosContainerRequestOptions;
import com.microsoft.azure.cosmos.CosmosContainerResponse;
import com.microsoft.azure.cosmos.CosmosContainerSettings;
import com.microsoft.azure.cosmos.CosmosDatabase;
import com.microsoft.azure.cosmos.CosmosDatabaseRequestOptions;
import com.microsoft.azure.cosmos.CosmosDatabaseResponse;
import com.microsoft.azure.cosmos.CosmosItem;
import com.microsoft.azure.cosmos.CosmosItemRequestOptions;
import com.microsoft.azure.cosmos.CosmosItemResponse;
import com.microsoft.azure.cosmos.CosmosItemSettings;
import com.microsoft.azure.cosmos.changefeed.ChangeFeedContextClient;
import com.microsoft.azure.cosmosdb.BridgeInternal;
import com.microsoft.azure.cosmosdb.ChangeFeedOptions;
import com.microsoft.azure.cosmosdb.FeedOptions;
import com.microsoft.azure.cosmosdb.FeedResponse;
import com.microsoft.azure.cosmosdb.PartitionKeyRange;
import com.microsoft.azure.cosmosdb.SqlQuerySpec;
import com.microsoft.azure.cosmosdb.rx.AsyncDocumentClient;
import hu.akarnokd.rxjava.interop.RxJavaInterop;
import reactor.adapter.rxjava.RxJava2Adapter;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.net.URI;

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
        this.documentClient = CosmosContainer.getContextClient(cosmosContainer);
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
        this.documentClient = CosmosContainer.getContextClient(cosmosContainer);
        this.rxScheduler = rxScheduler;

    }

    @Override
    public Flux<FeedResponse<PartitionKeyRange>> readPartitionKeyRangeFeed(String partitionKeyRangesOrCollectionLink, FeedOptions feedOptions) {
        return RxJava2Adapter.flowableToFlux(
            RxJavaInterop.toV2Flowable(
                this.documentClient.readPartitionKeyRanges(partitionKeyRangesOrCollectionLink, feedOptions)))
            .subscribeOn(this.rxScheduler);
    }

    @Override
    public Flux<FeedResponse<CosmosItemSettings>> createDocumentChangeFeedQuery(CosmosContainer collectionLink, ChangeFeedOptions feedOptions) {
        return collectionLink.queryChangeFeedItems(feedOptions)
            .subscribeOn(this.rxScheduler);
    }

    @Override
    public Mono<CosmosDatabaseResponse> readDatabase(CosmosDatabase database, CosmosDatabaseRequestOptions options) {
        return database.read()
            .subscribeOn(this.rxScheduler);
    }

    @Override
    public Mono<CosmosContainerResponse> readContainer(CosmosContainer containerLink, CosmosContainerRequestOptions options) {
        return containerLink.read(options)
            .subscribeOn(this.rxScheduler);
    }

    @Override
    public Mono<CosmosItemResponse> createItem(CosmosContainer containerLink, Object document, CosmosItemRequestOptions options, boolean disableAutomaticIdGeneration) {
        return containerLink.createItem(document, options)
            .subscribeOn(this.rxScheduler);
    }

    @Override
    public Mono<CosmosItemResponse> deleteItem(CosmosItem itemLink, CosmosItemRequestOptions options) {
        return itemLink.delete(options)
            .subscribeOn(this.rxScheduler);
    }

    @Override
    public Mono<CosmosItemResponse> replaceItem(CosmosItem itemLink, Object document, CosmosItemRequestOptions options) {
        return itemLink.replace(document, options)
            .subscribeOn(this.rxScheduler);
    }

    @Override
    public Mono<CosmosItemResponse> readItem(CosmosItem itemLink, CosmosItemRequestOptions options) {
        return itemLink.read(options)
            .subscribeOn(this.rxScheduler);
    }

    @Override
    public Flux<FeedResponse<CosmosItemSettings>> queryItems(CosmosContainer containerLink, SqlQuerySpec querySpec, FeedOptions options) {
        return containerLink.queryItems(querySpec, options)
            .subscribeOn(this.rxScheduler);
    }

    @Override
    public URI getServiceEndpoint() {
        return documentClient.getServiceEndpoint();
    }

    @Override
    public Mono<CosmosContainerSettings> readContainerSettings(CosmosContainer containerLink, CosmosContainerRequestOptions options) {
        return null;
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
