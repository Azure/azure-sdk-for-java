// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.implementation;

import com.azure.ai.textanalytics.models.InformationResolution;
import com.azure.ai.textanalytics.models.InformationUnit;

public final class InformationResolutionPropertiesHelper {
    private static InformationResolutionAccessor accessor;

    private InformationResolutionPropertiesHelper() { }

    /**
     * Type defining the methods to set the non-public properties of an {@link InformationResolution} instance.
     */
    public interface InformationResolutionAccessor {
        void setUnit(InformationResolution informationResolution, InformationUnit unit);
        void setValue(InformationResolution informationResolution, double value);
    }

    /**
     * The method called from {@link InformationResolution} to set it's accessor.
     *
     * @param informationResolutionAccessor The accessor.
     */
    public static void setAccessor(final InformationResolutionAccessor informationResolutionAccessor) {
        accessor = informationResolutionAccessor;
    }

    public static void setUnit(InformationResolution informationResolution, InformationUnit unit) {
        accessor.setUnit(informationResolution, unit);
    }

    public static void setValue(InformationResolution informationResolution, double value) {
        accessor.setValue(informationResolution, value);
    }
}
