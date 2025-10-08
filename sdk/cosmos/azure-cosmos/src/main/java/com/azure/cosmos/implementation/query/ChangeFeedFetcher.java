// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.query;

import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.CosmosException;
import com.azure.cosmos.implementation.DiagnosticsClientContext;
import com.azure.cosmos.implementation.DocumentClientRetryPolicy;
import com.azure.cosmos.implementation.GlobalEndpointManager;
import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.guava25.base.Strings;
import com.azure.cosmos.implementation.perPartitionCircuitBreaker.GlobalPartitionEndpointManagerForPerPartitionCircuitBreaker;
import com.azure.cosmos.implementation.ImplementationBridgeHelpers;
import com.azure.cosmos.implementation.GoneException;
import com.azure.cosmos.implementation.MetadataDiagnosticsContext;
import com.azure.cosmos.implementation.ObservableHelper;
import com.azure.cosmos.implementation.PartitionKeyRangeGoneRetryPolicy;
import com.azure.cosmos.implementation.PathsHelper;
import com.azure.cosmos.implementation.ResourceType;
import com.azure.cosmos.implementation.RetryContext;
import com.azure.cosmos.implementation.RxDocumentClientImpl;
import com.azure.cosmos.implementation.RxDocumentServiceRequest;
import com.azure.cosmos.implementation.ShouldRetryResult;
import com.azure.cosmos.implementation.changefeed.common.ChangeFeedState;
import com.azure.cosmos.implementation.feedranges.FeedRangeContinuation;
import com.azure.cosmos.implementation.feedranges.FeedRangeInternal;
import com.azure.cosmos.implementation.routing.Range;
import com.azure.cosmos.implementation.spark.OperationContextAndListenerTuple;
import com.azure.cosmos.models.FeedResponse;
import com.azure.cosmos.models.ModelBridgeInternal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

class ChangeFeedFetcher<T> extends Fetcher<T> {
    private final static ImplementationBridgeHelpers.FeedResponseHelper.FeedResponseAccessor  feedResponseAccessor =
        ImplementationBridgeHelpers.FeedResponseHelper.getFeedResponseAccessor();
    private final ChangeFeedState changeFeedState;
    private final Supplier<RxDocumentServiceRequest> createRequestFunc;
    private final Supplier<DocumentClientRetryPolicy> feedRangeContinuationRetryPolicySupplier;
    private final boolean completeAfterAllCurrentChangesRetrieved;
    private final Long endLSN;

    public ChangeFeedFetcher(
        RxDocumentClientImpl client,
        Supplier<RxDocumentServiceRequest> createRequestFunc,
        Function<RxDocumentServiceRequest, Mono<FeedResponse<T>>> executeFunc,
        ChangeFeedState changeFeedState,
        Map<String, Object> requestOptionProperties,
        int top,
        int maxItemCount,
        boolean isSplitHandlingDisabled,
        boolean completeAfterAllCurrentChangesRetrieved,
        Long endLSN,
        OperationContextAndListenerTuple operationContext,
        GlobalEndpointManager globalEndpointManager,
        GlobalPartitionEndpointManagerForPerPartitionCircuitBreaker globalPartitionEndpointManagerForPerPartitionCircuitBreaker,
        DiagnosticsClientContext diagnosticsClientContext) {
        super(executeFunc, true, top, maxItemCount, operationContext, null, globalEndpointManager, globalPartitionEndpointManagerForPerPartitionCircuitBreaker);

        checkNotNull(client, "Argument 'client' must not be null.");
        checkNotNull(createRequestFunc, "Argument 'createRequestFunc' must not be null.");
        checkNotNull(changeFeedState, "Argument 'changeFeedState' must not be null.");
        this.changeFeedState = changeFeedState;

        // For changeFeedProcessor with pkRange version, ChangeFeedState.containerRid will be name based rather than resouceId,
        // due to the inconsistency of the ChangeFeedState.containerRid format, so in order to generate the correct path,
        // we use a RxDocumentServiceRequest here
        RxDocumentServiceRequest documentServiceRequest = createRequestFunc.get();
        String collectionLink = PathsHelper.generatePath(
            ResourceType.DocumentCollection, documentServiceRequest, false);

        this.feedRangeContinuationRetryPolicySupplier =
            () -> this.getFeedRangeContinuationRetryPolicy(
                client,
                requestOptionProperties,
                collectionLink,
                isSplitHandlingDisabled,
                diagnosticsClientContext);
        this.createRequestFunc = createRequestFunc;
        this.completeAfterAllCurrentChangesRetrieved = completeAfterAllCurrentChangesRetrieved;
        this.endLSN = endLSN;
    }

