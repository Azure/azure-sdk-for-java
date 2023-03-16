// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.ai.textanalytics.implementation.TemperatureResolutionPropertiesHelper;
import com.azure.core.annotation.Immutable;

/** Represents the temperature entity resolution model. */
@Immutable
public final class TemperatureResolution extends BaseResolution {
    /*
     * The temperature Unit of measurement.
     */
    private TemperatureUnit unit;

    /*
     * The numeric value that the extracted text denotes.
     */
    private double value;

    static {
        TemperatureResolutionPropertiesHelper.setAccessor(
            new TemperatureResolutionPropertiesHelper.TemperatureResolutionAccessor() {
                @Override
                public void setUnit(TemperatureResolution temperatureResolution, TemperatureUnit unit) {
                    temperatureResolution.setUnit(unit);
                }

                @Override
                public void setValue(TemperatureResolution temperatureResolution, double value) {
                    temperatureResolution.setValue(value);
                }
            });
    }

    /**
     * Get the unit property: The temperature Unit of measurement.
     *
     * @return the unit value.
     */
    public TemperatureUnit getUnit() {
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
        return ResolutionKind.TEMPERATURE_RESOLUTION;
    }

    private void setUnit(TemperatureUnit unit) {
        this.unit = unit;
    }

    private void setValue(double value) {
        this.value = value;
    }
}
