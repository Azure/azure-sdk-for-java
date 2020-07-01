// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.models;

import com.azure.core.annotation.Immutable;

/**
 * The FormField model.
 */
@Immutable
public final class FormField {

    private final float confidence;
    private final FieldData labelData;
    private final String name;
    private final FieldValue fieldValue;
    private final FieldData valueData;

    /**
     * Constructs a FormField object.
     *
     * @param confidence The confidence of the recognized field.
     * @param labelData The text, bounding box, and field elements for the field label.
     * @param name The name the field or label.
     * @param fieldValue The value of the recognized field.
     * @param valueData The the text, bounding box, and field elements for the field value.
     */
    public FormField(final float confidence, final FieldData labelData, final String name, final FieldValue fieldValue,
        final FieldData valueData) {
        this.confidence = confidence;
        this.labelData = labelData;
        this.name = name;
        this.fieldValue = fieldValue;
        this.valueData = valueData;
    }

    /**
     * Get the estimated confidence value of the recognized field.
     *
     * @return the confidence value.
     */
    public float getConfidence() {
        return this.confidence;
    }

    /**
     * Get the text, bounding box, and field elements for the field label.
     *
     * @return the text, bounding box, and field elements for the field value.
     */
    public FieldData getLabelData() {
        return this.labelData;
    }

    /**
     * Get the name of the field in the provided document.
     *
     * @return the name of field or label.
     */
    public String getName() {
        return this.name;
    }

    /**
     * Get the value of the recognized field.
     *
     * @return Value of the recognized field.
     */
    public FieldValue getFieldValue() {
        return this.fieldValue;
    }

    /**
     * Get the text, bounding box, and field elements for the field value.
     *
     * @return the text, bounding box, and field elements for the field value.
     */
    public FieldData getValueData() {
        return this.valueData;
    }
}
