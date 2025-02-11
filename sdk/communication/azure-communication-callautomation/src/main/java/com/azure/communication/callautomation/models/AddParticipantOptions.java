// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.models;

import com.azure.core.annotation.Fluent;

import java.time.Duration;

/**
 * The options for adding participants.
 */
@Fluent
public final class AddParticipantOptions {

    /**
     * Information for the target being added
     */
    private final CallInvite targetParticipant;

    /**
     * The operational context
     */
    private String operationContext;

    /**
     * The timeout to wait for the invited participant to pickup.
     * The maximum value of this is 180 seconds.
     */
    private Duration invitationTimeout;

    /**
     * The overridden call back URL override for operation.
     */
    private String operationCallbackUrl;

    /**
     * Constructor
     * @param targetParticipant target callinvite
     */
    public AddParticipantOptions(CallInvite targetParticipant) {
        this.targetParticipant = targetParticipant;
    }

    /**
     * Get Information for participant to add
     * @return target callInvite
     */
    public CallInvite getTargetParticipant() {
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
     * Get the overridden call back URL override for operation.
     *
     * @return the operationCallbackUrl
     */
    public String getOperationCallbackUrl() {
        return operationCallbackUrl;
    }

    /**
     * Get the invitationTimeoutInSeconds.
     *
     * @return the Invitation Timeout In Seconds
     */
    public Duration getInvitationTimeout() {
        return invitationTimeout;
    }

    /**
     * Set the operationContext.
     *
     * @param operationContext the operationContext to set
     * @return the AddParticipantOptions object itself.
     */
    public AddParticipantOptions setOperationContext(String operationContext) {
        this.operationContext = operationContext;
        return this;
    }

    /**
     * Set a callback URI that overrides the default callback URI set by CreateCall/AnswerCall for this operation.
     * This setup is per-action. If this is not set, the default callback URI set by CreateCall/AnswerCall will be used.
     *
     * @param operationCallbackUrl the operationCallbackUrl to set
     * @return the AddParticipantOptions object itself.
     */
    public AddParticipantOptions setOperationCallbackUrl(String operationCallbackUrl) {
        this.operationCallbackUrl = operationCallbackUrl;
        return this;
    }

    /**
     * Set the invitationTimeoutInSeconds.
     *
     * @param invitationTimeout Set the timeout to wait for the invited participant to pickup.
     *                                   The maximum value of this is 180 seconds.
     * @return the AddParticipantOptions object itself.
     */
    public AddParticipantOptions setInvitationTimeout(Duration invitationTimeout) {
        this.invitationTimeout = invitationTimeout;
        return this;
    }
}
