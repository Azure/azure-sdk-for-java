// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.models;

import java.util.List;

/**
 * Class to represent the Float value for
 * {@link com.azure.ai.formrecognizer.implementation.models.FieldValue#getValueString()}
 */
public class StringValue extends FieldValue<String> {
    /*
     * String value.
     */
    private String valueString;

    /**
     * Constructs a StringValue.
     *
     * @param text The text content of the extracted field.
     * @param boundingBox Bounding box of the field value.
     * @param valueString String value.
     */
    public StringValue(String text, BoundingBox boundingBox, String valueString) {
        super(text, boundingBox);
        this.valueString = valueString;
    }

    @Override
    public String getValue() {
        return this.valueString;
    }

    @Override
    public void setValue(String value) {
        this.valueString = value;
    }

    @Override
    public List<Element> getElements() {
        return super.getElements();
    }

    @Override
    public Float getConfidence() {
        return super.getConfidence();
    }
}
