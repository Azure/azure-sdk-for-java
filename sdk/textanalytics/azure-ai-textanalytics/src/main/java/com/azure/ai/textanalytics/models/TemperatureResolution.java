// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

/** Represents the temperature entity resolution model. */
public final class TemperatureResolution extends BaseResolution {
    /*
     * The temperature Unit of measurement.
     */
    private final TemperatureUnit unit;

    /*
     * The numeric value that the extracted text denotes.
     */
    private final double value;

    /**
     * Create a temperature entity resolution model.
     *
     * @param unit The temperature Unit of measurement.
     * @param value The numeric value that the extracted text denotes.
     */
    public TemperatureResolution(TemperatureUnit unit, double value) {
        this.unit = unit;
        this.value = value;
    }

    /**
     * Get the unit property: The temperature Unit of measurement.
     *
     * @return the unit value.
     */
    public TemperatureUnit getUnit() {
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
