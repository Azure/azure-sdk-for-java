// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.models;

import com.azure.core.util.logging.ClientLogger;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

/**
 * Define enum values for FieldValue types.
 */
@SuppressWarnings("unchecked")
public enum FieldValueType {
    /**
     * Static value string for FieldValueType.
     */
    STRING {
        @Override
        public <T> T cast(FormField<?> formField) {
            if (isFieldValueNull(formField)) {
                return null;
            }
            return (T) String.valueOf(formField.getValue());
        }
    },

    /**
     * Static value date for FieldValueType.
     */
    DATE {
        @Override
        public <T> T cast(FormField<?> formField) {
            if (isFieldValueNull(formField)) {
                return null;
            }
            if (this == formField.getValueType()) {
                return (T) formField.getValue();
            } else if (STRING == formField.getValueType()) {
                return (T) LocalDate.parse(formField.getValue().toString(), DateTimeFormatter.ofPattern("yyyy/MM/dd"));
            } else {
                throw LOGGER.logExceptionAsError(new UnsupportedOperationException(String.format("Cannot cast from "
                    + "field value of type %s to type %s", formField.getValueType(), DATE)));
            }
        }
    },

    /**
     * Static value time for FieldValueType.
     */
    TIME {
        @Override
        public <T> T cast(FormField<?> formField) {
            if (isFieldValueNull(formField)) {
                return null;
            }
            if (this == formField.getValueType()) {
                return (T) formField.getValue();
            } else if (STRING == formField.getValueType()) {
                return (T) LocalTime.parse(formField.getValue().toString(), DateTimeFormatter.ofPattern("HH:mm:ss"));
            } else {
                throw LOGGER.logExceptionAsError(new UnsupportedOperationException(String.format("Cannot cast from "
                    + "field value of type %s to type %s", formField.getValueType(), TIME)));
            }
        }
    },

    /**
     * Static value phone number for FieldValueType.
     */
    PHONE_NUMBER {
        @Override
        public <T> T cast(FormField<?> formField) {
            if (isFieldValueNull(formField)) {
                return null;
            }
            if (this == formField.getValueType()) {
                return (T) formField.getValue();
            } else if (STRING == formField.getValueType()) {
                return (T) formField.getValue();
            } else {
                throw LOGGER.logExceptionAsError(new UnsupportedOperationException(String.format("Cannot cast from "
                    + "field value of type %s to type %s", formField.getValueType(), PHONE_NUMBER)));
            }
        }
    },

    /**
     * Static value double for FieldValueType.
     */
    DOUBLE {
        @Override
        public <T> T cast(FormField<?> formField) {
            if (isFieldValueNull(formField)) {
                return null;
            }
            if (this == formField.getValueType()) {
                return (T) formField.getValue();
            } else if (STRING == formField.getValueType()) {
                return (T) Double.valueOf(formField.getValue().toString());
            } else {
                throw LOGGER.logExceptionAsError(new UnsupportedOperationException(String.format("Cannot cast from "
                    + "field value of type %s to type %s", formField.getValueType(), DOUBLE)));
            }
        }
    },

    /**
     * Static value long for FieldValueType.
     */
    LONG {
        @Override
        public <T> T cast(FormField<?> formField) {
            if (isFieldValueNull(formField)) {
                return null;
            }
            if (this == formField.getValueType()) {
                return (T) formField.getValue();
            } else if (STRING == formField.getValueType()) {
                return (T) Long.valueOf(formField.getValue().toString());
            } else {
                throw LOGGER.logExceptionAsError(new UnsupportedOperationException(String.format("Cannot cast from "
                    + "field value of type %s to type %s", formField.getValueType(), LONG)));
            }
        }
    },

    /**
     * Static value list for FieldValueType.
     */
    LIST {
        @Override
        public <T> T cast(FormField<?> formField) {
            return getCollectionTypeCast(formField);
        }
    },

    /**
     * Static value map for FieldValueType.
     */
    MAP {
        @Override
        public <T> T cast(FormField<?> formField) {
            return getCollectionTypeCast(formField);
        }
    };

    static boolean isFieldValueNull(FormField<?> formField) {
        Objects.requireNonNull(formField, "'formField' cannot be null");
        return formField.getValue() == null;
    }

    <T> T getCollectionTypeCast(FormField<?> formField) {
        if (isFieldValueNull(formField)) {
            return null;
        }
        if (this == formField.getValueType()) {
            return (T) formField.getValue();
        } else {
            throw LOGGER.logExceptionAsError(new UnsupportedOperationException(String.format("Cannot cast from "
                + "field value of type %s to type %s", formField.getValueType(), this)));
        }
    }

    private static final ClientLogger LOGGER = new ClientLogger(FieldValueType.class);

    /**
     * Converts the form field value to a specific enum type.
     *
     * @param formField The recognized field value that needs to be converted.
     * @param <T> the class of the field.
     *
     * @return the converted value of the recognized field.
     * @throws UnsupportedOperationException if the {@code formField} type does not match the casting value type.
     * @throws NullPointerException if {@code formField} is {@code null}
     */
    public abstract <T> T cast(FormField<?> formField);
}
