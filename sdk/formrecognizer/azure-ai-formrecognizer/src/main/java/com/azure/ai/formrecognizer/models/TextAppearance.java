// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.models;

import com.azure.ai.formrecognizer.implementation.TextAppearanceHelper;

/** The TextAppearance model representing the appearance of the text line. */
public final class TextAppearance {
    /*
     * An object representing the style of the text line.
     */
    private TextStyle style;

    static {
        TextAppearanceHelper.setAccessor(new TextAppearanceHelper.TextAppearanceAccessor() {
            @Override
            public void setStyle(TextAppearance textAppearance, TextStyle textStyle) {
                textAppearance.setStyle(textStyle);
            }
        });
    }

    /**
     * Get the style of the text line.
     *
     * @return the style value.
     */
    public TextStyle getStyle() {
        return this.style;
    }

    /**
     * Private setter to set an object representing the style of the text line.
     *
     * @param style the style value to set.
     * @return the Appearance object itself.
     */
    private TextAppearance setStyle(TextStyle style) {
        this.style = style;
        return this;
    }
}
