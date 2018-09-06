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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import com.microsoft.azure.cosmosdb.BridgeInternal;
import com.microsoft.azure.cosmosdb.Document;
import com.microsoft.azure.cosmosdb.FeedResponse;
import com.microsoft.azure.cosmosdb.Resource;
import com.microsoft.azure.cosmosdb.Undefined;
import com.microsoft.azure.cosmosdb.internal.Constants;
import com.microsoft.azure.cosmosdb.internal.HttpConstants;
import com.microsoft.azure.cosmosdb.internal.query.QueryItem;
import com.microsoft.azure.cosmosdb.internal.query.aggregation.AggregateOperator;
import com.microsoft.azure.cosmosdb.internal.query.aggregation.Aggregator;
import com.microsoft.azure.cosmosdb.internal.query.aggregation.AverageAggregator;
import com.microsoft.azure.cosmosdb.internal.query.aggregation.CountAggregator;
import com.microsoft.azure.cosmosdb.internal.query.aggregation.MaxAggregator;
import com.microsoft.azure.cosmosdb.internal.query.aggregation.MinAggregator;
import com.microsoft.azure.cosmosdb.internal.query.aggregation.SumAggregator;

import rx.Observable;

public class AggregateDocumentQueryExecutionContext<T extends Resource> implements IDocumentQueryExecutionComponent<T>{

    private IDocumentQueryExecutionComponent<T> component;
    private Aggregator aggregator;
    
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
    public Observable<FeedResponse<T>> drainAsync(int maxPageSize) {
        
        return this.component.drainAsync(maxPageSize)
                .toList()
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
                    }
                    
                    if (this.aggregator.getResult() == null || !this.aggregator.getResult().equals(Undefined.Value())) {
                        Document aggregateDocument = new Document();
                        aggregateDocument.set(Constants.Properties.AGGREGATE, this.aggregator.getResult());
                        aggregateResults.add(aggregateDocument);
                    }

                    headers.put(HttpConstants.HttpHeaders.REQUEST_CHARGE, Double.toString(requestCharge));
                    FeedResponse<Document> frp = BridgeInternal.createFeedResponse(aggregateResults, headers);
                    return (FeedResponse<T>) frp;
                });
    }

    public static <T extends Resource>  Observable<IDocumentQueryExecutionComponent<T>> createAsync(
            Observable<IDocumentQueryExecutionComponent<T>> observableComponent, Collection<AggregateOperator> aggregates) {

        return observableComponent.map( component -> {
            return new AggregateDocumentQueryExecutionContext<T>(component, aggregates);
        });
    }

    public IDocumentQueryExecutionComponent<T> getComponent() {
        return this.component;
    }

}