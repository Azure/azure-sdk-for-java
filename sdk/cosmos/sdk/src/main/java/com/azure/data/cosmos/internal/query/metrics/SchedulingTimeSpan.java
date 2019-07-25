// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.cosmos.internal.query.metrics;

public class SchedulingTimeSpan {

    /**
     * The total time taken from when the process arrives to when it ended.
     */
    private final long turnaroundTime;

    /**
     * The total latency (time) taken from when the process arrived to when the CPU actually started working on it.
     */
    private final long responseTime;

    /**
     * The total time the process spent in the running state.
     */
    private final long runTime;

    /**
     * The total time that the process spent is on the ready or waiting state.
     */
    private final long waitTime;

    /**
     * NUMBER of times the process was preempted.
     */
    private final long numPreemptions;

    public SchedulingTimeSpan(long turnaroundTime, long responseTime, long runTime, long waitTime, long numPreemptions) {
        this.turnaroundTime = turnaroundTime;
        this.responseTime = responseTime;
        this.runTime = runTime;
        this.waitTime = waitTime;
        this.numPreemptions = numPreemptions;
    }

    public long getTurnaroundTime() {
        return turnaroundTime;
    }

    public long getResponseTime() {
        return responseTime;
    }

    public long getRunTime() {
        return runTime;
    }

    public long getWaitTime() {
        return waitTime;
    }

    public long getNumPreemptions() {
        return numPreemptions;
    }
}
