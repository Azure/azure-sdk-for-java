// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.query;

import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.implementation.Resource;
import com.azure.cosmos.implementation.RxDocumentServiceRequest;
import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;
import com.azure.cosmos.implementation.feedranges.FeedRangeInternal;
import com.azure.cosmos.models.FeedResponse;
import reactor.core.publisher.Mono;

import java.util.function.BiFunction;
import java.util.function.Function;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

class ServerSideOnlyContinuationFetcherImpl<T extends Resource> extends Fetcher<T> {
    private final BiFunction<String, Integer, RxDocumentServiceRequest> createRequestFunc;
    private volatile String continuationToken;

    public ServerSideOnlyContinuationFetcherImpl(BiFunction<String, Integer,
        RxDocumentServiceRequest> createRequestFunc,
                                                 Function<RxDocumentServiceRequest,
                                                     Mono<FeedResponse<T>>> executeFunc,
                                                 String continuationToken,
                                                 boolean isChangeFeed,
                                                 int top,
                                                 int maxItemCount) {

        super(executeFunc, isChangeFeed, top, maxItemCount);

        checkNotNull(createRequestFunc, "Argument 'createRequestFunc' must not be null.");
        this.createRequestFunc = createRequestFunc;

        this.continuationToken = continuationToken;
    }

    @Override
    protected String applyServerResponseContinuation(
        String serverContinuationToken,
        RxDocumentServiceRequest request) {

        return this.continuationToken = serverContinuationToken;
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

    @Override
    protected RxDocumentServiceRequest createRequest(int maxItemCount) {
        return this.createRequestFunc.apply(this.continuationToken, maxItemCount);
    }
}
