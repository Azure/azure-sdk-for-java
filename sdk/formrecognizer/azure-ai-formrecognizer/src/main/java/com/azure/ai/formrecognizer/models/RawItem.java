// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.models;

import com.azure.core.annotation.Immutable;

/**
 * The RawItem model.
 */
@Immutable
public class RawItem {
    /*
     * BoundingBox specifying relative coordinates of the element.
     */
    private final BoundingBox boundingBox;
    /*
     * Text content of the extracted field.
     */
    private final String text;

    /**
     * Creates raw OCR item.
     *
     * @param text The text content of ExtractedField.
     * @param boundingBox The BoundingBox of ExtractedField.
     */
    RawItem(final String text, final BoundingBox boundingBox) {
        this.boundingBox = boundingBox;
        this.text = text;
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
}
