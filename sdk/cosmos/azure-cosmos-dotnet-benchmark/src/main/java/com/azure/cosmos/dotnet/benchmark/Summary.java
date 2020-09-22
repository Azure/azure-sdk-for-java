package com.azure.cosmos.dotnet.benchmark;

import org.fusesource.jansi.Ansi;

class Summary {
    private long successfulOperationsCount;
    private long failedOperationsCount;
    private double ruCharges;
    private double elapsedTimeInMs;

    public Summary() {
        this(0,0, 0);
    }

    public Summary(
        long successfulOperationsCount,
        long failedOperationsCount,
        double ruCharges) {

        this.successfulOperationsCount = successfulOperationsCount;
        this.failedOperationsCount = failedOperationsCount;
        this.ruCharges = ruCharges;
    }

    public long getTotalOperationsCount() {
        return this.successfulOperationsCount + this.failedOperationsCount;
    }

    public Summary setElapsedTimeInMs(double elapsedTimeInMs) {
        this.elapsedTimeInMs = elapsedTimeInMs;

        return this;
    }

    public Summary add(Summary otherSummary) {
        this.successfulOperationsCount += otherSummary.successfulOperationsCount;
        this.failedOperationsCount += otherSummary.failedOperationsCount;
        this.ruCharges += otherSummary.ruCharges;
        this.elapsedTimeInMs += otherSummary.elapsedTimeInMs;

        return this;
    }

    public Summary subtract(Summary otherSummary) {
        this.successfulOperationsCount -= otherSummary.successfulOperationsCount;
        this.failedOperationsCount -= otherSummary.failedOperationsCount;
        this.ruCharges -= otherSummary.ruCharges;
        this.elapsedTimeInMs -= otherSummary.elapsedTimeInMs;

        return this;
    }

    public double getRps() {
        long total = this.getTotalOperationsCount();
        return Math.min(total / this.elapsedTimeInMs * 1000, total);
    }

    public double getRups() {
        return Math.min(this.ruCharges / this.elapsedTimeInMs * 1000, this.ruCharges);
    }

    private String getPrintableString(long globalTotal) {
        return String.format(
            "Stats, total: %,d   success: %,d   fail: %,d   RPs: %.2f   RUps: %.2f",
            globalTotal,
            this.successfulOperationsCount,
            this.failedOperationsCount,
            this.getRps(),
            this.getRups());
    }

    public void print(long globalTotal) {
        Utility.traceInformation(this.getPrintableString(globalTotal));
    }

    public void print(long globalTotal, Ansi.Color color) {
        Utility.traceInformation(this.getPrintableString(globalTotal), color);
    }
}
