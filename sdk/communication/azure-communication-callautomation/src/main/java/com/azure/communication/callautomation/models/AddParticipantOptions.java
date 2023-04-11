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
     * Information for the target being add
     */
    private final CallInvite targetCallInvite;

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
     * Constructor
     * @param targetCallInvite target callinvite
     */
    public AddParticipantOptions(CallInvite targetCallInvite) {
        this.targetCallInvite = targetCallInvite;
    }

    /**
     * Get Information for participant to add
     * @return target callInvite
     */
    public CallInvite getTargetCallInvite() {
        return targetCallInvite;
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
