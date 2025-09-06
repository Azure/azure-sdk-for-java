// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.query.metrics;


import com.azure.cosmos.implementation.apachecommons.lang.time.StopWatch;

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
     * which in turn starts the stopwatch for response time and turnaround time.
     */
    public void ready() {
        startStopWatch(this.turnaroundTimeStopwatch);
        startStopWatch(this.responseTimeStopwatch);
    }

    public void start() {
        synchronized (this.runTimeStopwatch) {
            if (this.runTimeStopwatch.isStarted()) {
                return;
            }
            if (!this.responded) {
                // This is the first time the process got a response, so the response time stopwatch needs to stop.
                stopStopWatch(this.responseTimeStopwatch);
                this.responded = true;
            }
            this.runTimeStopwatch.reset();
            this.runTimeStopwatch.start();
        }
    }

    public void stop() {
        synchronized (this.runTimeStopwatch) {
            if (!this.runTimeStopwatch.isStarted()) {
                return;
            }
            this.runTimeStopwatch.stop();
            this.numPreemptions++;
        }
    }

    public void terminate() {
        stopStopWatch(this.turnaroundTimeStopwatch);
        stopStopWatch(this.responseTimeStopwatch);
    }

    private void startStopWatch(StopWatch stopwatch) {
        synchronized (stopwatch) {
            if (stopwatch.isStarted()) {
                return; // idempotent start
            }
            stopwatch.start();
        }
    }

    private void stopStopWatch(StopWatch stopwatch) {
        synchronized (stopwatch) {
            if (!stopwatch.isStarted()) {
                return; // idempotent stop
            }
            stopwatch.stop();
        }
    }
}
