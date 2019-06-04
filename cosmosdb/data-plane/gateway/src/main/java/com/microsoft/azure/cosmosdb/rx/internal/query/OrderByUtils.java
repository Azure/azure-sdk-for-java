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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.github.davidmoten.rx.Transformers;
import com.microsoft.azure.cosmosdb.Resource;
import com.microsoft.azure.cosmosdb.internal.RequestChargeTracker;
import com.microsoft.azure.cosmosdb.internal.ResourceId;
import com.microsoft.azure.cosmosdb.internal.query.ItemComparator;
import com.microsoft.azure.cosmosdb.internal.query.QueryItem;
import com.microsoft.azure.cosmosdb.internal.query.SortOrder;
import com.microsoft.azure.cosmosdb.internal.query.orderbyquery.OrderByRowResult;
import com.microsoft.azure.cosmosdb.internal.query.orderbyquery.OrderbyRowComparer;
import com.microsoft.azure.cosmosdb.QueryMetrics;

import com.microsoft.azure.cosmosdb.rx.internal.BadRequestException;
import org.apache.commons.lang3.tuple.Pair;
import rx.Observable;
import rx.Observable.Transformer;

class OrderByUtils {

    public static <T extends Resource> Observable<OrderByRowResult<T>> orderedMerge(Class<T> klass,
                                                                                    OrderbyRowComparer<T> consumeComparer,
                                                                                    RequestChargeTracker tracker,
                                                                                    List<DocumentProducer<T>> documentProducers,
                                                                                    Map<String, QueryMetrics> queryMetricsMap,
                                                                                    Map<String, OrderByContinuationToken> targetRangeToOrderByContinuationTokenMap) {
        return toOrderByQueryResultObservable(klass, documentProducers.get(0), tracker, queryMetricsMap, targetRangeToOrderByContinuationTokenMap, consumeComparer.getSortOrders())
                .compose(
                        Transformers.orderedMergeWith(
                                documentProducers.subList(1, documentProducers.size())
                                        .stream()
                                        .map(producer -> toOrderByQueryResultObservable(klass, producer, tracker, queryMetricsMap, targetRangeToOrderByContinuationTokenMap, consumeComparer.getSortOrders()))
                                        .collect(Collectors.toList()), consumeComparer, false, 1));
    }

    private static <T extends Resource> Observable<OrderByRowResult<T>> toOrderByQueryResultObservable(Class<T> klass,
                                                                                                       DocumentProducer<T> producer,
                                                                                                       RequestChargeTracker tracker,
                                                                                                       Map<String, QueryMetrics> queryMetricsMap,
                                                                                                       Map<String, OrderByContinuationToken> targetRangeToOrderByContinuationTokenMap,
                                                                                                       List<SortOrder> sortOrders) {
        return producer
                .produceAsync()
                .compose(new OrderByUtils.PageToItemTransformer<T>(klass, tracker, queryMetricsMap, targetRangeToOrderByContinuationTokenMap, sortOrders));
    }

    private static class PageToItemTransformer<T extends Resource> implements Transformer<DocumentProducer<T>.DocumentProducerFeedResponse, OrderByRowResult<T>> {
        private final RequestChargeTracker tracker;
        private final Class<T> klass;
        private final Map<String, QueryMetrics> queryMetricsMap;
        private final Map<String, OrderByContinuationToken> targetRangeToOrderByContinuationTokenMap;
        private final List<SortOrder> sortOrders;

        public PageToItemTransformer(Class<T> klass, RequestChargeTracker tracker, Map<String, QueryMetrics> queryMetricsMap,
                                     Map<String, OrderByContinuationToken> targetRangeToOrderByContinuationTokenMap, List<SortOrder> sortOrders) {
            this.klass = klass;
            this.tracker = tracker;
            this.queryMetricsMap = queryMetricsMap;
            this.targetRangeToOrderByContinuationTokenMap = targetRangeToOrderByContinuationTokenMap;
            this.sortOrders = sortOrders;
        }

