// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.models;

import com.azure.core.util.ExpandableStringEnum;
import com.azure.core.util.logging.ClientLogger;

import java.util.Objects;

/**
 * Define enum values for FieldValue types.
 */
public final class FieldValueType extends ExpandableStringEnum<com.azure.ai.formrecognizer.models.FieldValueType> {
    private final ClientLogger logger = new ClientLogger(FieldValueType.class);

    /**
     * Static value string for FieldValueType.
     */
    public static final FieldValueType STRING = fromString("string");

    /**
     * Static value date for FieldValueType.
     */
    public static final FieldValueType DATE = fromString("date");

    /**
     * Static value time for FieldValueType.
     */
    public static final FieldValueType TIME = fromString("time");

    /**
     * Static value phone number for FieldValueType.
     */
    public static final FieldValueType PHONE_NUMBER = fromString("phoneNumber");

    /**
     * Static value float for FieldValueType.
     */
    public static final FieldValueType FLOAT = fromString("number");

    /**
     * Static value integer for FieldValueType.
     */
    public static final FieldValueType INTEGER = fromString("integer");

    /**
     * Static value list for FieldValueType.
     */
    public static final FieldValueType LIST = fromString("array");

    /**
     * Static value map for FieldValueType.
     */
    public static final FieldValueType MAP = fromString("object");

    /**
     * Parses a serialized value to a {@link FieldValueType} instance.
     *
     * @param value the serialized value to parse.
     *
     * @return the parsed FieldValueType object, or null if unable to parse.
     */
    public static FieldValueType fromString(String value) {
        return fromString(value, FieldValueType.class);
    }

    /**
     * Converts the form field value to a specific expandable string enum type.
     *
     * @param formField The recognized field value that needs to be converted.
     * @param <T> the class of the field.
     *
     * @return the converted value of the recognized field.
     * @throws UnsupportedOperationException if the {@code formField} type does not match the casting value type.
     * @throws NullPointerException if {@code formField} is {@code null}
     */
    @SuppressWarnings({"unchecked"})
    public <T> T cast(FormField<?> formField) {
        Objects.requireNonNull(formField, "'formField' cannot be null");
        if (DATE.equals(this)) {
            if (DATE == formField.getValueType()) {
                return (T) formField.getValue();
            } else {
                logger.logExceptionAsError(new UnsupportedOperationException(String.format("Unsupported cast operation "
                    + "or field value of type: %s", formField.getValueType())));
            }
        }
        if (TIME.equals(this)) {
            if (TIME == formField.getValueType()) {
                return (T) formField.getValue();
            } else {
                logger.logExceptionAsError(new UnsupportedOperationException(String.format("Unsupported cast operation "
                    + "or field value of type: %s", formField.getValueType())));
            }
        }
        if (PHONE_NUMBER.equals(this)) {
            if (PHONE_NUMBER == formField.getValueType()) {
                return (T) formField.getValue();
            } else {
                throw logger.logExceptionAsError(new UnsupportedOperationException(String.format("Unsupported cast "
                    + "operation for field value of type: %s", formField.getValueType())));
            }
        }
        if (LIST.equals(this)) {
            if (LIST == formField.getValueType()) {
                return (T) formField.getValue();
            } else {
                throw logger.logExceptionAsError(new UnsupportedOperationException(String.format("Unsupported cast "
                    + "operation or field value of type: %s", formField.getValueType())));
            }
        }
        if (MAP.equals(this)) {
            if (MAP == formField.getValueType()) {
                return (T) formField.getValue();
            } else {
                throw logger.logExceptionAsError(new UnsupportedOperationException(String.format("Unsupported cast "
                    + "operation or field value of type: %s", formField.getValueType())));
            }
        }
        if (FLOAT.equals(this)) {
            if (FLOAT == formField.getValueType()) {
                return (T) formField.getValue();
            } else {
                throw logger.logExceptionAsError(new UnsupportedOperationException(String.format("Unsupported cast "
                    + "operation or field value of type: %s", formField.getValueType())));
            }
        }
        if (INTEGER.equals(this)) {
            if (FLOAT == formField.getValueType()) {
                return (T) formField.getValue();
            } else {
                throw logger.logExceptionAsError(new UnsupportedOperationException(String.format("Unsupported cast "
                    + "operation or field value of type: %s", formField.getValueType())));
            }
        }
        if (STRING.equals(this)) {
            if (STRING == formField.getValueType()) {
                return (T) formField.getValue();
            } else {
                throw logger.logExceptionAsError(new UnsupportedOperationException(String.format("Unsupported cast "
                    + "operation or field value of type: %s", formField.getValueType())));
            }
        }
        return (T) formField.getValue();
    }
}
