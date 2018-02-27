/**
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

    private IDocumentQueryExecutionComponent<T> component;
    private int top;

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
                .takeWhile(new Func1<FeedResponse<T>, Boolean>() {

                    int innerTop = top;
                    boolean isLastPage = false;
                    
                    //if top number of documents are contained in n pages,
                    //return false on n + 1 th page
                    @Override
                    public Boolean call(FeedResponse<T> frp) {
                        
                        innerTop -= frp.getResults().size();
                        
                        if (innerTop <= 0) {
                            isLastPage = !isLastPage;
                        }
                        
                        return innerTop > 0  || isLastPage;
                    }
                })
                .map(new Func1<FeedResponse<T>, FeedResponse<T>>(){

                    int innerTop = top;
                    @Override
                    public FeedResponse<T> call(FeedResponse<T> t) {
                        int size = Math.min(innerTop, t.getResults().size());
                        innerTop -= t.getResults().size();
                        
                        if (innerTop > 0) {
                            return t;
                        } else {
                            return BridgeInternal.createFeedResponse(t.getResults().subList(0, size), t.getResponseHeaders());
                        }
                    }
                });
    }

}