        @Override
        public Observable<OrderByRowResult<T>> call(Observable<DocumentProducer<T>.DocumentProducerFeedResponse> source) {
            return source.flatMap(documentProducerFeedResponse -> {
                for (String key : documentProducerFeedResponse.pageResult.getQueryMetrics().keySet()) {
                    if (queryMetricsMap.containsKey(key)) {
                        QueryMetrics qm = documentProducerFeedResponse.pageResult.getQueryMetrics().get(key);
                        queryMetricsMap.get(key).add(qm);
                    } else {
                        queryMetricsMap.put(key, documentProducerFeedResponse.pageResult.getQueryMetrics().get(key));
                    }
                }
                List<T> results = documentProducerFeedResponse.pageResult.getResults();
                OrderByContinuationToken orderByContinuationToken = targetRangeToOrderByContinuationTokenMap.get(documentProducerFeedResponse.sourcePartitionKeyRange.getId());
                if (orderByContinuationToken != null) {
                    Pair<Boolean, ResourceId> booleanResourceIdPair = ResourceId.tryParse(orderByContinuationToken.getRid());
                    if (!booleanResourceIdPair.getLeft()) {
                        return Observable.error(new BadRequestException(String.format("Invalid Rid in the continuation token %s for OrderBy~Context.",
                                orderByContinuationToken.getCompositeContinuationToken().getToken())));
                    }
                    ResourceId continuationTokenRid = booleanResourceIdPair.getRight();
                    results = results.stream()
                            .filter(tOrderByRowResult -> {
                                // When we resume a query on a partition there is a possibility that we only read a partial page from the backend
                                // meaning that will we repeat some documents if we didn't do anything about it.
                                // The solution is to filter all the documents that come before in the sort order, since we have already emitted them to the client.
                                // The key is to seek until we get an order by value that matches the order by value we left off on.
                                // Once we do that we need to seek to the correct _rid within the term,
                                // since there might be many documents with the same order by value we left off on.
                                List<QueryItem> queryItems = new ArrayList<QueryItem>();
                                ArrayNode arrayNode = (ArrayNode) tOrderByRowResult.get("orderByItems");
                                for (JsonNode jsonNode : arrayNode) {
                                    QueryItem queryItem = new QueryItem(jsonNode.toString());
                                    queryItems.add(queryItem);
                                }

                                // Check  if its the same orderby item from the token
                                long cmp = 0;
                                for (int i = 0; i < sortOrders.size(); i++) {
                                    cmp = ItemComparator.getInstance().compare(orderByContinuationToken.getOrderByItems()[i].getItem(),
                                            queryItems.get(i).getItem());
                                    if (cmp != 0) {
                                        cmp = sortOrders.get(i).equals(SortOrder.Descending) ? -cmp : cmp;
                                        break;
                                    }
                                }
                                
                                if (cmp == 0) {
                                    // Once the item matches the order by items from the continuation tokens
                                    // We still need to remove all the documents that have a lower rid in the rid sort order.
                                    // If there is a tie in the sort order the documents should be in _rid order in the same direction as the first order by field.
                                    // So if it's ORDER BY c.age ASC, c.name DESC the _rids are ASC
                                    // If ti's ORDER BY c.age DESC, c.name DESC the _rids are DESC
                                    cmp = (continuationTokenRid.getDocument() - ResourceId.tryParse(tOrderByRowResult.getResourceId()).getRight().getDocument());

                                    if (sortOrders.iterator().next().equals(SortOrder.Descending)) {
                                        cmp = -cmp;
                                    }
                                    return (cmp <= 0);
                                }
                                return true;

                            })
                            .collect(Collectors.toList());

                }

                tracker.addCharge(documentProducerFeedResponse.pageResult.getRequestCharge());
                Observable<T> x = Observable.<T>from(results);

                return x.map(r -> new OrderByRowResult<T>(
                        klass,
                        r.toJson(),
                        documentProducerFeedResponse.sourcePartitionKeyRange,
                        documentProducerFeedResponse.pageResult.getResponseContinuation()));
            }, 1);
        }
    }

}
