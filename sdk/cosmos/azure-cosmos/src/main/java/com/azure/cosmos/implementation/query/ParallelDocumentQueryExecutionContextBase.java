// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.query;

import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.CosmosItemSerializer;
import com.azure.cosmos.implementation.DiagnosticsClientContext;
import com.azure.cosmos.implementation.DocumentClientRetryPolicy;
import com.azure.cosmos.implementation.DocumentCollection;
import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.ImplementationBridgeHelpers;
import com.azure.cosmos.implementation.PartitionKeyRange;
import com.azure.cosmos.implementation.ResourceType;
import com.azure.cosmos.implementation.RxDocumentServiceRequest;
import com.azure.cosmos.implementation.Strings;
import com.azure.cosmos.implementation.feedranges.FeedRangeEpkImpl;
import com.azure.cosmos.implementation.routing.PartitionKeyInternal;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.FeedResponse;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.models.SqlQuerySpec;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * While this class is public, but it is not part of our published public APIs.
 * This is meant to be internally used only by our sdk.
 */
public abstract class ParallelDocumentQueryExecutionContextBase<T>
        extends DocumentQueryExecutionContextBase<T> implements IDocumentQueryExecutionComponent<T> {

    protected final List<DocumentProducer<T>> documentProducers;
    protected final SqlQuerySpec querySpec;
    protected int top = -1;
    private final CosmosItemSerializer itemSerializer;

    protected ParallelDocumentQueryExecutionContextBase(DiagnosticsClientContext diagnosticsClientContext,
                                                        IDocumentQueryClient client,
                                                        ResourceType resourceTypeEnum, Class<T> resourceType,
                                                        SqlQuerySpec query, CosmosQueryRequestOptions cosmosQueryRequestOptions, String resourceLink, String rewrittenQuery,
                                                        UUID correlatedActivityId, boolean shouldUnwrapSelectValue, AtomicBoolean isQueryCancelledOnTimeout) {
        super(diagnosticsClientContext, client, resourceTypeEnum, resourceType, query, cosmosQueryRequestOptions, resourceLink, correlatedActivityId, isQueryCancelledOnTimeout);

        CosmosItemSerializer candidateSerializer = client.getEffectiveItemSerializer(this.cosmosQueryRequestOptions);
        this.itemSerializer = candidateSerializer != CosmosItemSerializer.DEFAULT_SERIALIZER
            ? candidateSerializer
            : ValueUnwrapCosmosItemSerializer.create(shouldUnwrapSelectValue);
        documentProducers = new ArrayList<>();
        if (!Strings.isNullOrEmpty(rewrittenQuery)) {
            this.querySpec = new SqlQuerySpec(rewrittenQuery, super.query.getParameters());
        } else {
            this.querySpec = super.query;
        }
    }

    protected void initialize(
        DocumentCollection collection,
        Map<FeedRangeEpkImpl, String> feedRangeToContinuationTokenMap,
        int initialPageSize,
        SqlQuerySpec querySpecForInit) {
        Map<String, String> commonRequestHeaders = createCommonHeadersAsync(this.getFeedOptions(null, null));
        for (Map.Entry<FeedRangeEpkImpl, String> entry : feedRangeToContinuationTokenMap.entrySet()) {
            TriFunction<FeedRangeEpkImpl, String, Integer, RxDocumentServiceRequest> createRequestFunc = (feedRange,
                                                                                                           continuationToken, pageSize) -> {
                Map<String, String> headers = new HashMap<>(commonRequestHeaders);
                headers.put(HttpConstants.HttpHeaders.CONTINUATION, continuationToken);
                headers.put(HttpConstants.HttpHeaders.PAGE_SIZE, Strings.toString(pageSize));
                headers.put(HttpConstants.HttpHeaders.CORRELATED_ACTIVITY_ID, correlatedActivityId.toString());

                PartitionKeyInternal partitionKeyInternal = null;
                if (cosmosQueryRequestOptions.getPartitionKey() != null && cosmosQueryRequestOptions.getPartitionKey() != PartitionKey.NONE) {
                    // the next if statement is for the method `createDocumentServiceRequestWithFeedRange` below
                    // with this added logic we don't have to remove the header (since the header never gets set) and
                    // partitionKeyInternal gets passed as null which avoids the feedRange normalization.
                    if (!PartitionKeyInternal.isPartialPartitionKeyQuery(collection, cosmosQueryRequestOptions.getPartitionKey())) {
                        partitionKeyInternal = BridgeInternal.getPartitionKeyInternal(cosmosQueryRequestOptions.getPartitionKey());
                        headers.put(HttpConstants.HttpHeaders.PARTITION_KEY, partitionKeyInternal.toJson());
                    }
                }

                ImplementationBridgeHelpers.CosmosQueryRequestOptionsHelper.getCosmosQueryRequestOptionsAccessor().setPartitionKeyDefinition(cosmosQueryRequestOptions, collection.getPartitionKey());
                ImplementationBridgeHelpers.CosmosQueryRequestOptionsHelper.getCosmosQueryRequestOptionsAccessor().setCollectionRid(cosmosQueryRequestOptions, collection.getResourceId());
                return this.createDocumentServiceRequestWithFeedRange(headers, querySpecForInit, partitionKeyInternal, feedRange,
                                                         collection.getResourceId(), cosmosQueryRequestOptions.getThroughputControlGroupName());
            };

            Function<RxDocumentServiceRequest, Mono<FeedResponse<T>>> executeFunc = (request) ->  this.executeRequestAsync(
                    this.itemSerializer,
                    request);
            final FeedRangeEpkImpl targetRange = entry.getKey();
            final String continuationToken = entry.getValue();
            DocumentProducer<T> dp =
                createDocumentProducer(
                    collection.getResourceId(),
                    continuationToken,
                    initialPageSize,
                    cosmosQueryRequestOptions,
                    querySpecForInit,
                    commonRequestHeaders,
                    createRequestFunc,
                    executeFunc,
                    () -> client.getResetSessionTokenRetryPolicy().getRequestPolicy(this.diagnosticsClientContext),
                    targetRange,
                    collection.getSelfLink());

            documentProducers.add(dp);
        }
    }

    abstract protected DocumentProducer<T> createDocumentProducer(String collectionRid,
                                                                  String initialContinuationToken,
                                                                  int initialPageSize,
                                                                  CosmosQueryRequestOptions cosmosQueryRequestOptions,
                                                                  SqlQuerySpec querySpecForInit,
                                                                  Map<String, String> commonRequestHeaders,
                                                                  TriFunction<FeedRangeEpkImpl, String, Integer, RxDocumentServiceRequest> createRequestFunc,
                                                                  Function<RxDocumentServiceRequest,
                                                                  Mono<FeedResponse<T>>> executeFunc,
                                                                  Supplier<DocumentClientRetryPolicy> createRetryPolicyFunc,
                                                                  FeedRangeEpkImpl feedRange,
                                                                  String collectionLink);

    @Override
    abstract public Flux<FeedResponse<T>> drainAsync(int maxPageSize);

    public void setTop(int newTop) {
        this.top = newTop;

        for (DocumentProducer<T> producer : this.documentProducers) {
            producer.top = newTop;
        }
    }

    protected void initializeReadMany(
        Map<PartitionKeyRange, SqlQuerySpec> rangeQueryMap,
        CosmosQueryRequestOptions cosmosQueryRequestOptions,
        DocumentCollection collection) {
        Map<String, String> commonRequestHeaders = createCommonHeadersAsync(this.getFeedOptions(null, null));

        for (Map.Entry<PartitionKeyRange, SqlQuerySpec> entry : rangeQueryMap.entrySet()) {
            final PartitionKeyRange targetRange = entry.getKey();
            final FeedRangeEpkImpl feedRangeEpk = new FeedRangeEpkImpl(targetRange.toRange());
            final SqlQuerySpec querySpec = entry.getValue();
            TriFunction<FeedRangeEpkImpl, String, Integer, RxDocumentServiceRequest> createRequestFunc = (
                partitionKeyRange,
                continuationToken, pageSize) -> {
                Map<String, String> headers = new HashMap<>(commonRequestHeaders);
                headers.put(HttpConstants.HttpHeaders.CONTINUATION, continuationToken);
                headers.put(HttpConstants.HttpHeaders.PAGE_SIZE, Strings.toString(pageSize));

                ImplementationBridgeHelpers.CosmosQueryRequestOptionsHelper.getCosmosQueryRequestOptionsAccessor().setPartitionKeyDefinition(cosmosQueryRequestOptions, collection.getPartitionKey());
                ImplementationBridgeHelpers.CosmosQueryRequestOptionsHelper.getCosmosQueryRequestOptionsAccessor().setCollectionRid(cosmosQueryRequestOptions, collection.getResourceId());
                return this.createDocumentServiceRequestWithFeedRange(headers,
                    querySpec,
                    null,
                    partitionKeyRange,
                    collection.getResourceId(),
                    cosmosQueryRequestOptions.getThroughputControlGroupName());
            };

            Function<RxDocumentServiceRequest, Mono<FeedResponse<T>>> executeFunc = (request) -> this.executeRequestAsync(
                    this.itemSerializer,
                    request);

            DocumentProducer<T> dp =
                createDocumentProducer(
                    collection.getResourceId(),
                    null,
                    -1,
                    cosmosQueryRequestOptions,
                    querySpec,
                    commonRequestHeaders,
                    createRequestFunc,
                    executeFunc,
                    () -> client.getResetSessionTokenRetryPolicy().getRequestPolicy(this.diagnosticsClientContext),
                    feedRangeEpk,
                    collection.getSelfLink());

            documentProducers.add(dp);
        }
    }
}
