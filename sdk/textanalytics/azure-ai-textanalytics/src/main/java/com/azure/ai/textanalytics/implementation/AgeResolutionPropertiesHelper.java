// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.implementation;

import com.azure.ai.textanalytics.models.AgeResolution;
import com.azure.ai.textanalytics.models.AgeUnit;

public final class AgeResolutionPropertiesHelper {
    private static AgeResolutionAccessor accessor;

    private AgeResolutionPropertiesHelper() { }

    /**
     * Type defining the methods to set the non-public properties of an {@link AgeResolution} instance.
     */
    public interface AgeResolutionAccessor {
        void setUnit(AgeResolution ageResolution, AgeUnit unit);
        void setValue(AgeResolution ageResolution, double value);
    }

    /**
     * The method called from {@link AgeResolution} to set it's accessor.
     *
     * @param ageResolutionAccessor The accessor.
     */
    public static void setAccessor(final AgeResolutionAccessor ageResolutionAccessor) {
        accessor = ageResolutionAccessor;
    }

    public static void setUnit(AgeResolution ageResolution, AgeUnit unit) {
        accessor.setUnit(ageResolution, unit);
    }

    public static void setValue(AgeResolution ageResolution, double value) {
        accessor.setValue(ageResolution, value);
    }
}
