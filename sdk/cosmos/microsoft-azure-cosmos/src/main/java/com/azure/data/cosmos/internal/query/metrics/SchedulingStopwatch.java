// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.cosmos.internal.query.metrics;

import org.apache.commons.lang3.time.StopWatch;

public class SchedulingStopwatch {
    private StopWatch turnaroundTimeStopwatch;
    private StopWatch responseTimeStopwatch;
    private StopWatch runTimeStopwatch;
    private long numPreemptions;
    private boolean responded;

    public SchedulingStopwatch() {
        this.turnaroundTimeStopwatch = new StopWatch();
        this.responseTimeStopwatch = new StopWatch();
        this.runTimeStopwatch = new StopWatch();
    }

    public SchedulingTimeSpan getElapsedTime() {
        return new SchedulingTimeSpan(this.turnaroundTimeStopwatch.getTime(), this.responseTimeStopwatch.getTime(),
                this.runTimeStopwatch.getTime(),
                this.turnaroundTimeStopwatch.getTime() - this.runTimeStopwatch.getTime(), this.numPreemptions);
    }

    /**
     * Tells the SchedulingStopwatch know that the process is in a state where it is ready to be worked on,
     * which in turn starts the stopwatch for for response time and turnaround time.
     */
    public void ready() {
        startStopWatch(this.turnaroundTimeStopwatch);
        startStopWatch(this.responseTimeStopwatch);
    }

    public void start() {
        if (!this.runTimeStopwatch.isStarted()) {
            if (!this.responded) {
                // This is the first time the process got a response, so the response time stopwatch needs to stop.
                this.responseTimeStopwatch.stop();
                this.responded = true;
            }
            this.runTimeStopwatch.reset();
            startStopWatch(this.runTimeStopwatch);
        }
    }

    public void stop() {
        if (this.runTimeStopwatch.isStarted()) {
            stopStopWatch(this.runTimeStopwatch);
            this.numPreemptions++;
        }
    }

    public void terminate() {
        stopStopWatch(this.turnaroundTimeStopwatch);
        stopStopWatch(this.responseTimeStopwatch);
    }

    private void startStopWatch(StopWatch stopwatch) {
        synchronized (stopwatch) {
            stopwatch.start();
        }
    }

    private void stopStopWatch(StopWatch stopwatch) {
        synchronized (stopwatch) {
            stopwatch.stop();
        }
    }
}
