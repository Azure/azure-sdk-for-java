// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.query;

import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.implementation.ClientSideRequestStatistics;
import com.azure.cosmos.implementation.Configs;
import com.azure.cosmos.implementation.Document;
import com.azure.cosmos.implementation.ImplementationBridgeHelpers;
import com.azure.cosmos.implementation.QueryMetrics;
import com.azure.cosmos.implementation.RequestChargeTracker;
import com.azure.cosmos.implementation.Resource;
import com.azure.cosmos.implementation.query.orderbyquery.OrderByRowResult;
import com.azure.cosmos.implementation.query.orderbyquery.OrderbyRowComparer;
import reactor.core.publisher.Flux;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.function.Function;

public class NonStreamingOrderByUtils {
    private final static
    ImplementationBridgeHelpers.CosmosDiagnosticsHelper.CosmosDiagnosticsAccessor diagnosticsAccessor =
        ImplementationBridgeHelpers.CosmosDiagnosticsHelper.getCosmosDiagnosticsAccessor();

    public static <T extends Resource> Flux<OrderByRowResult<Document>> nonStreamingOrderedMerge(OrderbyRowComparer<Document> consumeComparer,
                                                                                                 RequestChargeTracker tracker,
                                                                                                 List<DocumentProducer<Document>> documentProducers,
                                                                                                 int initialPageSize,
                                                                                                 Map<String, QueryMetrics> queryMetricsMap,
                                                                                                 Collection<ClientSideRequestStatistics> clientSideRequestStatistics) {
        @SuppressWarnings("unchecked")
        Flux<OrderByRowResult<Document>>[] fluxes = documentProducers
            .subList(0, documentProducers.size())
            .stream()
            .map(producer ->
                toNonStreamingOrderByQueryResultObservable(producer, tracker, queryMetricsMap, initialPageSize,
                    consumeComparer, clientSideRequestStatistics))
            .toArray(Flux[]::new);
        return Flux.mergeComparingDelayError(1, consumeComparer, fluxes);
    }

    private static Flux<OrderByRowResult<Document>> toNonStreamingOrderByQueryResultObservable(DocumentProducer<Document> producer,
                                                                                               RequestChargeTracker tracker,
                                                                                               Map<String, QueryMetrics> queryMetricsMap,
                                                                                               int initialPageSize,
                                                                                               OrderbyRowComparer<Document> consumeComparer,
                                                                                               Collection<ClientSideRequestStatistics> clientSideRequestStatisticsList) {
        return producer
            .produceAsync()
            .transformDeferred(new NonStreamingOrderByUtils.PageToItemTransformer(tracker, queryMetricsMap, initialPageSize,
                consumeComparer, clientSideRequestStatisticsList));
    }

    private static class PageToItemTransformer implements
        Function<Flux<DocumentProducer<Document>.DocumentProducerFeedResponse>, Flux<OrderByRowResult<Document>>> {
        private final RequestChargeTracker tracker;
        private final Map<String, QueryMetrics> queryMetricsMap;
        private final Integer initialPageSize;
        private final OrderbyRowComparer<Document> consumeComparer;
        private final Collection<ClientSideRequestStatistics> clientSideRequestStatistics;

        private PageToItemTransformer(RequestChargeTracker tracker, Map<String, QueryMetrics> queryMetricsMap,
                                      Integer initialPageSize, OrderbyRowComparer<Document> consumeComparer,
                                      Collection<ClientSideRequestStatistics> clientSideRequestStatistics) {
            this.tracker = tracker;
            this.queryMetricsMap = queryMetricsMap;
            this.initialPageSize = initialPageSize;
            this.consumeComparer = consumeComparer;
            this.clientSideRequestStatistics = clientSideRequestStatistics;
        }

        @Override
        public Flux<OrderByRowResult<Document>> apply(Flux<DocumentProducer<Document>.DocumentProducerFeedResponse> source) {
            // the size of the priority queue is set to size+1, because when the pq reaches the max size we add that
            // item and then remove the element. If we don't do this, then when adding this element the size of the pq
            // will be increased automatically by 50% and then there would be inconsistent results for later pages.
            PriorityBlockingQueue<OrderByRowResult<Document>> priorityQueue = new PriorityBlockingQueue<>(initialPageSize + 1, consumeComparer);
            return source.flatMap(documentProducerFeedResponse -> {
                    clientSideRequestStatistics.addAll(
                        diagnosticsAccessor.getClientSideRequestStatisticsForQueryPipelineAggregations(documentProducerFeedResponse
                            .pageResult.getCosmosDiagnostics()));

                    QueryMetrics.mergeQueryMetricsMap(queryMetricsMap,
                        BridgeInternal.queryMetricsFromFeedResponse(documentProducerFeedResponse.pageResult));
                    List<Document> results = documentProducerFeedResponse.pageResult.getResults();
                    results.forEach(r -> {
                        OrderByRowResult<Document> orderByRowResult = new OrderByRowResult<Document>(
                            r.toJson(),
                            documentProducerFeedResponse.sourceFeedRange,
                            null);
                        priorityQueue.add(orderByRowResult);
                        if (priorityQueue.size() > initialPageSize) {
                            PriorityBlockingQueue<OrderByRowResult<Document>> tempPriorityQueue = new PriorityBlockingQueue<>(initialPageSize + 1, consumeComparer);
                            for (int i = 0; i < initialPageSize; i++) {
                                tempPriorityQueue.add(priorityQueue.poll());
                            }
                            priorityQueue.clear();
                            priorityQueue.addAll(tempPriorityQueue);
                        }
                    });
                    tracker.addCharge(documentProducerFeedResponse.pageResult.getRequestCharge());
                    // Returning an empty Flux since we are only processing and managing state here
                    return Flux.empty();
                }, 1)
                .thenMany(Flux.defer(() -> Flux.fromIterable(priorityQueue)));
        }
    }
}
