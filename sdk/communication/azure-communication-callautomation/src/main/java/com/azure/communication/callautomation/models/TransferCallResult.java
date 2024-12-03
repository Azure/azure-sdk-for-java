// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.models;

import com.azure.communication.callautomation.implementation.accesshelpers.TransferCallResponseConstructorProxy;
import com.azure.communication.callautomation.implementation.models.TransferCallResponseInternal;
import com.azure.core.annotation.Immutable;

import java.util.Objects;

/** The TransferCallResult model. */
@Immutable
public final class TransferCallResult {
    /*
     * The operation context provided by client.
     */
    private final String operationContext;

    static {
        TransferCallResponseConstructorProxy.setAccessor(
            new TransferCallResponseConstructorProxy.TransferCallResponseConstructorAccessor() {
                @Override
                public TransferCallResult create(TransferCallResponseInternal internalHeaders) {
                    return new TransferCallResult(internalHeaders);
                }
            });
    }

    /**
     * Public constructor.
     *
     */
    public TransferCallResult() {
        this.operationContext = null;
    }

    /**
     * Package-private constructor of the class, used internally only.
     *
     * @param transferCallResponseInternal The response from the service.
     */
    TransferCallResult(TransferCallResponseInternal transferCallResponseInternal) {
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
