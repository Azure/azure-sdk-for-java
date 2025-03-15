// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.perPartitionAutomaticFailover;

import java.time.Instant;

public class EndToEndTimeoutErrorTracker {

    private final Instant failureWindowStart;

    private final Integer errorCount;

    public EndToEndTimeoutErrorTracker(Instant failureWindowStart, Integer errorCount) {
        this.failureWindowStart = failureWindowStart;
        this.errorCount = errorCount;
    }

    public Instant getFailureWindowStart() {
        return this.failureWindowStart;
    }

    public Integer getErrorCount() {
        return this.errorCount;
    }
}
