// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.throughputControl;

import com.azure.cosmos.implementation.OperationType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class ThroughputControlTrackingUnit {

    private static final Logger logger = LoggerFactory.getLogger(ThroughputControlTrackingUnit.class);

    private final OperationType operationType;
    private final AtomicInteger rejectedRequests;
    private final AtomicInteger passedRequests;
    private final AtomicReference<Double> successRuUsage;
    private final AtomicInteger successResponse;
    private final AtomicInteger failedResponse;
    private final AtomicInteger outOfCycleResponse;
    private String throughputControlCycleId;

    public ThroughputControlTrackingUnit(OperationType operationType, String throughputControlCycleId) {
        this.operationType = operationType;

        this.rejectedRequests = new AtomicInteger(0);
        this.passedRequests = new AtomicInteger(0);
        this.successRuUsage = new AtomicReference<>(0d);
        this.successResponse = new AtomicInteger(0);
        this.failedResponse = new AtomicInteger(0);
        this.outOfCycleResponse = new AtomicInteger(0);
        this.throughputControlCycleId = throughputControlCycleId;
    }

    public void reset(String newCycleId) {
        if (this.rejectedRequests.get() > 0
            || this.passedRequests.get() > 0
            || this.successResponse.get() > 0
            || this.failedResponse.get() > 0) {

            double sAvgRuPerRequest = 0.0;
            if (this.successResponse.get() != 0) {
                sAvgRuPerRequest = successRuUsage.get() / this.successResponse.get();
            }

            logger.debug(
                "[CycleId: {}, operationType: {}, rejectedCnt: {}, passedCnt: {}, sAvgRu: {}, successCnt: {}, failedCnt: {}, outOfCycleCnt: {}]",
                this.throughputControlCycleId,
                this.operationType.toString(),
                this.rejectedRequests.get(),
                this.passedRequests.get(),
                sAvgRuPerRequest,
                this.successResponse.get(),
                this.failedResponse.get(),
                this.outOfCycleResponse.get());
        }

        this.rejectedRequests.set(0);
        this.passedRequests.set(0);
        this.successRuUsage.set(0d);
        this.successResponse.set(0);
        this.failedResponse.set(0);
        this.outOfCycleResponse.set(0);
        this.throughputControlCycleId = newCycleId;
    }

    public void increasePassedRequest(){
        this.passedRequests.incrementAndGet();
    }

    public void increaseRejectedRequest(){
        this.rejectedRequests.incrementAndGet();
    }

    public void increaseSuccessResponse() {
        this.successResponse.incrementAndGet();
    }

    public void increaseFailedResponse() { this.failedResponse.incrementAndGet(); }

    public void increaseOutOfCycleResponse() { this.outOfCycleResponse.incrementAndGet(); }

    public void trackRRuUsage(double ruUsage) {
        this.successRuUsage.getAndAccumulate(ruUsage, (available, newRuUsage) -> available + newRuUsage);
    }

    public int getRejectedRequests() {
        return rejectedRequests.get();
    }

    public int getPassedRequests() {
        return passedRequests.get();
    }

    public double getSuccessRuUsage() {
        return successRuUsage.get();
    }

    public int getSuccessResponse() {
        return successResponse.get();
    }

    public int getFailedResponse() {
        return failedResponse.get();
    }

    public int getOutOfCycleResponse() {
        return outOfCycleResponse.get();
    }
}
