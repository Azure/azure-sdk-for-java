// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.models;

import com.azure.communication.callautomation.implementation.accesshelpers.RemoveParticipantsResponseConstructorProxy;
import com.azure.communication.callautomation.implementation.models.RemoveParticipantsResponseInternal;
import com.azure.core.annotation.Immutable;

import java.util.Objects;

/** The RemoveParticipantsResult model. */
@Immutable
public final class RemoveParticipantsResult {
    /*
     * The operation context provided by client.
     */
    private final String operationContext;

    static {
        RemoveParticipantsResponseConstructorProxy.setAccessor(
            new RemoveParticipantsResponseConstructorProxy.RemoveParticipantsResponseConstructorAccessor() {
                @Override
                public RemoveParticipantsResult create(RemoveParticipantsResponseInternal internalHeaders) {
                    return new RemoveParticipantsResult(internalHeaders);
                }
            });
    }

    /**
     * Public constructor.
     *
     */
    public RemoveParticipantsResult() {
        this.operationContext = null;
    }

    /**
     * Package-private constructor of the class, used internally only.
     *
     * @param  removeParticipantsResponseInternal The response from the service
     */
    RemoveParticipantsResult(RemoveParticipantsResponseInternal removeParticipantsResponseInternal) {
        Objects.requireNonNull(removeParticipantsResponseInternal, "removeParticipantsResponseInternal must not be null");

        this.operationContext = removeParticipantsResponseInternal.getOperationContext();
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
