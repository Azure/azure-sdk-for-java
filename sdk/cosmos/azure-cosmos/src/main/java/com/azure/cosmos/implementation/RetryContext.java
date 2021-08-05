// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RetryContext {
    @JsonIgnore
    private volatile Instant retryStartTime;
    @JsonIgnore
    private volatile Instant retryEndTime;

    private List<int[]> statusAndSubStatusCodes;

    public void addStatusAndSubStatusCode(int statusCode, int subStatusCode) {
        if (statusAndSubStatusCodes == null) {
            statusAndSubStatusCodes = Collections.synchronizedList(new ArrayList<>());
        }
        int[] statusAndSubStatusCode = {statusCode, subStatusCode};
        statusAndSubStatusCodes.add(statusAndSubStatusCode);
    }

    public List<int[]> getStatusAndSubStatusCodes() {
        return statusAndSubStatusCodes;
    }

    public int getRetryCount() {
        if (this.statusAndSubStatusCodes != null) {
            return this.statusAndSubStatusCodes.size();
        }

        return 0;
    }

    public long getRetryLatency() {
        if (this.retryStartTime != null && this.retryEndTime != null && this.statusAndSubStatusCodes != null) {
            return Duration.between(this.retryStartTime, this.retryEndTime).toMillis();
        } else {
            return 0;
        }
    }

    public void updateEndTime() {
        this.retryEndTime = Instant.now();
    }

    public void captureStartTimeIfNotSet() {
        if (this.retryStartTime == null) {
            this.retryStartTime = Instant.now();
        }
    }

    public Instant getRetryStartTime() {
        return retryStartTime;
    }
}