    @Override
    public Mono<FeedResponse<T>> nextPage() {
        DocumentClientRetryPolicy retryPolicy = this.feedRangeContinuationRetryPolicySupplier.get();

        if (retryPolicy == null) {
            return this.nextPageInternal(null);
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
            () -> nextPageInternal(retryPolicy),
            retryPolicy);
    }

    private Mono<FeedResponse<T>> nextPageInternal(DocumentClientRetryPolicy retryPolicy) {
        return Mono.fromSupplier(() -> nextPageCore(retryPolicy))
                   .flatMap(Function.identity())
                   .flatMap((r) -> {
                       FeedRangeContinuation continuationSnapshot =
                           this.changeFeedState.getContinuation();

                       if (this.completeAfterAllCurrentChangesRetrieved || this.endLSN != null) {
                           if (continuationSnapshot != null) {

                               //track the end-LSN for each sub-feedRange and then find the next sub-feedRange to fetch more changes
                               boolean shouldComplete = continuationSnapshot.hasFetchedAllChanges(r, endLSN);
                               if (shouldComplete) {
                                   this.disableShouldFetchMore();
                                   return Mono.just(r);
                               }

                               if (ModelBridgeInternal.<T>noChanges(r)) {
                                   // if we have reached here, it means we have got 304 for the current feedRange,
                                   // but we need to continue drain the changes from other sub-feedRange
                                   this.reEnableShouldFetchMoreForRetry();
                                   return Mono.empty();
                               }
                           }
                       } else {
                           // complete query based on 304s
                           if (continuationSnapshot != null &&
                               continuationSnapshot.handleChangeFeedNotModified(r) == ShouldRetryResult.RETRY_NOW) {

                               // not all continuations have been drained yet
                               // repeat with the next continuation
                               this.reEnableShouldFetchMoreForRetry();
                               return Mono.empty();
                           }
                       }

                       return Mono.just(r);
                   })
                   .repeatWhenEmpty(o -> o);
    }

    @Override
    protected String applyServerResponseContinuation(
        String serverContinuationToken,
        RxDocumentServiceRequest request,
        FeedResponse<T> response) {

        boolean isNoChanges = feedResponseAccessor.getNoChanges(response);
        boolean shouldMoveToNextTokenOnETagReplace = !isNoChanges && !this.completeAfterAllCurrentChangesRetrieved && this.endLSN == null;
        return this.changeFeedState.applyServerResponseContinuation(
            serverContinuationToken, request, shouldMoveToNextTokenOnETagReplace);
    }

