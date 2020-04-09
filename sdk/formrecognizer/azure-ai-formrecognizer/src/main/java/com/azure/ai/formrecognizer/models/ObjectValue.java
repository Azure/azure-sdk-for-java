// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.models;

import java.util.List;
import java.util.Map;

/**
 * Class to represent the Array value for
 * {@link com.azure.ai.formrecognizer.implementation.models.FieldValue#getValueObject()}
 */
public class ObjectValue extends FieldValue<Map<String, FieldValue<?>>> {

    /*
     * Object value.
     */
    private final Map<String, FieldValue<?>> valueObject;

    /*
     * Type of the FieldValue.
     */
    private final FieldValueType fieldValueType;

    /**
     * Constructs a ObjectValue.
     *
     * @param text The text content of the extracted field.
     * @param boundingBox Bounding box of the field value.
     * @param valueArray Array of field values.
     * @param pageNumber The page number on which this field exists.
     * @param elements The list of reference elements when includeTextDetails is set to true.
     */
    public ObjectValue(String text, BoundingBox boundingBox, Map<String, FieldValue<?>> valueArray, 
        int pageNumber, List<FormContent> elements) {
        super(text, boundingBox, pageNumber, elements);
        this.valueObject = valueArray;
        this.fieldValueType = FieldValueType.OBJECT;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getPageNumber() {
        return super.getPageNumber();
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
    public Map<String, FieldValue<?>> getValue() {
        return this.valueObject;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public FieldValueType getType() {
        return this.fieldValueType;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<FormContent> getElements() {
        return super.getElements();
    }
}
