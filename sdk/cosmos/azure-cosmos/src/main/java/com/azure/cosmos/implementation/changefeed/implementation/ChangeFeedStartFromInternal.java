// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.changefeed.implementation;

import com.azure.cosmos.implementation.JsonSerializable;
import com.azure.cosmos.implementation.RxDocumentServiceRequest;
import com.azure.cosmos.implementation.feedranges.FeedRangeInternal;

import java.time.Instant;

public abstract class ChangeFeedStartFromInternal extends JsonSerializable {
    ChangeFeedStartFromInternal() {
    }

    public static ChangeFeedStartFromInternal createFromBeginning() {
        return InstanceHolder.FROM_BEGINNING_SINGLETON;
    }

    public static ChangeFeedStartFromInternal createFromETagAndFeedRange(
        String eTag,
        FeedRangeInternal feedRange) {

        return new ChangeFeedStartFromETagAndFeedRangeImpl(eTag, feedRange);
    }

    public static ChangeFeedStartFromInternal createFromNow() {
        return InstanceHolder.FROM_NOW_SINGLETON;
    }

    public static ChangeFeedStartFromInternal createFromPointInTime(Instant pointInTime) {
        return new ChangeFeedStartFromPointInTimeImpl(pointInTime);
    }

    @Override
    public void populatePropertyBag() {
        super.populatePropertyBag();
    }

    @Override
    public String toString() {
        return this.toJson();
    }

    public abstract void populateRequest(
        ChangeFeedStartFromVisitor visitor,
        RxDocumentServiceRequest request);

    private static final class InstanceHolder {
        static final ChangeFeedStartFromBeginningImpl FROM_BEGINNING_SINGLETON =
            new ChangeFeedStartFromBeginningImpl();

        static final ChangeFeedStartFromNowImpl FROM_NOW_SINGLETON =
            new ChangeFeedStartFromNowImpl();
    }
}