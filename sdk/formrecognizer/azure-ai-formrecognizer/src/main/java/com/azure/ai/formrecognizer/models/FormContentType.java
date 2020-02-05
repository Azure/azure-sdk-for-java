// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum FormContentType {
    // TODO: Expandable enum
    APPLICATION_PDF("application/pdf"),

    /**
     * Enum value image/jpeg.
     */
    IMAGE_JPEG("image/jpeg"),

    /**
     * Enum value image/png.
     */
    IMAGE_PNG("image/png"),

    /**
     * Enum value image/tiff.
     */
    IMAGE_TIFF("image/tiff");

    private final String value;

    FormContentType(String value) {
        this.value = value;
    }

    /**
     * Parses a serialized value to a ContentType instance.
     *
     * @param value the serialized value to parse.
     *
     * @return the parsed ContentType object, or null if unable to parse.
     */
    @JsonCreator
    public static FormContentType fromString(String value) {
        FormContentType[] items = FormContentType.values();
        for (FormContentType item : items) {
            if (item.toString().equalsIgnoreCase(value)) {
                return item;
            }
        }
        return null;
    }

    @JsonValue
    @Override
    public String toString() {
        return this.value;
    }
}
