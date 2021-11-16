// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver.models;

import com.azure.core.annotation.Immutable;

/** The response payload for transfer call operation. */
@Immutable
public final class TransferCallResult {
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
    private final CallingOperationResultDetails resultInfo;

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
     * Get the resultInfo property: The result info for the operation.
     *
     * @return the resultInfo value.
     */
    public CallingOperationResultDetails getResultInfo() {
        return this.resultInfo;
    }

    /**
     * Initializes a new instance of TransferCallResult.
     *
     * @param operationId the operationId value.
     * @param status the status value.
     * @param operationContext the operationContext value.
     * @param resultInfo the resultInfo value.
     */
    public TransferCallResult(String operationId, CallingOperationStatus status, String operationContext, CallingOperationResultDetails resultInfo) {
        this.operationId = operationId;
        this.status = status;
        this.operationContext = operationContext;
        this.resultInfo = resultInfo;
    }
}
