// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.models;

import com.azure.core.annotation.Immutable;

/**
 * The FormField model.
 */
@Immutable
public final class FormField<T> {

    private final float confidence;
    private final FieldData labelData;
    private final String name;
    private final FieldData valueData;
    private final T value;
    private final FieldValueType valueType;

    /**
     * Constructs a FormField object.
     *
     * @param confidence The confidence of the recognized field.
     * @param labelData The text, bounding box, and field elements for the field label.
     * @param name The name the field or label.
     * @param value The value of the recognized field.
     * @param valueData The text, bounding box, and field elements for the field value.
     * @param valueType The type of the value of the recognized field.
     */
    public FormField(final float confidence, final FieldData labelData, final String name, final T value,
        final FieldData valueData, FieldValueType valueType) {
        this.confidence = confidence;
        this.labelData = labelData;
        this.name = name;
        this.value = value;
        this.valueData = valueData;
        this.valueType = valueType;
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
     * @return the value of the recognized field.
     */
    public T getValue() {
        return this.value;
    }

    /**
     * Get the text, bounding box, and field elements for the field value.
     * The type of the value of the recognized field.
     * Possible types include: 'String',
     * 'LocalDate', 'LocalTime', 'Integer', 'Float', 'Map', or 'List'.
     *
     * @return the type of the value of the field.
     */
    public FieldValueType getValueType() {
        return valueType;
    }

    /**
     * Get the text, bounding box, and text content of the field value.
     *
     * @return the text, bounding box, and field elements for the field value.
     */
    public FieldData getValueData() {
        return this.valueData;
    }
}
