// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.models;

import com.azure.communication.common.CommunicationIdentifier;
import com.azure.core.annotation.Fluent;

import java.util.List;

/**
 * The options for muting a participant.
 */
@Fluent
public final class MuteParticipantsOptions {
    /**
     * The participants to mute.
     * Only one participant currently supported.
     * Only ACS Users are currently supported.
     */
    private final List<CommunicationIdentifier> targetParticipant;

    /**
     * The operational context
     */
    private String operationContext;

    /**
     * Constructor
     *
     * @param targetParticipant The targetParticipant to mute.
     */
    public MuteParticipantsOptions(List<CommunicationIdentifier> targetParticipant) {
        this.targetParticipant = targetParticipant;
    }

    /**
     * Get the participant.
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
     * @return the RemoveParticipantsOptions object itself.
     */
    public MuteParticipantsOptions setOperationContext(String operationContext) {
        this.operationContext = operationContext;
        return this;
    }
}
