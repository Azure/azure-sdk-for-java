// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.models;

import com.azure.communication.common.CommunicationIdentifier;
import com.azure.communication.common.PhoneNumberIdentifier;
import com.azure.core.annotation.Fluent;

import java.util.List;
import java.util.Map;

/**
 * The options for creating a group call.
 */
@Fluent
public class CreateGroupCallOptions {
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
     * Display name for call source
     */
    private String sourceDisplayName;
    
    /**
     * PhoneNumber for call source when making PSTN call
     */
    private PhoneNumberIdentifier sourceCallIdNumber;
    
    /**
     * Custom Context for PSTN targets
     */
    private Map<String, String> sipHeaders;
    
    /**
     * Custom Context for Voip targets
     */
    private Map<String, String> voipHeaders;
    
    
    

    /**
     * Constructor
     *
     * @param targets The targets of the call.
     * @param callbackUrl The call back URI.
     */
    public CreateGroupCallOptions(List<CommunicationIdentifier> targets, String callbackUrl) {
        this.targets = targets;
        this.callbackUrl = callbackUrl;
        this.sourceDisplayName = null;
        this.sourceCallIdNumber = null;
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
     * get caller's display name
     * @return display name for caller
     */
    public String getSourceDisplayName() {
        return sourceDisplayName;
    }

    /**
     * get PhoneNumberIdentifier for PSTN caller
     * @return PhoneNumberIdentifier for PSTN caller
     */
    public PhoneNumberIdentifier getSourceCallIdNumber() {
        return sourceCallIdNumber;
    }
    
    /**
     * Get Custom Context for PSTN targets
     * @return Custom Context for PSTN targets
     */
    public Map<String, String> getSipHeaders() {
        return sipHeaders;
    }
    
    /**
     *  Get Custom Context for Voip targets
     * @return Custom Context for Voip targets
     */
    public Map<String, String> getVoipHeaders() {
        return voipHeaders;
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

    /**
     * set display name for caller
     * @param sourceDisplayName display name for caller
     * @return the CreateGroupCallOptions object itself
     */
    public CreateGroupCallOptions setSourceDisplayName(String sourceDisplayName) {
        this.sourceDisplayName = sourceDisplayName;
        return this;
    }

    /**
     * set PhoneNumberIdentifier for PSTN caller
     * @param sourceCallIdNumber PhoneNumberIdentifier for PSTN caller
     * @return the CreateGroupCallOptions object itself
     */
    public CreateGroupCallOptions setSourceCallIdNumber(PhoneNumberIdentifier sourceCallIdNumber) {
        this.sourceCallIdNumber = sourceCallIdNumber;
        return this;
    }
    
    
    /**
     * Set Custom Context for PSNT targets
     * @param sipHeaders collection of Custom Context for PSTN targets 
     * @return the CreateGroupCallOptions object itself
     */
    public CreateGroupCallOptions setSipHeaders(Map<String, String> sipHeaders) {
        this.sipHeaders = sipHeaders;
        return this;
    }
    
    
    /**
     * Set Custom Context for Voip targets
     * @param voipHeaders collection of Custom Context for Voip targets 
     * @return the CreateGroupCallOptions object itself
     */
    public CreateGroupCallOptions setVoipHeaders(Map<String, String> voipHeaders) {
        this.voipHeaders = voipHeaders;
        return this;
    }
    
    
}
