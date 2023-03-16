// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.ai.textanalytics.implementation.NumericRangeResolutionPropertiesHelper;
import com.azure.core.annotation.Immutable;

/** represents the resolution of numeric intervals. */
@Immutable
public final class NumericRangeResolution extends BaseResolution {
    /*
     * The kind of range that the resolution object represents.
     */
    private RangeKind rangeKind;

    /*
     * The beginning value of  the interval.
     */
    private double minimum;

    /*
     * The ending value of the interval.
     */
    private double maximum;

    static {
        NumericRangeResolutionPropertiesHelper.setAccessor(
            new NumericRangeResolutionPropertiesHelper.NumericRangeResolutionAccessor() {
                @Override
                public void setRangeKind(NumericRangeResolution numericRangeResolution, RangeKind rangeKind) {
                    numericRangeResolution.setRangeKind(rangeKind);
                }

                @Override
                public void setMinimum(NumericRangeResolution numericRangeResolution, double minimum) {
                    numericRangeResolution.setMinimum(minimum);
                }

                @Override
                public void setMaximum(NumericRangeResolution numericRangeResolution, double maximum) {
                    numericRangeResolution.setMaximum(maximum);
                }
            });
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

    @Override
    public ResolutionKind getType() {
        return ResolutionKind.NUMERIC_RANGE_RESOLUTION;
    }

    private void setRangeKind(RangeKind rangeKind) {
        this.rangeKind = rangeKind;
    }

    private void setMinimum(double minimum) {
        this.minimum = minimum;
    }

    private void setMaximum(double maximum) {
        this.maximum = maximum;
    }
}
