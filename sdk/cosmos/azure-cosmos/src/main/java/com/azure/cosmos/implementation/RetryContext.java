// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation;

import com.fasterxml.jackson.annotation.JsonIgnore;
import reactor.util.retry.Retry;

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

    public RetryContext() {}

    public RetryContext(RetryContext toBeCloned) {
        this.retryStartTime = toBeCloned.retryStartTime;
        this.retryEndTime = toBeCloned.retryEndTime;
        if (toBeCloned.statusAndSubStatusCodes != null) {
            statusAndSubStatusCodes = Collections.synchronizedList(new ArrayList<>(toBeCloned.statusAndSubStatusCodes));
        }
    }

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

    public void merge(RetryContext other) {
        if (other == null) {
            return;
        }

        if (other.retryStartTime != null) {
            if (this.retryStartTime == null || this.retryStartTime.isAfter(other.retryStartTime)) {
                this.retryStartTime = other.retryStartTime;
            }
        }

        if (this.retryEndTime != null) {
            if (other.retryEndTime == null || this.retryEndTime.isBefore(other.retryEndTime)) {
                this.retryEndTime = other.retryEndTime;
            }
        }

        if (other.statusAndSubStatusCodes != null) {
            if (this.statusAndSubStatusCodes == null) {
                this.statusAndSubStatusCodes = other.statusAndSubStatusCodes;
            } else {
                this.statusAndSubStatusCodes.addAll(other.statusAndSubStatusCodes);
            }
        }
    }
}
