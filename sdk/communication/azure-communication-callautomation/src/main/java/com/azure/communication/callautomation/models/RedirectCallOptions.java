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
    private final CallInvite target;

    /**
     * Constructor
     *
     * @param incomingCallContext The incoming call context.
     * @param target Information of target of being redirected to.
     */
    public RedirectCallOptions(String incomingCallContext, CallInvite target) {
        this.incomingCallContext = incomingCallContext;
        this.target = target;
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
    public CallInvite getTarget() {
        return target;
    }
}
