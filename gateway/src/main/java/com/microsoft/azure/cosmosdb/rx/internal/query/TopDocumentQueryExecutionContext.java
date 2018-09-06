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

import com.microsoft.azure.cosmosdb.BridgeInternal;
import com.microsoft.azure.cosmosdb.FeedResponse;
import com.microsoft.azure.cosmosdb.Resource;

import rx.Observable;
import rx.functions.Func1;

public class TopDocumentQueryExecutionContext <T extends Resource> implements IDocumentQueryExecutionComponent<T>{

    private final IDocumentQueryExecutionComponent<T> component;
    private final int top;

    public TopDocumentQueryExecutionContext(IDocumentQueryExecutionComponent<T> component, int top) {
        this.component = component;
        this.top = top;
    }

    public static <T extends Resource>  Observable<IDocumentQueryExecutionComponent<T>> createAsync(
            Observable<IDocumentQueryExecutionComponent<T>> observableComponent, int top) {

        return observableComponent.map( component -> {
           return new TopDocumentQueryExecutionContext<T>(component, top);
        });
    }

    @Override
    public Observable<FeedResponse<T>> drainAsync(int maxPageSize) {
        ParallelDocumentQueryExecutionContextBase<T> context;
        
        if (this.component instanceof AggregateDocumentQueryExecutionContext<?>) {
            context = (ParallelDocumentQueryExecutionContextBase<T>) 
                      ((AggregateDocumentQueryExecutionContext<T>) this.component).getComponent();
        } else {
            context = (ParallelDocumentQueryExecutionContextBase<T>) this.component;
        }
        
        context.setTop(this.top);

        return this.component.drainAsync(maxPageSize)
                .takeUntil(new Func1<FeedResponse<T>, Boolean>() {

                    private volatile int fetchedItems = 0;

                    @Override
                    public Boolean call(FeedResponse<T> frp) {

                        fetchedItems += frp.getResults().size();

                        // take until we have at least top many elements fetched
                        return fetchedItems >= top;
                    }
                })
                .map(new Func1<FeedResponse<T>, FeedResponse<T>>(){

                    private volatile int collectedItems = 0;
                    private volatile boolean lastPage = false;

                    @Override
                    public FeedResponse<T> call(FeedResponse<T> t) {

                        if (collectedItems + t.getResults().size() <= top) {
                            collectedItems += t.getResults().size();
                            return t;
                        } else {
                            assert lastPage == false;
                            lastPage = true;
                            int lastPageSize = top - collectedItems;
                            collectedItems += lastPageSize;
                            return BridgeInternal.createFeedResponse(t.getResults().subList(0,
                                    lastPageSize), t.getResponseHeaders());
                        }
                    }
                });
    }
}
