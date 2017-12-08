/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.cognitiveservices.faceapi;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Defines values for ExposureLevels.
 */
public enum ExposureLevels {
    /** Enum value UnderExposure. */
    UNDER_EXPOSURE("UnderExposure"),

    /** Enum value GoodExposure. */
    GOOD_EXPOSURE("GoodExposure"),

    /** Enum value OverExposure. */
    OVER_EXPOSURE("OverExposure");

    /** The actual serialized value for a ExposureLevels instance. */
    private String value;

    ExposureLevels(String value) {
        this.value = value;
    }

    /**
     * Parses a serialized value to a ExposureLevels instance.
     *
     * @param value the serialized value to parse.
     * @return the parsed ExposureLevels object, or null if unable to parse.
     */
    @JsonCreator
    public static ExposureLevels fromString(String value) {
        ExposureLevels[] items = ExposureLevels.values();
        for (ExposureLevels item : items) {
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
