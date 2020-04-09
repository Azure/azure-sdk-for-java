// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.models;

/**
 * A list of references to the text elements constituting the {@link FieldValue}.
 */
public abstract class Element extends RawItem {

    /**
     * Constructs an {@code Element}.
     *
     * @param text Text content of the extracted field.
     * @param boundingBox List of {@link Point points} specifying relative coordinates of the element.
     */
    Element(String text, BoundingBox boundingBox) {
        super(text, boundingBox);
    }

    abstract ElementType getType();
}
