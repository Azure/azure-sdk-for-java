// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.feedranges;

import com.azure.cosmos.implementation.RxDocumentServiceRequest;

final class FeedRangeRxDocumentServiceRequestPopulatorVisitor
    extends GenericFeedRangeVisitor<RxDocumentServiceRequest> {

    @Override
    public void visit(FeedRangeEpk feedRange,
                      RxDocumentServiceRequest rxDocumentServiceRequest) {
        // TODO fabianm - Implement
    }

    @Override
    public void visit(FeedRangePartitionKeyRange feedRange, RxDocumentServiceRequest rxDocumentServiceRequest) {
        // TODO fabianm - Implement
    }

    @Override
    public void visit(FeedRangePartitionKey feedRange, RxDocumentServiceRequest rxDocumentServiceRequest) {
        // TODO fabianm - Implement
    }
}
