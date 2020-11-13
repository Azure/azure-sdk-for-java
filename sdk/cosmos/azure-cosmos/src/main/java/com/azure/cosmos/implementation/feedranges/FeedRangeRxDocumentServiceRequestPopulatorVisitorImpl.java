// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.feedranges;

import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.RxDocumentServiceRequest;
import com.azure.cosmos.implementation.routing.Range;

import java.util.Map;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

final class FeedRangeRxDocumentServiceRequestPopulatorVisitorImpl
    extends GenericFeedRangeVisitor<RxDocumentServiceRequest> {

    public final static FeedRangeRxDocumentServiceRequestPopulatorVisitorImpl SINGLETON =
        new FeedRangeRxDocumentServiceRequestPopulatorVisitorImpl();

    private FeedRangeRxDocumentServiceRequestPopulatorVisitorImpl() {
    }

    @Override
    public void visit(FeedRangeEpkImpl feedRange,
                      RxDocumentServiceRequest rxDocumentServiceRequest) {

        checkNotNull(feedRange, "'feedRange' must not be null");
        checkNotNull(rxDocumentServiceRequest, "'rxDocumentServiceRequest' must not be null");

        final Map<String, Object> properties = rxDocumentServiceRequest.getPropertiesOrThrow();

        // In case EPK has already been set by compute
        if (properties.containsKey(EpkRequestPropertyConstants.START_EPK_STRING)) {
            return;
        }

        final Range<String> range = feedRange.getRange();

        properties.put(EpkRequestPropertyConstants.END_EPK_STRING, range.getMax());
        properties.put(EpkRequestPropertyConstants.START_EPK_STRING, range.getMin());
    }

    @Override
    public void visit(FeedRangePartitionKeyRangeImpl feedRange,
                      RxDocumentServiceRequest rxDocumentServiceRequest) {

        checkNotNull(feedRange, "'feedRange' must not be null");
        checkNotNull(rxDocumentServiceRequest, "'rxDocumentServiceRequest' must not be null");

        rxDocumentServiceRequest.routeTo(feedRange.getPartitionKeyRangeIdentity());
    }

    @Override
    public void visit(FeedRangePartitionKeyImpl feedRange,
                      RxDocumentServiceRequest rxDocumentServiceRequest) {
        checkNotNull(feedRange, "'feedRange' must not be null");
        checkNotNull(rxDocumentServiceRequest, "'rxDocumentServiceRequest' must not be null");

        rxDocumentServiceRequest.getHeaders().put(
            HttpConstants.HttpHeaders.PARTITION_KEY,
            feedRange.getPartitionKeyInternal().toJson());
        rxDocumentServiceRequest.setPartitionKeyInternal(feedRange.getPartitionKeyInternal());
    }
}
