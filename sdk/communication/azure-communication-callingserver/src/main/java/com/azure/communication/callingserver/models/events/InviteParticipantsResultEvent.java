// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver.models.events;

import com.azure.communication.callingserver.models.OperationStatus;
import com.azure.communication.callingserver.models.ResultInfo;
import com.azure.core.util.BinaryData;

/**
 * The invited participants result event.
 */
public final class InviteParticipantsResultEvent extends CallingServerEventBase {
    /**
     * The event type.
     */
    public static final String EVENT_TYPE = "Microsoft.Communication.InviteParticipantResult";

    /**
     * The result info.
     */
    private ResultInfo resultInfo;

    /**
     * Get the result info.
     * 
     * @return the result info value.
     */
    public ResultInfo getResultInfo() {
        return this.resultInfo;
    }

    /**
     * Set the result info.
     * 
     * @param resultInfo the result info.
     * @return the InviteParticipantsResultEvent object itself.
     */
    public InviteParticipantsResultEvent setResultInfo(ResultInfo resultInfo) {
        this.resultInfo = resultInfo;
        return this;
    }

    /**
     * The operation context.
     */
    private String operationContext;

    /**
     * Get the operation context.
     *
     * @return the operation context value.
     */
    public String getState() {
        return this.operationContext;
    }

    /**
     * Set the operation context.
     *
     * @param operationContext the operation context.
     * @return the InviteParticipantsResultEvent object itself.
     */
    public InviteParticipantsResultEvent setState(String operationContext) {
        this.operationContext = operationContext;
        return this;
    }

    /**
     * The status of the operation.
     */
    private OperationStatus status;

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
     * @return the InviteParticipantsResultEvent object itself.
     */
    public InviteParticipantsResultEvent setStatus(OperationStatus status) {
        this.status = status;
        return this;
    }

    /**
     * The call leg Id.
     */
    private String callLegId;

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
     * @return the InviteParticipantsResultEvent object itself.
     */
    public InviteParticipantsResultEvent setCallLegId(String callLegId) {
        this.callLegId = callLegId;
        return this;
    }

    /**
     * Initializes a new instance of InviteParticipantsResultEvent.
     */
    public InviteParticipantsResultEvent() {

    }

    /**
     * Initializes a new instance of InviteParticipantsResultEvent
     * 
     * @param resultInfo The result info.
     * @param operationContext The operation context.
     * @param status The status.
     * @param callLegId The call leg id.
     * @throws IllegalArgumentException if any parameter is null or empty.
     */
    public InviteParticipantsResultEvent(ResultInfo resultInfo, String operationContext, OperationStatus status,
            String callLegId) {
        if (resultInfo == null) {
            throw new IllegalArgumentException("object resultInfo cannot be null");
        }
        if (operationContext == null || operationContext.isEmpty()) {
            throw new IllegalArgumentException("object operationContext cannot be null or empty");
        }
        if (status == null) {
            throw new IllegalArgumentException("object status cannot be null");
        }
        if (callLegId == null || callLegId.isEmpty()) {
            throw new IllegalArgumentException("object callLegId cannot be null or empty");
        }
        this.resultInfo = resultInfo;
        this.operationContext = operationContext;
        this.status = status;
        this.callLegId = callLegId;
    }

    /**
     * Deserialize {@link InviteParticipantsResultEvent} event.
     * 
     * @param eventData binary data for event
     * @return {@link InviteParticipantsResultEvent} event.
     */
    public static InviteParticipantsResultEvent deserialize(BinaryData eventData) {
        if (eventData == null) {
            return null;
        }
        return eventData.toObject(InviteParticipantsResultEvent.class);
    }
}
