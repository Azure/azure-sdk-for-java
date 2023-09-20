// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.models;

import com.azure.communication.common.CommunicationIdentifier;
import com.azure.communication.common.PhoneNumberIdentifier;
import com.azure.core.annotation.Fluent;

import java.util.HashMap;
import java.util.List;

/**
 * The options for creating a group call.
 */
@Fluent
public final class CreateGroupCallOptions {
    /**
     * The targets of the call.
     */
    private final List<CommunicationIdentifier> targetParticipants;

    /**
     * The call back URI.
     */
    private final String callbackUrl;

    /*
     * The endpoint URL of the Azure Cognitive Services resource attached
     */
    private String azureCognitiveServicesUrl;

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
     * Custom Context
     */
    private final CustomContext customContext;




    /**
     * Constructor
     *
     * @param targetParticipants The targets of the call.
     * @param callbackUrl The call back URI.
     */
    public CreateGroupCallOptions(List<CommunicationIdentifier> targetParticipants, String callbackUrl) {
        this.targetParticipants = targetParticipants;
        this.callbackUrl = callbackUrl;
        this.sourceDisplayName = null;
        this.sourceCallIdNumber = null;
        this.customContext = new CustomContext(new HashMap<String, String>(), new HashMap<String, String>());
    }

    /**
     * Get the targets.
     *
     * @return the targets list.
     */
    public List<CommunicationIdentifier> getTargetParticipants() {
        return targetParticipants;
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
    public String getAzureCognitiveServicesUrl() {
        return this.azureCognitiveServicesUrl;
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
     *  get custom context
     * @return custom context
     */
    public CustomContext getCustomContext() {
        return customContext;
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
     * @param azureCognitiveServicesUrl the azureCognitiveServicesEndpointUrl value to set.
     * @return the AnswerCallRequestInternal object itself.
     */
    public CreateGroupCallOptions setAzureCognitiveServicesUrl(String azureCognitiveServicesUrl) {
        this.azureCognitiveServicesUrl = azureCognitiveServicesUrl;
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
}
