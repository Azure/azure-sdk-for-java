// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver.models;

import com.azure.core.annotation.Immutable;

/** The response payload for add participant operation. */
@Immutable
public final class AddParticipantResult {
    /*
     * The operation id.
     */
    private final String operationId;

    /*
     * The status of the operation
     */
    private final CallingOperationStatus status;

    /*
     * The operation context
     */
    private final String operationContext;

    /*
     * The result details
     */
    private final CallingOperationResultDetails resultDetails;

    /**
     * Get the operationId property: The operation id.
     *
     * @return the operationId value.
     */
    public String getOperationId() {
        return this.operationId;
    }

    /**
     * Get the status property: Gets or sets the status of the operation.
     *
     * @return the status value.
     */
    public CallingOperationStatus getStatus() {
        return status;
    }

    /**
     * Get the operationContext property: Gets or sets the operation context.
     *
     * @return the operationContext value.
     */
    public String getOperationContext() {
        return operationContext;
    }

    /**
     * Get the resultDetails property: Gets or sets the result info.
     *
     * @return the resultDetails value.
     */
    public CallingOperationResultDetails getResultDetails() {
        return resultDetails;
    }

    /**
     * Initializes a new instance of AddParticipantResult.
     *
     * @param operationId the operationId value.
     * @param status the status value.
     * @param operationContext the operationContext value.
     * @param resultDetails the resultDetails value.
     */
    public AddParticipantResult(String operationId, CallingOperationStatus status, String operationContext, CallingOperationResultDetails resultDetails) {
        this.operationId = operationId;
        this.status = status;
        this.operationContext = operationContext;
        this.resultDetails = resultDetails;
    }
}

