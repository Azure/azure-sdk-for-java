// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.experimental.jsonpatch;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRawValue;

import java.util.Optional;

/**
 * Represents a JSON Patch operation.
 */
final class JsonPatchOperation {
    @JsonProperty("op")
    private final String op;

    @JsonProperty("from")
    private final String from;

    @JsonProperty("path")
    private final String path;

    @JsonProperty("value")
    @JsonRawValue
    private final Optional<String> optionalValue;

    JsonPatchOperation(String op, String from, String path, Optional<String> optionalValue) {
        this.op = op;
        this.from = from;
        this.path = path;
        this.optionalValue = optionalValue;
    }

    String getOp() {
        return op;
    }

    String getFrom() {
        return from;
    }

    String getPath() {
        return path;
    }

    Optional<String> getOptionalValue() {
        return optionalValue;
    }
}
