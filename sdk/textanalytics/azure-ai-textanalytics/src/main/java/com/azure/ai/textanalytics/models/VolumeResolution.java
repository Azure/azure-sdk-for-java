// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

/** Represents the volume entity resolution model. */
public final class VolumeResolution extends BaseResolution {
    /*
     * The Volume Unit of measurement
     */
    private final VolumeUnit unit;

    /*
     * The numeric value that the extracted text denotes.
     */
    private final double value;

    /**
     * Creates the volume entity resolution model.
     *
     * @param unit The Volume Unit of measurement.
     * @param value The numeric value that the extracted text denotes.
     */
    public VolumeResolution(VolumeUnit unit, double value) {
        this.unit = unit;
        this.value = value;
    }

    /**
     * Get the unit property: The Volume Unit of measurement.
     *
     * @return the unit value.
     */
    public VolumeUnit getUnit() {
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
