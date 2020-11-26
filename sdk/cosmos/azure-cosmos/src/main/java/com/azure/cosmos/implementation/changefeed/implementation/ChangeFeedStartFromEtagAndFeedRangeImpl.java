// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.changefeed.implementation;

import com.azure.cosmos.implementation.RxDocumentServiceRequest;
import com.azure.cosmos.implementation.Strings;
import com.azure.cosmos.implementation.feedranges.FeedRangeInternal;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

class ChangeFeedStartFromEtagAndFeedRangeImpl extends ChangeFeedStartFromInternal {
    private final String etag;
    private final FeedRangeInternal feedRange;

    public ChangeFeedStartFromEtagAndFeedRangeImpl(String etag, FeedRangeInternal feedRange) {
        super();

        checkNotNull(feedRange, "Argument 'feedRange' must not be null");

        this.etag = etag;
        this.feedRange = feedRange;
    }

    public String getEtag() {
        return this.etag;
    }

    public FeedRangeInternal getFeedRange() {
        return this.feedRange;
    }

    @Override
    void accept(ChangeFeedStartFromVisitor visitor, RxDocumentServiceRequest request) {
        visitor.Visit(this, request);
    }
}
