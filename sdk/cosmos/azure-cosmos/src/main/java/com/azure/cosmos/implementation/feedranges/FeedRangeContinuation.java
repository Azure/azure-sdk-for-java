// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.feedranges;

import com.azure.cosmos.implementation.RxDocumentClientImpl;
import com.azure.cosmos.implementation.RxDocumentServiceResponse;
import com.azure.cosmos.implementation.ShouldRetryResult;
import reactor.core.publisher.Mono;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

public abstract class FeedRangeContinuation {
    protected final FeedRangeInternal feedRange;
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

    /* TODO fabianm - infinite recursion
    public static FeedRangeContinuation tryParse(String toStringValue) {
        return FeedRangeCompositeContinuationImpl.tryParse(toStringValue);
    }*/

    public abstract ShouldRetryResult handleChangeFeedNotModified(
        RxDocumentServiceResponse responseMessage);

    public abstract Mono<ShouldRetryResult> handleSplit(
        RxDocumentClientImpl client,
        RxDocumentServiceResponse responseMessage);

    public abstract void accept(FeedRangeContinuationVisitor visitor);
}
