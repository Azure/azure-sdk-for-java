// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.models;

import com.azure.core.annotation.Fluent;

/**
 * The options for creating a call.
 */
@Fluent
public class RejectCallOptions {
    /**
     * The incoming call context.
     */
    private final String incomingCallContext;

    /**
     * Reason of rejecting the call.
     */
    private CallRejectReason callRejectReason;

    /**
     * Constructor
     *
     * @param incomingCallContext The incoming call context.
     */
    public RejectCallOptions(String incomingCallContext) {
        this.incomingCallContext = incomingCallContext;
        this.callRejectReason = CallRejectReason.NONE;
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
     * Get the callRejectReason
     *
     * @return the callRejectReason
     */
    public CallRejectReason getCallRejectReason() {
        return callRejectReason;
    }

    /**
     * Set the callRejectReason
     *
     * @param callRejectReason The callRejectReason
     * @return the RejectCallOptions object itself.
     */
    public RejectCallOptions setCallRejectReason(CallRejectReason callRejectReason) {
        this.callRejectReason = callRejectReason;
        return this;
    }
}
