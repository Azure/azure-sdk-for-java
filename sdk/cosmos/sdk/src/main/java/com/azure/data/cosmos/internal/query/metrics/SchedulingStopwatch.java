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
