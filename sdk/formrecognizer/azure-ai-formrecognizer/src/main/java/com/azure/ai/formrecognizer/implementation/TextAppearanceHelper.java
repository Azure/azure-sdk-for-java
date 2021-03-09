// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.implementation;

import com.azure.ai.formrecognizer.models.TextAppearance;
import com.azure.ai.formrecognizer.models.TextStyle;

/**
 * The helper class to set the non-public properties of an {@link TextAppearance} instance.
 */
public final class TextAppearanceHelper {
    private static TextAppearanceAccessor accessor;

    private TextAppearanceHelper() { }

    /**
     * Type defining the methods to set the non-public properties of an {@link TextAppearance} instance.
     */
    public interface TextAppearanceAccessor {
        void setStyle(TextAppearance textAppearance, TextStyle textStyle);
    }

    /**
     * The method called from {@link TextAppearance} to set it's accessor.
     *
     * @param styleAccessor The accessor.
     */
    public static void setAccessor(final TextAppearanceAccessor styleAccessor) {
        accessor = styleAccessor;
    }

    public static void setStyle(TextAppearance textAppearance, TextStyle textStyle) {
        accessor.setStyle(textAppearance, textStyle);
    }
}
