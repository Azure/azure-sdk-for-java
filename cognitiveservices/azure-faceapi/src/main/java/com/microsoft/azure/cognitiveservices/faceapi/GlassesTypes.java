/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.cognitiveservices.faceapi;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Defines values for GlassesTypes.
 */
public enum GlassesTypes {
    /** Enum value noGlasses. */
    NO_GLASSES("noGlasses"),

    /** Enum value readingGlasses. */
    READING_GLASSES("readingGlasses"),

    /** Enum value sunglasses. */
    SUNGLASSES("sunglasses"),

    /** Enum value swimmingGoggles. */
    SWIMMING_GOGGLES("swimmingGoggles");

    /** The actual serialized value for a GlassesTypes instance. */
    private String value;

    GlassesTypes(String value) {
        this.value = value;
    }

    /**
     * Parses a serialized value to a GlassesTypes instance.
     *
     * @param value the serialized value to parse.
     * @return the parsed GlassesTypes object, or null if unable to parse.
     */
    @JsonCreator
    public static GlassesTypes fromString(String value) {
        GlassesTypes[] items = GlassesTypes.values();
        for (GlassesTypes item : items) {
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
