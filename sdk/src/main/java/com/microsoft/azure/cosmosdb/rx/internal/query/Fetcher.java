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

import com.microsoft.azure.cosmosdb.FeedOptionsBase;
import com.microsoft.azure.cosmosdb.FeedResponse;
import com.microsoft.azure.cosmosdb.Resource;
import com.microsoft.azure.cosmosdb.BridgeInternal;
import com.microsoft.azure.cosmosdb.rx.internal.RxDocumentServiceRequest;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.functions.Func1;
import rx.functions.Func2;
import rx.Observable;

class Fetcher<T extends Resource> {
    private final static Logger logger = LoggerFactory.getLogger(Fetcher.class);

    private final Func2<String, Integer, RxDocumentServiceRequest> createRequestFunc;
    private final Func1<RxDocumentServiceRequest, Observable<FeedResponse<T>>> executeFunc;
    private final boolean isChangeFeed;

    private volatile boolean shouldFetchMore;
    private volatile int maxItemCount;
    private volatile int top;
    private volatile String continuationToken;

    public Fetcher(Func2<String, Integer, RxDocumentServiceRequest> createRequestFunc,
                   Func1<RxDocumentServiceRequest, Observable<FeedResponse<T>>> executeFunc,
                   FeedOptionsBase options,
                   boolean isChangeFeed,
                   int top,
                   int maxItemCount) {

        this.createRequestFunc = createRequestFunc;
        this.executeFunc = executeFunc;
        this.isChangeFeed = isChangeFeed;

        this.continuationToken = options.getRequestContinuation();
        this.top = top;
        if (top == -1) {
            this.maxItemCount = maxItemCount;
        } else {
            // it is a top query, we should not retrieve more than requested top.
            this.maxItemCount = Math.min(maxItemCount, top);
        }
        this.shouldFetchMore = true;
    }

    public boolean shouldFetchMore() {
        return shouldFetchMore;
    }

    public Observable<FeedResponse<T>> nextPage() {
        RxDocumentServiceRequest request = createRequest();
        return nextPage(request);
    }

    private void updateState(FeedResponse<T> response) {
        continuationToken = response.getResponseContinuation();
        if (top != -1) {
            top -= response.getResults().size();
            if (top < 0) {
                // this shouldn't happen
                // this means backend retrieved more items than requested
                logger.warn("Azure Cosmos DB BackEnd Service returned more than requested {} items", maxItemCount);
                top = 0;
            }
            maxItemCount = Math.min(maxItemCount, top);
        }

        shouldFetchMore = shouldFetchMore &&
                // if token is null or top == 0 then done
                (!StringUtils.isEmpty(continuationToken) && (top != 0)) &&
                // if change feed query and no changes then done
                (!isChangeFeed || !BridgeInternal.noChanges(response));

        logger.debug("Fetcher state updated: " +
                        "isChangeFeed = {}, continuation token = {}, max item count = {}, should fetch more = {}",
                        isChangeFeed, continuationToken, maxItemCount, shouldFetchMore);
    }

    private RxDocumentServiceRequest createRequest() {
        if (!shouldFetchMore) {
            // this should never happen
            logger.error("invalid state, trying to fetch more after completion");
            throw new IllegalStateException("Invalid state, trying to fetch more after completion");
        }

        return createRequestFunc.call(continuationToken, maxItemCount);
    }

    private Observable<FeedResponse<T>> nextPage(RxDocumentServiceRequest request) {
        return executeFunc.call(request).map(rsp -> {
            updateState(rsp);
            return rsp;
        });
    }
}
