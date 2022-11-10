// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.ai.textanalytics.implementation.WeightResolutionPropertiesHelper;
import com.azure.core.annotation.Immutable;

/** Represents the weight entity resolution model. */
@Immutable
public final class WeightResolution extends BaseResolution {
    /*
     * The weight Unit of measurement.
     */
    private WeightUnit unit;

    /*
     * The numeric value that the extracted text denotes.
     */
    private double value;

    static {
        WeightResolutionPropertiesHelper.setAccessor(new WeightResolutionPropertiesHelper.WeightResolutionAccessor() {
            @Override
            public void setUnit(WeightResolution weightResolution, WeightUnit unit) {
                weightResolution.setUnit(unit);
            }

            @Override
            public void setValue(WeightResolution weightResolution, double value) {
                weightResolution.setValue(value);
            }
        });
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

    private void setUnit(WeightUnit unit) {
        this.unit = unit;
    }

    private void setValue(double value) {
        this.value = value;
    }
}
