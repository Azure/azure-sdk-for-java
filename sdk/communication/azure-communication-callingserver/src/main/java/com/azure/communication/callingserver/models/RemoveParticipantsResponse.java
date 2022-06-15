// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver.models;

import com.azure.core.annotation.Immutable;

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
     * @param  operationId the operationId
     * @param  status the status
     * @param  operationContext the operationContext
     * @param  resultDetails the resultDetails
     */
    public RemoveParticipantsResponse(String operationId, CallingOperationStatus status, String operationContext,
                                      CallingOperationResultDetails resultDetails) {
        this.operationId = operationId;
        this.status = status;
        this.operationContext = operationContext;
        this.resultDetails = resultDetails;
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
