package com.azure.cosmos.implementation.query;

import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.implementation.ClientSideRequestStatistics;
import com.azure.cosmos.implementation.Document;
import com.azure.cosmos.implementation.ImplementationBridgeHelpers;
import com.azure.cosmos.implementation.QueryMetrics;
import com.azure.cosmos.implementation.RequestChargeTracker;
import com.azure.cosmos.implementation.Resource;
import com.azure.cosmos.implementation.query.orderbyquery.OrderByRowResult;
import com.azure.cosmos.implementation.query.orderbyquery.OrderbyRowComparer;
import com.azure.cosmos.models.ModelBridgeInternal;
import reactor.core.publisher.Flux;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

public class NonStreamingOrderByUtils {
    private final static
    ImplementationBridgeHelpers.CosmosDiagnosticsHelper.CosmosDiagnosticsAccessor diagnosticsAccessor =
        ImplementationBridgeHelpers.CosmosDiagnosticsHelper.getCosmosDiagnosticsAccessor();

    public static <T extends Resource> Flux<OrderByRowResult<Document>> nonStreamingOrderedMerge(OrderbyRowComparer<Document> consumeComparer,
                                                                                                 RequestChargeTracker tracker,
                                                                                                 List<DocumentProducer<Document>> documentProducers,
                                                                                                 Map<String, QueryMetrics> queryMetricsMap,
                                                                                                 int maxSizePerPartition,
                                                                                                 Collection<ClientSideRequestStatistics> clientSideRequestStatistics) {
        @SuppressWarnings("unchecked")
        Flux<OrderByRowResult<Document>>[] fluxes = documentProducers
            .subList(0, documentProducers.size())
            .stream()
            .map(producer ->
                toNonStreamingOrderByQueryResultObservable(producer, tracker, queryMetricsMap,
                    maxSizePerPartition, consumeComparer, clientSideRequestStatistics))
            .toArray(Flux[]::new);
        return Flux.mergeOrdered(consumeComparer, fluxes);
    }

    private static Flux<OrderByRowResult<Document>> toNonStreamingOrderByQueryResultObservable(DocumentProducer<Document> producer,
                                                                                               RequestChargeTracker tracker,
                                                                                               Map<String, QueryMetrics> queryMetricsMap,
                                                                                               int maxSizePerPartition,
                                                                                               OrderbyRowComparer<Document> consumeComparer,
                                                                                               Collection<ClientSideRequestStatistics> clientSideRequestStatisticsList) {
        return producer
            .produceAsync()
            .transformDeferred(new NonStreamingOrderByUtils.PageToItemTransformer(tracker, queryMetricsMap,
                maxSizePerPartition, consumeComparer, clientSideRequestStatisticsList));
    }

    private static class PageToItemTransformer implements
        Function<Flux<DocumentProducer<Document>.DocumentProducerFeedResponse>, Flux<OrderByRowResult<Document>>> {
        private final RequestChargeTracker tracker;
        private final Map<String, QueryMetrics> queryMetricsMap;
        private final Integer maxSizePerPartition;
        private final OrderbyRowComparer<Document> consumeComparer;
        private final Collection<ClientSideRequestStatistics> clientSideRequestStatistics;

        private PageToItemTransformer(RequestChargeTracker tracker, Map<String, QueryMetrics> queryMetricsMap,
                                      Integer maxSizePerPartition, OrderbyRowComparer<Document> consumeComparer,
                                      Collection<ClientSideRequestStatistics> clientSideRequestStatistics) {
            this.tracker = tracker;
            this.queryMetricsMap = queryMetricsMap;
            this.maxSizePerPartition = maxSizePerPartition;
            this.consumeComparer = consumeComparer;
            this.clientSideRequestStatistics = clientSideRequestStatistics;
        }

        @Override
        public Flux<OrderByRowResult<Document>> apply(Flux<DocumentProducer<Document>.DocumentProducerFeedResponse> source) {
            PriorityQueue<OrderByRowResult<Document>> priorityQueue = new PriorityQueue<>(consumeComparer);
            AtomicBoolean emitFlag = new AtomicBoolean(true);

            return source.flatMap(documentProducerFeedResponse -> {
                    // Checks if the max size has been reached, if so, stop processing new pages
                    if (emitFlag.get()) {
                        clientSideRequestStatistics.addAll(
                            diagnosticsAccessor.getClientSideRequestStatisticsForQueryPipelineAggregations(documentProducerFeedResponse
                                .pageResult.getCosmosDiagnostics()));

                        QueryMetrics.mergeQueryMetricsMap(queryMetricsMap,
                            BridgeInternal.queryMetricsFromFeedResponse(documentProducerFeedResponse.pageResult));
                        List<Document> results = documentProducerFeedResponse.pageResult.getResults();
                        results.forEach(r -> {
                            OrderByRowResult<Document> orderByRowResult = new OrderByRowResult<Document>(
                                ModelBridgeInternal.toJsonFromJsonSerializable(r),
                                documentProducerFeedResponse.sourceFeedRange,
                                null);
                            if (priorityQueue.size() < maxSizePerPartition) {
                                priorityQueue.add(orderByRowResult);
                            } else {
                                emitFlag.set(false);
                            }
                        });
                        tracker.addCharge(documentProducerFeedResponse.pageResult.getRequestCharge());
                    }
                    // Returning an empty Flux since we are only processing and managing state here
                    return Flux.empty();
                }, 1)
                .thenMany(Flux.defer(() -> Flux.fromIterable(priorityQueue)));
        }
    }
}
