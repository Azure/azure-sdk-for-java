// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.models;


import com.azure.communication.common.CommunicationIdentifier;
import com.azure.core.annotation.Fluent;

import java.util.List;

/**
 * The options for removing participants.
 */
@Fluent
public final class RemoveParticipantsOptions {
    /**
     * The list of participants to remove.
     */
    private final List<CommunicationIdentifier> participants;

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
     * @param participants The list of participants to invite.
     */
    public RemoveParticipantsOptions(List<CommunicationIdentifier> participants) {
        this.participants = participants;
    }

    /**
     * Get the participants.
     *
     * @return the list of participants to be added
     */
    public List<CommunicationIdentifier> getParticipants() {
        return participants;
    }

    /**
     * Get the operationContext.
     *
     * @return the operationContext
     */
    public String  getOperationContext() {
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
    public RemoveParticipantsOptions setRepeatabilityHeaders(RepeatabilityHeaders repeatabilityHeaders) {
        this.repeatabilityHeaders = repeatabilityHeaders;
        return this;
    }

    /**
     * Set the operationContext.
     *
     * @param operationContext the operationContext to set
     * @return the RemoveParticipantsOptions object itself.
     */
    public RemoveParticipantsOptions setOperationContext(String operationContext) {
        this.operationContext = operationContext;
        return this;
    }
}
