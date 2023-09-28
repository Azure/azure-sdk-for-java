// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.models;

import java.util.Objects;

import com.azure.communication.callautomation.implementation.accesshelpers.CancelAddParticipantResponseConstructorProxy;
import com.azure.communication.callautomation.implementation.accesshelpers.CancelAddParticipantResponseConstructorProxy.CancelAddParticipantResponseConstructorAccessor;
import com.azure.communication.callautomation.implementation.models.CancelAddParticipantResponse;
import com.azure.core.annotation.Immutable;

/** The CancelAddParticipantResult model. */
@Immutable
public final class CancelAddParticipantResult {

    /**
     * The invitation ID used to cancel the add participant request.
     */
    private final String invitationId;

    /**
     * The operation context provided by client.
     */
    private final String operationContext;

    static {
        CancelAddParticipantResponseConstructorProxy.setAccessor(
                new CancelAddParticipantResponseConstructorAccessor() {
                    @Override
                    public CancelAddParticipantResult create(CancelAddParticipantResponse internalHeaders) {
                        return new CancelAddParticipantResult(internalHeaders);
                    }
                });
    }

    /**
     * Public constructor.
     */
    public CancelAddParticipantResult() {
        invitationId = null;
        operationContext = null;
    }

    /**
     * Package-private constructor of the class, used internally only.
     * 
     * @param cancelAddParticipantResponseInternal The response from the service.
     */
    CancelAddParticipantResult(CancelAddParticipantResponse cancelAddParticipantResponseInternal) {
        Objects.requireNonNull(cancelAddParticipantResponseInternal,
                "cancelAddParticipantResponseInternal must not be null");

        invitationId = cancelAddParticipantResponseInternal.getInvitationId();
        operationContext = cancelAddParticipantResponseInternal.getOperationContext();
    }

    /**
     * Get the invitationId property: The invitation ID used to cancel the add
     * participant request.
     * 
     * @return the invitationId value.
     */
    public String getInvitationId() {
        return invitationId;
    }

    /**
     * Get the operationContext property: The operation context provided by client.
     * 
     * @return the operationContext value.
     */
    public String getOperationContext() {
        return operationContext;
    }
}
