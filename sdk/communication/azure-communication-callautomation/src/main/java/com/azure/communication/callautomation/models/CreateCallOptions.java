// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.models;

import com.azure.core.annotation.Fluent;

/**
 * The options for creating a p2p call.
 */
@Fluent
public final class CreateCallOptions {
    /**
     * Call invitee information.
     */
    private final CallInvite callInvite;

    /**
     * The call back URI.
     */
    private final String callbackUrl;

    /*
     * The endpoint URL of the Azure Cognitive Services resource attached
     */
    private String cognitiveServicesEndpoint;

    /**
     * A customer set value used to track the answering of a call.
     */
    private String operationContext;

    /**
     * Constructor
     * @param callInvite Call invitee information.
     * @param callbackUrl The call back URI.
     */
    public CreateCallOptions(CallInvite callInvite, String callbackUrl) {
        this.callInvite = callInvite;
        this.callbackUrl = callbackUrl;
    }

    /**
     * Get the operationContext: A customer set value used to track the answering of a call.
     *
     * @return the operationContext value.
     */
    public String getOperationContext() {
        return operationContext;
    }

    /**
     * Set the operationContext: A customer set value used to track the answering of a call.
     *
     * @param operationContext A customer set value used to track the answering of a call.
     * @return the CreateCallOptions object itself.
     */
    public CreateCallOptions setOperationContext(String operationContext) {
        this.operationContext = operationContext;
        return this;
    }

    /**
     *  Get Call invitee information
     * @return call invitee information
     */
    public CallInvite getCallInvite() {
        return callInvite;
    }

    /**
     * Get the call back uri.
     *
     * @return the call back uri.
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
     * Set the cognitiveServicesEndpoint property: The endpoint URL of the Azure Cognitive Services resource
     * attached.
     *
     * @param cognitiveServicesEndpoint the cognitiveServicesEndpoint value to set.
     * @return the CreateCallOptions object itself.
     */
    public CreateCallOptions setCognitiveServicesEndpoint(String cognitiveServicesEndpoint) {
        this.cognitiveServicesEndpoint = cognitiveServicesEndpoint;
        return this;
    }
}
