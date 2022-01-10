// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver.models.events;

import com.azure.communication.callingserver.implementation.converters.ResultDetailsConverter;
import com.azure.communication.callingserver.implementation.models.TransferCallResultEventInternal;
import com.azure.communication.callingserver.models.CallingOperationResultDetails;
import com.azure.communication.callingserver.models.CallingOperationStatus;
import com.azure.core.annotation.Immutable;
import com.azure.core.util.BinaryData;

/** The transfer call result event. */
@Immutable
public final class TransferCallResultEvent extends CallingServerEventBase{
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
        return this.resultDetails;
    }

    /**
     * Get the operationContext property: The operation context.
     *
     * @return the operationContext value.
     */
    public String getOperationContext() {
        return this.operationContext;
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
     * Initializes a new instance of TransferCallResultEvent.
     *
     * @param resultDetails the resultDetails value.
     * @param operationContext The value to identify context of the operation. This is used to co-relate other
     *                         communications related to this operation
     * @param status the status value.
     */
    TransferCallResultEvent(CallingOperationResultDetails resultDetails, String operationContext, CallingOperationStatus status) {
        this.resultDetails = resultDetails;
        this.operationContext = operationContext;
        this.status = status;
    }

    /**
     * Deserialize {@link TransferCallResultEvent} event.
     *
     * @param eventData binary data for event
     * @return {@link TransferCallResultEvent} event.
     */
    public static TransferCallResultEvent deserialize(BinaryData eventData) {
        if (eventData == null) {
            return null;
        }
        TransferCallResultEventInternal transferCallResultEventInternal =
            eventData.toObject(TransferCallResultEventInternal.class);

        return new TransferCallResultEvent(
            ResultDetailsConverter.convert(transferCallResultEventInternal.getResultDetails()),
            transferCallResultEventInternal.getOperationContext(),
            transferCallResultEventInternal.getStatus());
    }
}
