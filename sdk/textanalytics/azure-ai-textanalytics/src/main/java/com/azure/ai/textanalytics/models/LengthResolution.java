// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.ai.textanalytics.implementation.LengthResolutionPropertiesHelper;
import com.azure.core.annotation.Immutable;

/** Represents the length entity resolution model. */
@Immutable
public final class LengthResolution extends BaseResolution {
    /*
     * The length Unit of measurement
     */
    private LengthUnit unit;

    /*
     * The numeric value that the extracted text denotes.
     */
    private double value;

    static {
        LengthResolutionPropertiesHelper.setAccessor(new LengthResolutionPropertiesHelper.LengthResolutionAccessor() {
            @Override
            public void setUnit(LengthResolution lengthResolution, LengthUnit unit) {
                lengthResolution.setUnit(unit);
            }

            @Override
            public void setValue(LengthResolution lengthResolution, double value) {
                lengthResolution.setValue(value);
            }
        });
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

    @Override
    public ResolutionKind getType() {
        return ResolutionKind.LENGTH_RESOLUTION;
    }

    private void setUnit(LengthUnit unit) {
        this.unit = unit;
    }

    private void setValue(double value) {
        this.value = value;
    }
}
