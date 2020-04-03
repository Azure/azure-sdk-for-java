// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.models;

public abstract class FormContent {

    /*
     * Text content of the extracted field.
     */
    private final String text;

    /*
     * The 1-based page number in the input document.
     */
    private final int pageNumber;

    /*
     * BoundingBox specifying relative coordinates of the element.
     */
    private final BoundingBox boundingBox;

    /**
     * Creates raw OCR item.
     *
     * @param text The text content of ExtractedField.
     * @param boundingBox The BoundingBox of ExtractedField.
     * @param pageNumber the 1 based page number.
     */
    FormContent(final String text, final BoundingBox boundingBox, final int pageNumber) {
        this.boundingBox = boundingBox;
        this.text = text;
        this.pageNumber = pageNumber;
    }

    /**
     * BoundingBox property of the element.
     *
     * @return the bounding box of the element.
     */
    public BoundingBox getBoundingBox() {
        return boundingBox;
    }

    /**
     * The text of the extracted item.
     *
     * @return The text of the extracted item.
     */
    public String getText() {
        return text;
    }

    /**
     * Get the 1-based page number in the input document.
     *
     * @return the page number value.
     */
    public int getPageNumber() {
        return this.pageNumber;
    }
}
