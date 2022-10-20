// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.core.annotation.Immutable;

/** Represents the Age entity resolution model. */
@Immutable
public final class AgeResolution extends BaseResolution {
    /*
     * The Age Unit of measurement
     */
    private final AgeUnit unit;

    /*
     * The numeric value that the extracted text denotes.
     */
    private final double value;

    /**
     * Create an Age entity resolution model.
     *
     * @param unit The Age Unit of measurement.
     * @param value The numeric value that the extracted text denotes.
     */
    public AgeResolution(AgeUnit unit, double value) {
        this.unit = unit;
        this.value = value;
    }

    /**
     * Get the unit property: The Age Unit of measurement.
     *
     * @return the unit value.
     */
    public AgeUnit getUnit() {
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
