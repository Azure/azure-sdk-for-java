// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.feedranges;

import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.implementation.IRetryPolicy;
import com.azure.cosmos.implementation.RxDocumentServiceResponse;
import reactor.core.publisher.Mono;

final class FeedRangeCompositeContinuation extends FeedRangeContinuation {
    @Override
    public String getContinuation() {
        // TODO fabianm - Implement
        return null;
    }

    @Override
    public void replaceContinuation(String continuationToken) {
        // TODO fabianm - Implement
    }

    @Override
    public boolean isDone() {
        // TODO fabianm - Implement
        return false;
    }

    @Override
    public void validateContainer(String containerRid) {
        // TODO fabianm - Implement
    }

    @Override
    public IRetryPolicy.ShouldRetryResult handleChangeFeedNotModified(RxDocumentServiceResponse responseMessage) {
        // TODO fabianm - Implement
        return null;
    }

    @Override
    public Mono<IRetryPolicy.ShouldRetryResult> handleSplitAsync(
        CosmosAsyncContainer containerCore,
        RxDocumentServiceResponse responseMessage) {

        // TODO fabianm - Implement
        return null;
    }

    @Override
    public void accept(FeedRangeContinuationVisitor visitor) {
        // TODO fabianm - Implement
    }
}
