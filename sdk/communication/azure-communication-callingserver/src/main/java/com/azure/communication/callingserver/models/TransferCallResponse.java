// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver.models;

import com.azure.communication.callingserver.implementation.accesshelpers.TransferCallResponseConstructorProxy;
import com.azure.communication.callingserver.implementation.models.TransferCallResponseInternal;
import com.azure.core.annotation.Immutable;

import java.util.Objects;

/** The TransferCallResponse model. */
@Immutable
public final class TransferCallResponse {
    /*
     * The operation id.
     */
    private final String operationId;

    /*
     * The status of the operation
     */
    private final CallingOperationStatus status;

    /*
     * The operation context provided by client.
     */
    private final String operationContext;

    /*
     * The result info for the operation.
     */
    private final CallingOperationResultDetails resultDetails;

    static {
        TransferCallResponseConstructorProxy.setAccessor(
            new TransferCallResponseConstructorProxy.TransferCallResponseConstructorAccessor() {
                @Override
                public TransferCallResponse create(TransferCallResponseInternal internalHeaders) {
                    return new TransferCallResponse(internalHeaders);
                }
            });
    }

    /**
     * Public constructor.
     *
     */
    public TransferCallResponse() {
        this.operationId = null;
        this.status = null;
        this.operationContext = null;
        this.resultDetails = null;

    }

    /**
     * Package-private constructor of the class, used internally only.
     *
     * @param transferCallResponseInternal The response from the service.
     */
    TransferCallResponse(TransferCallResponseInternal transferCallResponseInternal) {
        Objects.requireNonNull(transferCallResponseInternal, "transferCallResponseInternal must not be null");

        this.operationId = transferCallResponseInternal.getOperationId();
        this.status = CallingOperationStatus.fromString(transferCallResponseInternal.getStatus().toString());
        this.operationContext = transferCallResponseInternal.getOperationContext();
        this.resultDetails = new CallingOperationResultDetails(transferCallResponseInternal.getResultDetails());
    }

    /**
     * Get the operationId property: The operation id.
     *
     * @return the operationId value.
     */
    public String getOperationId() {
        return this.operationId;
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
     * Get the operationContext property: The operation context provided by client.
     *
     * @return the operationContext value.
     */
    public String getOperationContext() {
        return this.operationContext;
    }

    /**
     * Get the resultDetails property: The result info for the operation.
     *
     * @return the resultDetails value.
     */
    public CallingOperationResultDetails getResultDetails() {
        return this.resultDetails;
    }
}
