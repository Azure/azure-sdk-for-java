// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver.models;

import com.azure.communication.callingserver.implementation.models.AddParticipantsResponseInternal;
import com.azure.core.annotation.Immutable;

import java.util.Objects;

/** The AddParticipantsResponse model. */
@Immutable
public final class AddParticipantsResponse {
    /*
     * The operation id.
     */
    private final String operationId;

    /*
     * The status of the operation
     */
    private final CallingOperationStatus status;

    /*
     * The operation context provided by client.
     */
    private final String operationContext;

    /*
     * The result info for the operation.
     */
    private final CallingOperationResultDetails resultDetails;

    /**
     * Public constructor.
     *
     */
    public AddParticipantsResponse() {
        this.operationId = null;
        this.status = null;
        this.operationContext = null;
        this.resultDetails = null;
    }

    /**
     * Constructor of the class
     *
     * @param addParticipantsResponseInternal The response from the addParticipant service
     */
    AddParticipantsResponse(AddParticipantsResponseInternal addParticipantsResponseInternal) {
        Objects.requireNonNull(addParticipantsResponseInternal, "addParticipantsResponseInternal must not be null");

        this.operationId = addParticipantsResponseInternal.getOperationId();
        this.status = CallingOperationStatus.fromString(addParticipantsResponseInternal.getStatus().toString());
        this.operationContext = addParticipantsResponseInternal.getOperationContext();
        this.resultDetails = new CallingOperationResultDetails(addParticipantsResponseInternal.getResultDetails());
    }

    /**
     * Get the operationId property: The operation id.
     *
     * @return the operationId value.
     */
    public String getOperationId() {
        return this.operationId;
    }

    /**
     * Get the status property: The status of the operation.
     *
     * @return the status value.
     */
    public CallingOperationStatus getStatus() {
        return this.status;
    }

    /**
     * Get the operationContext property: The operation context provided by client.
     *
     * @return the operationContext value.
     */
    public String getOperationContext() {
        return this.operationContext;
    }

    /**
     * Get the resultDetails property: The result info for the operation.
     *
     * @return the resultDetails value.
     */
    public CallingOperationResultDetails getResultDetails() {
        return this.resultDetails;
    }
}
