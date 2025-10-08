// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.query;

import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.CosmosDiagnostics;
import com.azure.cosmos.CosmosException;
import com.azure.cosmos.implementation.DocumentClientRetryPolicy;
import com.azure.cosmos.implementation.GlobalEndpointManager;
import com.azure.cosmos.implementation.ObservableHelper;
import com.azure.cosmos.implementation.RxDocumentClientImpl;
import com.azure.cosmos.implementation.RxDocumentServiceRequest;
import com.azure.cosmos.implementation.Strings;
import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;
import com.azure.cosmos.implementation.perPartitionCircuitBreaker.GlobalPartitionEndpointManagerForPerPartitionCircuitBreaker;
import com.azure.cosmos.implementation.spark.OperationContextAndListenerTuple;
import com.azure.cosmos.models.FeedResponse;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

class ServerSideOnlyContinuationNonDocumentFetcherImpl<T> extends Fetcher<T> {
    private final BiFunction<String, Integer, RxDocumentServiceRequest> createRequestFunc;
    private final Supplier<DocumentClientRetryPolicy> retryPolicySupplier;
    private volatile String continuationToken;

    public ServerSideOnlyContinuationNonDocumentFetcherImpl(
        RxDocumentClientImpl client,
        BiFunction<String, Integer, RxDocumentServiceRequest> createRequestFunc,
        Function<RxDocumentServiceRequest, Mono<FeedResponse<T>>> executeFunc,
        String continuationToken,
        boolean isChangeFeed,
        int top,
        int maxItemCount,
        OperationContextAndListenerTuple operationContext,
        List<CosmosDiagnostics> cancelledRequestDiagnosticsTracker,
        GlobalEndpointManager globalEndpointManager,
        GlobalPartitionEndpointManagerForPerPartitionCircuitBreaker globalPartitionEndpointManagerForPerPartitionCircuitBreaker) {

        super(
            executeFunc,
            isChangeFeed,
            top,
            maxItemCount,
            operationContext,
            cancelledRequestDiagnosticsTracker,
            globalEndpointManager,
            globalPartitionEndpointManagerForPerPartitionCircuitBreaker);

        checkNotNull(client, "Argument 'client' must not be null.");
        checkNotNull(createRequestFunc, "Argument 'createRequestFunc' must not be null.");

        this.createRequestFunc = createRequestFunc;
        this.continuationToken = continuationToken;
        this.retryPolicySupplier = () -> client.getResetSessionTokenRetryPolicy().getRequestPolicy(null);
    }

    @Override
    protected String applyServerResponseContinuation(
        String serverContinuationToken,
        RxDocumentServiceRequest request,
        FeedResponse<T> response) {

        return this.continuationToken = serverContinuationToken;
    }

    @Override
    protected String applyServerResponseContinuation(String serverContinuationToken, RxDocumentServiceRequest request, CosmosException cosmosException) {
        return Strings.Emtpy;
    }

    @Override
    public Mono<FeedResponse<T>> nextPage() {
        DocumentClientRetryPolicy retryPolicy = this.retryPolicySupplier.get();

        if (retryPolicy == null) {
            return this.nextPageCore(null);
        }

        return ObservableHelper.inlineIfPossible(
            () -> nextPageCore(retryPolicy),
            retryPolicy);
    }

    @Override
    protected RxDocumentServiceRequest createRequest(
        int maxItemCount,
        DocumentClientRetryPolicy documentClientRetryPolicy) {
        RxDocumentServiceRequest request = this.createRequestFunc.apply(this.continuationToken, maxItemCount);

        if (documentClientRetryPolicy != null) {
            documentClientRetryPolicy.onBeforeSendRequest(request);
        }

        return request;
    }

    @Override
    protected boolean isFullyDrained(boolean isChangeFeed, FeedResponse<T> response) {
        // if token is null or if change feed query and no changes then done
        return StringUtils.isEmpty(continuationToken) ||
            (isChangeFeed && BridgeInternal.noChanges(response));
    }

    @Override
    protected String getContinuationForLogging() {
        return this.continuationToken;
    }
}
