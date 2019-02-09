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

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.github.davidmoten.rx.Transformers;
import com.microsoft.azure.cosmosdb.Resource;
import com.microsoft.azure.cosmosdb.internal.RequestChargeTracker;
import com.microsoft.azure.cosmosdb.internal.query.orderbyquery.OrderByRowResult;
import com.microsoft.azure.cosmosdb.internal.query.orderbyquery.OrderbyRowComparer;
import com.microsoft.azure.cosmosdb.QueryMetrics;

import rx.Observable;
import rx.Observable.Transformer;

class OrderByUtils {

    public static <T extends Resource> Observable<OrderByRowResult<T>> orderedMerge(Class<T> klass, 
            OrderbyRowComparer<T> consumeComparer,
            RequestChargeTracker tracker, 
            List<DocumentProducer<T>> documentProducers, Map<String, QueryMetrics> queryMetricsMap) {
        return toOrderByQueryResultObservable(klass, documentProducers.get(0), tracker, queryMetricsMap)
                .compose(
                        Transformers.orderedMergeWith(
                                documentProducers.subList(1, documentProducers.size())
                                .stream()
                                .map(producer -> toOrderByQueryResultObservable(klass, producer, tracker, queryMetricsMap))
                                .collect(Collectors.toList()), consumeComparer, false, 1));
    }

    private static <T extends Resource> Observable<OrderByRowResult<T>> toOrderByQueryResultObservable(Class<T> klass,
                                                                                                       DocumentProducer<T> producer,
                                                                                                       RequestChargeTracker tracker,
                                                                                                       Map<String, QueryMetrics> queryMetricsMap) {
        return producer
                .produceAsync()
                .compose(new OrderByUtils.PageToItemTransformer<T>(klass, tracker, queryMetricsMap));
    }

    private static class PageToItemTransformer<T extends Resource> implements Transformer<DocumentProducer<T>.DocumentProducerFeedResponse, OrderByRowResult<T>> {
        private final RequestChargeTracker tracker;
        private final Class<T> klass;
        private final Map<String, QueryMetrics> queryMetricsMap;

        public PageToItemTransformer(Class<T> klass, RequestChargeTracker tracker, Map<String, QueryMetrics> queryMetricsMap) {
            this.klass = klass;
            this.tracker = tracker;
            this.queryMetricsMap = queryMetricsMap;
        }

        @Override
        public Observable<OrderByRowResult<T>> call(Observable<DocumentProducer<T>.DocumentProducerFeedResponse> source) {
            return source.flatMap(documentProducerFeedResponse -> {
                for(String key : documentProducerFeedResponse.pageResult.getQueryMetrics().keySet()) {
                    if (queryMetricsMap.containsKey(key)) {
                        QueryMetrics qm = documentProducerFeedResponse.pageResult.getQueryMetrics().get(key);
                        queryMetricsMap.get(key).add(qm);
                    } else {
                        queryMetricsMap.put(key, documentProducerFeedResponse.pageResult.getQueryMetrics().get(key));
                    }
                }
                tracker.addCharge(documentProducerFeedResponse.pageResult.getRequestCharge());
                Observable<T> x = Observable.<T>from(documentProducerFeedResponse.pageResult.getResults());

                return x.map(r -> new OrderByRowResult<T>(klass, r.toJson(), documentProducerFeedResponse.sourcePartitionKeyRange));
            }, 1);
        }
    }

}