    @Override
    protected String applyServerResponseContinuation(String serverContinuationToken, RxDocumentServiceRequest request, CosmosException cosmosException) {

        if (!Strings.isNullOrEmpty(serverContinuationToken)) {
            String replacedContinuation = this.changeFeedState.applyServerResponseContinuation(
                serverContinuationToken, request, false);

            Map<String, String> responseHeaders = cosmosException.getResponseHeaders();
            responseHeaders.put(HttpConstants.HttpHeaders.E_TAG, replacedContinuation);

            return replacedContinuation;
        }

        return com.azure.cosmos.implementation.Strings.Emtpy;
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
    protected RxDocumentServiceRequest createRequest(
        int maxItemCount,
        DocumentClientRetryPolicy documentClientRetryPolicy) {
        RxDocumentServiceRequest request = this.createRequestFunc.get();

        if (documentClientRetryPolicy != null) {
            request.requestContext.setClientRetryPolicySupplier(() -> documentClientRetryPolicy);
            documentClientRetryPolicy.onBeforeSendRequest(request);
        }

        this.changeFeedState.populateRequest(request, maxItemCount);
        return request;
    }

    private static final class FeedRangeContinuationFeedRangeGoneRetryPolicy extends DocumentClientRetryPolicy {
        private final static Logger LOGGER = LoggerFactory.getLogger(FeedRangeContinuationFeedRangeGoneRetryPolicy.class);

        private final ChangeFeedState state;
        private final RxDocumentClientImpl client;
        private final DocumentClientRetryPolicy nextRetryPolicy;
        private final Map<String, Object> requestOptionProperties;
        private MetadataDiagnosticsContext diagnosticsContext;
        private final RetryContext retryContext;
        private final Supplier<String> operationContextTextProvider;

        public FeedRangeContinuationFeedRangeGoneRetryPolicy(
            RxDocumentClientImpl client,
            ChangeFeedState state,
            DocumentClientRetryPolicy nextRetryPolicy,
            Map<String, Object> requestOptionProperties,
            RetryContext retryContext,
            Supplier<String> operationContextTextProvider) {

            checkNotNull(
                operationContextTextProvider,
                "Argument 'operationContextTextProvider' must not be null.");
            this.client = client;
            this.state = state;
            this.nextRetryPolicy = nextRetryPolicy;
            this.requestOptionProperties = requestOptionProperties;
            this.diagnosticsContext = null;
            this.retryContext = retryContext;
            this.operationContextTextProvider = operationContextTextProvider;
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
                        LOGGER.warn(
                            "Exception not applicable - will fail the request. Context: {}",
                            this.operationContextTextProvider.get(),
                            e);
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
                            .map(effectiveRange -> this.state.setContinuation(
                                FeedRangeContinuation.create(
                                    this.state.getContainerRid(),
                                    this.state.getFeedRange(),
                                    effectiveRange)))
                            .flatMap(state -> state.getContinuation().handleFeedRangeGone(client, (GoneException)e));
                    }

                    return this
                        .state
                        .getContinuation()
                        .handleFeedRangeGone(client, (GoneException)e)
                        .flatMap(feedRangeGoneShouldRetryResult -> {
                            if (!feedRangeGoneShouldRetryResult.shouldRetry) {
                                LOGGER.warn(
                                    "No partition split or merge error - will fail the request. Context: {}",
                                    this.operationContextTextProvider.get(),
                                    e);
                            } else {
                                LOGGER.debug(
                                    "HandleFeedRangeGone will retry. Context: {}",
                                    this.operationContextTextProvider.get(),
                                    e);
                            }

                            return Mono.just(feedRangeGoneShouldRetryResult);
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

    private DocumentClientRetryPolicy getFeedRangeContinuationRetryPolicy(
        RxDocumentClientImpl client,
        Map<String, Object> requestOptionProperties,
        String collectionLink,
        boolean isSplitHandlingDisabled,
        DiagnosticsClientContext diagnosticsClientContext) {

        DocumentClientRetryPolicy feedRangeContinuationRetryPolicy;

        // constructing retry policies for changeFeed requests
        DocumentClientRetryPolicy retryPolicyInstance =
            client.getResetSessionTokenRetryPolicy().getRequestPolicy(diagnosticsClientContext);

        if (isSplitHandlingDisabled) {
            // True for ChangeFeedProcessor - where all retry-logic is handled
            feedRangeContinuationRetryPolicy = retryPolicyInstance;
        } else {
            // TODO @fabianm wire up clientContext - for now no availability strategy is wired up for ChangeFeed
            // requests - and this is expected/by design for now. But it is certainly worth discussing/checking whether
            // we should include change feed requests as well - there are a few challenges especially for multi master
            // accounts depending on the consistency level - and usually change feed is not processed in OLTP
            // scenarios, so, keeping it out of scope for now is a reasonable decision. But probably worth
            // double checking this decision in a few months.
            retryPolicyInstance = new PartitionKeyRangeGoneRetryPolicy(client,
                client.getCollectionCache(),
                client.getPartitionKeyRangeCache(),
                collectionLink,
                retryPolicyInstance,
                requestOptionProperties);

            feedRangeContinuationRetryPolicy = new FeedRangeContinuationFeedRangeGoneRetryPolicy(
                client,
                this.changeFeedState,
                retryPolicyInstance,
                requestOptionProperties,
                retryPolicyInstance.getRetryContext(),
                this::getOperationContextText);
        }

        return feedRangeContinuationRetryPolicy;
    }
}
