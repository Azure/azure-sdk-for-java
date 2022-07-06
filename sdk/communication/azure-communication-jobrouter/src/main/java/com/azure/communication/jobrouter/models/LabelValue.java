// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.jobrouter.models;

/**
 * Wrapper class for labels. Supports double, String and boolean types.
 */
public class LabelValue {

    /**
     * Value to pass to server.
     */
    private final Object value;

    /**
     * Constructor for numerical value.
     * @param numericValue numeric value of label.
     */
    public LabelValue(double numericValue) {
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
    public LabelValue(boolean boolValue) {
        this.value = boolValue;
    }

    /**
     * Returns value of type Object.
     * @return value
     */
    public Object getValue() {
        return this.value;
    }
}
