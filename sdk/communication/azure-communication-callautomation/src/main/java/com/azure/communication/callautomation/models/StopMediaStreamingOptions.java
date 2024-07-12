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

    /*
     * The value to identify context of the operation.
     */
    private String operationContext;

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

    /**
     * Get the operationContext property: The value to identify context of the operation.
     * 
     * @return the operationContext value.
     */
    public String getOperationContext() {
        return this.operationContext;
    }

    /**
     * Set the operationContext property: The value to identify context of the operation.
     * 
     * @param operationContext the operationContext value to set.
     * @return the StopMediaStreamingOptions object itself.
     */
    public StopMediaStreamingOptions setOperationContext(String operationContext) {
        this.operationContext = operationContext;
        return this;
    }
}
