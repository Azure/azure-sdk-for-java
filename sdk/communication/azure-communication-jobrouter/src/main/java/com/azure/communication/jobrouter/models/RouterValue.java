// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.jobrouter.models;

import com.azure.core.util.logging.ClientLogger;

/**
 * Wrapper class for labels. Supports double, String and boolean types.
 */
public final class RouterValue {
    private static final ClientLogger LOGGER = new ClientLogger(RouterValue.class);

    /**
     * Value to pass to server.
     */
    private final Object value;

    /**
     * Constructor for integer value.
     * @param integerValue integer value of label.
     */
    public RouterValue(Integer integerValue) {
        this.value = integerValue;
    }

    /**
     * Constructor for numerical value.
     * @param numericValue numeric value of label.
     */
    public RouterValue(Double numericValue) {
        this.value = numericValue;
    }

    /**
     * Constructor for string value.
     * @param stringValue string value of label.
     */
    public RouterValue(String stringValue) {
        this.value = stringValue;
    }

    /**
     * Constructor for boolean value.
     * @param boolValue boolean value of label.
     */
    public RouterValue(Boolean boolValue) {
        this.value = boolValue;
    }

    RouterValue(Object objectValue) {
        this.value = objectValue;
    }

    /**
     * Returns Integer value of object
     * @return (Integer) value.
     */
    public Integer getValueAsInteger() {
        if (value.getClass() == Integer.class) {
            return (Integer) this.value;
        }
        throw LOGGER.logExceptionAsError(new IllegalStateException("value is not of type Integer."));
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
