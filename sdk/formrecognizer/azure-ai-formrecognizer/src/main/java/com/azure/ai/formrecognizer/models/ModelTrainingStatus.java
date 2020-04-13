// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.models;

/**
 * A enum class represents all status a training model can have.
 */
public enum ModelTrainingStatus {
    /**
     * Enum value creating.
     */
    CREATING("creating"),

    /**
     * Enum value ready.
     */
    READY("ready"),

    /**
     * Enum value invalid.
     */
    INVALID("invalid");

    /**
     * The actual serialized value for a ModelStatus instance.
     */
    private final String value;

    /**
     * Constructs a {@link ModelTrainingStatus} box object.
     *
     * @param value The serialized value to parse.
     */
    ModelTrainingStatus(String value) {
        this.value = value;
    }

    /**
     * Parses a serialized value to a ModelStatus instance.
     *
     * @param value The serialized value to parse.
     * @return The parsed ModelStatus object, or null if unable to parse.
     */
    public static ModelTrainingStatus fromString(String value) {
        ModelTrainingStatus[] items = ModelTrainingStatus.values();
        for (ModelTrainingStatus item : items) {
            if (item.toString().equalsIgnoreCase(value)) {
                return item;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return this.value;
    }
}
