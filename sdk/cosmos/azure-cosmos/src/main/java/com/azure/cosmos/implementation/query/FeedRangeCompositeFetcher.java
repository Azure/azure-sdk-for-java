// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.query;

import com.azure.cosmos.implementation.Resource;
import com.azure.cosmos.implementation.RxDocumentServiceRequest;
import com.azure.cosmos.implementation.feedranges.FeedRangeContinuation;
import com.azure.cosmos.models.FeedResponse;
import reactor.core.publisher.Mono;

import java.util.function.BiFunction;
import java.util.function.Function;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

class FeedRangeCompositeFetcher<T extends Resource> extends Fetcher<T> {
    private final FeedRangeContinuation feedRangeContinuation;

    public FeedRangeCompositeFetcher(
        BiFunction<String, Integer, RxDocumentServiceRequest> createRequestFunc,
        Function<RxDocumentServiceRequest, Mono<FeedResponse<T>>> executeFunc,
        FeedRangeContinuation continuation,
        boolean isChangeFeed,
        int top,
        int maxItemCount) {

        super(createRequestFunc, executeFunc, isChangeFeed, top, maxItemCount);

        checkNotNull(continuation, "Argument 'continuation' must not be null.");
        this.feedRangeContinuation = continuation;
    }

    @Override
    protected String applyServerResponseContinuation(String serverContinuationToken) {
        this.feedRangeContinuation.replaceContinuation(serverContinuationToken);
        return this.feedRangeContinuation.toJson();
    }

    @Override
    protected boolean isFullyDrained(boolean isChangefeed, FeedResponse<T> response) {
        return this.feedRangeContinuation.isDone();
    }

    @Override
    protected String getContinuationForLogging() {
        return this.feedRangeContinuation.toJson();
    }

    @Override
    protected String getRequestContinuation() {
        return this.feedRangeContinuation.getContinuation();
    }
}
