// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.models;

import com.azure.communication.callautomation.implementation.accesshelpers.SendDtmfResponseConstructorProxy;
import com.azure.communication.callautomation.implementation.models.SendDtmfResponseInternal;
import com.azure.core.annotation.Immutable;

import java.util.Objects;

/** The SendDtmfResult model. */
@Immutable
public final class SendDtmfResult {
    /*
     * The operation context provided by client.
     */
    private final String operationContext;

    static {
        SendDtmfResponseConstructorProxy.setAccessor(
            new SendDtmfResponseConstructorProxy.SendDtmfResponseConstructorAccessor() {
                @Override
                public SendDtmfResult create(SendDtmfResponseInternal internalHeaders) {
                    return new SendDtmfResult(internalHeaders);
                }
            });
    }

    /**
     * Public constructor.
     *
     */
    public SendDtmfResult() {
        this.operationContext = null;
    }

    /**
     * Constructor of the class
     *
     * @param sendDtmfResponseInternal The response from the sendDtmf service
     */
    SendDtmfResult(SendDtmfResponseInternal sendDtmfResponseInternal) {
        Objects.requireNonNull(sendDtmfResponseInternal, "sendDtmfResponseInternal must not be null");

        this.operationContext = sendDtmfResponseInternal.getOperationContext();
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
