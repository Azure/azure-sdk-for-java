// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.ai.textanalytics.implementation.AgeResolutionPropertiesHelper;
import com.azure.core.annotation.Immutable;

/** Represents the Age entity resolution model. */
@Immutable
public final class AgeResolution extends BaseResolution {
    /*
     * The Age Unit of measurement
     */
    private AgeUnit unit;

    /*
     * The numeric value that the extracted text denotes.
     */
    private double value;

    static {
        AgeResolutionPropertiesHelper.setAccessor(new AgeResolutionPropertiesHelper.AgeResolutionAccessor() {
            @Override
            public void setUnit(AgeResolution ageResolution, AgeUnit unit) {
                ageResolution.setUnit(unit);
            }

            @Override
            public void setValue(AgeResolution ageResolution, double value) {
                ageResolution.setValue(value);
            }
        });
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

    private void setUnit(AgeUnit unit) {
        this.unit = unit;
    }

    private void setValue(double value) {
        this.value = value;
    }
}
