// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.models;

import com.azure.core.annotation.Fluent;

/**
 * The options for cancelling add participant operation.
 */
@Fluent
public final class CancelAddParticipantOperationOptions {
    /**
     * The invitation ID used to cancel the add participant request.
     */
    private final String invitationId;

    /**
     * The operational context
     */
    private String operationContext;

    /**
     * Set a callback URI that overrides the default callback URI set by CreateCall/AnswerCall for this operation.
     * This setup is per-action. If this is not set, the default callback URI set by CreateCall/AnswerCall will be used.
     */
    private String operationCallbackUrl;

    /**
     * Constructor
     *
     * @param invitationId The invitation ID used to cancel the add participant request.
     */
    public CancelAddParticipantOperationOptions(String invitationId) {
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
     * Get the overridden call back URL override for operation.
     *
     * @return the operationCallbackUrl
     */
    public String getOperationCallbackUrl() {
        return operationCallbackUrl;
    }

    /**
     * Set the operationContext.
     *
     * @param operationContext the operationContext to set
     * @return the CancelAddParticipantOptions object itself.
     */
    public CancelAddParticipantOperationOptions setOperationContext(String operationContext) {
        this.operationContext = operationContext;
        return this;
    }

    /**
     * Set a callback URI that overrides the default callback URI set by CreateCall/AnswerCall for this operation.
     * This setup is per-action. If this is not set, the default callback URI set by CreateCall/AnswerCall will be used.
     *
     * @param operationCallbackUrl the operationCallbackUrl to set
     * @return the CancelAddParticipantOptions object itself.
     */
    public CancelAddParticipantOperationOptions setOperationCallbackUrl(String operationCallbackUrl) {
        this.operationCallbackUrl = operationCallbackUrl;
        return this;
    }
}
