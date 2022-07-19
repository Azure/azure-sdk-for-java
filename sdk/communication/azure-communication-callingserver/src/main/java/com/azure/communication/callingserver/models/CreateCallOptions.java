// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver.models;

import com.azure.communication.common.CommunicationIdentifier;
import com.azure.core.annotation.Fluent;

import java.net.URI;
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
    private final URI callbackUri;

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
     * Constructor
     *
     * @param source The source property.
     * @param targets The targets of the call.
     * @param callbackUri The call back URI.
     */
    public CreateCallOptions(CommunicationIdentifier source, List<CommunicationIdentifier> targets, URI callbackUri) {
        this.source = source;
        this.targets = targets;
        this.callbackUri = callbackUri;
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
    public URI getCallbackUri() {
        return callbackUri;
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
}
