// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

/** Represents the information (data) entity resolution model. */
public final class InformationResolution extends BaseResolution {
    /*
     * The information (data) Unit of measurement.
     */
    private final InformationUnit unit;

    /*
     * The numeric value that the extracted text denotes.
     */
    private final double value;

    /**
     * Create an information (data) entity resolution model.
     *
     * @param unit The information (data) Unit of measurement.
     * @param value The numeric value that the extracted text denotes.
     */
    public InformationResolution(InformationUnit unit, double value) {
        this.unit = unit;
        this.value = value;
    }

    /**
     * Get the unit property: The information (data) Unit of measurement.
     *
     * @return the unit value.
     */
    public InformationUnit getUnit() {
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
