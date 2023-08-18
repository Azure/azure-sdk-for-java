// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.models;

import com.azure.communication.common.CommunicationIdentifier;
import com.azure.core.annotation.Fluent;

/**
 * The options for adding participants.
 */
@Fluent
public final class TransferCallToParticipantOptions {

    private final CommunicationIdentifier targetParticipant;

    /**
     * The operational context
     */
    private String operationContext;


    /**
     * Constructor
     *
     * @param targetParticipant {@link CommunicationIdentifier}contains information for TranferTarget.
     */
    public TransferCallToParticipantOptions(CommunicationIdentifier targetParticipant) {
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
     * @return a {@link CommunicationIdentifier} with information to transfer target
     */
    public CommunicationIdentifier getTargetParticipant() {
        return targetParticipant;
    }
}
