// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.models;

import com.azure.communication.common.CommunicationIdentifier;
import com.azure.core.annotation.Fluent;

/**
 * The options for muting a participant.
 */
@Fluent
public final class MuteParticipantOptions {
    /**
     * The participant to mute.
     * Only ACS Users are currently supported.
     */
    private final CommunicationIdentifier targetParticipant;

    /**
     * The operational context
     */
    private String operationContext;

    /**
     * Constructor
     *
     * @param targetParticipant The targetParticipant to mute.
     */
    public MuteParticipantOptions(CommunicationIdentifier targetParticipant) {
        this.targetParticipant = targetParticipant;
    }

    /**
     * Get the participant.
     *
     * @return the participant to mute.
     */
    public CommunicationIdentifier getTargetParticipant() {
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
     * @return the MuteParticipantOptions object itself.
     */
    public MuteParticipantOptions setOperationContext(String operationContext) {
        this.operationContext = operationContext;
        return this;
    }
}
