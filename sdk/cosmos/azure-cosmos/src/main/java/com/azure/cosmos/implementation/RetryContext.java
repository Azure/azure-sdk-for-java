// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.List;

public class RetryContext {

    @JsonIgnore
    public List<int[]> directRetrySpecificStatusAndSubStatusCodes;
    @JsonIgnore
    public List<int[]> genericRetrySpecificStatusAndSubStatusCodes;

    @JsonIgnore
    public ZonedDateTime retryStartTime;
    @JsonIgnore
    public ZonedDateTime retryEndTime;

    public int retryCount;

    public List<int[]> statusAndSubStatusCodes;

    public RetryContext() {
    }

    public RetryContext(RetryContext retryContext) {
        if (retryContext != null) {
            this.retryCount = retryContext.retryCount;
            this.statusAndSubStatusCodes = retryContext.statusAndSubStatusCodes;
            if(this.retryStartTime == null) {
                this.retryStartTime = retryContext.retryStartTime;
            }
            this.retryEndTime = retryContext.retryEndTime;
        }
    }

    public long getRetryLatency(){
        if(this.retryStartTime != null && this.retryEndTime != null) {
            return Duration.between(this.retryStartTime, this.retryEndTime).toMillis();
        } else {
            return 0;
        }
    }
}
