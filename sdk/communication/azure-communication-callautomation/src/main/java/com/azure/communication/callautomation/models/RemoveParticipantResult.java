// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.models;

import com.azure.communication.callautomation.implementation.accesshelpers.RemoveParticipantResponseConstructorProxy;
import com.azure.communication.callautomation.implementation.models.RemoveParticipantResponseInternal;
import com.azure.core.annotation.Immutable;

import java.util.Objects;

/** The RemoveParticipantResult model. */
@Immutable
public final class RemoveParticipantResult {
    /*
     * The operation context provided by client.
     */
    private final String operationContext;

    static {
        RemoveParticipantResponseConstructorProxy.setAccessor(
            new RemoveParticipantResponseConstructorProxy.RemoveParticipantResponseConstructorAccessor() {
                @Override
                public RemoveParticipantResult create(RemoveParticipantResponseInternal internalHeaders) {
                    return new RemoveParticipantResult(internalHeaders);
                }
            });
    }

    /**
     * Public constructor.
     *
     */
    public RemoveParticipantResult() {
        this.operationContext = null;
    }

    /**
     * Package-private constructor of the class, used internally only.
     *
     * @param  removeParticipantResponseInternal The response from the service
     */
    RemoveParticipantResult(RemoveParticipantResponseInternal removeParticipantResponseInternal) {
        Objects.requireNonNull(removeParticipantResponseInternal, "removeParticipantResponseInternal must not be null");

        this.operationContext = removeParticipantResponseInternal.getOperationContext();
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
