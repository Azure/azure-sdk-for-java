/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.cognitiveservices.computervision;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Defines values for DomainModels.
 */
public enum DomainModels {
    /** Enum value Celebrities. */
    CELEBRITIES("Celebrities"),

    /** Enum value Landmarks. */
    LANDMARKS("Landmarks");

    /** The actual serialized value for a DomainModels instance. */
    private String value;

    DomainModels(String value) {
        this.value = value;
    }

    /**
     * Parses a serialized value to a DomainModels instance.
     *
     * @param value the serialized value to parse.
     * @return the parsed DomainModels object, or null if unable to parse.
     */
    @JsonCreator
    public static DomainModels fromString(String value) {
        DomainModels[] items = DomainModels.values();
        for (DomainModels item : items) {
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
