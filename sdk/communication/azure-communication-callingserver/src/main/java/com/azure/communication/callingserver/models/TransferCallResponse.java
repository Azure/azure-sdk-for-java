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
     * The operation context provided by client.
     */
    private final String operationContext;

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
        this.operationContext = null;
    }

    /**
     * Package-private constructor of the class, used internally only.
     *
     * @param transferCallResponseInternal The response from the service.
     */
    TransferCallResponse(TransferCallResponseInternal transferCallResponseInternal) {
        Objects.requireNonNull(transferCallResponseInternal, "transferCallResponseInternal must not be null");

        this.operationContext = transferCallResponseInternal.getOperationContext();
    }

    /**
     * Get the operationContext property: The operation context provided by client.
     *
     * @return the operationContext value.
     */
    public String getOperationContext() {
        return this.operationContext;
    }
}
