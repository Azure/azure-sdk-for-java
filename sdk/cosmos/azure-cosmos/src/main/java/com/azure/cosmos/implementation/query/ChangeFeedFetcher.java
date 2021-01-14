// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.query;

import com.azure.cosmos.implementation.GoneException;
import com.azure.cosmos.implementation.IRetryPolicy;
import com.azure.cosmos.implementation.ObservableHelper;
import com.azure.cosmos.implementation.Resource;
import com.azure.cosmos.implementation.RetryPolicyWithDiagnostics;
import com.azure.cosmos.implementation.RxDocumentClientImpl;
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
    private final ChangeFeedState changeFeedState;
    private final Supplier<RxDocumentServiceRequest> createRequestFunc;
    private final IRetryPolicy feedRangeContinuationSplitRetryPolicy;

    public ChangeFeedFetcher(
        RxDocumentClientImpl client,
        Supplier<RxDocumentServiceRequest> createRequestFunc,
        Function<RxDocumentServiceRequest, Mono<FeedResponse<T>>> executeFunc,
        ChangeFeedState changeFeedState,
        int top,
        int maxItemCount) {

        super(executeFunc, true, top, maxItemCount);

        checkNotNull(client, "Argument 'client' must not be null.");
        checkNotNull(createRequestFunc, "Argument 'createRequestFunc' must not be null.");
        checkNotNull(changeFeedState, "Argument 'changeFeedState' must not be null.");
        this.createRequestFunc = createRequestFunc;
        this.changeFeedState = changeFeedState;
        this.feedRangeContinuationSplitRetryPolicy = new FeedRangeContinuationSplitRetryPolicy(
            client,
            this.changeFeedState);
    }

    @Override
    public Mono<FeedResponse<T>> nextPage() {
        return ObservableHelper.inlineIfPossible(
            this::nextPageInternal,
            this.feedRangeContinuationSplitRetryPolicy);
    }

    // TODO fabianm - the retry policy handling here via
    // FeedRangeContinuationSplitRetryPolicy + the
    // retry on 304 via the repeatWhenEmpty
    // is not well structured. Hard to read and maintain
    // especially don't like the side effect of triggering the
    // split of the feed range continuation and resetting the
    // should fetch more flag on 304 retries from the retry policies
    // Planning to proceed with merging the PR for now
    // and then refactor the retry logic here in a separate PR
    private Mono<FeedResponse<T>> nextPageInternal() {
        return Mono.fromSupplier(this::nextPageCore)
                   .flatMap(Function.identity())
                   .flatMap((r) -> {
                       FeedRangeContinuation continuationSnapshot =
                           this.changeFeedState.getContinuation();

                       if (continuationSnapshot != null &&
                           continuationSnapshot.handleChangeFeedNotModified(r) == ShouldRetryResult.RETRY_IMMEDIATELY) {

                           this.reenableShouldFetchMoreForRetry();
                           return Mono.empty();
                       }

                       return Mono.just(r);
                   })
                   .repeatWhenEmpty(o -> o);
    }

    @Override
    protected String applyServerResponseContinuation(String serverContinuationToken) {
        return this.changeFeedState.applyServerResponseContinuation(serverContinuationToken);
    }

    @Override
    protected boolean isFullyDrained(boolean isChangeFeed, FeedResponse<T> response) {
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

    private static final class FeedRangeContinuationSplitRetryPolicy extends RetryPolicyWithDiagnostics {
        private final ChangeFeedState state;
        private final RxDocumentClientImpl client;

        public FeedRangeContinuationSplitRetryPolicy(
            RxDocumentClientImpl client,
            ChangeFeedState state) {

            this.client = client;
            this.state = state;
        }


        @Override
        public Mono<ShouldRetryResult> shouldRetry(Exception e) {
            if (!(e instanceof GoneException)) {
                return Mono.just(ShouldRetryResult.noRetry());
            }

            if (this.state.getContinuation() == null)
            {
                this.state.setContinuation(
                    FeedRangeContinuation.createForFullFeedRange(
                        this.state.getContainerRid(),
                        this.state.getFeedRange())
                );
            }

            return this.state.getContinuation().handleSplit(client, (GoneException)e);
        }
    }
}
