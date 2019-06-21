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

package com.azure.data.cosmos.internal.query;

import com.azure.data.cosmos.BridgeInternal;
import com.azure.data.cosmos.CosmosClientException;
import com.azure.data.cosmos.FeedResponse;
import com.azure.data.cosmos.Resource;
import com.azure.data.cosmos.internal.HttpConstants;
import com.azure.data.cosmos.internal.Utils.ValueHolder;
import reactor.core.publisher.Flux;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

public class TopDocumentQueryExecutionContext<T extends Resource> implements IDocumentQueryExecutionComponent<T> {

    private final IDocumentQueryExecutionComponent<T> component;
    private final int top;

    public TopDocumentQueryExecutionContext(IDocumentQueryExecutionComponent<T> component, int top) {
        this.component = component;
        this.top = top;
    }

    public static <T extends Resource> Flux<IDocumentQueryExecutionComponent<T>> createAsync(
            Function<String, Flux<IDocumentQueryExecutionComponent<T>>> createSourceComponentFunction,
            int topCount, String topContinuationToken) {
        TakeContinuationToken takeContinuationToken;

        if (topContinuationToken == null) {
            takeContinuationToken = new TakeContinuationToken(topCount, null);
        } else {
            ValueHolder<TakeContinuationToken> outTakeContinuationToken = new ValueHolder<TakeContinuationToken>();
            if (!TakeContinuationToken.tryParse(topContinuationToken, outTakeContinuationToken)) {
                String message = String.format("INVALID JSON in continuation token %s for Top~Context",
                        topContinuationToken);
                CosmosClientException dce = new CosmosClientException(HttpConstants.StatusCodes.BADREQUEST,
                        message);
                return Flux.error(dce);
            }

            takeContinuationToken = outTakeContinuationToken.v;
        }

        if (takeContinuationToken.getTakeCount() > topCount) {
            String message = String.format(
                    "top count in continuation token: %d can not be greater than the top count in the query: %d.",
                    takeContinuationToken.getTakeCount(), topCount);
            CosmosClientException dce = new CosmosClientException(HttpConstants.StatusCodes.BADREQUEST, message);
            return Flux.error(dce);
        }

        return createSourceComponentFunction
                .apply(takeContinuationToken.getSourceToken())
                .map(component -> new TopDocumentQueryExecutionContext<>(component, takeContinuationToken.getTakeCount()));
    }

    @Override
    public Flux<FeedResponse<T>> drainAsync(int maxPageSize) {
        ParallelDocumentQueryExecutionContextBase<T> context;

        if (this.component instanceof AggregateDocumentQueryExecutionContext<?>) {
            context = (ParallelDocumentQueryExecutionContextBase<T>) ((AggregateDocumentQueryExecutionContext<T>) this.component)
                    .getComponent();
        } else {
            context = (ParallelDocumentQueryExecutionContextBase<T>) this.component;
        }

        context.setTop(this.top);

        return this.component.drainAsync(maxPageSize).takeUntil(new Predicate<FeedResponse<T>>() {

            private volatile int fetchedItems = 0;

            @Override
            public boolean test(FeedResponse<T> frp) {

                fetchedItems += frp.results().size();

                // take until we have at least top many elements fetched
                return fetchedItems >= top;
            }
        }).map(new Function<FeedResponse<T>, FeedResponse<T>>() {

            private volatile int collectedItems = 0;
            private volatile boolean lastPage = false;

            @Override
            public FeedResponse<T> apply(FeedResponse<T> t) {

                if (collectedItems + t.results().size() <= top) {
                    collectedItems += t.results().size();

                    Map<String, String> headers = new HashMap<>(t.responseHeaders());
                    if (top != collectedItems) {
                        // Add Take Continuation Token
                        String sourceContinuationToken = t.continuationToken();
                        TakeContinuationToken takeContinuationToken = new TakeContinuationToken(top - collectedItems,
                                sourceContinuationToken);
                        headers.put(HttpConstants.HttpHeaders.CONTINUATION, takeContinuationToken.toJson());
                    } else {
                        // Null out the continuation token
                        headers.put(HttpConstants.HttpHeaders.CONTINUATION, null);
                    }

                    return BridgeInternal.createFeedResponseWithQueryMetrics(t.results(), headers,
                            t.queryMetrics());
                } else {
                    assert lastPage == false;
                    lastPage = true;
                    int lastPageSize = top - collectedItems;
                    collectedItems += lastPageSize;

                    // Null out the continuation token
                    Map<String, String> headers = new HashMap<>(t.responseHeaders());
                    headers.put(HttpConstants.HttpHeaders.CONTINUATION, null);

                    return BridgeInternal.createFeedResponseWithQueryMetrics(t.results().subList(0, lastPageSize),
                            headers, t.queryMetrics());
                }
            }
        });
    }
}
