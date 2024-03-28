// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.query;

import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.CosmosException;
import com.azure.cosmos.implementation.BadRequestException;
import com.azure.cosmos.implementation.ClientSideRequestStatistics;
import com.azure.cosmos.implementation.DistinctClientSideRequestStatisticsCollection;
import com.azure.cosmos.implementation.Document;
import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.ImplementationBridgeHelpers;
import com.azure.cosmos.implementation.JsonSerializable;
import com.azure.cosmos.implementation.QueryMetrics;
import com.azure.cosmos.implementation.query.aggregation.AggregateOperator;
import com.azure.cosmos.models.FeedResponse;
import com.azure.cosmos.models.ModelBridgeInternal;
import com.fasterxml.jackson.databind.node.ObjectNode;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.BiFunction;

public final class GroupByDocumentQueryExecutionContext implements
    IDocumentQueryExecutionComponent<Document> {

    private final static
    ImplementationBridgeHelpers.CosmosDiagnosticsHelper.CosmosDiagnosticsAccessor diagnosticsAccessor =
        ImplementationBridgeHelpers.CosmosDiagnosticsHelper.getCosmosDiagnosticsAccessor();
    public static final String CONTINUATION_TOKEN_NOT_SUPPORTED_WITH_GROUP_BY = "Continuation token is not supported " +
                                                                                    "for queries with GROUP BY." +
                                                                                    "Do not use continuation token" +
                                                                                    " or remove the GROUP BY " +
                                                                                    "from the query.";
    private final IDocumentQueryExecutionComponent<Document> component;
    private final GroupingTable groupingTable;

    GroupByDocumentQueryExecutionContext(
        IDocumentQueryExecutionComponent<Document> component,
        GroupingTable groupingTable) {
        this.component = component;
        this.groupingTable = groupingTable;
    }

    public static Flux<IDocumentQueryExecutionComponent<Document>> createAsync(
        BiFunction<String, PipelinedDocumentQueryParams<Document>, Flux<IDocumentQueryExecutionComponent<Document>>> createSourceComponentFunction,
        String continuationToken,
        Map<String, AggregateOperator> groupByAliasToAggregateType,
        List<String> orderedAliases,
        boolean hasSelectValue,
        PipelinedDocumentQueryParams<Document> documentQueryParams) {
        if (continuationToken != null) {
            CosmosException dce = new BadRequestException(CONTINUATION_TOKEN_NOT_SUPPORTED_WITH_GROUP_BY);
            return Flux.error(dce);
        }
        if (groupByAliasToAggregateType == null) {
            throw new IllegalArgumentException("groupByAliasToAggregateType should not be null");
        }
        if (orderedAliases == null) {
            throw new IllegalArgumentException("orderedAliases should not be null");
        }
        GroupingTable table = new GroupingTable(groupByAliasToAggregateType, orderedAliases, hasSelectValue);
        // Have to pass non-null continuation token once supported
        return createSourceComponentFunction.apply(null, documentQueryParams)
                   .map(component -> new GroupByDocumentQueryExecutionContext(component, table));
    }

    @Override
    public Flux<FeedResponse<Document>> drainAsync(int maxPageSize) {
        return this.component.drainAsync(maxPageSize)
            .collectList()
            .map(superList -> {
                double requestCharge = 0;
                List<Document> documentList = new ArrayList<>();
                /* Do groupBy stuff here */
                // Stage 1:
                // Drain the groupings fully from all continuation and all partitions
                Collection<ClientSideRequestStatistics> diagnosticsList = new DistinctClientSideRequestStatisticsCollection();
                ConcurrentMap<String, QueryMetrics> queryMetrics = new ConcurrentHashMap<>();
                for (FeedResponse<Document> page : superList) {
                    List<Document> results = page.getResults();
                    documentList.addAll(results);
                    requestCharge += page.getRequestCharge();
                    QueryMetrics.mergeQueryMetricsMap(queryMetrics, BridgeInternal.queryMetricsFromFeedResponse(page));
                    diagnosticsList.addAll(
                        diagnosticsAccessor.getClientSideRequestStatisticsForQueryPipelineAggregations(page.getCosmosDiagnostics()));
                }

                this.aggregateGroupings(documentList);

                // Stage 2:
                // Emit the results from the grouping table page by page
                List<Document> groupByResults = null;
                if (this.groupingTable != null) {
                    groupByResults = this.groupingTable.drain(maxPageSize);
                }

                return createFeedResponseFromGroupingTable(requestCharge, queryMetrics, groupByResults,
                                                           diagnosticsList);
            }).expand(tFeedResponse -> {
                // For groupBy query, we have already drained everything for the first page request
                // so for following requests, we will just need to drain page by page from the grouping table
                List<Document> groupByResults = null;
                if (this.groupingTable != null) {
                    groupByResults = this.groupingTable.drain(maxPageSize);
                }

                if (groupByResults == null || groupByResults.size() == 0) {
                    return Mono.empty();
                }

                FeedResponse<Document> response = createFeedResponseFromGroupingTable(0,
                                                                               new ConcurrentHashMap<>(),
                                                                               groupByResults, new HashSet<>());
                return Mono.just(response);
            });
    }

    private FeedResponse<Document> createFeedResponseFromGroupingTable(
        double requestCharge,
        ConcurrentMap<String, QueryMetrics> queryMetrics,
        List<Document> groupByResults,
        Collection<ClientSideRequestStatistics> diagnostics) {

        if (this.groupingTable == null) {
            throw new IllegalStateException("No grouping table defined.");
        }

        HashMap<String, String> headers = new HashMap<>();
        headers.put(HttpConstants.HttpHeaders.REQUEST_CHARGE, Double.toString(requestCharge));
        FeedResponse<Document> frp = BridgeInternal.createFeedResponseWithQueryMetrics(groupByResults, headers,
            queryMetrics, null, false,
            false, null);
        diagnosticsAccessor.addClientSideDiagnosticsToFeed(
            frp.getCosmosDiagnostics(), diagnostics);

        return frp;
    }

    private void aggregateGroupings(List<Document> superList) {
        for (Document d : superList) {
            RewrittenGroupByProjection rewrittenGroupByProjection =
                new RewrittenGroupByProjection(d.getPropertyBag());
            this.groupingTable.addPayLoad(rewrittenGroupByProjection);
        }
    }

    /**
     * When a group by query gets rewritten the projection looks like:
     * <br/>
     * SELECT
     * [{"item": c.age}, {"item": c.name}] AS groupByItems,
     * {"age": c.age, "name": c.name} AS payload
     * <br/>
     * This class just lets us easily access the "groupByItems" and "payload" property.
     */
    static final class RewrittenGroupByProjection extends JsonSerializable {
        private static final String GROUP_BY_ITEMS_PROPERTY_NAME = "groupByItems";
        private static final String PAYLOAD_PROPERTY_NAME = "payload";

        public RewrittenGroupByProjection(ObjectNode objectNode) {
            super(objectNode);
            if (objectNode == null) {
                throw new IllegalArgumentException("objectNode can not be null");
            }
        }

        /**
         * Getter for property 'groupByItems'.
         *
         * @return Value for property 'groupByItems'.
         */
        public List<Document> getGroupByItems() {
            List<Document> groupByItems = this.getList(GROUP_BY_ITEMS_PROPERTY_NAME, Document.class);
            if (groupByItems == null) {
                throw new IllegalStateException("Underlying object does not have an 'groupByItems' field.");
            }
            return groupByItems;
        }

        /**
         * Getter for property 'payload'.
         *
         * @return Value for property 'payload'.
         */
        public Document getPayload() {
            if (!this.has(PAYLOAD_PROPERTY_NAME)) {
                throw new IllegalStateException("Underlying object does not have an 'payload' field.");
            }

            return new Document((ObjectNode) this.get(PAYLOAD_PROPERTY_NAME));
        }

        @Override
        public boolean equals(Object o) {
            return super.equals(o);
        }

        @Override
        public int hashCode() {
            return super.hashCode();
        }
    }
}
