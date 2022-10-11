// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.models;

import com.azure.core.annotation.Fluent;

/**
 * The options for creating a call.
 */
@Fluent
public class HangUpOptions {
    /**
     * Boolean to determine if the call should be terminated for all participants.
     */
    private final boolean isForEveryone;

    /**
     * Repeatability Headers Configuration
     */
    private RepeatabilityHeaders repeatabilityHeaders;

    /**
     * Constructor
     *
     * @param isForEveryone Boolean to determine if the call should be terminated for all participants.
     */
    public HangUpOptions(boolean isForEveryone) {
        this.isForEveryone = isForEveryone;
    }

    /**
     * Get the boolean to determine if the call should be terminated for all participants.
     *
     * @return the isForEveryone.
     */
    public boolean getIsForEveryone() {
        return isForEveryone;
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
     * @return the HangUpOptions object itself.
     */
    public HangUpOptions setRepeatabilityHeaders(RepeatabilityHeaders repeatabilityHeaders) {
        this.repeatabilityHeaders = repeatabilityHeaders;
        return this;
    }
}
