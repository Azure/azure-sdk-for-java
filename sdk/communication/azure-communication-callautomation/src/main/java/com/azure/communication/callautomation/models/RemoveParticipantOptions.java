// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.models;


import com.azure.communication.common.CommunicationIdentifier;
import com.azure.core.annotation.Fluent;

/**
 * The options for removing participants.
 */
@Fluent
public final class RemoveParticipantOptions {
    /**
     * The list of participants to remove.
     */
    private final CommunicationIdentifier participant;

    /**
     * The operational context
     */
    private String operationContext;

    /**
     * Callback URI override
     */
    private String callbackUrl;

    /**
     * Constructor
     *
     * @param participant The list of participants to invite.
     */
    public RemoveParticipantOptions(CommunicationIdentifier participant) {
        this.participant = participant;
    }

    /**
     * Get the participants.
     *
     * @return the participant being removed
     */
    public CommunicationIdentifier getParticipant() {
        return participant;
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
     * Get the callback URI override.
     *
     * @return the callbackUriOverride
     */
    public String getCallbackUrl() {
        return callbackUrl;
    }

    /**
     * Set the operationContext.
     *
     * @param operationContext the operationContext to set
     * @return the RemoveParticipantOptions object itself.
     */
    public RemoveParticipantOptions setOperationContext(String operationContext) {
        this.operationContext = operationContext;
        return this;
    }

    /**
     * Set the callbackUriOverride.
     *
     * @param callbackUrl the callbackUriOverride to set
     * @return the RemoveParticipantOptions object itself.
     */
    public RemoveParticipantOptions setCallbackUrl(String callbackUrl) {
        this.callbackUrl = callbackUrl;
        return this;
    }
}
