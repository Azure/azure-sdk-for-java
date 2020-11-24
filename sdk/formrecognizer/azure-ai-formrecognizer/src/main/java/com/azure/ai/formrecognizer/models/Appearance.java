// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.models;

import com.azure.ai.formrecognizer.implementation.AppearanceHelper;

/** An object representing the appearance of the text line. */
public final class Appearance {
    /*
     * An object representing the style of the text line.
     */
    private Style style;

    static {
        AppearanceHelper.setAccessor(new AppearanceHelper.AppearanceAccessor() {
            @Override
            public void setStyle(Appearance appearance, Style style) {
                appearance.setStyle(style);
            }
        });
    }

    /**
     * Get the style property: An object representing the style of the text line.
     *
     * @return the style value.
     */
    public Style getStyle() {
        return this.style;
    }

    /**
     * Private setter to set an object representing the style of the text line.
     *
     * @param style the style value to set.
     * @return the Appearance object itself.
     */
    private Appearance setStyle(Style style) {
        this.style = style;
        return this;
    }
}
