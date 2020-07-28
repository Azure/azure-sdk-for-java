// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.indexes.models;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Defines values for IndexerExecutionStatus.
 */
public enum IndexerExecutionStatus {
    /**
     * Enum value transientFailure.
     */
    TRANSIENT_FAILURE("transientFailure"),

    /**
     * Enum value success.
     */
    SUCCESS("success"),

    /**
     * Enum value inProgress.
     */
    IN_PROGRESS("inProgress"),

    /**
     * Enum value reset.
     */
    RESET("reset");

    /**
     * The actual serialized value for a IndexerExecutionStatus instance.
     */
    private final String value;

    IndexerExecutionStatus(String value) {
        this.value = value;
    }

    @JsonValue
    @Override
    public String toString() {
        return this.value;
    }
}
