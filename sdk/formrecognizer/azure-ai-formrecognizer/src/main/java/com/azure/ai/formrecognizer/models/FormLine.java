// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.models;

import com.azure.core.annotation.Immutable;
import com.azure.core.util.IterableStream;

/**
 * The FormLine model.
 */
@Immutable
public final class FormLine extends FormContent {

    /*
     * List of words in the text line.
     */
    private final IterableStream<FormWord> formWords;

    /**
     * Creates raw OCR item.
     * When includeTextDetails is set to true, a list of recognized text lines.
     *
     * @param text The text content of recognized field.
     * @param boundingBox The BoundingBox of the recognized field.
     * @param pageNumber the page number.
     * @param formWords The list of word element references.
     */
    public FormLine(String text, BoundingBox boundingBox, Integer pageNumber,
        final IterableStream<FormWord> formWords) {
        super(text, boundingBox, pageNumber, TextContentType.LINE);
        this.formWords = formWords;
    }

    /**
     * Get the words property: List of words in the text line.
     *
     * @return the words value.
     */
    public IterableStream<FormWord> getFormWords() {
        return this.formWords;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TextContentType getTextContentType() {
        return super.getTextContentType();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BoundingBox getBoundingBox() {
        return super.getBoundingBox();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getText() {
        return super.getText();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Integer getPageNumber() {
        return super.getPageNumber();
    }
}
