// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.models;

import com.azure.communication.common.CommunicationIdentifier;
import com.azure.core.annotation.Fluent;

import java.time.Instant;
import java.util.UUID;

/**
 * The options for muting all participants.
 */
@Fluent
public final class MuteAllParticipantsOptions {
    /**
     * The participant that emitted the request.
     */
    private CommunicationIdentifier requestInitiator;

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
     */
    public MuteAllParticipantsOptions() {
        this.repeatabilityHeaders = new RepeatabilityHeaders(UUID.fromString("0-0-0-0-0"), Instant.MIN);
    }

    /**
     * Get the participant.
     *
     * @return the participants to be mute.
     */
    public CommunicationIdentifier getRequestInitiator() {
        return requestInitiator;
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
     * @return the MuteAllParticipantsOptions object itself.
     */
    public MuteAllParticipantsOptions setRepeatabilityHeaders(RepeatabilityHeaders repeatabilityHeaders) {
        this.repeatabilityHeaders = repeatabilityHeaders;
        return this;
    }

    /**
     * Set the operationContext.
     *
     * @param operationContext the operationContext to set
     * @return the MuteAllParticipantsOptions object itself.
     */
    public MuteAllParticipantsOptions setOperationContext(String operationContext) {
        this.operationContext = operationContext;
        return this;
    }

    /**
     * Set the request initiator.
     * @param requestInitiator the participant that initiated the request.
     * @return the MuteAllParticipantsOptions object itself.
     */
    public MuteAllParticipantsOptions setRequestInitiator(CommunicationIdentifier requestInitiator) {
        this.requestInitiator = requestInitiator;
        return this;
    }
}
