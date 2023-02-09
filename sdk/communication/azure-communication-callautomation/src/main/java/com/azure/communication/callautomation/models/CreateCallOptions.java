// Copyright (c) Microsoft Corporation. All rights reserved.

package com.azure.communication.callautomation.models;

import java.time.Instant;
import java.util.UUID;

import com.azure.core.annotation.Fluent;

/**
 * The options for creating a p2p call.
 */
@Fluent
public class CreateCallOptions {
	
	/**
     * The source property.
     */
    private final CallSource source;
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
	
    
    public CreateCallOptions(CallSource source, CallInvite callInvite, String callbackUri) {
    	this.source = source;
    	this.callInvite = callInvite;
    	this.callbackUrl = callbackUri;
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
     * Get the azureCognitiveServicesEndpointUrl property: The endpoint URL of the Azure Cognitive Services resource
     * attached.
     *
     * @return the azureCognitiveServicesEndpointUrl value.
     */
    public String getAzureCognitiveServicesEndpointUrl() {
		return azureCognitiveServicesEndpointUrl;
	}
    
    /**
     * Set the azureCognitiveServicesEndpointUrl property: The endpoint URL of the Azure Cognitive Services resource
     * attached.
     *
     * @param azureCognitiveServicesEndpointUrl the azureCognitiveServicesEndpointUrl value to set.
     * @return the AnswerCallRequestInternal object itself.
     */
	public CreateCallOptions setAzureCognitiveServicesEndpointUrl(String azureCognitiveServicesEndpointUrl) {
		this.azureCognitiveServicesEndpointUrl = azureCognitiveServicesEndpointUrl;
		return this;
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
     * @return the CreateCallOptions object itself.
     */
	public CreateCallOptions setMediaStreamingConfiguration(MediaStreamingOptions mediaStreamingOptions) {
		this.mediaStreamingOptions = mediaStreamingOptions;
		return this;
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
     * Set the repeatability headers
     *
     * @param repeatabilityHeaders The repeatability headers configuration.
     * @return the CreateCallOptions object itself.
     */
	public CreateCallOptions setRepeatabilityHeaders(RepeatabilityHeaders repeatabilityHeaders) {
		this.repeatabilityHeaders = repeatabilityHeaders;
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
}
