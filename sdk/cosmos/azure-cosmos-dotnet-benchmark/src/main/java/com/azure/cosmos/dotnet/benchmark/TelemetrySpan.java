package com.azure.cosmos.dotnet.benchmark;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class TelemetrySpan implements AutoCloseable {
    private static final AtomicInteger latencyIndex = new AtomicInteger();
    private static boolean includePercentiles = false;
    private static double[] latencyHistogram;
    private static boolean preparedForCalcualtions = false;

    private long startedAtInNanos;
    private final boolean disableTelemetry;

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

    public void start() {
        this.startedAtInNanos = System.nanoTime();
    }

    public static void setIncludePercentiles(boolean includePercentiles) {
        TelemetrySpan.includePercentiles = includePercentiles;
    }

    private static void recordLatency(double elapsedTimeInMs) {
        int index = latencyIndex.incrementAndGet();
        latencyHistogram[index] = elapsedTimeInMs;
    }

    public static void resetLatencyHistogram(int totalNumberOfIterations) {
        latencyHistogram = new double[totalNumberOfIterations];
        latencyIndex.set(-1);
        preparedForCalcualtions = false;
    }

    public static TelemetrySpan createNew(
        boolean disableTelemetry) {

        return new TelemetrySpan(
            disableTelemetry);
    }

    public static void prepareForCalculations() {
        double[] temp = new double[latencyIndex.get() + 1];
        System.arraycopy(latencyHistogram, 0, temp, 0, temp.length);

        Arrays.sort(temp);

        preparedForCalcualtions = true;
        latencyHistogram = temp;
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
}
