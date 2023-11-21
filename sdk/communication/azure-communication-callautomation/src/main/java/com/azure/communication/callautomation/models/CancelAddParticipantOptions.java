// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.models;

import com.azure.core.annotation.Fluent;

/**
 * The options for cancelling add participant.
 */
@Fluent
public final class CancelAddParticipantOptions {
    /**
     * The inviation ID used to cancel the add participant request.
     */
    private final String invitationId;

    /**
     * The operational context
     */
    private String operationContext;

    /**
     * Callback URI override
     */
    private String callbackUrl;

    /**
     * Constructor
     *
     * @param invitationId The inviation ID used to cancel the add participant request.
     */
    public CancelAddParticipantOptions(String invitationId) {
        this.invitationId = invitationId;
    }

    /**
     * Get the invitationId.
     *
     * @return invitationId
     */
    public String getInvitationId() {
        return invitationId;
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
     * Get the callback URI override.
     *
     * @return the callbackUriOverride
     */
    public String getCallbackUrl() {
        return callbackUrl;
    }

    /**
     * Set the operationContext.
     *
     * @param operationContext the operationContext to set
     * @return the CancelAddParticipantOptions object itself.
     */
    public CancelAddParticipantOptions setOperationContext(String operationContext) {
        this.operationContext = operationContext;
        return this;
    }

    /**
     * Set the callbackUriOverride.
     *
     * @param callbackUrl the callbackUriOverride to set
     * @return the CancelAddParticipantOptions object itself.
     */
    public CancelAddParticipantOptions setCallbackUrl(String callbackUrl) {
        this.callbackUrl = callbackUrl;
        return this;
    }
}
