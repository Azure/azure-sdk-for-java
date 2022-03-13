// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.models;

import com.azure.ai.formrecognizer.implementation.util.DocumentStyleHelper;

import java.util.List;

/**
 * An object representing observed text styles.
 */
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
    void setIsHandwritten(Boolean isHandwritten) {
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
    void setSpans(List<DocumentSpan> spans) {
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
    void setConfidence(float confidence) {
        this.confidence = confidence;
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
        });
    }
}
