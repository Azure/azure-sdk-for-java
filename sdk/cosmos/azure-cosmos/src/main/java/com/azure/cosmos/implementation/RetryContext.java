// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class RetryContext {
    @JsonIgnore
    public volatile Instant retryStartTime;
    @JsonIgnore
    public volatile Instant retryEndTime;
    @JsonIgnore
    public volatile boolean isCurrentExceptionCapturedInLastRetry;
    @JsonIgnore
    public volatile boolean retryCounterIncremented;

    private volatile List<int[]> statusAndSubStatusCodes;
    private volatile int retryCount;

    public void addStatusAndSubStatusCode(Integer index, int statusCode, int subStatusCode) {
        if (statusAndSubStatusCodes == null) {
            statusAndSubStatusCodes = new ArrayList<>();
        }
        int[] statusAndSubStatusCode = {statusCode, subStatusCode};
        if (index == null) {
            statusAndSubStatusCodes.add(statusAndSubStatusCode);
        } else {
            statusAndSubStatusCodes.add(index, statusAndSubStatusCode);
        }
    }

    public List<int[]> getStatusAndSubStatusCodes() {
        return statusAndSubStatusCodes;
    }

    public int getRetryCount() {
        return this.retryCount;
    }

    public void incrementRetry(){
        this.retryCount++;
    }

    public long getRetryLatency() {
        if (this.retryStartTime != null && this.retryEndTime != null) {
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
}
