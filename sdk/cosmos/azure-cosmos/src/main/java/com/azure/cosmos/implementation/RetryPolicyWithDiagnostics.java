// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation;

import java.time.Duration;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

public abstract class RetryPolicyWithDiagnostics implements IRetryPolicy{

     private volatile int retriesCountForDiagnostics;
     private volatile ZonedDateTime retryStartTime;
     private volatile ZonedDateTime retryEndTime;
     private volatile List<int[]> statusAndSubStatusCodes;

    @Override
    public ZonedDateTime getStartTime() {
        return retryStartTime;
    }

    @Override
    public ZonedDateTime getEndTime() {
        return retryEndTime;
    }

    @Override
    public void addStatusAndSubStatusCode(Integer index, int statusCode, int subStatusCode) {
        if(statusAndSubStatusCodes == null) {
            statusAndSubStatusCodes = new ArrayList<>();
        }
        int[] statusAndSubStatusCode = {statusCode, subStatusCode};
        if(index == null) {
            statusAndSubStatusCodes.add(statusAndSubStatusCode);
        }else {
            statusAndSubStatusCodes.add(index,statusAndSubStatusCode);
        }
    }

    @Override
    public List<int[]> getStatusAndSubStatusCodes(){
        return statusAndSubStatusCodes;
    }

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
