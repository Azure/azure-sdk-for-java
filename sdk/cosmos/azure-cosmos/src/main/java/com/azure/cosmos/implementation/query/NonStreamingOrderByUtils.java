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
import java.util.PriorityQueue;
import java.util.function.Function;

public class NonStreamingOrderByUtils {

    private static ImplementationBridgeHelpers.CosmosDiagnosticsHelper.CosmosDiagnosticsAccessor diagAccessor() {
        return ImplementationBridgeHelpers.CosmosDiagnosticsHelper.getCosmosDiagnosticsAccessor();
    }

    public static <T extends Resource> Flux<OrderByRowResult<Document>> nonStreamingOrderedMerge(OrderbyRowComparer<Document> consumeComparer,
                                                                                                 RequestChargeTracker tracker,
                                                                                                 List<DocumentProducer<Document>> documentProducers,
                                                                                                 int candidateLimit,
                                                                                                 boolean useGlobalTop,
                                                                                                 Map<String, QueryMetrics> queryMetricsMap,
                                                                                                 Collection<ClientSideRequestStatistics> clientSideRequestStatistics) {
        PageToItemTransformer retainCandidates = new PageToItemTransformer(
            tracker, queryMetricsMap, candidateLimit, consumeComparer, clientSideRequestStatistics);
        if (useGlobalTop && !documentProducers.isEmpty()) {
            // With only TOP downstream, a row outside the global top K can never be returned.
            return Flux.fromIterable(documentProducers)
                .flatMap(DocumentProducer::produceAsync, documentProducers.size(), 1)
                .transformDeferred(retainCandidates);
        }

        @SuppressWarnings("unchecked")
        Flux<OrderByRowResult<Document>>[] fluxes = documentProducers
            .stream()
            .map(producer -> producer.produceAsync().transformDeferred(retainCandidates))
            .toArray(Flux[]::new);
        return Flux.mergeComparingDelayError(1, consumeComparer, fluxes);
    }

    private static class PageToItemTransformer implements
        Function<Flux<DocumentProducer<Document>.DocumentProducerFeedResponse>, Flux<OrderByRowResult<Document>>> {
        private final RequestChargeTracker tracker;
        private final Map<String, QueryMetrics> queryMetricsMap;
        private final int candidateLimit;
        private final OrderbyRowComparer<Document> consumeComparer;
        private final Collection<ClientSideRequestStatistics> clientSideRequestStatistics;

        private PageToItemTransformer(RequestChargeTracker tracker, Map<String, QueryMetrics> queryMetricsMap,
                                      int candidateLimit, OrderbyRowComparer<Document> consumeComparer,
                                      Collection<ClientSideRequestStatistics> clientSideRequestStatistics) {
            this.tracker = tracker;
            this.queryMetricsMap = queryMetricsMap;
            this.candidateLimit = candidateLimit;
            this.consumeComparer = consumeComparer;
            this.clientSideRequestStatistics = clientSideRequestStatistics;
        }

        @Override
        public Flux<OrderByRowResult<Document>> apply(Flux<DocumentProducer<Document>.DocumentProducerFeedResponse> source) {
            // Keep the worst retained row at the head for efficient eviction.
            PriorityQueue<OrderByRowResult<Document>> candidates = new PriorityQueue<>(consumeComparer.reversed());
            return source.doOnNext(documentProducerFeedResponse -> {
                clientSideRequestStatistics.addAll(
                    diagAccessor().getClientSideRequestStatisticsForQueryPipelineAggregations(documentProducerFeedResponse
                        .pageResult.getCosmosDiagnostics()));

                QueryMetrics.mergeQueryMetricsMap(queryMetricsMap,
                    BridgeInternal.queryMetricsFromFeedResponse(documentProducerFeedResponse.pageResult));
                List<Document> results = documentProducerFeedResponse.pageResult.getResults();
                results.forEach(row -> {
                    candidates.add(new OrderByRowResult<>(row.toJson(), documentProducerFeedResponse.sourceFeedRange, null));
                    if (candidates.size() > candidateLimit) {
                        candidates.poll();
                    }
                });
                tracker.addCharge(documentProducerFeedResponse.pageResult.getRequestCharge());
            })
                .thenMany(Flux.defer(() -> Flux.fromIterable(candidates).sort(consumeComparer)));
        }
    }
}
