// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.feedranges;

import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.implementation.IRetryPolicy;
import com.azure.cosmos.implementation.RxDocumentServiceResponse;
import com.azure.cosmos.models.FeedRange;
import reactor.core.publisher.Mono;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

abstract class FeedRangeContinuation {
    private final FeedRangeInternal feedRange;
    private final String containerRid;

    // for mocking
    protected FeedRangeContinuation() {
        this.feedRange = null;
        this.containerRid = null;
    }

    public FeedRangeContinuation(String containerRid, FeedRangeInternal feedRange) {
        checkNotNull(feedRange, "expected non-null feedRange");
        this.feedRange = feedRange;
        this.containerRid = containerRid;
    }

    public String getContainerRid() {
        return this.containerRid;
    }

    public FeedRangeInternal getFeedRange() {
        return this.feedRange;
    }

    public abstract String getContinuation();

    public abstract void replaceContinuation(String continuationToken);

    public abstract boolean isDone();

    public abstract void validateContainer(String containerRid);

    public static FeedRangeContinuation tryParse(String toStringValue)
    {
        return FeedRangeCompositeContinuation.tryParse(toStringValue);
    }

    public abstract IRetryPolicy.ShouldRetryResult handleChangeFeedNotModified(
        RxDocumentServiceResponse responseMessage);

    public abstract Mono<IRetryPolicy.ShouldRetryResult> handleSplitAsync(
        CosmosAsyncContainer containerCore,
        RxDocumentServiceResponse responseMessage);

    public abstract void accept(FeedRangeContinuationVisitor visitor);
}
