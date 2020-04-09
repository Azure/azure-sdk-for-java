// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.models;

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

    ModelTrainingStatus(String value) {
        this.value = value;
    }

    /**
     * Parses a serialized value to a ModelStatus instance.
     *
     * @param value the serialized value to parse.
     * @return the parsed ModelStatus object, or null if unable to parse.
     */
    public static com.azure.ai.formrecognizer.models.ModelTrainingStatus fromString(String value) {
        com.azure.ai.formrecognizer.models.ModelTrainingStatus[] items = com.azure.ai.formrecognizer.models.ModelTrainingStatus.values();
        for (com.azure.ai.formrecognizer.models.ModelTrainingStatus item : items) {
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
