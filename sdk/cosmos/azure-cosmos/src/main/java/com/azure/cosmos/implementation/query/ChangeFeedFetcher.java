// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.query;

import com.azure.cosmos.implementation.Resource;
import com.azure.cosmos.implementation.RxDocumentServiceRequest;
import com.azure.cosmos.implementation.ShouldRetryResult;
import com.azure.cosmos.implementation.changefeed.implementation.ChangeFeedState;
import com.azure.cosmos.implementation.feedranges.FeedRangeContinuation;
import com.azure.cosmos.models.FeedResponse;
import com.azure.cosmos.models.ModelBridgeInternal;
import reactor.core.publisher.Mono;

import java.util.function.Function;
import java.util.function.Supplier;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

class ChangeFeedFetcher<T extends Resource> extends Fetcher<T> {
    private final Supplier<RxDocumentServiceRequest> createRequestFunc;
    private final ChangeFeedState changeFeedState;

    public ChangeFeedFetcher(
        Supplier<RxDocumentServiceRequest> createRequestFunc,
        Function<RxDocumentServiceRequest, Mono<FeedResponse<T>>> executeFunc,
        ChangeFeedState changeFeedState,
        int top,
        int maxItemCount) {

        super(executeFunc, true, top, maxItemCount);

        checkNotNull(createRequestFunc, "Argument 'createRequestFunc' must not be null.");
        checkNotNull(changeFeedState, "Argument 'changeFeedState' must not be null.");
        this.createRequestFunc = createRequestFunc;
        this.changeFeedState = changeFeedState;
    }

    @Override
    protected String applyServerResponseContinuation(String serverContinuationToken) {
        return this.changeFeedState.applyServerResponseContinuation(serverContinuationToken);
    }

    @Override
    protected boolean isFullyDrained(boolean isChangefeed, FeedResponse<T> response) {
        if (ModelBridgeInternal.noChanges(response)) {
            return true;
        }

        FeedRangeContinuation continuation = this.changeFeedState.getContinuation();
        return continuation != null && continuation.isDone();
    }

    @Override
    protected String getContinuationForLogging() {
        return this.changeFeedState.toJson();
    }

    @Override
    protected RxDocumentServiceRequest createRequest(int maxItemCount) {
        RxDocumentServiceRequest request = this.createRequestFunc.get();
        this.changeFeedState.populateRequest(request, maxItemCount);
        return request;
    }

    @Override
    public Mono<FeedResponse<T>> nextPage() {
        return Mono.fromSupplier(this::nextPageCore)
            .flatMap(Function.identity())
            .flatMap((r) -> {
               FeedRangeContinuation continuationSnapshot = this.changeFeedState.getContinuation();

               if (continuationSnapshot != null &&
                   continuationSnapshot.handleChangeFeedNotModified(r) == ShouldRetryResult.RETRY_IMMEDIATELY)
               {
                   return Mono.empty();
               }

               return Mono.just(r);
            })
            .repeatWhenEmpty(o -> o);
    }
}
