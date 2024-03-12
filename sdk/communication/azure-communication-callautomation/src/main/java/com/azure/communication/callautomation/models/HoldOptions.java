// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.models;

import com.azure.communication.common.CommunicationIdentifier;

/**
 * Options for the Hold operation.
 */
public class HoldOptions {

    /**
     * Participant to put on hold.
     */
    private final CommunicationIdentifier targetParticipant;

    /**
     * Audio to play while on hold.
     */
    private PlaySource playSourceInfo;

    /**
     * Operation context.
     */
    private String operationContext;

    /**
     * operationCallbackUri.
     */
    private String operationCallbackUri;

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
    public PlaySource getPlaySourceInfo() {
        return playSourceInfo;
    }

    /**
     * Sets the playSourceInfo.
     * @param playSourceInfo playSourceInfo
     * @return The HoldOptions object.
     */
    public HoldOptions setPlaySourceInfo(PlaySource playSourceInfo) {
        this.playSourceInfo = playSourceInfo;
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
     * Get the operationCallbackUri.
     * @return operationCallbackUri.
     */
    public String getOperationCallbackUri() {
        return operationCallbackUri;
    }

    /**
     * Sets the operationCallbackUri.
     * @param operationCallbackUri operationCallbackUri
     * @return The HoldOptions object.
     */
    public HoldOptions setOperationCallbackUri(String operationCallbackUri) {
        this.operationCallbackUri = operationCallbackUri;
        return this;
    }
}
