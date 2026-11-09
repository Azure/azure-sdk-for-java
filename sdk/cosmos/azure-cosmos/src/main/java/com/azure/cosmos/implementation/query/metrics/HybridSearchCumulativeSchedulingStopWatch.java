// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.query.metrics;

import com.azure.cosmos.implementation.apachecommons.lang.time.StopWatch;

public class HybridSearchCumulativeSchedulingStopWatch extends SchedulingStopwatch {
    private long cumulativeRunTime;
    private StopWatch runTimeStopwatch;
    private long numPreemptions;
    private boolean responded;

    public HybridSearchCumulativeSchedulingStopWatch() {
        super();
        this.cumulativeRunTime = 0;
        this.runTimeStopwatch = new StopWatch();
    }

    @Override
    public SchedulingTimeSpan getElapsedTime() {
        SchedulingTimeSpan parentTimeSpan = super.getElapsedTime();

        long totalRunTime = this.cumulativeRunTime;
        if (this.runTimeStopwatch.isStarted()) {
            totalRunTime += this.runTimeStopwatch.getTime();
        }

        return new SchedulingTimeSpan(
            parentTimeSpan.getTurnaroundTime(),
            parentTimeSpan.getResponseTime(),
            totalRunTime,
            parentTimeSpan.getTurnaroundTime() - totalRunTime,
            this.numPreemptions
        );
    }

    @Override
    public void start() {
        synchronized (this.runTimeStopwatch) {
            if (this.runTimeStopwatch.isStarted()) {
                return;
            }
            if (!this.responded) {
                this.responded = true;
            }
            // Don't reset - allow cumulative timing
            this.runTimeStopwatch.start();
        }
    }

    @Override
    public void stop() {
        synchronized (this.runTimeStopwatch) {
            if (!this.runTimeStopwatch.isStarted()) {
                return;
            }
            this.runTimeStopwatch.stop();
            // Add elapsed time to cumulative total
            this.cumulativeRunTime += this.runTimeStopwatch.getTime();
            // Reset for next cycle
            this.runTimeStopwatch.reset();
            this.numPreemptions++;
        }
    }
}
