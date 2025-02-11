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
     * Set a callback URI that overrides the default callback URI set by CreateCall/AnswerCall for this operation.
     * This setup is per-action. If this is not set, the default callback URI set by CreateCall/AnswerCall will be used.
     */
    private String operationCallbackUrl;

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
    public String getOperationContext() {
        return operationContext;
    }

    /**
     * Get the overridden call back URL override for operation.
     *
     * @return the operationCallbackUrl
     */
    public String getOperationCallbackUrl() {
        return operationCallbackUrl;
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
     * Set a callback URI that overrides the default callback URI set by CreateCall/AnswerCall for this operation.
     * This setup is per-action. If this is not set, the default callback URI set by CreateCall/AnswerCall will be used.
     *
     * @param operationCallbackUrl the operationCallbackUrl to set
     * @return the RemoveParticipantOptions object itself.
     */
    public RemoveParticipantOptions setOperationCallbackUrl(String operationCallbackUrl) {
        this.operationCallbackUrl = operationCallbackUrl;
        return this;
    }
}
