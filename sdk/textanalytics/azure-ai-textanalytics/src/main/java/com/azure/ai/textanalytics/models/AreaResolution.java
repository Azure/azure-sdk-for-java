// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.ai.textanalytics.implementation.AreaResolutionPropertiesHelper;
import com.azure.core.annotation.Immutable;

/** Represents the area entity resolution model. */
@Immutable
public final class AreaResolution extends BaseResolution {
    /*
     * The area Unit of measurement
     */
    private AreaUnit unit;

    /*
     * The numeric value that the extracted text denotes.
     */
    private double value;

    static {
        AreaResolutionPropertiesHelper.setAccessor(new AreaResolutionPropertiesHelper.AreaResolutionAccessor() {
            @Override
            public void setUnit(AreaResolution areaResolution, AreaUnit unit) {
                areaResolution.setUnit(unit);
            }

            @Override
            public void setValue(AreaResolution areaResolution, double value) {
                areaResolution.setValue(value);
            }
        });
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

    private void setUnit(AreaUnit unit) {
        this.unit = unit;
    }

    private void setValue(double value) {
        this.value = value;
    }
}
