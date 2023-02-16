// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.implementation;

import com.azure.ai.textanalytics.models.LengthResolution;
import com.azure.ai.textanalytics.models.LengthUnit;

public final class LengthResolutionPropertiesHelper {
    private static LengthResolutionAccessor accessor;

    private LengthResolutionPropertiesHelper() { }

    /**
     * Type defining the methods to set the non-public properties of an {@link LengthResolution} instance.
     */
    public interface LengthResolutionAccessor {
        void setUnit(LengthResolution lengthResolution, LengthUnit unit);
        void setValue(LengthResolution lengthResolution, double value);
    }

    /**
     * The method called from {@link LengthResolution} to set it's accessor.
     *
     * @param lengthResolutionAccessor The accessor.
     */
    public static void setAccessor(final LengthResolutionAccessor lengthResolutionAccessor) {
        accessor = lengthResolutionAccessor;
    }

    public static void setUnit(LengthResolution lengthResolution, LengthUnit unit) {
        accessor.setUnit(lengthResolution, unit);
    }

    public static void setValue(LengthResolution lengthResolution, double value) {
        accessor.setValue(lengthResolution, value);
    }
}
