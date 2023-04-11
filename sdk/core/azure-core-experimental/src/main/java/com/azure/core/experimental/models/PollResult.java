// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.experimental.models;

import com.azure.core.annotation.Immutable;
import com.azure.core.models.ResponseError;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/** Provides status details for long running operations. */
@Immutable
public final class PollResult {
    @JsonProperty(value = "id", required = true, access = JsonProperty.Access.WRITE_ONLY)
    private String id;

    @JsonProperty(value = "error", access = JsonProperty.Access.WRITE_ONLY)
    private ResponseError error;

    /**
     * Creates an instance of ResourceOperationStatusUserError class.
     */
    @JsonCreator
    private PollResult() {
    }

    /**
     * Get the id property: The unique ID of the operation.
     *
     * @return the id value.
     */
    public String getId() {
        return this.id;
    }

    /**
     * Get the error property: Error object that describes the error when status is "Failed".
     *
     * @return the error value.
     */
    public ResponseError getError() {
        return this.error;
    }
}
