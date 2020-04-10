// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.models;

import com.azure.core.annotation.Immutable;
import com.azure.core.util.IterableStream;

/**
 * The FormLine model.
 */
@Immutable
public class FormLine extends FormContent {

    /*
     * List of words in the text line.
     */
    private final IterableStream<FormWord> formWords;

    /**
     * Creates raw OCR item.
     * When includeTextDetails is set to true, a list of recognized text lines.
     *
     * @param text The text content of ExtractedField.
     * @param boundingBox The BoundingBox of ExtractedField.
     * @param pageNumber the pagenumber.
     * @param formWords The formwords
     */
    public FormLine(String text, BoundingBox boundingBox, Integer pageNumber,
        final IterableStream<FormWord> formWords) {
        super(text, boundingBox, pageNumber);
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

    @Override
    public BoundingBox getBoundingBox() {
        return super.getBoundingBox();
    }

    @Override
    public String getText() {
        return super.getText();
    }

    @Override
    public Integer getPageNumber() {
        return super.getPageNumber();
    }
}
