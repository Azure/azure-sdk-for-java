// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.models;

import com.azure.core.annotation.Fluent;

/**
 * The options for adding participants.
 */
@Fluent
public final class TransferCallToParticipantOptions {

    /**
     * Iformation for TranferTarget
     */
    private final CallInvite targetParticipant;

    /**
     * The operational context
     */
    private String operationContext;


    /**
     * Constructor
     *
     * @param targetParticipant {@link CallInvite}contains information for TranferTarget.
     */
    public TransferCallToParticipantOptions(CallInvite targetParticipant) {
        this.targetParticipant = targetParticipant;
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
     * @return the TransferCallToParticipantOptions object itself.
     */
    public TransferCallToParticipantOptions setOperationContext(String operationContext) {
        this.operationContext = operationContext;
        return this;
    }

    /**
     * Get the call information to transfer target
     * @return a {@link CallInvite} with information to transfer target
     */
    public CallInvite getTargetParticipant() {
        return targetParticipant;
    }

}
