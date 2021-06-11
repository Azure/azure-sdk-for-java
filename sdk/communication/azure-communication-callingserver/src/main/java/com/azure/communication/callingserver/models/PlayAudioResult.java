// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver.models;

/** The response payload for play audio operation. */
public final class PlayAudioResult {
    /*
     * The identifier.
     */
    private final String id;

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
     * Get the id property: Gets or sets the identifier.
     *
     * @return the id value.
     */
    public String getId() {
        return this.id;
    }

    /**
     * Get the status property: Gets or sets the status of the operation.
     *
     * @return the status value.
     */
    public OperationStatus getStatus() {
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
    public ResultInfo getResultInfo() {
        return resultInfo;
    }

    /**
     * Initializes a new instance of PlayAudioResult.
     *
     * @param id the id value.
     * @param status the status value.
     * @param operationContext the operationContext value.
     * @param resultInfo the resultInfo value.
     */
    public PlayAudioResult(String id, OperationStatus status, String operationContext, ResultInfo resultInfo) {
        this.id = id;
        this.status = status;
        this.operationContext = operationContext;
        this.resultInfo = resultInfo;
    }
}
