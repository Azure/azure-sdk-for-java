// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.models;

/**
 * The options for creating a call.
 */
public final class RedirectCallOptions {
    /**
     * The incoming call context.
     */
    private final String incomingCallContext;

    /**
     * Information of target of being redirected to.
     */
    private final CallInvite targetParticipant;

    /**
     * Constructor
     *
     * @param incomingCallContext The incoming call context.
     * @param targetParticipant Information of target of being redirected to.
     */
    public RedirectCallOptions(String incomingCallContext, CallInvite targetParticipant) {
        this.incomingCallContext = incomingCallContext;
        this.targetParticipant = targetParticipant;
    }

    /**
     * Get the incomingCallContext.
     *
     * @return the incomingCallContext.
     */
    public String getIncomingCallContext() {
        return incomingCallContext;
    }

    /**
     * Information of target of being redirected to
     * @return the callInvite to redirect target
     */
    public CallInvite getTargetParticipant() {
        return targetParticipant;
    }
}
