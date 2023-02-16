// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.implementation;

import com.azure.ai.textanalytics.models.BooleanResolution;

public final class BooleanResolutionPropertiesHelper {
    private static BooleanResolutionAccessor accessor;

    private BooleanResolutionPropertiesHelper() { }

    /**
     * Type defining the methods to set the non-public properties of an {@link BooleanResolution} instance.
     */
    public interface BooleanResolutionAccessor {
        void setValue(BooleanResolution booleanResolution, boolean value);
    }

    /**
     * The method called from {@link BooleanResolution} to set it's accessor.
     *
     * @param booleanResolutionAccessor The accessor.
     */
    public static void setAccessor(final BooleanResolutionAccessor booleanResolutionAccessor) {
        accessor = booleanResolutionAccessor;
    }

    public static void setValue(BooleanResolution booleanResolution, boolean value) {
        accessor.setValue(booleanResolution, value);
    }
}
