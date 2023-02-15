// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.implementation;

import com.azure.ai.textanalytics.models.WeightResolution;
import com.azure.ai.textanalytics.models.WeightUnit;

public final class WeightResolutionPropertiesHelper {
    private static WeightResolutionAccessor accessor;

    private WeightResolutionPropertiesHelper() { }

    /**
     * Type defining the methods to set the non-public properties of an {@link WeightResolution} instance.
     */
    public interface WeightResolutionAccessor {
        void setUnit(WeightResolution weightResolution, WeightUnit unit);
        void setValue(WeightResolution weightResolution, double value);
    }

    /**
     * The method called from {@link WeightResolution} to set it's accessor.
     *
     * @param weightResolutionAccessor The accessor.
     */
    public static void setAccessor(final WeightResolutionAccessor weightResolutionAccessor) {
        accessor = weightResolutionAccessor;
    }

    public static void setUnit(WeightResolution weightResolution, WeightUnit unit) {
        accessor.setUnit(weightResolution, unit);
    }

    public static void setValue(WeightResolution weightResolution, double value) {
        accessor.setValue(weightResolution, value);
    }
}
