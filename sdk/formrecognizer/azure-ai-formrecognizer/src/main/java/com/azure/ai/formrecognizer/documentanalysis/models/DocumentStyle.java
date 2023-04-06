// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.documentanalysis.models;

import com.azure.ai.formrecognizer.documentanalysis.implementation.util.DocumentStyleHelper;
import com.azure.core.annotation.Immutable;

import java.util.List;

/**
 * An object representing observed text styles.
 */
@Immutable
public final class DocumentStyle {
    /*
     * Is content handwritten?
     */
    private Boolean isHandwritten;

    /*
     * Location of the text elements in the concatenated content the style
     * applies to.
     */
    private List<DocumentSpan> spans;

    /*
     * Confidence of correctly identifying the style.
     */
    private float confidence;

    /*
     * Visually most similar font from among the set of supported font families, with fallback fonts following CSS
     * convention (ex. 'Arial, sans-serif').
     */
    private String similarFontFamily;

    /*
     * Font style.
     */
    private FontStyle fontStyle;

    /*
     * Font weight.
     */
    private FontWeight fontWeight;

    /*
     * Foreground color in #rrggbb hexadecimal format.
     */
    private String color;

    /*
     * Background color in #rrggbb hexadecimal format..
     */
    private String backgroundColor;

    /**
     * Get the isHandwritten property: Is content handwritten?.
     *
     * @return the isHandwritten value.
     */
    public Boolean isHandwritten() {
        return this.isHandwritten;
    }

    /**
     * Set the isHandwritten property: Is content handwritten?.
     *
     * @param isHandwritten the isHandwritten value to set.
     * @return the DocumentStyle object itself.
     */
    private void setIsHandwritten(Boolean isHandwritten) {
        this.isHandwritten = isHandwritten;
    }

    /**
     * Get the spans property: Location of the text elements in the concatenated content the style applies to.
     *
     * @return the spans value.
     */
    public List<DocumentSpan> getSpans() {
        return this.spans;
    }

    /**
     * Set the spans property: Location of the text elements in the concatenated content the style applies to.
     *
     * @param spans the spans value to set.
     * @return the DocumentStyle object itself.
     */
    private void setSpans(List<DocumentSpan> spans) {
        this.spans = spans;
    }

    /**
     * Get the confidence property: Confidence of correctly identifying the style.
     *
     * @return the confidence value.
     */
    public float getConfidence() {
        return this.confidence;
    }

    /**
     * Set the confidence property: Confidence of correctly identifying the style.
     *
     * @param confidence the confidence value to set.
     * @return the DocumentStyle object itself.
     */
    private void setConfidence(float confidence) {
        this.confidence = confidence;
    }

    public Boolean getHandwritten() {
        return isHandwritten;
    }

    private void setHandwritten(Boolean handwritten) {
        isHandwritten = handwritten;
    }

    public String getSimilarFontFamily() {
        return similarFontFamily;
    }

    private void setSimilarFontFamily(String similarFontFamily) {
        this.similarFontFamily = similarFontFamily;
    }

    public FontStyle getFontStyle() {
        return fontStyle;
    }

    private void setFontStyle(FontStyle fontStyle) {
        this.fontStyle = fontStyle;
    }

    public FontWeight getFontWeight() {
        return fontWeight;
    }

    private void setFontWeight(FontWeight fontWeight) {
        this.fontWeight = fontWeight;
    }

    public String getColor() {
        return color;
    }

    private void setColor(String color) {
        this.color = color;
    }

    public String getBackgroundColor() {
        return backgroundColor;
    }

    private void setBackgroundColor(String backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    static {
        DocumentStyleHelper.setAccessor(new DocumentStyleHelper.DocumentStyleAccessor() {
            @Override
            public void setSpans(DocumentStyle documentStyle, List<DocumentSpan> spans) {
                documentStyle.setSpans(spans);
            }

            @Override
            public void setIsHandwritten(DocumentStyle documentStyle, Boolean isHandwritten) {
                documentStyle.setIsHandwritten(isHandwritten);
            }

            @Override
            public void setConfidence(DocumentStyle documentStyle, Float confidence) {
                documentStyle.setConfidence(confidence);
            }

            @Override
            public void setSimilarFontFamily(DocumentStyle documentStyle, String similarFontFamily) {
                documentStyle.setSimilarFontFamily(similarFontFamily);
            }

            @Override
            public void setFontStyle(DocumentStyle documentStyle, FontStyle fontStyle) {
                documentStyle.setFontStyle(fontStyle);
            }

            @Override
            public void setFontWeight(DocumentStyle documentStyle, FontWeight fontWeight) {
                documentStyle.setFontWeight(fontWeight);
            }

            @Override
            public void setColor(DocumentStyle documentStyle, String color) {
                documentStyle.setColor(color);
            }

            @Override
            public void setBackgroundColor(DocumentStyle documentStyle, String backgroundColor) {
                documentStyle.setBackgroundColor(backgroundColor);
            }
        });
    }
}
