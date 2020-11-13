// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.feedranges;

import com.azure.cosmos.implementation.RxDocumentServiceRequest;
import com.azure.cosmos.implementation.routing.Range;

import java.util.Map;
import java.util.function.BiConsumer;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

final class FeedRangeContinuationRxDocumentServiceRequestPopulatorVisitorImpl extends FeedRangeContinuationVisitor {

    private final RxDocumentServiceRequest request;
    private final BiConsumer<RxDocumentServiceRequest, String> fillContinuation;

    public FeedRangeContinuationRxDocumentServiceRequestPopulatorVisitorImpl(
        RxDocumentServiceRequest request, BiConsumer<RxDocumentServiceRequest, String> fillContinuation)
    {
        checkNotNull(request, "'request' must not be null");
        checkNotNull(fillContinuation, "'fillContinuation' must not be null");

        this.request = request;
        this.fillContinuation = fillContinuation;
    }

    @Override
    public void visit(FeedRangeCompositeContinuationImpl feedRangeCompositeContinuation) {
        checkNotNull(feedRangeCompositeContinuation, "'feedRangeCompositeContinuation' must not be null");

        final Map<String, Object> properties = this.request.getPropertiesOrThrow();

        // In case EPK has already been set by compute
        if (properties.containsKey(EpkRequestPropertyConstants.START_EPK_STRING)) {
            return;
        }

        final Range<String> range = feedRangeCompositeContinuation.getCurrentToken().getRange();

        properties.put(EpkRequestPropertyConstants.END_EPK_STRING, range.getMax());
        properties.put(EpkRequestPropertyConstants.START_EPK_STRING, range.getMin());

        this.fillContinuation.accept(this.request, feedRangeCompositeContinuation.getContinuation());
    }
}
