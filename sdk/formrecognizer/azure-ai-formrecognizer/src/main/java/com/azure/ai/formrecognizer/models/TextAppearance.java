// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.models;

import com.azure.ai.formrecognizer.implementation.TextAppearanceHelper;
import com.azure.core.annotation.Immutable;

/**
 * The TextAppearance model representing the appearance of the text line.
 */
@Immutable
public final class TextAppearance {
    /*
     * The text line style name, including handwriting and other.
     */
    private TextStyleName styleName;

    /*
     * The confidence of text line style.
     */
    private float styleConfidence;

    static {
        TextAppearanceHelper.setAccessor(new TextAppearanceHelper.TextAppearanceAccessor() {
            @Override
            public void setStyleConfidence(TextAppearance textAppearance, float styleConfidence) {
                textAppearance.setStyleConfidence(styleConfidence);
            }

            @Override
            public void setStyleName(TextAppearance textAppearance, TextStyleName styleName) {
                textAppearance.setStyleName(styleName);
            }
        });
    }

    /**
     * Get the text line style name. Possible values include handwriting and other.
     *
     * @return the style name value.
     */
    public TextStyleName getStyleName() {
        return this.styleName;
    }

    /**
     * Private setter to set the text line style name, including handwriting and other.
     *
     * @param styleName the style name value to set.
     * @return the TextAppearance object itself.
     */
    private TextAppearance setStyleName(TextStyleName styleName) {
        this.styleName = styleName;
        return this;
    }

    /**
     * Get the confidence of the recognized text line style.
     *
     * @return the confidence value.
     */
    public float getStyleConfidence() {
        return this.styleConfidence;
    }

    /**
     * Private setter to set the confidence of text line style.
     *
     * @param styleConfidence the style confidence value to set.
     * @return the TextAppearance object itself.
     */
    private TextAppearance setStyleConfidence(float styleConfidence) {
        this.styleConfidence = styleConfidence;
        return this;
    }
}
