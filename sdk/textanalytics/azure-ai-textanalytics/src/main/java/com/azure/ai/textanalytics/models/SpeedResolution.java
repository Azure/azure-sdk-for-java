// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

/** Represents the speed entity resolution model. */
public final class SpeedResolution extends BaseResolution {
    /*
     * The speed Unit of measurement
     */
    private final SpeedUnit unit;

    /*
     * The numeric value that the extracted text denotes.
     */
    private final double value;

    /**
     * Create a speed entity resolution model.
     *
     * @param unit The speed Unit of measurement.
     * @param value The numeric value that the extracted text denotes.
     */
    public SpeedResolution(SpeedUnit unit, double value) {
        this.unit = unit;
        this.value = value;
    }

    /**
     * Get the unit property: The speed Unit of measurement.
     *
     * @return the unit value.
     */
    public SpeedUnit getUnit() {
        return this.unit;
    }

    /**
     * Get the value property: The numeric value that the extracted text denotes.
     *
     * @return the value value.
     */
    public double getValue() {
        return this.value;
    }
}
