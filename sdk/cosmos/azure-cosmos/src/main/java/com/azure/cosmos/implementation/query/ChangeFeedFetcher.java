// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.query;

import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.implementation.DocumentClientRetryPolicy;
import com.azure.cosmos.implementation.GoneException;
import com.azure.cosmos.implementation.InvalidPartitionExceptionRetryPolicy;
import com.azure.cosmos.implementation.MetadataDiagnosticsContext;
import com.azure.cosmos.implementation.ObservableHelper;
import com.azure.cosmos.implementation.PartitionKeyRangeGoneRetryPolicy;
import com.azure.cosmos.implementation.PathsHelper;
import com.azure.cosmos.implementation.Resource;
import com.azure.cosmos.implementation.ResourceType;
import com.azure.cosmos.implementation.RetryContext;
import com.azure.cosmos.implementation.RxDocumentClientImpl;
import com.azure.cosmos.implementation.RxDocumentServiceRequest;
import com.azure.cosmos.implementation.ShouldRetryResult;
import com.azure.cosmos.implementation.changefeed.implementation.ChangeFeedState;
import com.azure.cosmos.implementation.feedranges.FeedRangeContinuation;
import com.azure.cosmos.implementation.feedranges.FeedRangeInternal;
import com.azure.cosmos.implementation.routing.Range;
import com.azure.cosmos.models.FeedResponse;
import com.azure.cosmos.models.ModelBridgeInternal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

class ChangeFeedFetcher<T extends Resource> extends Fetcher<T> {
    private final ChangeFeedState changeFeedState;
    private final Supplier<RxDocumentServiceRequest> createRequestFunc;
    private final DocumentClientRetryPolicy feedRangeContinuationSplitRetryPolicy;

    public ChangeFeedFetcher(
        RxDocumentClientImpl client,
        Supplier<RxDocumentServiceRequest> createRequestFunc,
        Function<RxDocumentServiceRequest, Mono<FeedResponse<T>>> executeFunc,
        ChangeFeedState changeFeedState,
        Map<String, Object> requestOptionProperties,
        int top,
        int maxItemCount,
        boolean isSplitHandlingDisabled) {

        super(executeFunc, true, top, maxItemCount);

        checkNotNull(client, "Argument 'client' must not be null.");
        checkNotNull(createRequestFunc, "Argument 'createRequestFunc' must not be null.");
        checkNotNull(changeFeedState, "Argument 'changeFeedState' must not be null.");
        this.changeFeedState = changeFeedState;

        if (isSplitHandlingDisabled) {
            // True for ChangeFeedProcessor - where all retry-logic is handled
            this.feedRangeContinuationSplitRetryPolicy = null;
            this.createRequestFunc = createRequestFunc;
        } else {
            DocumentClientRetryPolicy retryPolicyInstance = client.getResetSessionTokenRetryPolicy().getRequestPolicy();
            String collectionLink = PathsHelper.generatePath(
                ResourceType.DocumentCollection, changeFeedState.getContainerRid(), false);
            retryPolicyInstance = new InvalidPartitionExceptionRetryPolicy(
                client.getCollectionCache(),
                retryPolicyInstance,
                collectionLink,
                requestOptionProperties);

            retryPolicyInstance = new PartitionKeyRangeGoneRetryPolicy(client,
                client.getCollectionCache(),
                client.getPartitionKeyRangeCache(),
                collectionLink,
                retryPolicyInstance,
                requestOptionProperties);

            this.feedRangeContinuationSplitRetryPolicy = new FeedRangeContinuationSplitRetryPolicy(
                client,
                this.changeFeedState,
                retryPolicyInstance,
                requestOptionProperties,
                retryPolicyInstance.getRetryContext());
            this.createRequestFunc = () -> {
                RxDocumentServiceRequest request = createRequestFunc.get();
                this.feedRangeContinuationSplitRetryPolicy.onBeforeSendRequest(request);
                return request;
            };
        }
    }

    @Override
    public Mono<FeedResponse<T>> nextPage() {

        if (this.feedRangeContinuationSplitRetryPolicy == null) {
            return this.nextPageInternal();
        }


        // There are two conditions that require retries
        // in the change feed pipeline
        // 1) On 410 the FeedRangeContinuation needs to evaluate
        //    whether continuations need to be split (in the case that any continuation
        //    exists that would span more than one physical partition now
        // 2) On 304 a retry is needed if at least one continuation has not been drained yet.
        //    This prevents returning a 304 before we received a 304 for all continuations
        //
        // 410 handling: this is triggered by an exception - using an
        //               IRetryPolicy (FeedRangeContinuationSplitRetryPolicy)
        // 304 handling: this is not triggered by an exception (304 doesn't result in throwing)
        //               so using Reactor's built-in option of repeating the chain on an empty result
        //               so nextPageInternal below has the logic to return empty result
        //               if not all continuations have been drained yet.
        return ObservableHelper.inlineIfPossible(
            this::nextPageInternal,
            this.feedRangeContinuationSplitRetryPolicy);
    }

