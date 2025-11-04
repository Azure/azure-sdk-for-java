// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.models;

import com.azure.communication.common.CommunicationIdentifier;

/**
 * Options for the Unhold operation.
 */
public final class UnholdOptions {

    /*
     * Participants to be hold from the call.
     * Only ACS Users are supported.
     */
    private final CommunicationIdentifier targetParticipant;

    /**
     * Operation context.
     */
    private String operationContext;

    /*
     * Set a callback URL that overrides the default callback URL set by CreateCall/AnswerCall for this operation.
     * This setup is per-action. If this is not set, the default callback URL set by CreateCall/AnswerCall will be used.
     */
    private String operationCallbackUrl;

    /**
     * Create a new UnholdOptions object.
     * @param targetParticipant Participant to be put on unhold.
     */
    public UnholdOptions(CommunicationIdentifier targetParticipant) {
        this.targetParticipant = targetParticipant;
    }

    /**
     * Get Participant to be put on unhold.
     * @return participant.
     */
    public CommunicationIdentifier getTargetParticipant() {
        return targetParticipant;
    }

    /**
     * Get the operation context.
     * @return operation context.
     */
    public String getOperationContext() {
        return operationContext;
    }

    /**
     * Sets the operation context.
     * @param operationContext Operation Context
     * @return The UnholdOptions object.
     */
    public UnholdOptions setOperationContext(String operationContext) {
        this.operationContext = operationContext;
        return this;
    }

    /**
    * Get the operationCallbackUrl property: Set a callback URI that overrides the default callback URL set by
    * CreateCall/AnswerCall for this operation.
    * This setup is per-action. If this is not set, the default callback URL set by CreateCall/AnswerCall will be used.
    * 
    * @return the operationCallbackUrl value.
    */
    public String getOperationCallbackUrl() {
        return this.operationCallbackUrl;
    }

    /**
     * Set the operationCallbackUrl property: Set a callback URI that overrides the default callback URI set by
     * CreateCall/AnswerCall for this operation.
     * This setup is per-action. If this is not set, the default callback URI set by CreateCall/AnswerCall will be used.
     * 
     * @param operationCallbackUrl the operationCallbackUrl value to set.
     * @return the UnholdRequest object itself.
     */
    public UnholdOptions setOperationCallbackUrl(String operationCallbackUrl) {
        this.operationCallbackUrl = operationCallbackUrl;
        return this;
    }
}
