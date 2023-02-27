// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.models;

import com.azure.communication.common.CommunicationIdentifier;
import com.azure.core.annotation.Fluent;

import java.util.List;

/**
 * The options for unmuting a participant.
 */
@Fluent
public final class UnmuteParticipantsOptions {
    /**
     * The participants to unmute.
     *  Only one participant currently supported.
     *  Only ACS Users are currently supported.
     */
    private final List<CommunicationIdentifier> targetParticipant;

    /**
     * The operational context
     */
    private String operationContext;

    /**
     * Constructor
     *
     * @param targetParticipant The participants to unmute.
     */
    public UnmuteParticipantsOptions(List<CommunicationIdentifier> targetParticipant) {
        this.targetParticipant = targetParticipant;
    }

    /**
     * Get the participants.
     *
     * @return the participants to mute.
     */
    public List<CommunicationIdentifier> getTargetParticipant() {
        return targetParticipant;
    }

    /**
     * Get the operationContext.
     *
     * @return the operationContext
     */
    public String getOperationContext() {
        return operationContext;
    }

    /**
     * Set the operationContext.
     *
     * @param operationContext the operationContext to set
     * @return the UnmuteParticipantsOptions object itself.
     */
    public UnmuteParticipantsOptions setOperationContext(String operationContext) {
        this.operationContext = operationContext;
        return this;
    }
}
