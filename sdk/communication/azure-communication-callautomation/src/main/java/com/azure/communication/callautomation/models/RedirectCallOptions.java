// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.models;

import com.azure.communication.common.CommunicationIdentifier;
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
     * The target of being redirected to.
     */
    private final CommunicationIdentifier target;

    /**
     * Repeatability Headers Configuration
     */
    private RepeatabilityHeaders repeatabilityHeaders;

    /**
     * Constructor
     *
     * @param incomingCallContext The incoming call context.
     * @param target The target of being redirected to.
     */
    public RedirectCallOptions(String incomingCallContext, CommunicationIdentifier target) {
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
     * Get the target
     *
     * @return the target
     */
    public CommunicationIdentifier getTarget() {
        return target;
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
     * Set the repeatability headers
     *
     * @param repeatabilityHeaders The repeatability headers configuration.
     * @return the RedirectCallOptions object itself.
     */
    public RedirectCallOptions setRepeatabilityHeaders(RepeatabilityHeaders repeatabilityHeaders) {
        this.repeatabilityHeaders = repeatabilityHeaders;
        return this;
    }
}
