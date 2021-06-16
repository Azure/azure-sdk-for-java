// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver.models;

import com.azure.core.annotation.Immutable;

/** The response payload of the cancel all media operations. */
@Immutable
public final class CancelAllMediaOperationsResult {
    /*
     * The operation id.
     */
    private final String operationId;

    /*
     * The status of the operation
     */
    private final OperationStatus status;

    /*
     * The operation context
     */
    private final String operationContext;

    /*
     * The result info
     */
    private final ResultInfo resultInfo;

    /**
     * Get the operationId property: The operation id.
     *
     * @return the operationId value.
     */
    public String getOperationId() {
        return this.operationId;
    }

    /**
     * Get the status property: Gets the status of the operation.
     *
     * @return the status value.
     */
    public OperationStatus getStatus() {
        return status;
    }

    /**
     * Get the operationContext property: Gets the operation context.
     *
     * @return the operationContext value.
     */
    public String getOperationContext() {
        return operationContext;
    }

    /**
     * Get the resultInfo property: Gets the result info.
     *
     * @return the resultInfo value.
     */
    public ResultInfo getResultInfo() {
        return resultInfo;
    }

    /**
     * Initializes a new instance of CancelAllMediaOperationsResult.
     *
     * @param operationId the operationId value.
     * @param status the status value.
     * @param operationContext the operationContext value.
     * @param resultInfo the resultInfo value.
     */
    public CancelAllMediaOperationsResult(
        String operationId,
        OperationStatus status,
        String operationContext,
        ResultInfo resultInfo) {
        this.operationId = operationId;
        this.status = status;
        this.operationContext = operationContext;
        this.resultInfo = resultInfo;
    }
}
