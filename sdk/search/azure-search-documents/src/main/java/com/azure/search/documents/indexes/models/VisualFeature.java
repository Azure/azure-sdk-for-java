// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.indexes.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Defines values for VisualFeature.
 */
public enum VisualFeature {
    /**
     * Enum value adult.
     */
    ADULT("adult"),

    /**
     * Enum value brands.
     */
    BRANDS("brands"),

    /**
     * Enum value categories.
     */
    CATEGORIES("categories"),

    /**
     * Enum value description.
     */
    DESCRIPTION("description"),

    /**
     * Enum value faces.
     */
    FACES("faces"),

    /**
     * Enum value objects.
     */
    OBJECTS("objects"),

    /**
     * Enum value tags.
     */
    TAGS("tags");

    /**
     * The actual serialized value for a VisualFeature instance.
     */
    private final String value;

    VisualFeature(String value) {
        this.value = value;
    }

    /**
     * Parses a serialized value to a VisualFeature instance.
     *
     * @param value the serialized value to parse.
     * @return the parsed VisualFeature object, or null if unable to parse.
     */
    @JsonCreator
    public static VisualFeature fromString(String value) {
        VisualFeature[] items = VisualFeature.values();
        for (VisualFeature item : items) {
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
