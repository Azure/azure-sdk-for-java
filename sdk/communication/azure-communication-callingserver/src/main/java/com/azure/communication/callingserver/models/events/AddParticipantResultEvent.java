// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver.models.events;

import com.azure.communication.callingserver.implementation.converters.ResultDetailsConverter;
import com.azure.communication.callingserver.implementation.models.AddParticipantResultEventInternal;
import com.azure.communication.callingserver.models.CallingOperationResultDetails;
import com.azure.communication.callingserver.models.CallingOperationStatus;
import com.azure.core.annotation.Immutable;
import com.azure.core.util.BinaryData;

/** The add participant result event. */
@Immutable
public final class AddParticipantResultEvent extends CallingServerEventBase {
    /*
     * The result details.
     */
    private final CallingOperationResultDetails resultDetails;

    /*
     * The operation context.
     */
    private final String operationContext;

    /*
     * The status of the operation
     */
    private final CallingOperationStatus status;

    /**
     * Get the resultDetails property: The result details.
     *
     * @return the resultDetails value.
     */
    public CallingOperationResultDetails getResultDetails() {
        return resultDetails;
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
    public CallingOperationStatus getStatus() {
        return status;
    }

    /**
     * Initializes a new instance of AddParticipantResultEvent.
     *
     * @param resultDetails the resultDetails value.
     * @param operationContext The value to identify context of the operation. This is used to co-relate other
     *                         communications related to this operation
     * @param status the status value.
     */
    AddParticipantResultEvent(CallingOperationResultDetails resultDetails, String operationContext, CallingOperationStatus status) {
        this.resultDetails = resultDetails;
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
            ResultDetailsConverter.convert(addParticipantResultEventInternal.getResultDetails()),
            addParticipantResultEventInternal.getOperationContext(),
            addParticipantResultEventInternal.getStatus());
    }
}
