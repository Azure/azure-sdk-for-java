// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.models;

import com.azure.ai.formrecognizer.implementation.TextStyleHelper;

/** The TextStyle model representing the style of the text line. */
public final class TextStyle {
    /*
     * The text line style name, including handwriting and other.
     */
    private TextStyleName name;

    /*
     * The confidence of text line style.
     */
    private float confidence;

    static {
        TextStyleHelper.setAccessor(new TextStyleHelper.StyleAccessor() {
            @Override
            public void setConfidence(TextStyle textStyle, float confidence) {
                textStyle.setConfidence(confidence);
            }

            @Override
            public void setName(TextStyle textStyle, TextStyleName name) {
                textStyle.setName(name);
            }
        });
    }

    /**
     * Get the text line style name. Possible values include handwriting and other.
     *
     * @return the name value.
     */
    public TextStyleName getName() {
        return this.name;
    }

    /**
     * Private setter to set the text line style name, including handwriting and other.
     *
     * @param name the name value to set.
     * @return the Style object itself.
     */
    private TextStyle setName(TextStyleName name) {
        this.name = name;
        return this;
    }

    /**
     * Get the confidence of the recognized text line style.
     *
     * @return the confidence value.
     */
    public float getConfidence() {
        return this.confidence;
    }

    /**
     * Private setter to set the confidence of text line style.
     *
     * @param confidence the confidence value to set.
     * @return the Style object itself.
     */
    private TextStyle setConfidence(float confidence) {
        this.confidence = confidence;
        return this;
    }
}
