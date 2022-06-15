// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver.models;

import com.azure.core.annotation.Fluent;

/**
 * The options for remove participants.
 */
@Fluent
public final class RemoveParticipantsOptions {
    /**
     * The operation context
     */
    private String operationContext;

    /**
     * Get the operationContext.
     *
     * @return the operationContext value.
     */
    public String getOperationContext() {
        return operationContext;
    }

    /**
     * Set the callbackUri.
     *
     * @param operationContext the operationContext to set
     * @return the RemoveParticipantsOptions string itself.
     */
    public RemoveParticipantsOptions setOperationContext(String operationContext) {
        this.operationContext = operationContext;
        return this;
    }
}
