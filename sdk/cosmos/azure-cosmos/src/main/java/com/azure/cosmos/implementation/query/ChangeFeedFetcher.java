// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.query;

import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.Resource;
import com.azure.cosmos.implementation.RxDocumentServiceRequest;
import com.azure.cosmos.implementation.Strings;
import com.azure.cosmos.implementation.changefeed.implementation.ChangeFeedStartFromContinuationImpl;
import com.azure.cosmos.implementation.changefeed.implementation.ChangeFeedStartFromEtagAndFeedRangeImpl;
import com.azure.cosmos.implementation.changefeed.implementation.ChangeFeedStartFromInternal;
import com.azure.cosmos.implementation.changefeed.implementation.ChangeFeedState;
import com.azure.cosmos.implementation.changefeed.implementation.PopulateStartFromRequestOptionVisitorImpl;
import com.azure.cosmos.implementation.feedranges.FeedRangeContinuation;
import com.azure.cosmos.implementation.feedranges.FeedRangeInternal;
import com.azure.cosmos.implementation.feedranges.FeedRangeRxDocumentServiceRequestPopulatorVisitorImpl;
import com.azure.cosmos.implementation.feedranges.GenericFeedRangeVisitor;
import com.azure.cosmos.models.FeedResponse;
import reactor.core.publisher.Mono;

import java.util.function.BiFunction;
import java.util.function.Function;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

class ChangeFeedFetcher<T extends Resource> extends Fetcher<T> {
    private final Function<Integer, RxDocumentServiceRequest> createRequestFunc;
    private final ChangeFeedState changeFeedState;

    public ChangeFeedFetcher(
        Function<Integer, RxDocumentServiceRequest> createRequestFunc,
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
        FeedRangeContinuation continuation = this.changeFeedState.getContinuation();
        return continuation != null && continuation.isDone();
    }

    @Override
    protected String getContinuationForLogging() {
        return this.changeFeedState.toJson();
    }

    @Override
    protected RxDocumentServiceRequest createRequest(int maxItemCount) {
        RxDocumentServiceRequest request = this.createRequestFunc.apply(maxItemCount);



        ChangeFeedStartFromInternal effectiveStartFrom;
        final PopulateStartFromRequestOptionVisitorImpl populateRequestOptionsVisitor =
            new PopulateStartFromRequestOptionVisitorImpl(request);
        effectiveStartFrom.accept(populateRequestOptionsVisitor);
        FeedRangeContinuation continuation = this.changeFeedState.getContinuation();
        if (continuation != null) {
            effectiveStartFrom = new ChangeFeedStartFromContinuationImpl() {
            }
        } else {

        }







        if (effectiveFeedRange != null) {
            final GenericFeedRangeVisitor<RxDocumentServiceRequest> feedRangeVisitor =
                FeedRangeRxDocumentServiceRequestPopulatorVisitorImpl.SINGLETON;
            effectiveFeedRange.accept(feedRangeVisitor, request);
        }



        return request;
    }
}
