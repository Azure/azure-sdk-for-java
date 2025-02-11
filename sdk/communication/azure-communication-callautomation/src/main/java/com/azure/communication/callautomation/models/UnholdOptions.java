// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.models;

import com.azure.communication.common.CommunicationIdentifier;

/**
 * Options for the Unhold operation.
 */
public final class UnholdOptions {

    /**
     * Participant to put on unhold.
     */
    private final CommunicationIdentifier targetParticipant;

    /**
     * Operation context.
     */
    private String operationContext;

    /**
     * Create a new UnholdOptions object.
     * @param targetParticipant Participant to be put on unhold.
     */
    public UnholdOptions(CommunicationIdentifier targetParticipant) {
        this.targetParticipant = targetParticipant;
    }

    /**
     * Get Participant to be put on unhold.
     * @return participant.
     */
    public CommunicationIdentifier getTargetParticipant() {
        return targetParticipant;
    }

    /**
     * Get the operation context.
     * @return operation context.
     */
    public String getOperationContext() {
        return operationContext;
    }

    /**
     * Sets the operation context.
     * @param operationContext Operation Context
     * @return The UnholdOptions object.
     */
    public UnholdOptions setOperationContext(String operationContext) {
        this.operationContext = operationContext;
        return this;
    }
}
