// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

public class PointOperationContext {

    private final AtomicBoolean hasOperationSeenSuccess;

    private boolean isRequestHedged;

    public PointOperationContext(AtomicBoolean hasOperationSeenSuccess) {
        this.hasOperationSeenSuccess = hasOperationSeenSuccess;
    }

    public void setIsRequestHedged(boolean isRequestHedged) {
        this.isRequestHedged = isRequestHedged;
    }

    public boolean getIsRequestHedged() {
        return this.isRequestHedged;
    }

    public void setHasOperationSeenSuccess() {
        this.hasOperationSeenSuccess.set(true);
    }

    public boolean getHasOperationSeenSuccess() {
        return hasOperationSeenSuccess.get();
    }
}
