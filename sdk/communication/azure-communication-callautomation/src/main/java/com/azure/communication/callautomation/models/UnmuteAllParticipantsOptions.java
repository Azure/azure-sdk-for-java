// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.models;

import com.azure.core.annotation.Fluent;

import java.time.Instant;
import java.util.UUID;

/**
 * The options for muting all participants.
 */
@Fluent
public final class UnmuteAllParticipantsOptions {
    /**
     * The operational context
     */
    private String operationContext;

    /**
     * Repeatability Headers Configuration
     */
    private RepeatabilityHeaders repeatabilityHeaders;

    /**
     * Constructor
     *
     */
    public UnmuteAllParticipantsOptions() {
        this.repeatabilityHeaders = new RepeatabilityHeaders(UUID.fromString("0-0-0-0-0"), Instant.MIN);
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
     * @return the MuteAllParticipantsOptions object itself.
     */
    public UnmuteAllParticipantsOptions setRepeatabilityHeaders(RepeatabilityHeaders repeatabilityHeaders) {
        this.repeatabilityHeaders = repeatabilityHeaders;
        return this;
    }

    /**
     * Set the operationContext.
     *
     * @param operationContext the operationContext to set
     * @return the MuteAllParticipantsOptions object itself.
     */
    public UnmuteAllParticipantsOptions setOperationContext(String operationContext) {
        this.operationContext = operationContext;
        return this;
    }
}
