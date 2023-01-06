// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.models;


import com.azure.communication.common.CommunicationIdentifier;
import com.azure.core.annotation.Fluent;

import java.time.Instant;
import java.util.UUID;

/**
 * The options for removing participants.
 */
@Fluent
public final class MuteParticipantOptions {
    /**
     * The list of participants to remove.
     */
    private final CommunicationIdentifier participant;

    /**
     * The operational context
     */
    private String operationContext;

    /**
     * Repeatability Headers Configuration
     */
    private RepeatabilityHeaders repeatabilityHeaders;

    /**
     * Constructor
     *
     * @param participant The participant to mute.
     */
    public MuteParticipantOptions(CommunicationIdentifier participant) {
        this.participant = participant;
        this.repeatabilityHeaders = new RepeatabilityHeaders(UUID.fromString("0-0-0-0-0"), Instant.MIN);
    }

    /**
     * Get the participant.
     *
     * @return the participants to be mute.
     */
    public CommunicationIdentifier getParticipant() {
        return participant;
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
     * Get the Repeatability headers configuration.
     *
     * @return the repeatabilityHeaders
     */
    public RepeatabilityHeaders getRepeatabilityHeaders() {
        return repeatabilityHeaders;
    }

    /**
     * Set the repeatability headers
     *
     * @param repeatabilityHeaders The repeatability headers configuration.
     * @return the RemoveParticipantsOptions object itself.
     */
    public MuteParticipantOptions setRepeatabilityHeaders(RepeatabilityHeaders repeatabilityHeaders) {
        this.repeatabilityHeaders = repeatabilityHeaders;
        return this;
    }

    /**
     * Set the operationContext.
     *
     * @param operationContext the operationContext to set
     * @return the RemoveParticipantsOptions object itself.
     */
    public MuteParticipantOptions setOperationContext(String operationContext) {
        this.operationContext = operationContext;
        return this;
    }
}
