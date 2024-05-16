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
    @JsonProperty(value = "id", required = true)
    private final String operationId;

    @JsonProperty(value = "error")
    private ResponseError error;

    /**
     * Creates an instance of ResourceOperationStatusUserError class.
     *
     * @param operationId the unique ID of the operation.
     */
    @JsonCreator
    private PollResult(@JsonProperty(value = "id", required = true) String operationId) {
        this.operationId = operationId;
    }

    /**
     * Get the id property: The unique ID of the operation.
     *
     * @return the unique ID of the operation.
     */
    public String getOperationId() {
        return this.operationId;
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
