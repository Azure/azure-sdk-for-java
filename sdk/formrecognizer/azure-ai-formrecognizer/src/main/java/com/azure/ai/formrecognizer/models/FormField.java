// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.models;

import com.azure.core.annotation.Immutable;

/**
 * Represents a field recognized in an input document.
 */
@Immutable
public final class FormField {

    private final float confidence;
    private final FieldData labelData;
    private final String name;
    private final FieldValue value;
    private final FieldData valueData;

    /**
     * Constructs a FormField object.
     *
     * @param name The name the field or label.
     * @param labelData The text, bounding box, and field elements for the field label.
     * @param valueData The text, bounding box, and field elements for the field value.
     * @param value The value of the recognized field.
     * @param confidence The confidence of the recognized field.
     */
    public FormField(final String name, final FieldData labelData, final FieldData valueData,
        final FieldValue value, final float confidence) {
        this.confidence = confidence;
        this.labelData = labelData;
        this.name = name;
        this.value = value;
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
    public FieldValue getValue() {
        return this.value;
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
