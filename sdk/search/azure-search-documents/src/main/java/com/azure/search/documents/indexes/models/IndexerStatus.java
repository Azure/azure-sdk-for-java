// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.indexes.models;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Defines values for IndexerStatus.
 */
public enum IndexerStatus {
    /**
     * Enum value unknown.
     */
    UNKNOWN("unknown"),

    /**
     * Enum value error.
     */
    ERROR("error"),

    /**
     * Enum value running.
     */
    RUNNING("running");

    /**
     * The actual serialized value for a IndexerStatus instance.
     */
    private final String value;

    IndexerStatus(String value) {
        this.value = value;
    }

    @JsonValue
    @Override
    public String toString() {
        return this.value;
    }
}
