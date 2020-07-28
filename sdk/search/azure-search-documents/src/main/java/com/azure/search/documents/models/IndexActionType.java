// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.models;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Defines values for IndexActionType.
 */
public enum IndexActionType {
    /**
     * Enum value upload.
     */
    UPLOAD("upload"),

    /**
     * Enum value merge.
     */
    MERGE("merge"),

    /**
     * Enum value mergeOrUpload.
     */
    MERGE_OR_UPLOAD("mergeOrUpload"),

    /**
     * Enum value delete.
     */
    DELETE("delete");

    /**
     * The actual serialized value for a IndexActionType instance.
     */
    private final String value;

    IndexActionType(String value) {
        this.value = value;
    }

    @JsonValue
    @Override
    public String toString() {
        return this.value;
    }
}
