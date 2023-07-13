// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.jobrouter.models;

import com.azure.core.util.logging.ClientLogger;

/**
 * Wrapper class for labels. Supports double, String and boolean types.
 */
public final class LabelValue {

    private static final ClientLogger LOGGER = new ClientLogger(LabelValue.class);


    /**
     * Value to pass to server.
     */
    private final Object value;

    /**
     * Constructor for numerical value.
     * @param numericValue numeric value of label.
     */
    public LabelValue(Double numericValue) {
        this.value = numericValue;
    }

    /**
     * Constructor for string value.
     * @param stringValue string value of label.
     */
    public LabelValue(String stringValue) {
        this.value = stringValue;
    }

    /**
     * Constructor for boolean value.
     * @param boolValue boolean value of label.
     */
    public LabelValue(Boolean boolValue) {
        this.value = boolValue;
    }

    /**
     * Returns value of type Object.
     * @return value
     */
    public Object getValue() {
        return this.value;
    }

    /**
     * Returns Double value of object
     * @return (Double) value.
     */
    public Double getValueAsDouble() {
        if (value.getClass() == Double.class) {
            return (Double) this.value;
        }
        throw LOGGER.logExceptionAsError(new IllegalStateException("value is not of type Double."));
    }

    /**
     * Returns String value of object
     * @return (String) value.
     */
    public String getValueAsString() {
        if (value.getClass() == String.class) {
            return (String) this.value;
        }
        throw LOGGER.logExceptionAsError(new IllegalStateException("value is not of type String."));
    }

    /**
     * Returns Boolean value of object
     * @return (Boolean) value.
     */
    public Boolean getValueAsBoolean() {
        if (value.getClass() == Boolean.class) {
            return (Boolean) this.value;
        }
        throw LOGGER.logExceptionAsError(new IllegalStateException("value is not of type Boolean."));
    }
}
