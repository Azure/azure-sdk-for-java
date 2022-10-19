// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

/** represents the resolution of numeric intervals. */
public final class NumericRangeResolution extends BaseResolution {
    /*
     * The kind of range that the resolution object represents.
     */
    private final RangeKind rangeKind;

    /*
     * The beginning value of  the interval.
     */
    private final double minimum;

    /*
     * The ending value of the interval.
     */
    private final double maximum;

    /**
     * Create a resolution of numeric intervals.
     *
     * @param rangeKind The kind of range that the resolution object represents.
     * @param minimum The beginning value of the interval.
     * @param maximum The ending value of the interval.
     */
    public NumericRangeResolution(RangeKind rangeKind, double minimum, double maximum) {
        this.rangeKind = rangeKind;
        this.minimum = minimum;
        this.maximum = maximum;
    }

    /**
     * Get the rangeKind property: The kind of range that the resolution object represents.
     *
     * @return the rangeKind value.
     */
    public RangeKind getRangeKind() {
        return this.rangeKind;
    }

    /**
     * Get the minimum property: The beginning value of the interval.
     *
     * @return the minimum value.
     */
    public double getMinimum() {
        return this.minimum;
    }

    /**
     * Get the maximum property: The ending value of the interval.
     *
     * @return the maximum value.
     */
    public double getMaximum() {
        return this.maximum;
    }
}
