// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver.models.events;

import com.azure.communication.callingserver.implementation.converters.ResultInfoConverter;
import com.azure.communication.callingserver.implementation.models.AddParticipantResultEventInternal;
import com.azure.communication.callingserver.models.OperationStatus;
import com.azure.communication.callingserver.models.ResultInfo;
import com.azure.core.annotation.Immutable;
import com.azure.core.util.BinaryData;

/** The add participant result event. */
@Immutable
public final class AddParticipantResultEvent extends CallingServerEventBase {
    /*
     * The result details.
     */
    private final ResultInfo resultInfo;

    /*
     * The operation context.
     */
    private final String operationContext;

    /*
     * The status of the operation
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
     * Get the status property: Gets the status of the operation.
     *
     * @return the operation status value.
     */
    public OperationStatus getStatus() {
        return status;
    }

    /**
     * Initializes a new instance of AddParticipantResultEvent.
     *
     * @param resultInfo the resultInfo value.
     * @param operationContext The value to identify context of the operation. This is used to co-relate other
     *                         communications related to this operation
     * @param status the status value.
     */
    AddParticipantResultEvent(ResultInfo resultInfo, String operationContext, OperationStatus status) {
        this.resultInfo = resultInfo;
        this.operationContext = operationContext;
        this.status = status;
    }

    /**
     * Deserialize {@link AddParticipantResultEvent} event.
     *
     * @param eventData binary data for event
     * @return {@link AddParticipantResultEvent} event.
     */
    public static AddParticipantResultEvent deserialize(BinaryData eventData) {
        if (eventData == null) {
            return null;
        }
        AddParticipantResultEventInternal addParticipantResultEventInternal =
            eventData.toObject(AddParticipantResultEventInternal.class);

        return new AddParticipantResultEvent(
            ResultInfoConverter.convert(addParticipantResultEventInternal.getResultInfo()),
            addParticipantResultEventInternal.getOperationContext(),
            addParticipantResultEventInternal.getStatus());
    }
}
