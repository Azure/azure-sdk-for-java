// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.email.models;

import com.azure.core.annotation.Fluent;
import com.azure.core.models.ResponseError;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/** Status of the long running operation. */
@Fluent
public final class EmailSendResult {
    /*
     * The unique id of the operation. Use a UUID.
     */
    @JsonProperty(value = "id", required = true)
    private String id;

    /*
     * Status of operation.
     */
    @JsonProperty(value = "status", required = true)
    private EmailSendStatus status;

    /*
     * Response error when status is a non-success terminal state.
     */
    @JsonProperty(value = "error")
    private ResponseError error;

    /**
     * Creates an instance of EmailSendResult class.
     *
     * @param id the id value to set.
     * @param status the status value to set.
     * @param error the error value to set.
     */
    @JsonCreator
    public EmailSendResult(
        @JsonProperty(value = "id", required = true) String id,
        @JsonProperty(value = "status", required = true) EmailSendStatus status,
        @JsonProperty(value = "error") ResponseError error) {
        this.id = id;
        this.status = status;
        this.error = error;
    }

    /**
     * Get the id property: The unique id of the operation. Use a UUID.
     *
     * @return the id value.
     */
    public String getId() {
        return this.id;
    }

    /**
     * Get the status property: Status of operation.
     *
     * @return the status value.
     */
    public EmailSendStatus getStatus() {
        return this.status;
    }

    /**
     * Get the error property: Response error when status is a non-success terminal state.
     *
     * @return the error value.
     */
    public ResponseError getError() {
        return this.error;
    }
}
