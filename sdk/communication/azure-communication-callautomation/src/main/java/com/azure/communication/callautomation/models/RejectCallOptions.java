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
     * Repeatability Headers Configuration
     */
    private RepeatabilityHeaders repeatabilityHeaders;

    /**
     * Constructor
     *
     * @param incomingCallContext The incoming call context.
     */
    public RejectCallOptions(String incomingCallContext) {
        this.incomingCallContext = incomingCallContext;
        callRejectReason = CallRejectReason.NONE;
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
     * Get the Repeatability headers configuration.
     *
     * @return the repeatabilityHeaders
     */
    public RepeatabilityHeaders getRepeatabilityHeaders() {
        return repeatabilityHeaders;
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

    /**
     * Set the repeatability headers
     *
     * @param repeatabilityHeaders The repeatability headers configuration.
     * @return the RejectCallOptions object itself.
     */
    public RejectCallOptions setRepeatabilityHeaders(RepeatabilityHeaders repeatabilityHeaders) {
        this.repeatabilityHeaders = repeatabilityHeaders;
        return this;
    }
}
