// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver.models;

import com.azure.communication.callingserver.implementation.models.RemoveParticipantsResponseInternal;
import com.azure.core.annotation.Immutable;

import java.util.Objects;

/** The RemoveParticipantsResponse model. */
@Immutable
public final class RemoveParticipantsResponse {
    /*
     * The operation id.
     */
    private final String operationId;

    /*
     * The status of the operation, required.
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
     * Get the operationId property: The operation id.
     *
     * @param  removeParticipantsResponseInternal The response from the service
     */
    public RemoveParticipantsResponse(RemoveParticipantsResponseInternal removeParticipantsResponseInternal) {
        Objects.requireNonNull(removeParticipantsResponseInternal, "removeParticipantsResponseInternal must not be null");

        this.operationId = removeParticipantsResponseInternal.getOperationId();
        this.status = CallingOperationStatus.fromString(removeParticipantsResponseInternal.getStatus().toString());
        this.operationContext = removeParticipantsResponseInternal.getOperationContext();
        this.resultDetails = new CallingOperationResultDetails(removeParticipantsResponseInternal.getResultDetails());
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
