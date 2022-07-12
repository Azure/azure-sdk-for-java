// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.query;

import com.azure.cosmos.implementation.spark.OperationContextAndListenerTuple;
import com.azure.cosmos.models.FeedResponse;
import com.azure.cosmos.implementation.RxDocumentServiceRequest;
import com.azure.cosmos.models.ModelBridgeInternal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.util.function.Function;
import java.util.function.Supplier;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

abstract class Fetcher<T> {
    private final static Logger logger = LoggerFactory.getLogger(Fetcher.class);

    private final Function<RxDocumentServiceRequest, Mono<FeedResponse<T>>> executeFunc;
    private final boolean isChangeFeed;
    private final OperationContextAndListenerTuple operationContext;
    private Supplier<String> operationContextTextProvider;

    private volatile boolean shouldFetchMore;
    private volatile int maxItemCount;
    private volatile int top;

    public Fetcher(
        Function<RxDocumentServiceRequest, Mono<FeedResponse<T>>> executeFunc,
        boolean isChangeFeed,
        int top,
        int maxItemCount,
        OperationContextAndListenerTuple operationContext) {

        checkNotNull(executeFunc, "Argument 'executeFunc' must not be null.");

        this.executeFunc = executeFunc;
        this.isChangeFeed = isChangeFeed;

        this.operationContext = operationContext;
        this.operationContextTextProvider = () -> {
            String operationContextText = operationContext != null && operationContext.getOperationContext() != null ?
                operationContext.getOperationContext().toString() : "n/a";
            this.operationContextTextProvider = () -> operationContextText;
            return operationContextText;
        };

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
        logger.info("Getting next page core with request headers : {}", request.getHeaders());
        return nextPage(request);
    }

    protected abstract String applyServerResponseContinuation(
        String serverContinuationToken,
        RxDocumentServiceRequest request);

    protected abstract boolean isFullyDrained(boolean isChangeFeed, FeedResponse<T> response);

    protected abstract String getContinuationForLogging();

    public String getOperationContextText() {
        return this.operationContextTextProvider.get();
    }

    private void updateState(FeedResponse<T> response, RxDocumentServiceRequest request) {
        String transformedContinuation =
            this.applyServerResponseContinuation(response.getContinuationToken(), request);

        ModelBridgeInternal.setFeedResponseContinuationToken(transformedContinuation, response);
        if (top != -1) {
            top -= response.getResults().size();
            if (top < 0) {
                // this shouldn't happen
                // this means backend retrieved more items than requested
                logger.warn("Azure Cosmos DB BackEnd Service returned more than requested {} items, Context: {}",
                    maxItemCount,
                    this.operationContextTextProvider.get());
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
                    "isChangeFeed = {}, continuation token = {}, max item count = {}, should fetch more = {}, Context: {}",
                isChangeFeed, this.getContinuationForLogging(), maxItemCount, shouldFetchMore,
                this.operationContextTextProvider.get());
        }
    }

    protected void reenableShouldFetchMoreForRetry() {
        this.shouldFetchMore = true;
    }

    private RxDocumentServiceRequest createRequest() {
        if (!shouldFetchMore) {
            // this should never happen
            logger.error(
                "invalid state, trying to fetch more after completion, Context: {}",
                this.operationContextTextProvider.get());
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
