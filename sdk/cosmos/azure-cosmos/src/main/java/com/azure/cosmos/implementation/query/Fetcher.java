// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.query;

import com.azure.cosmos.CosmosDiagnostics;
import com.azure.cosmos.implementation.DocumentClientRetryPolicy;
import com.azure.cosmos.implementation.FeedOperationContextForCircuitBreaker;
import com.azure.cosmos.implementation.GlobalEndpointManager;
import com.azure.cosmos.implementation.perPartitionCircuitBreaker.GlobalPartitionEndpointManagerForPerPartitionCircuitBreaker;
import com.azure.cosmos.implementation.ImplementationBridgeHelpers;
import com.azure.cosmos.implementation.RxDocumentServiceRequest;
import com.azure.cosmos.implementation.spark.OperationContextAndListenerTuple;
import com.azure.cosmos.models.FeedResponse;
import com.azure.cosmos.models.ModelBridgeInternal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;
import reactor.core.publisher.SignalType;

import java.net.URI;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.Supplier;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

abstract class Fetcher<T> {
    private final static Logger logger = LoggerFactory.getLogger(Fetcher.class);

    private final static
    ImplementationBridgeHelpers.CosmosDiagnosticsHelper.CosmosDiagnosticsAccessor diagnosticsAccessor =
        ImplementationBridgeHelpers.CosmosDiagnosticsHelper.getCosmosDiagnosticsAccessor();

    private final Function<RxDocumentServiceRequest, Mono<FeedResponse<T>>> executeFunc;
    private final boolean isChangeFeed;
    private final OperationContextAndListenerTuple operationContext;
    private Supplier<String> operationContextTextProvider;

    private final AtomicBoolean shouldFetchMore;
    private final AtomicInteger maxItemCount;
    private final AtomicInteger top;
    private final List<CosmosDiagnostics> cancelledRequestDiagnosticsTracker;
    private final GlobalEndpointManager globalEndpointManager;
    private final GlobalPartitionEndpointManagerForPerPartitionCircuitBreaker globalPartitionEndpointManagerForPerPartitionCircuitBreaker;

    public Fetcher(
        Function<RxDocumentServiceRequest, Mono<FeedResponse<T>>> executeFunc,
        boolean isChangeFeed,
        int top,
        int maxItemCount,
        OperationContextAndListenerTuple operationContext,
        List<CosmosDiagnostics> cancelledRequestDiagnosticsTracker,
        GlobalEndpointManager globalEndpointManager,
        GlobalPartitionEndpointManagerForPerPartitionCircuitBreaker globalPartitionEndpointManagerForPerPartitionCircuitBreaker) {

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

        this.top = new AtomicInteger(top);
        if (top == -1) {
            this.maxItemCount = new AtomicInteger(maxItemCount);
        } else {
            // it is a top query, we should not retrieve more than requested top.
            this.maxItemCount = new AtomicInteger(Math.min(maxItemCount, top));
        }
        this.shouldFetchMore = new AtomicBoolean(true);
        this.cancelledRequestDiagnosticsTracker = cancelledRequestDiagnosticsTracker;
        this.globalEndpointManager = globalEndpointManager;
        this.globalPartitionEndpointManagerForPerPartitionCircuitBreaker = globalPartitionEndpointManagerForPerPartitionCircuitBreaker;
    }

    public final boolean shouldFetchMore() {
        return shouldFetchMore.get();
    }

    public Mono<FeedResponse<T>> nextPage() {
        return this.nextPageCore(null);
    }

    protected final Mono<FeedResponse<T>> nextPageCore(DocumentClientRetryPolicy documentClientRetryPolicy) {
        RxDocumentServiceRequest request = createRequest(documentClientRetryPolicy);
        return nextPage(request);
    }

    protected abstract String applyServerResponseContinuation(
        String serverContinuationToken,
        RxDocumentServiceRequest request,
        FeedResponse<T> response);

    protected abstract boolean isFullyDrained(boolean isChangeFeed, FeedResponse<T> response);

    protected abstract String getContinuationForLogging();

    public String getOperationContextText() {
        return this.operationContextTextProvider.get();
    }

