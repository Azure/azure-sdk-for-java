// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.dotnet.benchmark;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class TelemetrySpan implements AutoCloseable {
    private static final AtomicInteger latencyIndex = new AtomicInteger();
    private static boolean includePercentiles = false;
    private static double[] latencyHistogram;
    private static boolean preparedForCalcualtions = false;
    private final boolean disableTelemetry;
    private long startedAtInNanos;

    private TelemetrySpan(
        boolean disableTelemetry) {

        this.disableTelemetry = disableTelemetry;
    }

    @Override
    public void close() {
        long stoppedAtInNanos = System.nanoTime();
        if (!this.disableTelemetry) {
            if (TelemetrySpan.includePercentiles) {
                recordLatency(
                    TimeUnit.MILLISECONDS.convert(
                        stoppedAtInNanos - this.startedAtInNanos,
                        TimeUnit.NANOSECONDS));
            }
        }
    }

    public static TelemetrySpan createNew(
        boolean disableTelemetry) {

        return new TelemetrySpan(
            disableTelemetry);
    }

    public static double getLatencyPercentile(double percentile) {
        if (!preparedForCalcualtions) {
            throw new IllegalStateException("Must prepare for calculations first");
        }

        if (percentile < 0 || percentile > 1) {
            throw new IllegalArgumentException("percentile out of range");
        }

        int index = (int)(latencyHistogram.length * percentile);
        return latencyHistogram[index];
    }

    public static void prepareForCalculations() {
        double[] temp = new double[latencyIndex.get() + 1];
        System.arraycopy(latencyHistogram, 0, temp, 0, temp.length);

        Arrays.sort(temp);

        preparedForCalcualtions = true;
        latencyHistogram = temp;
    }

    public static void resetLatencyHistogram(int totalNumberOfIterations) {
        latencyHistogram = new double[totalNumberOfIterations];
        latencyIndex.set(-1);
        preparedForCalcualtions = false;
    }

    public static void setIncludePercentiles(boolean includePercentiles) {
        TelemetrySpan.includePercentiles = includePercentiles;
    }

    public void start() {
        this.startedAtInNanos = System.nanoTime();
    }

    private static void recordLatency(double elapsedTimeInMs) {
        int index = latencyIndex.incrementAndGet();
        latencyHistogram[index] = elapsedTimeInMs;
    }
}
