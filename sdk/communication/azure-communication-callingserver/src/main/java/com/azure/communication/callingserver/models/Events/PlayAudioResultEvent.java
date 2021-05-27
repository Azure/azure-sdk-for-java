// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver.models.events;

import com.azure.communication.callingserver.models.OperationStatus;
import com.azure.communication.callingserver.models.ResultInfo;

/**
 * The invited participants result event.
 */
public final class PlayAudioResultEvent extends CallEventBase
{
    /**
     * The event type.
     */
    public static final String EventType = "Microsoft.Communication.PlayAudioResult";

    /**
     * The result info.
     */
    public ResultInfo resultInfo;

    /**
     * Get the result info.
     * @return the result info value.
     */
    public ResultInfo getResultInfo() {
        return this.resultInfo;
    }

    /**
     * Set the result info.
     * @param resultInfo the result info.
     * @return the PlayAudioResultEvent object itself.
     */
    public PlayAudioResultEvent setResultInfo(ResultInfo resultInfo) {
        this.resultInfo = resultInfo;
        return this;
    }

    /**
     * The operation context.
     */
    public String operationContext;

    /**
     * Get the operation context.
     *
     * @return the operation context value.
     */
    public String getOperationContext() {
        return this.operationContext;
    }

    /**
     * Set the operation context.
     *
     * @param operationContext the operation context.
     * @return the PlayAudioResultEvent object itself.
     */
    public PlayAudioResultEvent setOperationContext(String operationContext) {
        this.operationContext = operationContext;
        return this;
    }

    /**
     * The status of the operation.
     */
    public OperationStatus status;

    /**
     * Get the status of the operation.
     *
     * @return the status of the operationd.
     */
    public OperationStatus getStatus() {
        return this.status;
    }

    /**
     * Set the status of the operation.
     *
     * @param status the status of the operation.
     * @return the PlayAudioResultEvent object itself.
     */
    public PlayAudioResultEvent setStatus(OperationStatus status) {
        this.status = status;
        return this;
    }

    /**
     * The call leg Id.
     */
    public String callLegId;

    /**
     * Get the call leg Id.
     *
     * @return the time of the recording started.
     */
    public String getCallLegId() {
        return this.callLegId;
    }

    /**
     * Set the call leg Id.
     *
     * @param callLegId the call leg id.
     * @return the PlayAudioResultEvent object itself.
     */
    public PlayAudioResultEvent setCallLegId(String callLegId) {
        this.callLegId = callLegId;
        return this;
    }

    /**
     * Initializes a new instance of PlayAudioResultEvent
     * @param resultInfo The result info.
     * @param operationContext The operation context.
     * @param status The status.
     * @param callLegId The call leg id.
     */
    public PlayAudioResultEvent(ResultInfo resultInfo, String operationContext, OperationStatus status, String callLegId)
    {
        if (resultInfo == null)
        {
            throw new IllegalArgumentException(String.format("object '%s' cannot be null", resultInfo.getClass().getName()));
        }
        if (operationContext == null || operationContext.isEmpty())
        {
            throw new IllegalArgumentException(String.format("object '%s' cannot be null", operationContext.getClass().getName()));
        }
        if (status == null)
        {
            throw new IllegalArgumentException(String.format("object '%s' cannot be null", status.getClass().getName()));
        }
        if (callLegId == null || callLegId.isEmpty())
        {
            throw new IllegalArgumentException(String.format("object '%s' cannot be null", callLegId.getClass().getName()));
        }
        this.resultInfo = resultInfo;
        this.operationContext = operationContext;
        this.status = status;
        this.callLegId = callLegId;
    }
}
