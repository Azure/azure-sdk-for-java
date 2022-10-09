// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.models;

import com.azure.communication.common.CommunicationIdentifier;
import com.azure.core.annotation.Fluent;

import java.util.List;

/**
 * The options for creating a call.
 */
@Fluent
public class CreateCallOptions {
    /**
     * The source property.
     */
    private final CommunicationIdentifier source;

    /**
     * The targets of the call.
     */
    private final List<CommunicationIdentifier> targets;

    /**
     * The call back URI.
     */
    private final String callbackUrl;

    /**
     * The source caller Id that's shown to the PSTN participant being invited.
     * Required only when inviting a PSTN participant.
     */
    private String sourceCallerId;

    /**
     * The subject
     */
    private String subject;

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
    public CreateCallOptions(CommunicationIdentifier source, List<CommunicationIdentifier> targets, String callbackUrl) {
        this.source = source;
        this.targets = targets;
        this.callbackUrl = callbackUrl;
    }

    /**
     * Get the source.
     *
     * @return the source value.
     */
    public CommunicationIdentifier getSource() {
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
     * Get the subject.
     *
     * @return the subject value.
     */
    public String getSubject() {
        return subject;
    }

    /**
     * Get the source caller Id that's shown to the PSTN participant being invited.
     *
     * @return the sourceCallerId value.
     */
    public String getSourceCallerId() {
        return sourceCallerId;
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
     * Set the subject.
     *
     * @param subject the subject.
     * @return the CreateCallOptions object itself.
     */
    public CreateCallOptions setSubject(String subject) {
        this.subject = subject;
        return this;
    }

    /**
     * Set the sourceCallerId.
     *
     * @param sourceCallerId The source caller Id that's shown to the PSTN participant being invited.
     *                       Required only when inviting a PSTN participant.
     * @return the CreateCallOptions object itself.
     */
    public CreateCallOptions setSourceCallerId(String sourceCallerId) {
        this.sourceCallerId = sourceCallerId;
        return this;
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
     * Set the repeatability headers
     *
     * @param repeatabilityHeaders The repeatability headers configuration.
     * @return the CreateCallOptions object itself.
     */
    public CreateCallOptions setRepeatabilityHeaders(RepeatabilityHeaders repeatabilityHeaders) {
        this.repeatabilityHeaders = repeatabilityHeaders;
        return this;
    }
}
