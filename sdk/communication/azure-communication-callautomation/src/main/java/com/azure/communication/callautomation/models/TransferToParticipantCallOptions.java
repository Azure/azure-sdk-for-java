// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.models;

import com.azure.core.annotation.Fluent;

/**
 * The options for adding participants.
 */
@Fluent
public class TransferToParticipantCallOptions {

    /**
     * Iformation for TranferTarget
     */
    private final CallInvite target;

    /**
     * The operational context
     */
    private String operationContext;


    /**
     * Constructor
     *
     * @param target {@link CallInvite}contains information for TranferTarget.
     */
    public TransferToParticipantCallOptions(CallInvite target) {
        this.target = target;
    }


    /**
     * Get the operationContext.
     *
     * @return the operationContext
     */
    public String  getOperationContext() {
        return operationContext;
    }

    /**
     * Set the operationContext.
     *
     * @param operationContext the operationContext to set
     * @return the TransferToParticipantCallOptions object itself.
     */
    public TransferToParticipantCallOptions setOperationContext(String operationContext) {
        this.operationContext = operationContext;
        return this;
    }

    /**
     * Get the call information to transfer target
     * @return a {@link CallInvite} with information to transfer target
     */
    public CallInvite getTarget() {
        return target;
    }

}
