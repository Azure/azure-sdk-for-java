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
package com.azure.data.cosmos.changefeed.internal;

import com.azure.data.cosmos.AsyncDocumentClient;
import com.azure.data.cosmos.ChangeFeedOptions;
import com.azure.data.cosmos.CosmosContainer;
import com.azure.data.cosmos.CosmosContainerRequestOptions;
import com.azure.data.cosmos.CosmosContainerResponse;
import com.azure.data.cosmos.CosmosContainerSettings;
import com.azure.data.cosmos.CosmosDatabase;
import com.azure.data.cosmos.CosmosDatabaseRequestOptions;
import com.azure.data.cosmos.CosmosDatabaseResponse;
import com.azure.data.cosmos.CosmosItem;
import com.azure.data.cosmos.CosmosItemProperties;
import com.azure.data.cosmos.CosmosItemRequestOptions;
import com.azure.data.cosmos.CosmosItemResponse;
import com.azure.data.cosmos.FeedOptions;
import com.azure.data.cosmos.FeedResponse;
import com.azure.data.cosmos.PartitionKeyRange;
import com.azure.data.cosmos.SqlQuerySpec;
import com.azure.data.cosmos.changefeed.ChangeFeedContextClient;
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
    public Flux<FeedResponse<CosmosItemProperties>> createDocumentChangeFeedQuery(CosmosContainer collectionLink, ChangeFeedOptions feedOptions) {
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
    public Flux<FeedResponse<CosmosItemProperties>> queryItems(CosmosContainer containerLink, SqlQuerySpec querySpec, FeedOptions options) {
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
