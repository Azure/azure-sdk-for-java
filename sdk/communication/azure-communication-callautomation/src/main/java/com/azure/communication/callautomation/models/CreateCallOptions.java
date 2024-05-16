// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.models;

import java.util.HashMap;

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
     * AI options for the call.
     */
    private CallIntelligenceOptions callIntelligenceOptions;

    /**
     * A customer set value used to track the answering of a call.
     */
    private String operationContext;

   /**
     * Media Streaming Configuration.
     */
    private MediaStreamingOptions mediaStreamingOptions;


    /**
     * Transcription Configuration.
     */
    private TranscriptionOptions transcriptionOptions;

    /**
     * Custom Context
     */
    private final CustomCallingContext customContext;

    /**
     * Constructor
     * @param callInvite Call invitee information.
     * @param callbackUrl The call back URI.
     */
    public CreateCallOptions(CallInvite callInvite, String callbackUrl) {
        this.callInvite = callInvite;
        this.callbackUrl = callbackUrl;
        this.customContext = new CustomCallingContext(new HashMap<String, String>(), new HashMap<String, String>());
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
     * Get the Transcription configuration.
     *
     * @return the transcriptionConfiguration.
     */
    public TranscriptionOptions getTranscriptionConfiguration() {
        return transcriptionOptions;
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
     * Set the transcription configuration.
     *
     * @param transcriptionOptions The transcription configuration.
     * @return the CreateCallOptions object itself.
     */
    public CreateCallOptions setTranscriptionConfiguration(TranscriptionOptions transcriptionOptions) {
        this.transcriptionOptions = transcriptionOptions;
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
     * Get the CallIntelligenceOptions property: AI options for the call such as cognitiveServicesEndpoint
     *
     * @return the callIntelligenceOptions value.
     */
    public CallIntelligenceOptions getCallIntelligenceOptions() {
        return this.callIntelligenceOptions;
    }

    /**
     * Set the CallIntelligenceOptions property: AI options for the call such as cognitiveServicesEndpoint
     *
     * @param callIntelligenceOptions the cognitiveServicesEndpoint value to set.
     * @return the CreateCallOptions object itself.
     */
    public CreateCallOptions setCallIntelligenceOptions(CallIntelligenceOptions callIntelligenceOptions) {
        this.callIntelligenceOptions = callIntelligenceOptions;
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
     *  get custom context
     * @return custom context
     */
    public CustomCallingContext getCustomContext() {
        return customContext;
    }
}
