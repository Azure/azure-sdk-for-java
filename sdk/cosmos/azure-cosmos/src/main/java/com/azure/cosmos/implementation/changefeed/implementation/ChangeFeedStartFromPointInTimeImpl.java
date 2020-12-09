// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.changefeed.implementation;

import com.azure.cosmos.implementation.Constants;
import com.azure.cosmos.implementation.RxDocumentServiceRequest;

import java.time.Instant;

import static com.azure.cosmos.BridgeInternal.setProperty;

class ChangeFeedStartFromPointInTimeImpl extends ChangeFeedStartFromInternal {
    private final Instant pointInTime;

    public ChangeFeedStartFromPointInTimeImpl(Instant pointInTime) {
        super();

        if (pointInTime == null) {
            throw new NullPointerException("pointInTime");
        }

        this.pointInTime = pointInTime;
    }

    public Instant getPointInTime() {
        return this.pointInTime;
    }

    @Override
    void accept(ChangeFeedStartFromVisitor visitor, RxDocumentServiceRequest request) {
        visitor.visit(this, request);
    }

    @Override
    public void populatePropertyBag() {

        super.populatePropertyBag();

        setProperty(
            this,
            com.azure.cosmos.implementation.Constants.Properties.CHANGE_FEED_START_FROM_TYPE,
            ChangeFeedStartFromTypes.POINT_IN_TIME);

        setProperty(
            this,
            Constants.Properties.CHANGE_FEED_START_FROM_POINT_IN_TIME_MS,
            this.pointInTime.toEpochMilli());
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof ChangeFeedStartFromEtagAndFeedRangeImpl)) {
            return false;
        }

        ChangeFeedStartFromPointInTimeImpl otherStartFrom = (ChangeFeedStartFromPointInTimeImpl) obj;
        return  this.pointInTime.equals(otherStartFrom.pointInTime);
    }

    @Override
    public int hashCode() {
        return this.pointInTime.hashCode();
    }
}
