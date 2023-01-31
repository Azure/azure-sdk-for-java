// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.implementation;

import com.azure.ai.textanalytics.models.NumberKind;
import com.azure.ai.textanalytics.models.NumberResolution;

public final class NumberResolutionPropertiesHelper {
    private static NumberResolutionAccessor accessor;

    private NumberResolutionPropertiesHelper() { }

    /**
     * Type defining the methods to set the non-public properties of an {@link NumberResolution} instance.
     */
    public interface NumberResolutionAccessor {
        void setNumberKind(NumberResolution numberResolution, NumberKind numberKind);
        void setValue(NumberResolution numberResolution, double value);
    }

    /**
     * The method called from {@link NumberResolution} to set it's accessor.
     *
     * @param numberResolutionAccessor The accessor.
     */
    public static void setAccessor(final NumberResolutionAccessor numberResolutionAccessor) {
        accessor = numberResolutionAccessor;
    }

    public static void setNumberKind(NumberResolution numberResolution, NumberKind numberKind) {
        accessor.setNumberKind(numberResolution, numberKind);
    }

    public static void setValue(NumberResolution numberResolution, double value) {
        accessor.setValue(numberResolution, value);
    }
}
