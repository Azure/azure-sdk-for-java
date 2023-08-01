// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.query;

import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.implementation.ClientSideRequestStatistics;
import com.azure.cosmos.implementation.DistinctClientSideRequestStatisticsCollection;
import com.azure.cosmos.implementation.Document;
import com.azure.cosmos.implementation.DocumentClientRetryPolicy;
import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.ImplementationBridgeHelpers;
import com.azure.cosmos.implementation.PartitionKeyRange;
import com.azure.cosmos.implementation.QueryMetrics;
import com.azure.cosmos.implementation.RequestChargeTracker;
import com.azure.cosmos.implementation.RxDocumentServiceRequest;
import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.implementation.feedranges.FeedRangeEpkImpl;
import com.azure.cosmos.implementation.query.orderbyquery.OrderByRowResult;
import com.azure.cosmos.implementation.query.orderbyquery.OrderbyRowComparer;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.FeedResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Supplier;

class OrderByDocumentProducer extends DocumentProducer<Document> {

    private static final ImplementationBridgeHelpers.FeedResponseHelper.FeedResponseAccessor feedResponseAccessor =
        ImplementationBridgeHelpers.FeedResponseHelper.getFeedResponseAccessor();
    private final OrderbyRowComparer<Document> consumeComparer;
    private final Map<FeedRangeEpkImpl, OrderByContinuationToken> targetRangeToOrderByContinuationTokenMap;

    OrderByDocumentProducer(
            OrderbyRowComparer<Document> consumeComparer,
            IDocumentQueryClient client,
            String collectionResourceId,
            CosmosQueryRequestOptions cosmosQueryRequestOptions,
            TriFunction<FeedRangeEpkImpl, String, Integer, RxDocumentServiceRequest> createRequestFunc,
            Function<RxDocumentServiceRequest, Mono<FeedResponse<Document>>> executeRequestFunc,
            FeedRangeEpkImpl feedRange,
            String collectionLink,
            Callable<DocumentClientRetryPolicy> createRetryPolicyFunc,
            Class<Document> resourceType,
            UUID correlatedActivityId,
            int initialPageSize,
            String initialContinuationToken,
            int top,
            Map<FeedRangeEpkImpl, OrderByContinuationToken> targetRangeToOrderByContinuationTokenMap,
            Supplier<String> operationContextTextProvider) {
        super(client, collectionResourceId, cosmosQueryRequestOptions, createRequestFunc, executeRequestFunc,
              collectionLink, createRetryPolicyFunc, resourceType, correlatedActivityId, initialPageSize,
              initialContinuationToken,top, feedRange, operationContextTextProvider);
        this.consumeComparer = consumeComparer;
        this.targetRangeToOrderByContinuationTokenMap = targetRangeToOrderByContinuationTokenMap;
    }

    protected Flux<DocumentProducerFeedResponse> produceOnFeedRangeGone(Flux<DocumentProducer<Document>> replacementProducers) {
        return replacementProducers.collectList().flux().flatMap(documentProducers -> {
            RequestChargeTracker tracker = new RequestChargeTracker();
            Map<String, QueryMetrics> queryMetricsMap = new ConcurrentHashMap<>();
            Collection<ClientSideRequestStatistics> clientSideRequestStatisticsList = new DistinctClientSideRequestStatisticsCollection();
            return OrderByUtils.orderedMerge(consumeComparer, tracker, documentProducers, queryMetricsMap,
                    targetRangeToOrderByContinuationTokenMap, clientSideRequestStatisticsList)
                    .map(orderByQueryResult -> resultPageFrom(tracker, orderByQueryResult));
        });
    }

    @SuppressWarnings("unchecked")
    private DocumentProducerFeedResponse resultPageFrom(RequestChargeTracker tracker, OrderByRowResult<Document> row) {
        double requestCharge = tracker.getAndResetCharge();
        Map<String, String> headers = Utils.immutableMapOf(HttpConstants.HttpHeaders.REQUEST_CHARGE, String.valueOf(requestCharge));
        FeedResponse<Document> fr = feedResponseAccessor.createFeedResponse(
            Collections.singletonList((Document) row), headers, null);
        return new DocumentProducerFeedResponse(fr, row.getSourceRange());
    }

    protected DocumentProducer<Document> createChildDocumentProducerOnSplit(
            PartitionKeyRange targetRange,
            String initialContinuationToken) {

        return new OrderByDocumentProducer(
            consumeComparer,
            client,
            collectionRid,
            cosmosQueryRequestOptions,
            createRequestFunc,
            executeRequestFuncWithRetries,
            new FeedRangeEpkImpl(targetRange.toRange()),
            collectionLink,
            createRetryPolicyFunc,
            resourceType ,
            correlatedActivityId,
            pageSize,
            initialContinuationToken,
            top,
            this.targetRangeToOrderByContinuationTokenMap,
            this.operationContextTextProvider);
    }

}
