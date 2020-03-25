// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.models;

import com.azure.core.annotation.Immutable;

@Immutable
/**
 * Creates an element of {@link ElementType#WORD}
 */
public class LineElement extends Element {
    /*
     * Type of the element.
     */
    private ElementType elementType;

    /**
     * Creates an {@link Element element} of {@link ElementType#LINE}.
     *
     * @param text Text content of the extracted field.
     * @param boundingBox Bounding box of the field value, if appropriate.
     */
    public LineElement(String text, BoundingBox boundingBox) {
        super(text, boundingBox);
        this.elementType = ElementType.LINE;
    }

    @Override
    public ElementType getType() {
        return this.elementType;
    }

    @Override
    public BoundingBox getBoundingBox() {
        return super.getBoundingBox();
    }

    @Override
    public String getText() {
        return super.getText();
    }
}
