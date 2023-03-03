// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.models;

import com.azure.core.annotation.Fluent;

/**
 * The options for creating a call.
 */
@Fluent
public class RedirectCallOptions {
    /**
     * The incoming call context.
     */
    private final String incomingCallContext;

    /**
     * Information of target of being redirected to.
     */
    private final CallInvite targetCallInvite;

    /**
     * Constructor
     *
     * @param incomingCallContext The incoming call context.
     * @param targetCallInvite Information of target of being redirected to.
     */
    public RedirectCallOptions(String incomingCallContext, CallInvite targetCallInvite) {
        this.incomingCallContext = incomingCallContext;
        this.targetCallInvite = targetCallInvite;
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
    public CallInvite getTargetCallImvite() {
        return targetCallInvite;
    }
}
