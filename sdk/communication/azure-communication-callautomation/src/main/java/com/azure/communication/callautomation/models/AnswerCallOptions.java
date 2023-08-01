// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.models;

import com.azure.core.annotation.Fluent;

/**
 * The options for creating a call.
 */
@Fluent
public final class AnswerCallOptions {
    /**
     * The incoming call context.
     */
    private final String incomingCallContext;

    /**
     * The call back URl.
     */
    private final String callbackUrl;


    /*
     * The endpoint URL of the Azure Cognitive Services resource attached
     */
    private String cognitiveServicesEndpoint;

    /**
     * The operational context
     */
    private String operationContext;

    /**
     * Constructor
     *
     * @param incomingCallContext The incoming call context.
     * @param callbackUrl The call back URl.
     */
    public AnswerCallOptions(String incomingCallContext, String callbackUrl) {
        this.incomingCallContext = incomingCallContext;
        this.callbackUrl = callbackUrl;
    }

    /**
     * Get the incomingCallContext.
     *
     * @return the incomingCallContext.
     */
    public String getIncomingCallContext() {
        return incomingCallContext;
    }

    /**
     * Get the call back url.
     *
     * @return the call back url.
     */
    public String getCallbackUrl() {
        return callbackUrl;
    }

    /**
     * Get the cognitiveServicesEndpoint property: The endpoint URL of the Azure Cognitive Services resource
     * attached.
     *
     * @return the cognitiveServicesEndpoint value.
     */
    public String getCognitiveServicesEndpoint() {
        return this.cognitiveServicesEndpoint;
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
     * Set the cognitiveServicesEndpoint property: The endpoint URL of the Azure Cognitive Services resource
     * attached.
     *
     * @param cognitiveServicesEndpoint the cognitiveServicesEndpoint value to set.
     * @return the AnswerCallOptions object itself.
     */
    public AnswerCallOptions setCognitiveServicesEndpoint(String cognitiveServicesEndpoint) {
        this.cognitiveServicesEndpoint = cognitiveServicesEndpoint;
        return this;
    }

     /**
     * Set the operationContext.
     *
     * @param operationContext the operationContext to set
     * @return the AnswerCallOptions object itself.
     */
    public AnswerCallOptions setOperationContext(String operationContext) {
        this.operationContext = operationContext;
        return this;
    }
}
