// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation;

import java.time.Duration;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

public abstract class RetryPolicyWithDiagnostics implements IRetryPolicy{

     private volatile int retriesCountForDiagnostics;
     public ZonedDateTime retryStartTime;
     public ZonedDateTime retryEndTime;


    @Override
    public int getRetryCount() {
        return this.retriesCountForDiagnostics;
    }

    @Override
    public void incrementRetry(){
        this.retriesCountForDiagnostics++;
    }

    @Override
    public Duration getRetryLatency(){
        if(this.retryStartTime != null && this.retryEndTime != null) {
            return Duration.between(this.retryStartTime, this.retryEndTime);
        } else {
            return null;
        }
    }

    @Override
    public void updateEndTime(){
        this.retryEndTime = ZonedDateTime.now(ZoneOffset.UTC);
    }

    @Override
    public void captureStartTimeIfNotSet(){
        if(this.retryStartTime == null) {
            this.retryStartTime = ZonedDateTime.now(ZoneOffset.UTC);;
        }
    }
}
