// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.changefeed.implementation;

import com.azure.cosmos.implementation.Constants;
import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.RxDocumentServiceRequest;
import com.azure.cosmos.implementation.Utils;

import java.time.Instant;

import static com.azure.cosmos.BridgeInternal.setProperty;
import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

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
    public boolean equals(Object obj) {
        if (!(obj instanceof ChangeFeedStartFromPointInTimeImpl)) {
            return false;
        }

        ChangeFeedStartFromPointInTimeImpl otherStartFrom = (ChangeFeedStartFromPointInTimeImpl)obj;
        return this.pointInTime.equals(otherStartFrom.pointInTime);
    }

    @Override
    public int hashCode() {
        return this.pointInTime.hashCode();
    }

    @Override
    public void populatePropertyBag() {

        super.populatePropertyBag();

        synchronized(this) {
            setProperty(
                this,
                com.azure.cosmos.implementation.Constants.Properties.CHANGE_FEED_START_FROM_TYPE,
                ChangeFeedStartFromTypes.POINT_IN_TIME);

            setProperty(
                this,
                Constants.Properties.CHANGE_FEED_START_FROM_POINT_IN_TIME_MS,
                this.pointInTime.toEpochMilli());
        }
    }

    @Override
    public void populateRequest(RxDocumentServiceRequest request) {
        checkNotNull(request, "Argument 'request' must not be null.");

        // Our current public contract for ChangeFeedProcessor uses DateTime.MinValue.ToUniversalTime as beginning.
        // We need to add a special case here, otherwise it would send it as normal StartTime.
        // The problem is Multi master accounts do not support StartTime header on ReadFeed, and thus,
        // it would break multi master Change Feed Processor users using Start From Beginning semantics.
        // It's also an optimization, since the backend won't have to binary search for the value.
        Instant pointInTime = this.getPointInTime();
        if (pointInTime != START_FROM_BEGINNING_TIME)
        {
            request.getHeaders().put(
                HttpConstants.HttpHeaders.IF_MODIFIED_SINCE,
                Utils.instantAsUTCRFC1123(pointInTime));
        }
    }

    @Override
    public boolean supportsFullFidelityRetention() {
        return false;
    }
}
