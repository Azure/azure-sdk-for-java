// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.query;

import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.models.FeedResponse;
import com.azure.cosmos.implementation.Resource;
import com.azure.cosmos.implementation.RxDocumentServiceRequest;
import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.util.function.BiFunction;
import java.util.function.Function;

class Fetcher<T extends Resource> {
    private final static Logger logger = LoggerFactory.getLogger(Fetcher.class);

    private final BiFunction<String, Integer, RxDocumentServiceRequest> createRequestFunc;
    private final Function<RxDocumentServiceRequest, Mono<FeedResponse<T>>> executeFunc;
    private final boolean isChangeFeed;

    private volatile boolean shouldFetchMore;
    private volatile int maxItemCount;
    private volatile int top;
    private volatile String continuationToken;

    public Fetcher(BiFunction<String, Integer, RxDocumentServiceRequest> createRequestFunc,
                   Function<RxDocumentServiceRequest, Mono<FeedResponse<T>>> executeFunc,
                   String continuationToken,
                   boolean isChangeFeed,
                   int top,
                   int maxItemCount) {

        this.createRequestFunc = createRequestFunc;
        this.executeFunc = executeFunc;
        this.isChangeFeed = isChangeFeed;

        this.continuationToken = continuationToken;
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

    public Mono<FeedResponse<T>> nextPage() {
        RxDocumentServiceRequest request = createRequest();
        return nextPage(request);
    }

    private void updateState(FeedResponse<T> response) {
        continuationToken = response.getContinuationToken();
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
            throw new IllegalStateException("INVALID state, trying to fetch more after completion");
        }

        return createRequestFunc.apply(continuationToken, maxItemCount);
    }

    private Mono<FeedResponse<T>> nextPage(RxDocumentServiceRequest request) {
        return executeFunc.apply(request).map(rsp -> {
            updateState(rsp);
            return rsp;
        });
    }
}
