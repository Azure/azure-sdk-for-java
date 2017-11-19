/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.cognitiveservices.computervision;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Defines values for VisualFeatureTypes.
 */
public enum VisualFeatureTypes {
    /** Enum value ImageType. */
    IMAGE_TYPE("ImageType"),

    /** Enum value Faces. */
    FACES("Faces"),

    /** Enum value Adult. */
    ADULT("Adult"),

    /** Enum value Categories. */
    CATEGORIES("Categories"),

    /** Enum value Color. */
    COLOR("Color"),

    /** Enum value Tags. */
    TAGS("Tags"),

    /** Enum value Description. */
    DESCRIPTION("Description");

    /** The actual serialized value for a VisualFeatureTypes instance. */
    private String value;

    VisualFeatureTypes(String value) {
        this.value = value;
    }

    /**
     * Parses a serialized value to a VisualFeatureTypes instance.
     *
     * @param value the serialized value to parse.
     * @return the parsed VisualFeatureTypes object, or null if unable to parse.
     */
    @JsonCreator
    public static VisualFeatureTypes fromString(String value) {
        VisualFeatureTypes[] items = VisualFeatureTypes.values();
        for (VisualFeatureTypes item : items) {
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
