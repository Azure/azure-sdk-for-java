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
     * The call back URI override.
     */
    private String callbackUrlOverride;

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
     * Get the call back URI override.
     *
     * @return the callbackUriOverride
     */
    public String getCallbackUrlOverride() {
        return callbackUrlOverride;
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
     * Set the call back URI override.
     *
     * @param callbackUrlOverride The call back URI override to set
     * @return the AddParticipantOptions object itself.
     */
    public AddParticipantOptions setCallbackUrlOverride(String callbackUrlOverride) {
        this.callbackUrlOverride = callbackUrlOverride;
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
