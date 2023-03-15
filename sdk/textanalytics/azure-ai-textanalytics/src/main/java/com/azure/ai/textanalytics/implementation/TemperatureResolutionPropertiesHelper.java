// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.implementation;


import com.azure.ai.textanalytics.models.TemperatureResolution;
import com.azure.ai.textanalytics.models.TemperatureUnit;

public final class TemperatureResolutionPropertiesHelper {
    private static TemperatureResolutionAccessor accessor;

    private TemperatureResolutionPropertiesHelper() { }

    /**
     * Type defining the methods to set the non-public properties of an {@link TemperatureResolution} instance.
     */
    public interface TemperatureResolutionAccessor {
        void setUnit(TemperatureResolution temperatureResolution, TemperatureUnit unit);
        void setValue(TemperatureResolution temperatureResolution, double value);
    }

    /**
     * The method called from {@link TemperatureResolution} to set it's accessor.
     *
     * @param speedResolutionAccessor The accessor.
     */
    public static void setAccessor(final TemperatureResolutionAccessor speedResolutionAccessor) {
        accessor = speedResolutionAccessor;
    }

    public static void setUnit(TemperatureResolution temperatureResolution, TemperatureUnit unit) {
        accessor.setUnit(temperatureResolution, unit);
    }

    public static void setValue(TemperatureResolution temperatureResolution, double value) {
        accessor.setValue(temperatureResolution, value);
    }
}
