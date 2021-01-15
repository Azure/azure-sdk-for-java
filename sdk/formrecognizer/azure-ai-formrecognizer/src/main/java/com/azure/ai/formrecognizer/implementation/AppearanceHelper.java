// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.implementation;

import com.azure.ai.formrecognizer.models.Appearance;
import com.azure.ai.formrecognizer.models.Style;

/**
 * The helper class to set the non-public properties of an {@link Appearance} instance.
 */
public final class AppearanceHelper {
    private static AppearanceAccessor accessor;

    private AppearanceHelper() { }

    /**
     * Type defining the methods to set the non-public properties of an {@link Appearance} instance.
     */
    public interface AppearanceAccessor {
        void setStyle(Appearance appearance, Style style);
    }

    /**
     * The method called from {@link Appearance} to set it's accessor.
     *
     * @param styleAccessor The accessor.
     */
    public static void setAccessor(final AppearanceAccessor styleAccessor) {
        accessor = styleAccessor;
    }

    public static void setStyle(Appearance appearance, Style style) {
        accessor.setStyle(appearance, style);
    }
}
