// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.implementation;

import com.azure.ai.textanalytics.models.OrdinalResolution;
import com.azure.ai.textanalytics.models.RelativeTo;

public final class OrdinalResolutionPropertiesHelper {
    private static OrdinalResolutionAccessor accessor;

    private OrdinalResolutionPropertiesHelper() { }

    /**
     * Type defining the methods to set the non-public properties of an {@link OrdinalResolution} instance.
     */
    public interface OrdinalResolutionAccessor {
        void setOffset(OrdinalResolution ordinalResolution, String offset);
        void setRelativeTo(OrdinalResolution ordinalResolution, RelativeTo relativeTo);
        void setValue(OrdinalResolution ordinalResolution, String value);
    }

    /**
     * The method called from {@link OrdinalResolution} to set it's accessor.
     *
     * @param ordinalResolutionAccessor The accessor.
     */
    public static void setAccessor(final OrdinalResolutionAccessor ordinalResolutionAccessor) {
        accessor = ordinalResolutionAccessor;
    }

    public static void setOffset(OrdinalResolution ordinalResolution, String offset) {
        accessor.setOffset(ordinalResolution, offset);
    }

    public static void setRelativeTo(OrdinalResolution ordinalResolution, RelativeTo relativeTo) {
        accessor.setRelativeTo(ordinalResolution, relativeTo);
    }

    public static void setValue(OrdinalResolution ordinalResolution, String value) {
        accessor.setValue(ordinalResolution, value);
    }
}
