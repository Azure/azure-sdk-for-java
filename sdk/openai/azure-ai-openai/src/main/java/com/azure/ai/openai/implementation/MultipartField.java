// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.openai.implementation;

/**
 * A field of a request for a multipart HTTP request.
 */
public class MultipartField {

    /**
     * The JSON key name of this field.
     */
    private final String wireName;

    /**
     * The JSON value of this field.
     */
    private final String value;

    /**
     *
     * @param wireName The JSON key name of this field.
     * @param value The JSON value of this field.
     */
    public MultipartField(String wireName, String value) {
        this.wireName = wireName;
        this.value = value;
    }

    /**
     *
     * @return The JSON key name of this field.
     */
    public String getWireName() {
        return wireName;
    }

    /**
     *
     * @return The JSON value of this field.
     */
    public String getValue() {
        return value;
    }
}
