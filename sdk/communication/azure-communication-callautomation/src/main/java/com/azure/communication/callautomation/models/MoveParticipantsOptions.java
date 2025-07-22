// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.models;

import com.azure.communication.common.CommunicationIdentifier;
import com.azure.core.annotation.Fluent;

import java.util.List;

/**
 * The options for moving participants.
 */
@Fluent
public final class MoveParticipantsOptions {

    /**
     * List of participants to move
     */
    private final List<CommunicationIdentifier> targetParticipants;

    /**
     * The CallConnectionId for the call you want to move the participant from
     */
    private final String fromCall;

    /**
     * The operational context
     */
    private String operationContext;

    /**
     * The overridden call back URL override for operation.
     */
    private String operationCallbackUrl;

    /**
     * Constructor
     * @param targetParticipants List of participants to move
     * @param fromCall The CallConnectionId for the call you want to move the participants from
     */
    public MoveParticipantsOptions(List<CommunicationIdentifier> targetParticipants, String fromCall) {
        this.targetParticipants = targetParticipants;
        this.fromCall = fromCall;
    }

    /**
     * Get the list of participants to move
     * @return target participants
     */
    public List<CommunicationIdentifier> getTargetParticipants() {
        return targetParticipants;
    }

    /**
     * Get the CallConnectionId for the call you want to move the participants from
     * @return fromCall
     */
    public String getFromCall() {
        return fromCall;
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
     * @return the MoveParticipantsOptions object itself.
     */
    public MoveParticipantsOptions setOperationContext(String operationContext) {
        this.operationContext = operationContext;
        return this;
    }

    /**
     * Set a callback URI that overrides the default callback URI set by CreateCall/AnswerCall for this operation.
     * This setup is per-action. If this is not set, the default callback URI set by CreateCall/AnswerCall will be used.
     *
     * @param operationCallbackUrl the operationCallbackUrl to set
     * @return the MoveParticipantsOptions object itself.
     */
    public MoveParticipantsOptions setOperationCallbackUrl(String operationCallbackUrl) {
        this.operationCallbackUrl = operationCallbackUrl;
        return this;
    }
}
