// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.models;

import com.azure.communication.common.CommunicationIdentifier;

/**
 * Options for the Hold operation.
 */
public final class HoldOptions {

    /**
     * Participant to put on hold.
     */
    private final CommunicationIdentifier targetParticipant;

    /**
     * Audio to play while on hold.
     */
    private PlaySource playSource;

    /**
     * Operation context.
     */
    private String operationContext;

    /**
     * operationCallbackUrl.
     */
    private String operationCallbackUrl;

    /**
     * Create a new HoldOptions object.
     * @param targetParticipant Participant to be put on hold.
     */
    public HoldOptions(CommunicationIdentifier targetParticipant) {
        this.targetParticipant = targetParticipant;
    }

    /**
     * Get Participant to be put on hold.
     * @return participant.
     */
    public CommunicationIdentifier getTargetParticipant() {
        return targetParticipant;
    }

    /**
     * Get PlaySourceInfo
     * @return the playSourceInfo.
     */
    public PlaySource getPlaySource() {
        return playSource;
    }

    /**
     * Sets the playSource.
     * @param playSource playSource
     * @return The HoldOptions object.
     */
    public HoldOptions setPlaySource(PlaySource playSource) {
        this.playSource = playSource;
        return this;
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
     * @return The HoldOptions object.
     */
    public HoldOptions setOperationContext(String operationContext) {
        this.operationContext = operationContext;
        return this;
    }

    /**
     * Get the operationCallbackUrl.
     * @return operationCallbackUrl.
     */
    public String getOperationCallbackUrl() {
        return operationCallbackUrl;
    }

    /**
     * Sets the operationCallbackUrl.
     * @param operationCallbackUrl operationCallbackUrl
     * @return The HoldOptions object.
     */
    public HoldOptions setOperationCallbackUri(String operationCallbackUrl) {
        this.operationCallbackUrl = operationCallbackUrl;
        return this;
    }
}
