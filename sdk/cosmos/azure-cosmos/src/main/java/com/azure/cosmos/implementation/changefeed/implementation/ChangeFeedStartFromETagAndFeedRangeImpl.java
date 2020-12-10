// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.changefeed.implementation;

import com.azure.cosmos.implementation.RxDocumentServiceRequest;
import com.azure.cosmos.implementation.feedranges.FeedRangeInternal;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

class ChangeFeedStartFromETagAndFeedRangeImpl extends ChangeFeedStartFromInternal {
    private final String eTag;
    private final FeedRangeInternal feedRange;

    public ChangeFeedStartFromETagAndFeedRangeImpl(String eTag, FeedRangeInternal feedRange) {
        super();

        checkNotNull(feedRange, "Argument 'feedRange' must not be null");

        this.eTag = eTag;
        this.feedRange = feedRange;
    }

    public String getETag() {
        return this.eTag;
    }

    public FeedRangeInternal getFeedRange() {
        return this.feedRange;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof ChangeFeedStartFromETagAndFeedRangeImpl)) {
            return false;
        }

        ChangeFeedStartFromETagAndFeedRangeImpl otherStartFrom =
            (ChangeFeedStartFromETagAndFeedRangeImpl)obj;

        if (this.eTag == null) {
            return otherStartFrom.eTag == null &&
                this.feedRange.equals(otherStartFrom.feedRange);
        }

        return this.eTag.compareTo(otherStartFrom.eTag) == 0 &&
            this.feedRange.equals(otherStartFrom.feedRange);
    }

    @Override
    public int hashCode() {
        int hash = 1;
        hash = (hash * 397) ^ this.feedRange.hashCode();

        if (this.eTag != null) {
            hash = (hash * 397) ^ this.eTag.hashCode();
        }

        return hash;
    }

    @Override
    public void populateRequest(ChangeFeedStartFromVisitor visitor,
                                RxDocumentServiceRequest request) {

        visitor.visit(this, request);
    }
}
