// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver.models.events;

import com.azure.communication.callingserver.implementation.converters.ResultInfoConverter;
import com.azure.communication.callingserver.implementation.models.InviteParticipantsResultEventInternal;
import com.azure.communication.callingserver.models.OperationStatus;
import com.azure.communication.callingserver.models.ResultInfo;
import com.azure.core.util.BinaryData;

/** The InviteParticipantResultEvent model. */
public final class InviteParticipantResultEvent {
    /*
     * The result details.
     */
    private final ResultInfo resultInfo;

    /*
     * The operation context.
     */
    private final String operationContext;

    /*
     * Gets or sets the status of the operation
     */
    private final OperationStatus status;

    /**
     * Get the resultInfo property: The result details.
     *
     * @return the resultInfo value.
     */
    public ResultInfo getResultInfo() {
        return resultInfo;
    }

    /**
     * Get the operationContext property: The operation context.
     *
     * @return the operationContext value.
     */
    public String getOperationContext() {
        return operationContext;
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
     * Initializes a new instance of InviteParticipantResultEvent.
     *
     * @param resultInfo the resultInfo value.
     * @param operationContext the operationContext value.
     * @param status the status value.
     */
    public InviteParticipantResultEvent(ResultInfo resultInfo, String operationContext, OperationStatus status) {
        this.resultInfo = resultInfo;
        this.operationContext = operationContext;
        this.status = status;
    }

    /**
     * Deserialize {@link InviteParticipantResultEvent} event.
     *
     * @param eventData binary data for event
     * @return {@link InviteParticipantResultEvent} event.
     */
    public static InviteParticipantResultEvent deserialize(BinaryData eventData) {
        if (eventData == null) {
            return null;
        }
        InviteParticipantsResultEventInternal inviteParticipantsResultEventInternal =
            eventData.toObject(InviteParticipantsResultEventInternal.class);
        return new InviteParticipantResultEvent(
            ResultInfoConverter.convert(inviteParticipantsResultEventInternal.getResultInfo()),
            inviteParticipantsResultEventInternal.getOperationContext(),
            inviteParticipantsResultEventInternal.getStatus());
    }
}
