// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.ai.textanalytics.implementation.SpeedResolutionPropertiesHelper;
import com.azure.core.annotation.Immutable;

/** Represents the speed entity resolution model. */
@Immutable
public final class SpeedResolution extends BaseResolution {
    /*
     * The speed Unit of measurement
     */
    private SpeedUnit unit;

    /*
     * The numeric value that the extracted text denotes.
     */
    private double value;

    static {
        SpeedResolutionPropertiesHelper.setAccessor(new SpeedResolutionPropertiesHelper.SpeedResolutionAccessor() {
            @Override
            public void setUnit(SpeedResolution speedResolution, SpeedUnit unit) {
                speedResolution.setUnit(unit);
            }

            @Override
            public void setValue(SpeedResolution speedResolution, double value) {
                speedResolution.setValue(value);
            }
        });
    }

    /**
     * Get the unit property: The speed Unit of measurement.
     *
     * @return the unit value.
     */
    public SpeedUnit getUnit() {
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

    private void setUnit(SpeedUnit unit) {
        this.unit = unit;
    }

    private void setValue(double value) {
        this.value = value;
    }
}
