// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.experimental.models;

import com.azure.core.annotation.Fluent;
import com.azure.core.models.ResponseError;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/** Provides status details for long running operations. */
@Fluent
public final class PollResult {
    @JsonProperty(value = "id", required = true)
    private final String id;

    @JsonProperty(value = "error")
    private ResponseError error;

    /**
     * Creates an instance of ResourceOperationStatusUserError class.
     *
     * @param id the unique ID of the operation.
     */
    @JsonCreator
    public PollResult(@JsonProperty(value = "id", required = true) String id) {
        this.id = id;
    }

    /**
     * Get the id property: The unique ID of the operation.
     *
     * @return the unique ID of the operation.
     */
    public String getId() {
        return this.id;
    }

    /**
     * Sets the error property: Error object that describes the error when status is "Failed".
     *
     * @param error the error object that describes the error when status is "Failed".
     */
    public void setError(ResponseError error) {
        this.error = error;
    }

    /**
     * Get the error property: Error object that describes the error when status is "Failed".
     *
     * @return the error object that describes the error when status is "Failed".
     */
    public ResponseError getError() {
        return this.error;
    }
}
