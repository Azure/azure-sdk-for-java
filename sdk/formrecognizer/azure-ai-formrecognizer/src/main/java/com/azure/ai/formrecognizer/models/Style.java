// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.models;

import com.azure.ai.formrecognizer.implementation.StyleHelper;

/** An object representing the style of the text line. */
public final class Style {
    /*
     * The text line style name, including handwriting and other.
     */
    private TextStyle name;

    /*
     * The confidence of text line style.
     */
    private float confidence;

    static {
        StyleHelper.setAccessor(new StyleHelper.StyleAccessor() {
            @Override
            public void setConfidence(Style style, float confidence) {
                style.setConfidence(confidence);
            }

            @Override
            public void setName(Style style, TextStyle name) {
                style.setName(name);
            }
        });
    }

    /**
     * Get the name property: The text line style name, including handwriting and other.
     *
     * @return the name value.
     */
    public TextStyle getName() {
        return this.name;
    }

    /**
     * Private setter to set the text line style name, including handwriting and other.
     *
     * @param name the name value to set.
     * @return the Style object itself.
     */
    private Style setName(TextStyle name) {
        this.name = name;
        return this;
    }

    /**
     * Get the confidence property: The confidence of text line style.
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
    private Style setConfidence(float confidence) {
        this.confidence = confidence;
        return this;
    }
}
