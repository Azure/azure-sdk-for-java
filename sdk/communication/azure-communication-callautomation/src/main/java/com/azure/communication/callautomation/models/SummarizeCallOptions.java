// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.models;

import com.azure.core.annotation.Fluent;

/**
 * Options for the summarize call operation.
 */
@Fluent
public final class SummarizeCallOptions {

    /**
     * The value to identify context of the operation.
     */
    private String operationContext;

    /*
    * Set a callback URL that overrides the default callback URL set by CreateCall/AnswerCall for this operation.
    * This setup is per-action. If this is not set, the default callback URL set by CreateCall/AnswerCall will be
    * used.
    */
    private String operationCallbackUrl;

    /*
     * Summarization configuration options.
     */
    private SummarizationOptions summarizationOptions;

    /**
     * Creates an instance of {@link SummarizeCallOptions}.
     */
    public SummarizeCallOptions() {
    }

    /**
     * Get the operation context.
     *
     * @return operation context.
     */
    public String getOperationContext() {
        return operationContext;
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
     * Get the summarizationOptions property: Summarization configuration options.
     * 
     * @return the summarizationOptions value.
     */
    public SummarizationOptions getSummarizationOptions() {
        return this.summarizationOptions;
    }

    /**
     * Sets the operation context.
     *
     * @param operationContext Operation Context
     * @return The SummarizeCallOptions object.
     */
    public SummarizeCallOptions setOperationContext(String operationContext) {
        this.operationContext = operationContext;
        return this;
    }

    /**
     * Set the operationCallbackUrl property: Set a callback URL that overrides the default callback URL set by
     * CreateCall/AnswerCall for this operation.
     * This setup is per-action. If this is not set, the default callback URL set by CreateCall/AnswerCall will be
     * used.
     * 
     * @param operationCallbackUrl the operationCallbackUrl value to set.
     * @return the SummarizeCallOptions object itself.
     */
    public SummarizeCallOptions setOperationCallbackUrl(String operationCallbackUrl) {
        this.operationCallbackUrl = operationCallbackUrl;
        return this;
    }

    /**
     * Set the summarizationOptions property: Summarization configuration options.
     * 
     * @param summarizationOptions the summarizationOptions value to set.
     * @return the SummarizeCallOptions object itself.
     */
    public SummarizeCallOptions setSummarizationOptions(SummarizationOptions summarizationOptions) {
        this.summarizationOptions = summarizationOptions;
        return this;
    }
}
