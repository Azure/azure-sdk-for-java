// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver.models;


import com.azure.communication.callingserver.implementation.models.PlayResponseInternal;
import com.azure.core.annotation.Immutable;

/** The PlayResponse model. */
@Immutable
public final class PlayResponse {
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
     * Public constructor
     *
     * @param playResponseInternal the internal response
     */
    public PlayResponse(PlayResponseInternal playResponseInternal) {
        this.operationId = playResponseInternal.getOperationId();
        this.operationContext = playResponseInternal.getOperationContext();
        this.status = CallingOperationStatus.fromString(playResponseInternal.getStatus().toString());
        this.resultDetails = new CallingOperationResultDetails(playResponseInternal.getResultDetails());
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