    private void updateState(FeedResponse<T> response, RxDocumentServiceRequest request) {
        String transformedContinuation =
            this.applyServerResponseContinuation(response.getContinuationToken(), request, response);

        ModelBridgeInternal.setFeedResponseContinuationToken(transformedContinuation, response);
        if (top.get() != -1) {
            top.accumulateAndGet(response.getResults().size(), (left, right) -> left - right);
            if (top.get() < 0) {
                // this shouldn't happen
                // this means backend retrieved more items than requested
                logger.warn("Azure Cosmos DB BackEnd Service returned more than requested {} items, Context: {}",
                    maxItemCount.get(),
                    this.operationContextTextProvider.get());
                top.set(0);
            }
            maxItemCount.accumulateAndGet(top.get(), Math::min);
        }

        if (shouldFetchMore.get() &&
            // if top == 0 then done
            (top.get() != 0) &&
            // if fullyDrained then done
            !this.isFullyDrained(this.isChangeFeed, response)) {
            shouldFetchMore.set(true);
        } else {
            shouldFetchMore.set(false);
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Fetcher state updated: " +
                    "isChangeFeed = {}, continuation token = {}, max item count = {}, should fetch more = {}, Context: {}",
                isChangeFeed, this.getContinuationForLogging(), maxItemCount.get(), shouldFetchMore.get(),
                this.operationContextTextProvider.get());
        }
    }

    protected void reEnableShouldFetchMoreForRetry() {
        this.shouldFetchMore.set(true);
    }

    protected RxDocumentServiceRequest createRequest(DocumentClientRetryPolicy documentClientRetryPolicy) {
        if (!shouldFetchMore.get()) {
            // this should never happen
            logger.error(
                "invalid state, trying to fetch more after completion, Context: {}",
                this.operationContextTextProvider.get());
            throw new IllegalStateException("INVALID state, trying to fetch more after completion");
        }

        return this.createRequest(maxItemCount.get(), documentClientRetryPolicy);
    }

    protected abstract RxDocumentServiceRequest createRequest(
        int maxItemCount,
        DocumentClientRetryPolicy documentClientRetryPolicy);

    private Mono<FeedResponse<T>> nextPage(RxDocumentServiceRequest request) {
        AtomicBoolean completed = new AtomicBoolean(false);

        return executeFunc
            .apply(request)
            .map(rsp -> {
                updateState(rsp, request);
                return rsp;
            })
            .doOnNext(response -> {
                completed.set(true);

                if (this.globalPartitionEndpointManagerForPerPartitionCircuitBreaker.isPartitionLevelCircuitBreakingApplicable(request)) {

                    checkNotNull(request.requestContext, "Argument 'request.requestContext' must not be null!");
                    FeedOperationContextForCircuitBreaker feedOperationContextForCircuitBreaker = request.requestContext.getFeedOperationContextForCircuitBreaker();

                    checkNotNull(feedOperationContextForCircuitBreaker, "Argument 'feedOperationContextForCircuitBreaker' must not be null!");

                    if (!feedOperationContextForCircuitBreaker.isThresholdBasedAvailabilityStrategyEnabled()) {
                        this.globalPartitionEndpointManagerForPerPartitionCircuitBreaker.handleLocationSuccessForPartitionKeyRange(request);
                        feedOperationContextForCircuitBreaker.addPartitionKeyRangeWithSuccess(request.requestContext.resolvedPartitionKeyRange, request.getResourceId());
                    }
                }
            })
            .doOnError(throwable -> completed.set(true))
            .doFinally(signalType -> {
                // If the signal type is not cancel(which means success or error), we do not need to tracking the diagnostics here
                // as the downstream will capture it
                //
                // Any of the reactor operators can terminate with a cancel signal instead of error signal
                // For example collectList, Flux.merge, takeUntil
                // We should only record cancellation diagnostics if the signal is cancelled and the request is cancelled
                if (signalType != SignalType.CANCEL
                    || this.cancelledRequestDiagnosticsTracker == null
                    || completed.get()) {
                    return;
                }

                if (this.globalPartitionEndpointManagerForPerPartitionCircuitBreaker.isPartitionLevelCircuitBreakingApplicable(request)) {

                    checkNotNull(request.requestContext, "Argument 'request.requestContext' must not be null!");

                    FeedOperationContextForCircuitBreaker feedOperationContextForCircuitBreaker = request.requestContext.getFeedOperationContextForCircuitBreaker();
                    checkNotNull(feedOperationContextForCircuitBreaker, "Argument 'feedOperationContextForCircuitBreaker' must not be null!");

                    if (!feedOperationContextForCircuitBreaker.isThresholdBasedAvailabilityStrategyEnabled()) {
                        if (this.globalEndpointManager != null) {
                            this.handleCancellationExceptionForPartitionKeyRange(request);
                        }
                    }
                }

                if (request.requestContext != null && request.requestContext.cosmosDiagnostics != null) {
                    this.cancelledRequestDiagnosticsTracker.add(request.requestContext.cosmosDiagnostics);
                }
            });
    }

    private void handleCancellationExceptionForPartitionKeyRange(RxDocumentServiceRequest failedRequest) {
        URI firstContactedLocationEndpoint = diagnosticsAccessor.getFirstContactedLocationEndpoint(failedRequest.requestContext.cosmosDiagnostics);

        if (firstContactedLocationEndpoint != null) {
            this.globalPartitionEndpointManagerForPerPartitionCircuitBreaker.handleLocationExceptionForPartitionKeyRange(failedRequest, firstContactedLocationEndpoint);
        }
    }
}
