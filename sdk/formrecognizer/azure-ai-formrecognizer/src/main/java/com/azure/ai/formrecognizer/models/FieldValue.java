// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.models;

/**
 * Represents the value of {@link FieldValue}.
 *
 * @param <T> Type of {@link FieldValue fieldValue}.
 */
public abstract class FieldValue<T> {

    /**
     * Gets the field value.
     *
     * @return The T field value
     */
    public abstract T getValue();

    /**
     * Gets the {@link FieldValueType type} the field value.
     *
     * @return The {@link FieldValueType type} the field value.
     */
    public abstract FieldValueType getType();
}
