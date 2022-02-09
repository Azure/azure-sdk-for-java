// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.query;

import com.azure.cosmos.models.FeedResponse;
import com.azure.cosmos.implementation.Resource;
import com.azure.cosmos.implementation.RxDocumentServiceRequest;
import com.azure.cosmos.models.ModelBridgeInternal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.util.function.Function;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

abstract class Fetcher<T extends Resource> {
    private final static Logger logger = LoggerFactory.getLogger(Fetcher.class);

    private final Function<RxDocumentServiceRequest, Mono<FeedResponse<T>>> executeFunc;
    private final boolean isChangeFeed;

    private volatile boolean shouldFetchMore;
    private volatile int maxItemCount;
    private volatile int top;

    public Fetcher(
        Function<RxDocumentServiceRequest, Mono<FeedResponse<T>>> executeFunc,
        boolean isChangeFeed,
        int top,
        int maxItemCount) {

        checkNotNull(executeFunc, "Argument 'executeFunc' must not be null.");

        this.executeFunc = executeFunc;
        this.isChangeFeed = isChangeFeed;

        this.top = top;
        if (top == -1) {
            this.maxItemCount = maxItemCount;
        } else {
            // it is a top query, we should not retrieve more than requested top.
            this.maxItemCount = Math.min(maxItemCount, top);
        }
        this.shouldFetchMore = true;
    }

    public final boolean shouldFetchMore() {
        return shouldFetchMore;
    }

    public Mono<FeedResponse<T>> nextPage() {
        return this.nextPageCore();
    }

    protected final Mono<FeedResponse<T>> nextPageCore() {
        RxDocumentServiceRequest request = createRequest();
        return nextPage(request);
    }

    protected abstract String applyServerResponseContinuation(
        String serverContinuationToken,
        RxDocumentServiceRequest request);

    protected abstract boolean isFullyDrained(boolean isChangeFeed, FeedResponse<T> response);

    protected abstract String getContinuationForLogging();

    private void updateState(FeedResponse<T> response, RxDocumentServiceRequest request) {
        String transformedContinuation =
            this.applyServerResponseContinuation(response.getContinuationToken(), request);

        ModelBridgeInternal.setFeedResponseContinuationToken(transformedContinuation, response);
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
            // if top == 0 then done
            (top != 0) &&
            // if fullyDrained then done
            !this.isFullyDrained(this.isChangeFeed, response);

        if (logger.isDebugEnabled()) {
            logger.debug("Fetcher state updated: " +
                    "isChangeFeed = {}, continuation token = {}, max item count = {}, should fetch more = {}",
                isChangeFeed, this.getContinuationForLogging(), maxItemCount, shouldFetchMore);
        }
    }

    protected void reenableShouldFetchMoreForRetry() {
        this.shouldFetchMore = true;
    }

    private RxDocumentServiceRequest createRequest() {
        if (!shouldFetchMore) {
            // this should never happen
            logger.error("invalid state, trying to fetch more after completion");
            throw new IllegalStateException("INVALID state, trying to fetch more after completion");
        }

        return this.createRequest(maxItemCount);
    }

    protected abstract RxDocumentServiceRequest createRequest(int maxItemCount);

    private Mono<FeedResponse<T>> nextPage(RxDocumentServiceRequest request) {
        return executeFunc.apply(request).map(rsp -> {
            updateState(rsp, request);
            return rsp;
        });
    }
}
