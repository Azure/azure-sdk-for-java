// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.query;

import com.azure.cosmos.implementation.query.aggregation.AggregateOperator;
import com.azure.cosmos.implementation.query.aggregation.Aggregator;
import com.azure.cosmos.implementation.query.aggregation.AverageAggregator;
import com.azure.cosmos.implementation.query.aggregation.CountAggregator;
import com.azure.cosmos.implementation.query.aggregation.MaxAggregator;
import com.azure.cosmos.implementation.query.aggregation.MinAggregator;
import com.azure.cosmos.implementation.query.aggregation.SumAggregator;
import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.implementation.Document;
import com.azure.cosmos.models.FeedResponse;
import com.azure.cosmos.implementation.Resource;
import com.azure.cosmos.implementation.Undefined;
import com.azure.cosmos.implementation.Constants;
import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.QueryMetrics;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;

public class AggregateDocumentQueryExecutionContext<T extends Resource> implements IDocumentQueryExecutionComponent<T>{

    private IDocumentQueryExecutionComponent<T> component;
    private Aggregator aggregator;
    private ConcurrentMap<String, QueryMetrics> queryMetricsMap = new ConcurrentHashMap<>();

    //QueryInfo class used in PipelinedDocumentQueryExecutionContext returns a Collection of AggregateOperators
    //while Multiple aggregates are allowed in queries targeted at a single partition, only a single aggregate is allowed in x-partition queries (currently)
    public AggregateDocumentQueryExecutionContext (IDocumentQueryExecutionComponent<T> component, Collection<AggregateOperator> aggregateOperators) {

        this.component = component;
        AggregateOperator aggregateOperator = aggregateOperators.iterator().next();

        switch (aggregateOperator) {
            case Average:
                this.aggregator = new AverageAggregator();
                break;
            case Count:
                this.aggregator = new CountAggregator();
                break;
            case Max:
                this.aggregator = new MaxAggregator();
                break;
            case Min:
                this.aggregator = new MinAggregator();
                break;
            case Sum:
                this.aggregator = new SumAggregator();
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + aggregateOperator.toString());
            }
        }

    @SuppressWarnings("unchecked")
    @Override
    public Flux<FeedResponse<T>> drainAsync(int maxPageSize) {

        return this.component.drainAsync(maxPageSize)
                .collectList()
                .map( superList -> {

                    double requestCharge = 0;
                    List<Document> aggregateResults = new ArrayList<Document>();
                    HashMap<String, String> headers = new HashMap<String, String>();

                    for(FeedResponse<T> page : superList) {

                        if (page.getResults().size() == 0) {
                            headers.put(HttpConstants.HttpHeaders.REQUEST_CHARGE, Double.toString(requestCharge));
                            FeedResponse<Document> frp = BridgeInternal.createFeedResponse(aggregateResults, headers);
                            return (FeedResponse<T>) frp;
                        }

                        Document doc = ((Document)page.getResults().get(0));
                        requestCharge += page.getRequestCharge();
                        QueryItem values = new QueryItem(doc.toJson());
                        this.aggregator.aggregate(values.getItem());
                        for(String key : BridgeInternal.queryMetricsFromFeedResponse(page).keySet()) {
                            if (queryMetricsMap.containsKey(key)) {
                                QueryMetrics qm = BridgeInternal.queryMetricsFromFeedResponse(page).get(key);
                                queryMetricsMap.get(key).add(qm);
                            } else {
                                queryMetricsMap.put(key, BridgeInternal.queryMetricsFromFeedResponse(page).get(key));
                            }
                        }
                    }

                    if (this.aggregator.getResult() == null || !this.aggregator.getResult().equals(Undefined.value())) {
                        Document aggregateDocument = new Document();
                        BridgeInternal.setProperty(aggregateDocument, Constants.Properties.VALUE, this.aggregator.getResult());
                        aggregateResults.add(aggregateDocument);
                    }

                    headers.put(HttpConstants.HttpHeaders.REQUEST_CHARGE, Double.toString(requestCharge));
                    FeedResponse<Document> frp = BridgeInternal.createFeedResponse(aggregateResults, headers);
                    if(!queryMetricsMap.isEmpty()) {
                        for(Map.Entry<String, QueryMetrics> entry: queryMetricsMap.entrySet()) {
                            BridgeInternal.putQueryMetricsIntoMap(frp, entry.getKey(), entry.getValue());
                        }
                    }
                    return (FeedResponse<T>) frp;
                }).flux();
    }

    public static <T extends Resource>  Flux<IDocumentQueryExecutionComponent<T>> createAsync(
            Function<String, Flux<IDocumentQueryExecutionComponent<T>>> createSourceComponentFunction,
            Collection<AggregateOperator> aggregates,
            String continuationToken) {

        return createSourceComponentFunction
                .apply(continuationToken)
                .map( component -> { return new AggregateDocumentQueryExecutionContext<T>(component, aggregates);});
    }

    public IDocumentQueryExecutionComponent<T> getComponent() {
        return this.component;
    }

}
