// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.administration.models;

/** Defines values for ModelOperationStatus. */
public enum ModelOperationStatus {
    /** Enum value notStarted. */
    NOT_STARTED("notStarted"),

    /** Enum value running. */
    RUNNING("running"),

    /** Enum value failed. */
    FAILED("failed"),

    /** Enum value succeeded. */
    SUCCEEDED("succeeded"),

    /** Enum value canceled. */
    CANCELED("canceled");

    /** The actual serialized value for a ModelOperationStatus instance. */
    private final String value;

    ModelOperationStatus(String value) {
        this.value = value;
    }

    /**
     * Parses a serialized value to a ModelOperationStatus instance.
     *
     * @param value the serialized value to parse.
     * @return the parsed ModelOperationStatus object, or null if unable to parse.
     */
    public static ModelOperationStatus fromString(String value) {
        ModelOperationStatus[] items = ModelOperationStatus.values();
        for (ModelOperationStatus item : items) {
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
