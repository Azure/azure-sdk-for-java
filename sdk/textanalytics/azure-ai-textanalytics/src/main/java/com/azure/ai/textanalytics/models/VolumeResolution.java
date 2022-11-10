// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.ai.textanalytics.implementation.VolumeResolutionPropertiesHelper;
import com.azure.core.annotation.Immutable;

/** Represents the volume entity resolution model. */
@Immutable
public final class VolumeResolution extends BaseResolution {
    /*
     * The Volume Unit of measurement
     */
    private VolumeUnit unit;

    /*
     * The numeric value that the extracted text denotes.
     */
    private double value;

    static {
        VolumeResolutionPropertiesHelper.setAccessor(new VolumeResolutionPropertiesHelper.VolumeResolutionAccessor() {
            @Override
            public void setUnit(VolumeResolution volumeResolution, VolumeUnit unit) {
                volumeResolution.setUnit(unit);
            }

            @Override
            public void setValue(VolumeResolution volumeResolution, double value) {
                volumeResolution.setValue(value);
            }
        });
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

    private void setUnit(VolumeUnit unit) {
        this.unit = unit;
    }

    private void setValue(double value) {
        this.value = value;
    }
}
