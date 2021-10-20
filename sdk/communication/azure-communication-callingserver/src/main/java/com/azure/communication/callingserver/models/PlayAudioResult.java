// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver.models;

import com.azure.core.annotation.Immutable;

/** The response payload for play audio operation. */
@Immutable
public final class PlayAudioResult {
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
     * The result info
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
     * Get the resultInfo property: Gets or sets the result info.
     *
     * @return the resultInfo value.
     */
    public CallingOperationResultDetails getResultInfo() {
        return resultInfo;
    }

    /**
     * Initializes a new instance of PlayAudioResult.
     *
     * @param operationId the operationId value.
     * @param status the status value.
     * @param operationContext the operationContext value.
     * @param resultInfo the resultInfo value.
     */
    public PlayAudioResult(String operationId, CallingOperationStatus status, String operationContext, CallingOperationResultDetails resultInfo) {
        this.operationId = operationId;
        this.status = status;
        this.operationContext = operationContext;
        this.resultInfo = resultInfo;
    }
}
