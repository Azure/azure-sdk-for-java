// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

/** Represents the area entity resolution model. */
public final class AreaResolution extends BaseResolution {
    /*
     * The area Unit of measurement
     */
    private final AreaUnit unit;

    /*
     * The numeric value that the extracted text denotes.
     */
    private final double value;

    /**
     * Represents the area entity resolution model.
     *
     * @param unit The area Unit of measurement.
     * @param value The numeric value that the extracted text denotes.
     */
    public AreaResolution(AreaUnit unit, double value) {
        this.unit = unit;
        this.value = value;
    }

    /**
     * Get the unit property: The area Unit of measurement.
     *
     * @return the unit value.
     */
    public AreaUnit getUnit() {
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
