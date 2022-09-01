// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.query;

import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.implementation.BadRequestException;
import com.azure.cosmos.implementation.ClientSideRequestStatistics;
import com.azure.cosmos.implementation.Document;
import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.QueryMetrics;
import com.azure.cosmos.implementation.RequestChargeTracker;
import com.azure.cosmos.implementation.Resource;
import com.azure.cosmos.implementation.ResourceId;
import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.implementation.apachecommons.lang.tuple.Pair;
import com.azure.cosmos.implementation.feedranges.FeedRangeEpkImpl;
import com.azure.cosmos.implementation.query.orderbyquery.OrderByRowResult;
import com.azure.cosmos.implementation.query.orderbyquery.OrderbyRowComparer;
import com.azure.cosmos.models.ModelBridgeInternal;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

class OrderByUtils {

    public static <T extends Resource> Flux<OrderByRowResult<Document>> orderedMerge(OrderbyRowComparer<Document> consumeComparer,
                                                                                     RequestChargeTracker tracker,
                                                                                     List<DocumentProducer<Document>> documentProducers,
                                                                                     Map<String, QueryMetrics> queryMetricsMap,
                                                                                     Map<FeedRangeEpkImpl, OrderByContinuationToken> targetRangeToOrderByContinuationTokenMap,
                                                                                     List<ClientSideRequestStatistics> clientSideRequestStatisticsList) {
        @SuppressWarnings("unchecked")
        Flux<OrderByRowResult<Document>>[] fluxes = documentProducers
                .subList(0, documentProducers.size())
                .stream()
                .map(producer ->
                        toOrderByQueryResultObservable(producer, tracker, queryMetricsMap,
                                                       targetRangeToOrderByContinuationTokenMap,
                                                       consumeComparer.getSortOrders(), clientSideRequestStatisticsList))
                .toArray(Flux[]::new);
        return Flux.mergeOrdered(consumeComparer, fluxes);
    }

    private static Flux<OrderByRowResult<Document>> toOrderByQueryResultObservable(DocumentProducer<Document> producer,
                                                                                                 RequestChargeTracker tracker,
                                                                                                 Map<String, QueryMetrics> queryMetricsMap,
                                                                                                 Map<FeedRangeEpkImpl, OrderByContinuationToken> targetRangeToOrderByContinuationTokenMap,
                                                                                                 List<SortOrder> sortOrders,
                                                                                                 List<ClientSideRequestStatistics> clientSideRequestStatisticsList) {
        return producer
                .produceAsync()
                   .transformDeferred(new OrderByUtils.PageToItemTransformer(tracker, queryMetricsMap,
                                                                      targetRangeToOrderByContinuationTokenMap,
                                                                      sortOrders, clientSideRequestStatisticsList));
    }

