// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.query;

import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.implementation.ClientSideRequestStatistics;
import com.azure.cosmos.implementation.feedranges.FeedRangeEpkImpl;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.FeedRange;
import com.azure.cosmos.models.FeedResponse;
import com.azure.cosmos.implementation.Resource;
import com.azure.cosmos.implementation.DocumentClientRetryPolicy;
import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.PartitionKeyRange;
import com.azure.cosmos.implementation.QueryMetrics;
import com.azure.cosmos.implementation.RequestChargeTracker;
import com.azure.cosmos.implementation.RxDocumentServiceRequest;
import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.implementation.query.orderbyquery.OrderByRowResult;
import com.azure.cosmos.implementation.query.orderbyquery.OrderbyRowComparer;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.function.Function;

class OrderByDocumentProducer<T extends Resource> extends DocumentProducer<T> {
    private final OrderbyRowComparer<T> consumeComparer;
    private final Map<FeedRangeEpkImpl, OrderByContinuationToken> targetRangeToOrderByContinuationTokenMap;

    OrderByDocumentProducer(
            OrderbyRowComparer<T> consumeComparer,
            IDocumentQueryClient client,
            String collectionResourceId,
            CosmosQueryRequestOptions cosmosQueryRequestOptions,
            TriFunction<FeedRangeEpkImpl, String, Integer, RxDocumentServiceRequest> createRequestFunc,
            Function<RxDocumentServiceRequest, Mono<FeedResponse<T>>> executeRequestFunc,
            PartitionKeyRange targetRange,
            FeedRangeEpkImpl feedRange,
            String collectionLink,
            Callable<DocumentClientRetryPolicy> createRetryPolicyFunc,
            Class<T> resourceType,
            UUID correlatedActivityId,
            int initialPageSize,
            String initialContinuationToken,
            int top,
            Map<FeedRangeEpkImpl, OrderByContinuationToken> targetRangeToOrderByContinuationTokenMap) {
        super(client, collectionResourceId, cosmosQueryRequestOptions, createRequestFunc, executeRequestFunc, targetRange,
              collectionLink, createRetryPolicyFunc, resourceType, correlatedActivityId, initialPageSize,
              initialContinuationToken,top, feedRange);
        this.consumeComparer = consumeComparer;
        this.targetRangeToOrderByContinuationTokenMap = targetRangeToOrderByContinuationTokenMap;
    }

    protected Flux<DocumentProducerFeedResponse> produceOnSplit(Flux<DocumentProducer<T>> replacementProducers) {
        return replacementProducers.collectList().flux().flatMap(documentProducers -> {
            RequestChargeTracker tracker = new RequestChargeTracker();
            Map<String, QueryMetrics> queryMetricsMap = new HashMap<>();
            List<ClientSideRequestStatistics> clientSideRequestStatisticsList = new ArrayList<>();
            return OrderByUtils.orderedMerge(resourceType, consumeComparer, tracker, documentProducers, queryMetricsMap,
                    targetRangeToOrderByContinuationTokenMap, clientSideRequestStatisticsList)
                    .map(orderByQueryResult -> resultPageFrom(tracker, orderByQueryResult));
        });
    }

    @SuppressWarnings("unchecked")
    private DocumentProducerFeedResponse resultPageFrom(RequestChargeTracker tracker, OrderByRowResult<T> row) {
        double requestCharge = tracker.getAndResetCharge();
        Map<String, String> headers = Utils.immutableMapOf(HttpConstants.HttpHeaders.REQUEST_CHARGE, String.valueOf(requestCharge));
        FeedResponse<T> fr = BridgeInternal.createFeedResponse(Collections.singletonList((T) row), headers);
        return new DocumentProducerFeedResponse(fr, row.getSourceRange());
    }

    protected DocumentProducer<T> createChildDocumentProducerOnSplit(
            PartitionKeyRange targetRange,
            String initialContinuationToken) {

        return new OrderByDocumentProducer<>(
            consumeComparer,
            client,
            collectionRid,
            cosmosQueryRequestOptions,
            createRequestFunc,
            executeRequestFuncWithRetries,
            targetRange,
            new FeedRangeEpkImpl(targetRange.toRange()),
            collectionLink,
            createRetryPolicyFunc,
            resourceType ,
            correlatedActivityId,
            pageSize,
            initialContinuationToken,
            top,
            this.targetRangeToOrderByContinuationTokenMap);
    }

}
