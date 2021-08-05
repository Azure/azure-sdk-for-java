// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.models;

import com.azure.ai.formrecognizer.implementation.FormLineHelper;
import com.azure.core.annotation.Immutable;

import java.util.Collections;
import java.util.List;

/**
 * Represents a line of text and its appearance.
 */
@Immutable
public final class FormLine extends FormElement {

    /*
     * List of words in the text line.
     */
    private final List<FormWord> words;

    /*
     * Line text appearance properties.
     */
    private TextAppearance appearance;

    static {
        FormLineHelper.setAccessor(new FormLineHelper.FormLineAccessor() {
            @Override
            public void setAppearance(FormLine formLine, TextAppearance textAppearance) {
                formLine.setAppearance(textAppearance);
            }
        });
    }

    /**
     * Creates raw OCR item.
     * When includeFieldElements is set to true, a list of recognized text lines.
     *
     * @param text The text content of recognized field.
     * @param boundingBox The BoundingBox of the recognized field.
     * @param pageNumber the page number.
     * @param words The list of word element references.
     */
    public FormLine(String text, FieldBoundingBox boundingBox, Integer pageNumber, final List<FormWord> words) {
        super(text, boundingBox, pageNumber);
        this.words = words == null ? null : Collections.unmodifiableList(words);
    }

    /**
     * Get the list of words in the text line.
     *
     * @return the unmodifiable list of words in the {@code FormLine}.
     */
    public List<FormWord> getWords() {
        return this.words;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public FieldBoundingBox getBoundingBox() {
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
    public int getPageNumber() {
        return super.getPageNumber();
    }

    /**
     * The private setter to set the appearance property
     * via {@link FormLineHelper.FormLineAccessor}.
     *
     * @param appearance the appearance text line.
     */
    private FormLine setAppearance(TextAppearance appearance) {
        this.appearance = appearance;
        return this;
    }

    /**
     * Get the appearance of the text line.
     *
     * @return the appearance of the text line.
     */
    public TextAppearance getAppearance() {
        return appearance;
    }
}
