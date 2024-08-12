// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.models;
import com.azure.core.annotation.Fluent;

/**
 * Options for the Stop media streaming operation.
 */
@Fluent
public final class StopMediaStreamingOptions {
/*
     * Set a callback URL that overrides the default callback URL set by CreateCall/AnswerCall for this operation.
     * This setup is per-action. If this is not set, the default callback URL set by CreateCall/AnswerCall will be
     * used.
     */
    private String operationCallbackUrl;

    /**
     * Creates an instance of StopMediaStreamingOptions class.
     */
    public StopMediaStreamingOptions() {
    }

    /**
     * Get the operationCallbackUrl property: Set a callback URL that overrides the default callback URL set by
     * CreateCall/AnswerCall for this operation.
     * This setup is per-action. If this is not set, the default callback URL set by CreateCall/AnswerCall will be
     * used.
     * 
     * @return the operationCallbackUrl value.
     */
    public String getOperationCallbackUrl() {
        return this.operationCallbackUrl;
    }

    /**
     * Set the operationCallbackUrl property: Set a callback URL that overrides the default callback URL set by
     * CreateCall/AnswerCall for this operation.
     * This setup is per-action. If this is not set, the default callback URL set by CreateCall/AnswerCall will be
     * used.
     * 
     * @param operationCallbackUrl the operationCallbackUrl value to set.
     * @return the StopMediaStreamingOptions object itself.
     */
    public StopMediaStreamingOptions setOperationCallbackUrl(String operationCallbackUrl) {
        this.operationCallbackUrl = operationCallbackUrl;
        return this;
    }
}
