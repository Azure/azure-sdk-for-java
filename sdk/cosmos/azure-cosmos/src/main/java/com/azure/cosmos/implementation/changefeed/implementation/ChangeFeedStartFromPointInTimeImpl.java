// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.changefeed.implementation;

import java.time.Instant;

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
    void accept(ChangeFeedStartFromVisitor visitor) {
        visitor.Visit(this);
    }
}
