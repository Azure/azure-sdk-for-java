// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.implementation;

import com.azure.ai.textanalytics.models.SpeedResolution;
import com.azure.ai.textanalytics.models.SpeedUnit;

public final class SpeedResolutionPropertiesHelper {
    private static SpeedResolutionAccessor accessor;

    private SpeedResolutionPropertiesHelper() { }

    /**
     * Type defining the methods to set the non-public properties of an {@link SpeedResolution} instance.
     */
    public interface SpeedResolutionAccessor {
        void setUnit(SpeedResolution speedResolution, SpeedUnit unit);
        void setValue(SpeedResolution speedResolution, double value);
    }

    /**
     * The method called from {@link SpeedResolution} to set it's accessor.
     *
     * @param speedResolutionAccessor The accessor.
     */
    public static void setAccessor(final SpeedResolutionAccessor speedResolutionAccessor) {
        accessor = speedResolutionAccessor;
    }

    public static void setUnit(SpeedResolution speedResolution, SpeedUnit unit) {
        accessor.setUnit(speedResolution, unit);
    }

    public static void setValue(SpeedResolution speedResolution, double value) {
        accessor.setValue(speedResolution, value);
    }
}
