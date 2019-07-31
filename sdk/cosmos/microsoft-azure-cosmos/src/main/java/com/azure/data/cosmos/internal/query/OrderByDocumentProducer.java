// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.cosmos.internal.query;

import com.azure.data.cosmos.BridgeInternal;
import com.azure.data.cosmos.FeedOptions;
import com.azure.data.cosmos.FeedResponse;
import com.azure.data.cosmos.Resource;
import com.azure.data.cosmos.internal.HttpConstants;
import com.azure.data.cosmos.internal.IDocumentClientRetryPolicy;
import com.azure.data.cosmos.internal.PartitionKeyRange;
import com.azure.data.cosmos.internal.QueryMetrics;
import com.azure.data.cosmos.internal.RequestChargeTracker;
import com.azure.data.cosmos.internal.RxDocumentServiceRequest;
import com.azure.data.cosmos.internal.Utils;
import com.azure.data.cosmos.internal.query.orderbyquery.OrderByRowResult;
import com.azure.data.cosmos.internal.query.orderbyquery.OrderbyRowComparer;
import reactor.core.publisher.Flux;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.function.Function;

class OrderByDocumentProducer<T extends Resource> extends DocumentProducer<T> {
    private final OrderbyRowComparer<T> consumeComparer;
    private final Map<String, OrderByContinuationToken> targetRangeToOrderByContinuationTokenMap;

    OrderByDocumentProducer(
            OrderbyRowComparer<T> consumeComparer,
            IDocumentQueryClient client,
            String collectionResourceId,
            FeedOptions feedOptions,
            TriFunction<PartitionKeyRange, String, Integer, RxDocumentServiceRequest> createRequestFunc,
            Function<RxDocumentServiceRequest, Flux<FeedResponse<T>>> executeRequestFunc,
            PartitionKeyRange targetRange,
            String collectionLink,
            Callable<IDocumentClientRetryPolicy> createRetryPolicyFunc,
            Class<T> resourceType, 
            UUID correlatedActivityId,
            int initialPageSize, 
            String initialContinuationToken, 
            int top,
            Map<String, OrderByContinuationToken> targetRangeToOrderByContinuationTokenMap) {
        super(client, collectionResourceId, feedOptions, createRequestFunc, executeRequestFunc, targetRange, collectionLink,
                createRetryPolicyFunc, resourceType, correlatedActivityId, initialPageSize, initialContinuationToken, top);
        this.consumeComparer = consumeComparer;
        this.targetRangeToOrderByContinuationTokenMap = targetRangeToOrderByContinuationTokenMap;
    }

    protected Flux<DocumentProducerFeedResponse> produceOnSplit(Flux<DocumentProducer<T>> replacementProducers) {
        return replacementProducers.collectList().flux().flatMap(documentProducers -> {
            RequestChargeTracker tracker = new RequestChargeTracker();
            Map<String, QueryMetrics> queryMetricsMap = new HashMap<>();
            return OrderByUtils.orderedMerge(resourceType, consumeComparer, tracker, documentProducers, queryMetricsMap,
                    targetRangeToOrderByContinuationTokenMap)
                    .map(orderByQueryResult -> resultPageFrom(tracker, orderByQueryResult));
        });
    }

    @SuppressWarnings("unchecked")
    private DocumentProducerFeedResponse resultPageFrom(RequestChargeTracker tracker, OrderByRowResult<T> row) {
        double requestCharge = tracker.getAndResetCharge();
        Map<String, String> headers = Utils.immutableMapOf(HttpConstants.HttpHeaders.REQUEST_CHARGE, String.valueOf(requestCharge));
        FeedResponse<T> fr = BridgeInternal.createFeedResponse(Collections.singletonList((T) row), headers);
        return new DocumentProducerFeedResponse(fr, row.getSourcePartitionKeyRange());
    }

    protected DocumentProducer<T> createChildDocumentProducerOnSplit(
            PartitionKeyRange targetRange,
            String initialContinuationToken) {

        return new OrderByDocumentProducer<>(
                consumeComparer,
                client,
                collectionRid,
                feedOptions,
                createRequestFunc,
                executeRequestFuncWithRetries,
                targetRange,
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
