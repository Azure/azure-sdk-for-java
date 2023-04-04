// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.query;

import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.implementation.ClientSideRequestStatistics;
import com.azure.cosmos.implementation.DistinctClientSideRequestStatisticsCollection;
import com.azure.cosmos.implementation.Document;
import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.ImplementationBridgeHelpers;
import com.azure.cosmos.implementation.QueryMetrics;
import com.azure.cosmos.implementation.query.aggregation.AggregateOperator;
import com.azure.cosmos.models.FeedResponse;
import com.fasterxml.jackson.databind.node.ObjectNode;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.BiFunction;

public class AggregateDocumentQueryExecutionContext
    implements IDocumentQueryExecutionComponent<Document>{

    private final static
    ImplementationBridgeHelpers.CosmosDiagnosticsHelper.CosmosDiagnosticsAccessor diagnosticsAccessor =
        ImplementationBridgeHelpers.CosmosDiagnosticsHelper.getCosmosDiagnosticsAccessor();

    private static final ImplementationBridgeHelpers.FeedResponseHelper.FeedResponseAccessor feedResponseAccessor =
        ImplementationBridgeHelpers.FeedResponseHelper.getFeedResponseAccessor();
    public static final String PAYLOAD_PROPERTY_NAME = "payload";
    private final boolean isValueAggregateQuery;
    private final IDocumentQueryExecutionComponent<Document> component;
    private final ConcurrentMap<String, QueryMetrics> queryMetricsMap = new ConcurrentHashMap<>();
    private final SingleGroupAggregator singleGroupAggregator;

    //QueryInfo class used in PipelinedDocumentQueryExecutionContext returns a Collection of AggregateOperators
    public AggregateDocumentQueryExecutionContext(IDocumentQueryExecutionComponent<Document> component,
                                                  List<AggregateOperator> aggregateOperators,
                                                  Map<String, AggregateOperator> groupByAliasToAggregateType,
                                                  List<String> orderedAliases,
                                                  boolean hasSelectValue,
                                                  String continuationToken) {

        this.component = component;
        this.isValueAggregateQuery = hasSelectValue;

        this.singleGroupAggregator = SingleGroupAggregator.create(aggregateOperators,
                                                                  groupByAliasToAggregateType,
                                                                  orderedAliases,
                                                                  hasSelectValue,
                                                                  continuationToken);
    }

    @Override
    public Flux<FeedResponse<Document>> drainAsync(int maxPageSize) {

        return this.component.drainAsync(maxPageSize)
                .collectList()
                .map( superList -> {

                    double requestCharge = 0;
                    List<Document> aggregateResults = new ArrayList<>();
                    HashMap<String, String> headers = new HashMap<>();
                    Collection<ClientSideRequestStatistics> diagnosticsList = new DistinctClientSideRequestStatisticsCollection();

                    for(FeedResponse<Document> page : superList) {
                        diagnosticsList.addAll(
                            diagnosticsAccessor.getClientSideRequestStatisticsForQueryPipelineAggregations(page.getCosmosDiagnostics()));

                        if (page.getResults().size() == 0) {
                            headers.put(HttpConstants.HttpHeaders.REQUEST_CHARGE, Double.toString(requestCharge));
                            FeedResponse<Document> frp = feedResponseAccessor.createFeedResponse(
                                aggregateResults, headers, null);
                            diagnosticsAccessor.addClientSideDiagnosticsToFeed(
                                frp.getCosmosDiagnostics(), diagnosticsList);
                            return frp;
                        }

                        requestCharge += page.getRequestCharge();

                        for (Document d : page.getResults()) {
                            RewrittenAggregateProjections rewrittenAggregateProjections =
                                new RewrittenAggregateProjections(this.isValueAggregateQuery,
                                    d); //d is always a Document
                            this.singleGroupAggregator.addValues(rewrittenAggregateProjections.getPayload());
                        }

                        QueryMetrics.mergeQueryMetricsMap(this.queryMetricsMap, BridgeInternal.queryMetricsFromFeedResponse(page));
                    }

                    Document aggregateDocument = this.singleGroupAggregator.getResult();
                    if (aggregateDocument != null) {
                        aggregateResults.add(aggregateDocument);
                    }

                    headers.put(HttpConstants.HttpHeaders.REQUEST_CHARGE, Double.toString(requestCharge));
                    FeedResponse<Document> frp = feedResponseAccessor.createFeedResponse(
                        aggregateResults, headers, null);
                    if(!queryMetricsMap.isEmpty()) {
                        for(Map.Entry<String, QueryMetrics> entry: queryMetricsMap.entrySet()) {
                            BridgeInternal.putQueryMetricsIntoMap(frp, entry.getKey(), entry.getValue());
                        }
                    }
                    diagnosticsAccessor.addClientSideDiagnosticsToFeed(
                        frp.getCosmosDiagnostics(), diagnosticsList);
                    return frp;
                }).flux();
    }

    public static Flux<IDocumentQueryExecutionComponent<Document>> createAsync(
        BiFunction<String, PipelinedDocumentQueryParams<Document>, Flux<IDocumentQueryExecutionComponent<Document>>> createSourceComponentFunction,
        Collection<AggregateOperator> aggregates,
        Map<String, AggregateOperator> groupByAliasToAggregateType,
        List<String> groupByAliases,
        boolean hasSelectValue,
        String continuationToken,
        PipelinedDocumentQueryParams<Document> documentQueryParams) {

        return createSourceComponentFunction
                   .apply(continuationToken, documentQueryParams)
                   .map(component -> new AggregateDocumentQueryExecutionContext(component,
                                                                        new ArrayList<>(aggregates),
                                                                        groupByAliasToAggregateType,
                                                                        groupByAliases,
                                                                        hasSelectValue,
                                                                        continuationToken));
    }

    public IDocumentQueryExecutionComponent<Document> getComponent() {
        return this.component;
    }

    static final class RewrittenAggregateProjections {
        private Document payload;

        public RewrittenAggregateProjections(boolean isValueAggregateQuery, Document document) {
            if (document == null) {
                throw new IllegalArgumentException("document cannot be null");
            }

            if (isValueAggregateQuery) {
                this.payload = new Document(document.getPropertyBag());
            } else {
                if (!document.has(PAYLOAD_PROPERTY_NAME)) {
                    throw new IllegalStateException("Underlying object does not have an 'payload' field.");
                }

                if (document.get(PAYLOAD_PROPERTY_NAME) instanceof ObjectNode) {
                    this.payload = new Document((ObjectNode) document.get(PAYLOAD_PROPERTY_NAME));
                }
            }
        }

        public Document getPayload() {
            return payload;
        }
    }

}
