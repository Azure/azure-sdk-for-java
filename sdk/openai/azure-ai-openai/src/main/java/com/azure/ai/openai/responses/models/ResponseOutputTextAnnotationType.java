// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.openai.responses.models;

/**
 * Defines values for ResponseOutputTextAnnotationType.
 */
public enum ResponseOutputTextAnnotationType {
    /**
     * Enum value file_citation.
     */
    FILE_CITATION("file_citation"),

    /**
     * Enum value url_citation.
     */
    URL_CITATION("url_citation"),

    /**
     * Enum value file_path.
     */
    FILE_PATH("file_path");

    /**
     * The actual serialized value for a ResponseOutputTextAnnotationType instance.
     */
    private final String value;

    ResponseOutputTextAnnotationType(String value) {
        this.value = value;
    }

    /**
     * Parses a serialized value to a ResponseOutputTextAnnotationType instance.
     * 
     * @param value the serialized value to parse.
     * @return the parsed ResponseOutputTextAnnotationType object, or null if unable to parse.
     */
    public static ResponseOutputTextAnnotationType fromString(String value) {
        if (value == null) {
            return null;
        }
        ResponseOutputTextAnnotationType[] items = ResponseOutputTextAnnotationType.values();
        for (ResponseOutputTextAnnotationType item : items) {
            if (item.toString().equalsIgnoreCase(value)) {
                return item;
            }
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return this.value;
    }
}
