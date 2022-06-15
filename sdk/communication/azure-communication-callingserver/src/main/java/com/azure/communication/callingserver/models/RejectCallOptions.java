// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver.models;

import com.azure.core.annotation.Fluent;

/**
 * The options for reject a call.
 */
@Fluent
public final class RejectCallOptions {
    /**
     * The reason why to reject the call.
     */
    private String callRejectReason;

    /**
     * Get the callRejectReason.
     *
     * @return the callRejectReason value.
     */
    public String getCallRejectReason() {
        return callRejectReason;
    }

    /**
     * Set the callRejectReason.
     *
     * @param callRejectReason the reason why to reject the call.
     * @return the callRejectReason object itself.
     */
    public RejectCallOptions setSubject(String callRejectReason) {
        this.callRejectReason = callRejectReason;
        return this;
    }
}
