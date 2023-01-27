// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.models;

import com.azure.communication.common.CommunicationIdentifier;
import com.azure.core.annotation.Fluent;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

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
     * Repeatability Headers Configuration
     */
    private RepeatabilityHeaders repeatabilityHeaders;

    /**
     * Constructor
     *
     * @param targetParticipant The participants to unmute.
     */
    public UnmuteParticipantsOptions(List<CommunicationIdentifier> targetParticipant) {
        this.targetParticipant = targetParticipant;
        this.repeatabilityHeaders = new RepeatabilityHeaders(UUID.fromString("0-0-0-0-0"), Instant.MIN);
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
    public UnmuteParticipantsOptions setRepeatabilityHeaders(RepeatabilityHeaders repeatabilityHeaders) {
        this.repeatabilityHeaders = repeatabilityHeaders;
        return this;
    }

    /**
     * Set the operationContext.
     *
     * @param operationContext the operationContext to set
     * @return the RemoveParticipantsOptions object itself.
     */
    public UnmuteParticipantsOptions setOperationContext(String operationContext) {
        this.operationContext = operationContext;
        return this;
    }
}