    private Mono<FeedResponse<T>> nextPageInternal() {
        return Mono.fromSupplier(this::nextPageCore)
                   .flatMap(Function.identity())
                   .flatMap((r) -> {
                       FeedRangeContinuation continuationSnapshot =
                           this.changeFeedState.getContinuation();

                       if (continuationSnapshot != null &&
                           continuationSnapshot.handleChangeFeedNotModified(r) == ShouldRetryResult.RETRY_NOW) {

                           // not all continuations have been drained yet
                           // repeat with the next continuation
                           this.reenableShouldFetchMoreForRetry();
                           return Mono.empty();
                       }

                       return Mono.just(r);
                   })
                   .repeatWhenEmpty(o -> o);
    }

    @Override
    protected String applyServerResponseContinuation(
        String serverContinuationToken,
        RxDocumentServiceRequest request) {

        return this.changeFeedState.applyServerResponseContinuation(
            serverContinuationToken, request);
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

    private static final class FeedRangeContinuationSplitRetryPolicy extends DocumentClientRetryPolicy {
        private final static Logger LOGGER = LoggerFactory.getLogger(FeedRangeContinuationSplitRetryPolicy.class);

        private final ChangeFeedState state;
        private final RxDocumentClientImpl client;
        private final DocumentClientRetryPolicy nextRetryPolicy;
        private final Map<String, Object> requestOptionProperties;
        private MetadataDiagnosticsContext diagnosticsContext;
        private final RetryContext retryContext;

        public FeedRangeContinuationSplitRetryPolicy(
            RxDocumentClientImpl client,
            ChangeFeedState state,
            DocumentClientRetryPolicy nextRetryPolicy,
            Map<String, Object> requestOptionProperties,
            RetryContext retryContext) {

            this.client = client;
            this.state = state;
            this.nextRetryPolicy = nextRetryPolicy;
            this.requestOptionProperties = requestOptionProperties;
            this.diagnosticsContext = null;
            this.retryContext = retryContext;
        }

        @Override
        public void onBeforeSendRequest(RxDocumentServiceRequest request) {
            this.diagnosticsContext =
                BridgeInternal.getMetaDataDiagnosticContext(request.requestContext.cosmosDiagnostics);
            this.nextRetryPolicy.onBeforeSendRequest(request);
        }

        @Override
        public Mono<ShouldRetryResult> shouldRetry(Exception e) {
            return this.nextRetryPolicy.shouldRetry(e).flatMap(shouldRetryResult -> {
                if (!shouldRetryResult.shouldRetry) {
                    if (!(e instanceof GoneException)) {
                        LOGGER.warn("Exception not applicable - will fail the request.", e);
                        return Mono.just(ShouldRetryResult.noRetry());
                    }

                    if (this.state.getContinuation() == null) {
                        final FeedRangeInternal feedRange = this.state.getFeedRange();
                        final Mono<Range<String>> effectiveRangeMono = feedRange.getNormalizedEffectiveRange(
                            this.client.getPartitionKeyRangeCache(),
                            this.diagnosticsContext,
                            this.client.getCollectionCache().resolveByRidAsync(
                                this.diagnosticsContext,
                                this.state.getContainerRid(),
                                this.requestOptionProperties)
                        );

                        return effectiveRangeMono
                            .map(effectiveRange -> {
                                return this.state.setContinuation(
                                    FeedRangeContinuation.create(
                                        this.state.getContainerRid(),
                                        this.state.getFeedRange(),
                                        effectiveRange));
                            })
                            .flatMap(state -> state.getContinuation().handleSplit(client, (GoneException)e));
                    }

                    return this
                        .state
                        .getContinuation()
                        .handleSplit(client, (GoneException)e)
                        .flatMap(splitShouldRetryResult -> {
                            if (!splitShouldRetryResult.shouldRetry) {
                                LOGGER.warn("No partition split error - will fail the request.", e);
                            } else {
                                LOGGER.debug("HandleSplit will retry.", e);
                            }

                            return Mono.just(shouldRetryResult);
                        });
                }

                LOGGER.trace("Retrying due to inner retry policy");
                return Mono.just(shouldRetryResult);
            });
        }

        @Override
        public RetryContext getRetryContext() {
            return this.retryContext;
        }
    }
}
