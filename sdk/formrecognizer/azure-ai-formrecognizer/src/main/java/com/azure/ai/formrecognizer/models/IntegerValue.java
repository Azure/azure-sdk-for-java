// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.models;

import java.util.List;

/**
 * The IntegerValue model.
 */
public class IntegerValue extends FieldValue<Integer> {

    /*
     * Integer value.
     */
    private Integer valueInteger;

    /**
     * Constructs an IntegerValue.
     *
     * @param text The text content of the extracted field.
     * @param boundingBox Bounding box of the field value.
     * @param valueInteger Integer value.
     */
    public IntegerValue(String text, BoundingBox boundingBox, Integer valueInteger) {
        super(text, boundingBox);
        this.valueInteger = valueInteger;
    }

    @Override
    public Integer getValue() {
        return this.valueInteger;
    }

    @Override
    public void setValue(Integer value) {
        this.valueInteger = value;
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
