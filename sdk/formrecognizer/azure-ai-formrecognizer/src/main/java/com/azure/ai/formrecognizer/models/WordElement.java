// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.models;

import com.azure.core.annotation.Immutable;

/**
 * Creates an element of type {@link ElementType#WORD}
 */
@Immutable
public final class WordElement extends Element {

    /*
     * Type of the element.
     */
    private final ElementType elementType;

    /**
     * Creates an {@link Element element} of type Word.
     *
     * @param text Text content of the extracted field.
     * @param boundingBox Bounding box of the field value, if appropriate.
     */
    public WordElement(String text, BoundingBox boundingBox) {
        super(text, boundingBox);
        this.elementType = ElementType.WORD;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ElementType getType() {
        return this.elementType;
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
}
