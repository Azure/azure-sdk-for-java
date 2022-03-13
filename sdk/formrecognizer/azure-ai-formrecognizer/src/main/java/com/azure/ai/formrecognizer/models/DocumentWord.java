// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.models;

import com.azure.ai.formrecognizer.implementation.util.DocumentWordHelper;

import java.util.List;

/**
 * A word object consisting of a contiguous sequence of characters. For non-space delimited languages, such as Chinese,
 * Japanese, and Korean, each character is represented as its own word.
 */
public final class DocumentWord {
    /*
     * Text content of the word.
     */
    private String content;

    /*
     * Bounding box of the word.
     */
    private List<Float> boundingBox;

    /*
     * Location of the word in the reading order concatenated content.
     */
    private DocumentSpan span;

    /*
     * Confidence of correctly extracting the word.
     */
    private float confidence;

    /**
     * Get the content property: Text content of the word.
     *
     * @return the content value.
     */
    public String getContent() {
        return this.content;
    }

    /**
     * Set the content property: Text content of the word.
     *
     * @param content the content value to set.
     * @return the DocumentWord object itself.
     */
    void setContent(String content) {
        this.content = content;
    }

    /**
     * Get the boundingBox property: Bounding box of the word.
     *
     * @return the boundingBox value.
     */
    public List<Float> getBoundingBox() {
        return this.boundingBox;
    }

    /**
     * Set the boundingBox property: Bounding box of the word.
     *
     * @param boundingBox the boundingBox value to set.
     * @return the DocumentWord object itself.
     */
    void setBoundingBox(List<Float> boundingBox) {
        this.boundingBox = boundingBox;
    }

    /**
     * Get the span property: Location of the word in the reading order concatenated content.
     *
     * @return the span value.
     */
    public DocumentSpan getSpan() {
        return this.span;
    }

    /**
     * Set the span property: Location of the word in the reading order concatenated content.
     *
     * @param span the span value to set.
     * @return the DocumentWord object itself.
     */
    void setSpan(DocumentSpan span) {
        this.span = span;
    }

    /**
     * Get the confidence property: Confidence of correctly extracting the word.
     *
     * @return the confidence value.
     */
    public float getConfidence() {
        return this.confidence;
    }

    /**
     * Set the confidence property: Confidence of correctly extracting the word.
     *
     * @param confidence the confidence value to set.
     * @return the DocumentWord object itself.
     */
    void setConfidence(float confidence) {
        this.confidence = confidence;
    }

    static {
        DocumentWordHelper.setAccessor(new DocumentWordHelper.DocumentWordAccessor() {
            @Override
            public void setBoundingBox(DocumentWord documentWord, List<Float> boundingBox) {
                documentWord.setBoundingBox(boundingBox);
            }

            @Override
            public void setContent(DocumentWord documentWord, String content) {
                documentWord.setContent(content);
            }

            @Override
            public void setSpan(DocumentWord documentWord, DocumentSpan span) {
                documentWord.setSpan(span);
            }

            @Override
            public void setConfidence(DocumentWord documentWord, float confidence) {
                documentWord.setConfidence(confidence);
            }
        });
    }
}
