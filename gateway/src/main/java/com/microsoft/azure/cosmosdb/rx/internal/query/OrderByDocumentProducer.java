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
package com.microsoft.azure.cosmosdb.rx.internal.query;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;

import com.microsoft.azure.cosmosdb.BridgeInternal;
import com.microsoft.azure.cosmosdb.FeedResponse;
import com.microsoft.azure.cosmosdb.PartitionKeyRange;
import com.microsoft.azure.cosmosdb.Resource;
import com.microsoft.azure.cosmosdb.internal.HttpConstants;
import com.microsoft.azure.cosmosdb.internal.RequestChargeTracker;
import com.microsoft.azure.cosmosdb.internal.query.orderbyquery.OrderByRowResult;
import com.microsoft.azure.cosmosdb.internal.query.orderbyquery.OrderbyRowComparer;
import com.microsoft.azure.cosmosdb.rx.internal.IDocumentClientRetryPolicy;
import com.microsoft.azure.cosmosdb.rx.internal.RetryPolicy;
import com.microsoft.azure.cosmosdb.rx.internal.RxDocumentServiceRequest;
import com.microsoft.azure.cosmosdb.rx.internal.Utils;

import rx.Observable;
import rx.functions.Func0;
import rx.functions.Func1;
import rx.functions.Func3;

class OrderByDocumentProducer<T extends Resource> extends DocumentProducer<T> {
    private final OrderbyRowComparer<T> consumeComparer;

    OrderByDocumentProducer(
            OrderbyRowComparer<T> consumeComparer,
            IDocumentQueryClient client, 
            String collectionResourceId,
            Func3<PartitionKeyRange, String, Integer, RxDocumentServiceRequest> createRequestFunc,
            Func1<RxDocumentServiceRequest, Observable<FeedResponse<T>>> executeRequestFunc,
            PartitionKeyRange targetRange, 
            String collectionLink,
            Func0<IDocumentClientRetryPolicy> createRetryPolicyFunc, 
            Class<T> resourceType, 
            UUID correlatedActivityId,
            int initialPageSize, 
            String initialContinuationToken, 
            int top) {
        super(client, collectionResourceId, createRequestFunc, executeRequestFunc, targetRange, collectionLink,
                createRetryPolicyFunc, resourceType, correlatedActivityId, initialPageSize, initialContinuationToken, top);
        this.consumeComparer = consumeComparer;
    }

    protected Observable<DocumentProducerFeedResponse> produceOnSplit(Observable<DocumentProducer<T>> replacementProducers) {
        Observable<DocumentProducerFeedResponse> res = replacementProducers.toList().single().flatMap(documentProducers -> {
            RequestChargeTracker tracker = new RequestChargeTracker();
            return OrderByUtils.orderedMerge(resourceType, consumeComparer, tracker, documentProducers)
                    .map(orderByQueryResult -> resultPageFrom(tracker, orderByQueryResult));
        });

        return res;
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
                createRequestFunc,
                executeRequestFuncWithRetries,
                targetRange,
                collectionLink,
                createRetryPolicyFunc,
                resourceType ,
                correlatedActivityId,
                pageSize,
                initialContinuationToken,
                top);
    }

}
