// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.models;

import com.azure.communication.common.CommunicationIdentifier;
import com.azure.core.annotation.Fluent;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * The options for creating a group call.
 */
@Fluent
public class CreateGroupCallOptions {
    /**
     * The source property.
     */
    private final CallSource source;

    /**
     * The targets of the call.
     */
    private final List<CommunicationIdentifier> targets;

    /**
     * The call back URI.
     */
    private final String callbackUrl;

    /*
     * The endpoint URL of the Azure Cognitive Services resource attached
     */
    private String azureCognitiveServicesEndpointUrl;

    /**
     * A customer set value used to track the answering of a call.
     */
    private String operationContext;

    /**
     * Media Streaming Configuration.
     */
    private MediaStreamingOptions mediaStreamingOptions;

    /**
     * Repeatability Headers Configuration
     */
    private RepeatabilityHeaders repeatabilityHeaders;

    /**
     * Constructor
     *
     * @param source The source property.
     * @param targets The targets of the call.
     * @param callbackUrl The call back URI.
     */
    public CreateGroupCallOptions(CallSource source, List<CommunicationIdentifier> targets, String callbackUrl) {
        this.source = source;
        this.targets = targets;
        this.callbackUrl = callbackUrl;
        this.repeatabilityHeaders = new RepeatabilityHeaders(UUID.fromString("0-0-0-0-0"), Instant.MIN);
    }

    /**
     * Get the source.
     *
     * @return the source value.
     */
    public CallSource getSource() {
        return source;
    }

    /**
     * Get the targets.
     *
     * @return the targets list.
     */
    public List<CommunicationIdentifier> getTargets() {
        return targets;
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
     * Get the operationContext: A customer set value used to track the answering of a call.
     *
     * @return the operationContext value.
     */
    public String getOperationContext() {
        return operationContext;
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
     * Get the Repeatability headers configuration.
     *
     * @return the repeatabilityHeaders
     */
    public RepeatabilityHeaders getRepeatabilityHeaders() {
        return repeatabilityHeaders;
    }

    /**
     * Set the operationContext: A customer set value used to track the answering of a call.
     *
     * @param operationContext A customer set value used to track the answering of a call.
     * @return the CreateCallOptions object itself.
     */
    public CreateGroupCallOptions setOperationContext(String operationContext) {
        this.operationContext = operationContext;
        return this;
    }

    /**
     * Set the media streaming configuration.
     *
     * @param mediaStreamingOptions The media streaming configuration.
     * @return the CreateCallOptions object itself.
     */
    public CreateGroupCallOptions setMediaStreamingConfiguration(MediaStreamingOptions mediaStreamingOptions) {
        this.mediaStreamingOptions = mediaStreamingOptions;
        return this;
    }

    /**
     * Set the repeatability headers
     *
     * @param repeatabilityHeaders The repeatability headers configuration.
     * @return the CreateCallOptions object itself.
     */
    public CreateGroupCallOptions setRepeatabilityHeaders(RepeatabilityHeaders repeatabilityHeaders) {
        this.repeatabilityHeaders = repeatabilityHeaders;
        return this;
    }

    /**
     * Set the azureCognitiveServicesEndpointUrl property: The endpoint URL of the Azure Cognitive Services resource
     * attached.
     *
     * @param azureCognitiveServicesEndpointUrl the azureCognitiveServicesEndpointUrl value to set.
     * @return the AnswerCallRequestInternal object itself.
     */
    public CreateGroupCallOptions setAzureCognitiveServicesEndpointUrl(String azureCognitiveServicesEndpointUrl) {
        this.azureCognitiveServicesEndpointUrl = azureCognitiveServicesEndpointUrl;
        return this;
    }
}
