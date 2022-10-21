// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.core.annotation.Immutable;

/** Represents the weight entity resolution model. */
@Immutable
public final class WeightResolution extends BaseResolution {
    /*
     * The weight Unit of measurement.
     */
    private final WeightUnit unit;

    /*
     * The numeric value that the extracted text denotes.
     */
    private final double value;

    /**
     * Create a weight entity resolution model.
     *
     * @param unit The weight Unit of measurement.
     * @param value The numeric value that the extracted text denotes.
     */
    public WeightResolution(WeightUnit unit, double value) {
        this.unit = unit;
        this.value = value;
    }

    /**
     * Get the unit property: The weight Unit of measurement.
     *
     * @return the unit value.
     */
    public WeightUnit getUnit() {
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
