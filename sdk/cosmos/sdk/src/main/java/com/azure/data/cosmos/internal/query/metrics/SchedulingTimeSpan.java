/*
 * The MIT License (MIT)
 * Copyright (c) 2018 Microsoft Corporation
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
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
