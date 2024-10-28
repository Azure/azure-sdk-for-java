// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.models;

import com.azure.communication.callautomation.implementation.accesshelpers.SendDtmfTonesResponseConstructorProxy;
import com.azure.communication.callautomation.implementation.models.SendDtmfTonesResultInternal;
import com.azure.core.annotation.Immutable;

import java.util.Objects;

/** The SendDtmfTonesResult model. */
@Immutable
public final class SendDtmfTonesResult {

    /*
     * The operation context provided by client.
     */
    private final String operationContext;

    static {
        SendDtmfTonesResponseConstructorProxy
            .setAccessor(new SendDtmfTonesResponseConstructorProxy.SendDtmfTonesResponseConstructorAccessor() {
                @Override
                public SendDtmfTonesResult create(SendDtmfTonesResultInternal internalHeaders) {
                    return new SendDtmfTonesResult(internalHeaders);
                }
            });
    }

    /**
     * Public constructor.
     *
     */
    public SendDtmfTonesResult() {
        this.operationContext = null;
    }

    /**
     * Constructor of the class
     *
     * @param sendDtmfTonesResultInternal The response from the sendDtmfTones service
     */
    SendDtmfTonesResult(SendDtmfTonesResultInternal sendDtmfTonesResultInternal) {
        Objects.requireNonNull(sendDtmfTonesResultInternal, "sendDtmfTonesResultInternal must not be null");

        this.operationContext = sendDtmfTonesResultInternal.getOperationContext();
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
