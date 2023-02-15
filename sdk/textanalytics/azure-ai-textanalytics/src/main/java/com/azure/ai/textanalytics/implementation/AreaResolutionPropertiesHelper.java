// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.implementation;

import com.azure.ai.textanalytics.models.AreaResolution;
import com.azure.ai.textanalytics.models.AreaUnit;

public final class AreaResolutionPropertiesHelper {
    private static AreaResolutionAccessor accessor;

    private AreaResolutionPropertiesHelper() { }

    /**
     * Type defining the methods to set the non-public properties of an {@link AreaResolution} instance.
     */
    public interface AreaResolutionAccessor {
        void setUnit(AreaResolution areaResolution, AreaUnit unit);
        void setValue(AreaResolution areaResolution, double value);
    }

    /**
     * The method called from {@link AreaResolution} to set it's accessor.
     *
     * @param areaResolutionAccessor The accessor.
     */
    public static void setAccessor(final AreaResolutionAccessor areaResolutionAccessor) {
        accessor = areaResolutionAccessor;
    }

    public static void setUnit(AreaResolution areaResolution, AreaUnit unit) {
        accessor.setUnit(areaResolution, unit);
    }

    public static void setValue(AreaResolution areaResolution, double value) {
        accessor.setValue(areaResolution, value);
    }
}
