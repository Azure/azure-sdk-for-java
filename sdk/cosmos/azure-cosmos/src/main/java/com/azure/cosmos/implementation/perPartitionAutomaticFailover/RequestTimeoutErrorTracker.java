// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.perPartitionAutomaticFailover;

import java.time.Instant;

public class RequestTimeoutErrorTracker {

    private final Instant failureWindowStart;

    private final int errorCount;

    public RequestTimeoutErrorTracker(Instant failureWindowStart, int errorCount) {
        this.failureWindowStart = failureWindowStart;
        this.errorCount = errorCount;
    }

    public Instant getFailureWindowStart() {
        return this.failureWindowStart;
    }

    public int getErrorCount() {
        return this.errorCount;
    }
}
