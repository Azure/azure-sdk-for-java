// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.query;

import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.implementation.ClientSideRequestStatistics;
import com.azure.cosmos.implementation.Document;
import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.QueryMetrics;
import com.azure.cosmos.implementation.Resource;
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

public class AggregateDocumentQueryExecutionContext<T extends Resource> implements IDocumentQueryExecutionComponent<T>{

    public static final String PAYLOAD_PROPERTY_NAME = "payload";
    private final boolean isValueAggregateQuery;
    private IDocumentQueryExecutionComponent<T> component;
    private ConcurrentMap<String, QueryMetrics> queryMetricsMap = new ConcurrentHashMap<>();
    private SingleGroupAggregator singleGroupAggregator;

    //QueryInfo class used in PipelinedDocumentQueryExecutionContext returns a Collection of AggregateOperators
    public AggregateDocumentQueryExecutionContext(IDocumentQueryExecutionComponent<T> component,
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

    @SuppressWarnings("unchecked")
    @Override
    public Flux<FeedResponse<T>> drainAsync(int maxPageSize) {

        return this.component.drainAsync(maxPageSize)
                .collectList()
                .map( superList -> {

                    double requestCharge = 0;
                    List<Document> aggregateResults = new ArrayList<>();
                    HashMap<String, String> headers = new HashMap<>();
                    List<ClientSideRequestStatistics> diagnosticsList = new ArrayList<>();

                    for(FeedResponse<T> page : superList) {
                        diagnosticsList.addAll(BridgeInternal
                                                   .getClientSideRequestStatisticsList(page.getCosmosDiagnostics()));

                        if (page.getResults().size() == 0) {
                            headers.put(HttpConstants.HttpHeaders.REQUEST_CHARGE, Double.toString(requestCharge));
                            FeedResponse<Document> frp = BridgeInternal.createFeedResponse(aggregateResults, headers);
                            BridgeInternal.addClientSideDiagnosticsToFeed(frp.getCosmosDiagnostics(), diagnosticsList);
                            return (FeedResponse<T>) frp;
                        }

                        requestCharge += page.getRequestCharge();

                        for (T d : page.getResults()) {
                            RewrittenAggregateProjections rewrittenAggregateProjections =
                                new RewrittenAggregateProjections(this.isValueAggregateQuery,
                                                                  (Document)d); //d is always a Document
                            this.singleGroupAggregator.addValues(rewrittenAggregateProjections.getPayload());
                        }

                        for(String key : BridgeInternal.queryMetricsFromFeedResponse(page).keySet()) {
                            if (queryMetricsMap.containsKey(key)) {
                                QueryMetrics qm = BridgeInternal.queryMetricsFromFeedResponse(page).get(key);
                                queryMetricsMap.get(key).add(qm);
                            } else {
                                queryMetricsMap.put(key, BridgeInternal.queryMetricsFromFeedResponse(page).get(key));
                            }
                        }
                    }

                    Document aggregateDocument = this.singleGroupAggregator.getResult();
                    if (aggregateDocument != null) {
                        aggregateResults.add(aggregateDocument);
                    }

                    headers.put(HttpConstants.HttpHeaders.REQUEST_CHARGE, Double.toString(requestCharge));
                    FeedResponse<Document> frp = BridgeInternal.createFeedResponse(aggregateResults, headers);
                    if(!queryMetricsMap.isEmpty()) {
                        for(Map.Entry<String, QueryMetrics> entry: queryMetricsMap.entrySet()) {
                            BridgeInternal.putQueryMetricsIntoMap(frp, entry.getKey(), entry.getValue());
                        }
                    }
                    BridgeInternal.addClientSideDiagnosticsToFeed(frp.getCosmosDiagnostics(), diagnosticsList);
                    return (FeedResponse<T>) frp;
                }).flux();
    }

    public static <T extends Resource> Flux<IDocumentQueryExecutionComponent<T>> createAsync(
        BiFunction<String, PipelinedDocumentQueryParams<T>, Flux<IDocumentQueryExecutionComponent<T>>> createSourceComponentFunction,
        Collection<AggregateOperator> aggregates,
        Map<String, AggregateOperator> groupByAliasToAggregateType,
        List<String> groupByAliases,
        boolean hasSelectValue,
        String continuationToken,
        PipelinedDocumentQueryParams<T> documentQueryParams) {

        return createSourceComponentFunction
                   .apply(continuationToken, documentQueryParams)
                   .map(component -> new AggregateDocumentQueryExecutionContext<T>(component,
                                                                        new ArrayList<>(aggregates),
                                                                        groupByAliasToAggregateType,
                                                                        groupByAliases,
                                                                        hasSelectValue,
                                                                        continuationToken));
    }

    public IDocumentQueryExecutionComponent<T> getComponent() {
        return this.component;
    }

    class RewrittenAggregateProjections {
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
