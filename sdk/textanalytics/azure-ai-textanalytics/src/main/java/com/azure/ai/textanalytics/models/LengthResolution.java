// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

/** Represents the length entity resolution model. */
public final class LengthResolution extends BaseResolution {
    /*
     * The length Unit of measurement
     */
    private final LengthUnit unit;

    /*
     * The numeric value that the extracted text denotes.
     */
    private final double value;

    /**
     * Create a length entity resolution model.
     *
     * @param unit The length Unit of measurement.
     * @param value The numeric value that the extracted text denotes.
     */
    public LengthResolution(LengthUnit unit, double value) {
        this.unit = unit;
        this.value = value;
    }

    /**
     * Get the unit property: The length Unit of measurement.
     *
     * @return the unit value.
     */
    public LengthUnit getUnit() {
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
