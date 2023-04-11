// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.models;

import com.azure.core.annotation.Fluent;

/**
 * The options for creating a call.
 */
@Fluent
public class AnswerCallOptions {
    /**
     * The incoming call context.
     */
    private final String incomingCallContext;

    /**
     * The call back URI.
     */
    private final String callbackUrl;

    /**
     * Media Streaming Configuration.
     */
    private MediaStreamingOptions mediaStreamingOptions;

    /*
     * The endpoint URL of the Azure Cognitive Services resource attached
     */
    private String azureCognitiveServicesEndpointUrl;

    /**
     * Constructor
     *
     * @param incomingCallContext The incoming call context.
     * @param callbackUrl The call back URI.
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
     * Get the call back uri.
     *
     * @return the call back uri.
     */
    public String getCallbackUrl() {
        return callbackUrl;
    }

    /**
     * Get the azureCognitiveServicesEndpointUrl property: The endpoint URL of the Azure Cognitive Services resource
     * attached.
     *
     * @return the azureCognitiveServicesEndpointUrl value.
     */
    public String getAzureCognitiveServicesEndpointUrl() {
        return this.azureCognitiveServicesEndpointUrl;
    }

    /**
     * Get the Media Streaming configuration.
     *
     * @return the mediaStreamingConfiguration.
     */
    public MediaStreamingOptions getMediaStreamingConfiguration() {
        return mediaStreamingOptions;
    }

    /**
     * Set the media streaming configuration.
     *
     * @param mediaStreamingOptions The media streaming configuration.
     * @return the AnswerCallOptions object itself.
     */
    public AnswerCallOptions setMediaStreamingConfiguration(MediaStreamingOptions mediaStreamingOptions) {
        this.mediaStreamingOptions = mediaStreamingOptions;
        return this;
    }

    /**
     * Set the azureCognitiveServicesEndpointUrl property: The endpoint URL of the Azure Cognitive Services resource
     * attached.
     *
     * @param azureCognitiveServicesEndpointUrl the azureCognitiveServicesEndpointUrl value to set.
     * @return the AnswerCallRequestInternal object itself.
     */
    public AnswerCallOptions setAzureCognitiveServicesEndpointUrl(String azureCognitiveServicesEndpointUrl) {
        this.azureCognitiveServicesEndpointUrl = azureCognitiveServicesEndpointUrl;
        return this;
    }
}