    private static class PageToItemTransformer implements
        Function<Flux<DocumentProducer<Document>.DocumentProducerFeedResponse>, Flux<OrderByRowResult<Document>>> {
        private final RequestChargeTracker tracker;
        private final Map<String, QueryMetrics> queryMetricsMap;
        private final Map<FeedRangeEpkImpl, OrderByContinuationToken> targetRangeToOrderByContinuationTokenMap;
        private final List<SortOrder> sortOrders;
        private final List<ClientSideRequestStatistics> clientSideRequestStatisticsList;

        public PageToItemTransformer(
            RequestChargeTracker tracker, Map<String, QueryMetrics> queryMetricsMap,
            Map<FeedRangeEpkImpl, OrderByContinuationToken> targetRangeToOrderByContinuationTokenMap,
            List<SortOrder> sortOrders, List<ClientSideRequestStatistics> clientSideRequestStatisticsList) {
            this.tracker = tracker;
            this.queryMetricsMap = queryMetricsMap;
            this.targetRangeToOrderByContinuationTokenMap = targetRangeToOrderByContinuationTokenMap;
            this.sortOrders = sortOrders;
            this.clientSideRequestStatisticsList = clientSideRequestStatisticsList;
        }

        @Override
        public Flux<OrderByRowResult<Document>> apply(Flux<DocumentProducer<Document>.DocumentProducerFeedResponse> source) {
            return source.flatMap(documentProducerFeedResponse -> {
                clientSideRequestStatisticsList.addAll(
                    BridgeInternal.getClientSideRequestStatisticsList(documentProducerFeedResponse
                                                                   .pageResult.getCosmosDiagnostics()));
                QueryMetrics.mergeQueryMetricsMap(queryMetricsMap,
                                                  BridgeInternal.queryMetricsFromFeedResponse(documentProducerFeedResponse.pageResult));
                List<Document> results = documentProducerFeedResponse.pageResult.getResults();
                OrderByContinuationToken orderByContinuationToken =
                    targetRangeToOrderByContinuationTokenMap.get(documentProducerFeedResponse.sourceFeedRange);
                if (orderByContinuationToken != null) {
                    Pair<Boolean, ResourceId> booleanResourceIdPair = ResourceId.tryParse(orderByContinuationToken.getRid());
                    if (!booleanResourceIdPair.getLeft()) {
                        return Flux.error(new BadRequestException(String.format("INVALID Rid in the continuation token %s for OrderBy~Context.",
                                orderByContinuationToken.getCompositeContinuationToken().getToken())));
                    }
                    ResourceId continuationTokenRid = booleanResourceIdPair.getRight();

                    String queryInfoExecution = documentProducerFeedResponse.pageResult.getResponseHeaders().getOrDefault(
                        HttpConstants.HttpHeaders.QUERY_EXECUTION_INFO, null);
                    QueryExecutionInfo queryExecutionInfo = Utils.parse(queryInfoExecution, QueryExecutionInfo.class);

                    results = results.stream()
                            .filter(tOrderByRowResult -> {
                                // When we resume a query on a partition there is a possibility that we only read a partial page from the backend
                                // meaning that will we repeat some documents if we didn't do anything about it.
                                // The solution is to filter all the documents that come before in the sort order, since we have already emitted them to the client.
                                // The key is to seek until we get an order by value that matches the order by value we left off on.
                                // Once we do that we need to seek to the correct _rid within the term,
                                // since there might be many documents with the same order by value we left off on.
                                List<QueryItem> queryItems = new ArrayList<QueryItem>();
                                ArrayNode arrayNode = (ArrayNode)ModelBridgeInternal.getObjectFromJsonSerializable(tOrderByRowResult, "orderByItems");
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
                                    // If there is a tie in the sort order the documents should be in _rid order in the same direction as the index (given by the backend)
                                    cmp = (continuationTokenRid.getDocument() - ResourceId.tryParse(tOrderByRowResult.getResourceId()).getRight().getDocument());
                                    if ((queryExecutionInfo == null) || queryExecutionInfo.getReverseRidEnabled())
                                    {
                                        // If reverse rid is enabled on the backend then fallback to the old way of doing it.
                                        // So if it's ORDER BY c.age ASC, c.name DESC the _rids are ASC
                                        // If it's ORDER BY c.age DESC, c.name DESC the _rids are DESC
                                        if (sortOrders.get(0) == SortOrder.Descending)
                                        {
                                            cmp = -cmp;
                                        }
                                    }
                                    else
                                    {
                                        // Go by the whatever order the index wants
                                        if (queryExecutionInfo.getReverseIndexScan())
                                        {
                                            cmp = -cmp;
                                        }
                                    }

                                    return (cmp <= 0);
                                }
                                return true;

                            })
                            .collect(Collectors.toList());

                }

                tracker.addCharge(documentProducerFeedResponse.pageResult.getRequestCharge());
                Flux<Document> x = Flux.fromIterable(results);

                return x.map(r -> new OrderByRowResult<Document>(
                        ModelBridgeInternal.toJsonFromJsonSerializable(r),
                        documentProducerFeedResponse.sourceFeedRange,
                        documentProducerFeedResponse.pageResult.getContinuationToken()));
            }, 1);
        }
    }

}
