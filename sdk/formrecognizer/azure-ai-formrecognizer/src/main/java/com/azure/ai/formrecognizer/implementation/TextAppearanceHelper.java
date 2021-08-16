// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.implementation;

import com.azure.ai.formrecognizer.models.TextAppearance;
import com.azure.ai.formrecognizer.models.TextStyleName;

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
        void setStyleName(TextAppearance textAppearance, TextStyleName styleName);
        void setStyleConfidence(TextAppearance textAppearance, float styleConfidence);
    }

    /**
     * The method called from {@link TextAppearance} to set it's accessor.
     *
     * @param textAppearanceAccessor The accessor.
     */
    public static void setAccessor(final TextAppearanceAccessor textAppearanceAccessor) {
        accessor = textAppearanceAccessor;
    }

    public static void setStyleName(TextAppearance textAppearance, TextStyleName styleName) {
        accessor.setStyleName(textAppearance, styleName);
    }

    public static void setStyleConfidence(TextAppearance textAppearance, float styleConfidence) {
        accessor.setStyleConfidence(textAppearance, styleConfidence);
    }
}
