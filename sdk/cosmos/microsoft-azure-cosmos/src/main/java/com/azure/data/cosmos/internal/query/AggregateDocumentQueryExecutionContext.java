// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.cosmos.internal.query;

import com.azure.data.cosmos.BridgeInternal;
import com.azure.data.cosmos.internal.Document;
import com.azure.data.cosmos.FeedResponse;
import com.azure.data.cosmos.Resource;
import com.azure.data.cosmos.internal.Undefined;
import com.azure.data.cosmos.internal.Constants;
import com.azure.data.cosmos.internal.HttpConstants;
import com.azure.data.cosmos.internal.QueryMetrics;
import com.azure.data.cosmos.internal.query.aggregation.AggregateOperator;
import com.azure.data.cosmos.internal.query.aggregation.Aggregator;
import com.azure.data.cosmos.internal.query.aggregation.AverageAggregator;
import com.azure.data.cosmos.internal.query.aggregation.CountAggregator;
import com.azure.data.cosmos.internal.query.aggregation.MaxAggregator;
import com.azure.data.cosmos.internal.query.aggregation.MinAggregator;
import com.azure.data.cosmos.internal.query.aggregation.SumAggregator;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
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
                        
                        if (page.results().size() == 0) {
                            headers.put(HttpConstants.HttpHeaders.REQUEST_CHARGE, Double.toString(requestCharge));
                            FeedResponse<Document> frp = BridgeInternal.createFeedResponse(aggregateResults, headers);
                            return (FeedResponse<T>) frp;
                        }
                        
                        Document doc = ((Document)page.results().get(0));
                        requestCharge += page.requestCharge();
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
                    
                    if (this.aggregator.getResult() == null || !this.aggregator.getResult().equals(Undefined.Value())) {
                        Document aggregateDocument = new Document();
                        BridgeInternal.setProperty(aggregateDocument, Constants.Properties.AGGREGATE, this.aggregator.getResult());
                        aggregateResults.add(aggregateDocument);
                    }

                    headers.put(HttpConstants.HttpHeaders.REQUEST_CHARGE, Double.toString(requestCharge));
                    FeedResponse<Document> frp = BridgeInternal.createFeedResponse(aggregateResults, headers);
                    if(!queryMetricsMap.isEmpty()) {
                        for(String key: queryMetricsMap.keySet()) {
                            BridgeInternal.putQueryMetricsIntoMap(frp, key, queryMetricsMap.get(key));
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