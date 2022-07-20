// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver.models;

import com.azure.communication.callingserver.implementation.accesshelpers.RemoveParticipantsResponseConstructorProxy;
import com.azure.communication.callingserver.implementation.models.RemoveParticipantsResponseInternal;
import com.azure.core.annotation.Immutable;

import java.util.Objects;

/** The RemoveParticipantsResponse model. */
@Immutable
public final class RemoveParticipantsResponse {
    /*
     * The operation context provided by client.
     */
    private final String operationContext;

    static {
        RemoveParticipantsResponseConstructorProxy.setAccessor(
            new RemoveParticipantsResponseConstructorProxy.RemoveParticipantsResponseConstructorAccessor() {
                @Override
                public RemoveParticipantsResponse create(RemoveParticipantsResponseInternal internalHeaders) {
                    return new RemoveParticipantsResponse(internalHeaders);
                }
            });
    }

    /**
     * Public constructor.
     *
     */
    public RemoveParticipantsResponse() {
        this.operationContext = null;
    }

    /**
     * Package-private constructor of the class, used internally only.
     *
     * @param  removeParticipantsResponseInternal The response from the service
     */
    RemoveParticipantsResponse(RemoveParticipantsResponseInternal removeParticipantsResponseInternal) {
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
