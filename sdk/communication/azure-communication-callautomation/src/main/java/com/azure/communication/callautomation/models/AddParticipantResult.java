// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.models;

import com.azure.communication.callautomation.implementation.accesshelpers.AddParticipantResponseConstructorProxy;
import com.azure.communication.callautomation.implementation.converters.CallParticipantConverter;
import com.azure.communication.callautomation.implementation.models.AddParticipantResponseInternal;
import com.azure.core.annotation.Immutable;

import java.util.Objects;

/** The AddParticipantResult model. */
@Immutable
public class AddParticipantResult {
    /*
     * The participant property.
     */
    private final CallParticipant participant;

    /*
     * The operation context provided by client.
     */
    private final String operationContext;

    /*
     * The invitation ID used to send out add participant request.
     */
    private final String invitationId;

    static {
        AddParticipantResponseConstructorProxy.setAccessor(
            new AddParticipantResponseConstructorProxy.AddParticipantResponseConstructorAccessor() {
                @Override
                public AddParticipantResult create(AddParticipantResponseInternal internalHeaders) {
                    return new AddParticipantResult(internalHeaders);
                }
            });
    }

    /**
     * Public constructor.
     *
     */
    public AddParticipantResult() {
        super();
        this.participant = null;
        this.operationContext = null;
        this.invitationId = null;
    }

    /**
     * Constructor of the class
     *
     * @param addParticipantResponseInternal The response from the addParticipant service
     */
    AddParticipantResult(AddParticipantResponseInternal addParticipantResponseInternal) {
        super();
        Objects.requireNonNull(addParticipantResponseInternal, "addParticipantResponseInternal must not be null");

        this.participant = CallParticipantConverter.convert(addParticipantResponseInternal.getParticipant());
        this.operationContext = addParticipantResponseInternal.getOperationContext();
        this.invitationId = addParticipantResponseInternal.getInvitationId();
    }

    /**
     * Get the participant property: The participant property.
     *
     * @return the participant value.
     */
    public CallParticipant getParticipant() {
        return this.participant;
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
     * Get the invitationId property: The invitation ID used to send out add
     * participant request.
     *
     * @return the invitationId value.
     */
    public String getInvitationId() {
        return invitationId;
    }
}
