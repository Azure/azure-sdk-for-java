// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver.models.events;

import com.azure.communication.callingserver.implementation.converters.ResultInfoConverter;
import com.azure.communication.callingserver.implementation.models.TransferCallResultEventInternal;
import com.azure.communication.callingserver.models.CallingOperationResultDetails;
import com.azure.communication.callingserver.models.CallingOperationStatus;
import com.azure.core.annotation.Immutable;
import com.azure.core.util.BinaryData;

/** The transfer call result event. */
@Immutable
public final class TransferCallResultEvent {
    /*
     * The result details.
     */
    private final CallingOperationResultDetails resultInfo;

    /*
     * The operation context.
     */
    private final String operationContext;

    /*
     * The status of the operation
     */
    private final CallingOperationStatus status;

    /**
     * Get the resultInfo property: The result details.
     *
     * @return the resultInfo value.
     */
    public CallingOperationResultDetails getResultInfo() {
        return this.resultInfo;
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
     * @param resultInfo the resultInfo value.
     * @param operationContext The value to identify context of the operation. This is used to co-relate other
     *                         communications related to this operation
     * @param status the status value.
     */
    TransferCallResultEvent(CallingOperationResultDetails resultInfo, String operationContext, CallingOperationStatus status) {
        this.resultInfo = resultInfo;
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
            ResultInfoConverter.convert(transferCallResultEventInternal.getResultInfo()),
            transferCallResultEventInternal.getOperationContext(),
            transferCallResultEventInternal.getStatus());
    }
}
