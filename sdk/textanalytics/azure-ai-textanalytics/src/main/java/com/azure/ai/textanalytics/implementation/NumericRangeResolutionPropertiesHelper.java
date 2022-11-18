// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.implementation;

import com.azure.ai.textanalytics.models.NumericRangeResolution;
import com.azure.ai.textanalytics.models.RangeKind;

public final class NumericRangeResolutionPropertiesHelper {
    private static NumericRangeResolutionAccessor accessor;

    private NumericRangeResolutionPropertiesHelper() { }

    /**
     * Type defining the methods to set the non-public properties of an {@link NumericRangeResolution} instance.
     */
    public interface NumericRangeResolutionAccessor {
        void setRangeKind(NumericRangeResolution numericRangeResolution, RangeKind rangeKind);
        void setMinimum(NumericRangeResolution numericRangeResolution, double minimum);
        void setMaximum(NumericRangeResolution numericRangeResolution, double maximum);
    }

    /**
     * The method called from {@link NumericRangeResolution} to set it's accessor.
     *
     * @param numericRangeResolutionAccessor The accessor.
     */
    public static void setAccessor(final NumericRangeResolutionAccessor numericRangeResolutionAccessor) {
        accessor = numericRangeResolutionAccessor;
    }

    public static void setRangeKind(NumericRangeResolution numericRangeResolution, RangeKind rangeKind) {
        accessor.setRangeKind(numericRangeResolution, rangeKind);
    }

    public static void setMinimum(NumericRangeResolution numericRangeResolution, double minimum) {
        accessor.setMinimum(numericRangeResolution, minimum);
    }

    public static void setMaximum(NumericRangeResolution numericRangeResolution, double maximum) {
        accessor.setMaximum(numericRangeResolution, maximum);
    }
}
