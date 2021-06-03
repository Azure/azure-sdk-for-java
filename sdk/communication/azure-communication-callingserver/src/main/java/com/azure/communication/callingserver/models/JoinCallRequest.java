// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver.models;

import com.azure.communication.callingserver.implementation.models.CallModality;
import com.azure.communication.callingserver.implementation.models.EventSubscriptionType;
import com.azure.communication.common.CommunicationIdentifier;
import com.azure.core.annotation.Fluent;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/** The request payload for join call. */
@Fluent
public final class JoinCallRequest {
    /*
     * The source of the call.
     */
    @JsonProperty(value = "source", required = true)
    private CommunicationIdentifier source;

    /*
     * The subject.
     */
    @JsonProperty(value = "subject")
    private String subject;

    /*
     * The callback URI.
     */
    @JsonProperty(value = "callbackUri", required = true)
    private String callbackUri;

    /*
     * The requested modalities.
     */
    @JsonProperty(value = "requestedModalities", required = true)
    private List<CallModality> requestedModalities;

    /*
     * The requested call events to subscribe to.
     */
    @JsonProperty(value = "requestedCallEvents", required = true)
    private List<EventSubscriptionType> requestedCallEvents;

    /**
     * Get the source property: The source of the call.
     *
     * @return the source value.
     */
    public CommunicationIdentifier getSource() {
        return this.source;
    }

    /**
     * Set the source property: The source of the call.
     *
     * @param source the source value to set.
     * @return the JoinCallRequest object itself.
     */
    public JoinCallRequest setSource(CommunicationIdentifier source) {
        this.source = source;
        return this;
    }

    /**
     * Get the subject property: The subject.
     *
     * @return the subject value.
     */
    public String getSubject() {
        return this.subject;
    }

    /**
     * Set the subject property: The subject.
     *
     * @param subject the subject value to set.
     * @return the JoinCallRequest object itself.
     */
    public JoinCallRequest setSubject(String subject) {
        this.subject = subject;
        return this;
    }

    /**
     * Get the callbackUri property: The callback URI.
     *
     * @return the callbackUri value.
     */
    public String getCallbackUri() {
        return this.callbackUri;
    }

    /**
     * Set the callbackUri property: The callback URI.
     *
     * @param callbackUri the callbackUri value to set.
     * @return the JoinCallRequest object itself.
     */
    public JoinCallRequest setCallbackUri(String callbackUri) {
        this.callbackUri = callbackUri;
        return this;
    }

    /**
     * Get the requestedModalities property: The requested modalities.
     *
     * @return the requestedModalities value.
     */
    public List<CallModality> getRequestedModalities() {
        return this.requestedModalities;
    }

    /**
     * Set the requestedModalities property: The requested modalities.
     *
     * @param requestedModalities the requestedModalities value to set.
     * @return the JoinCallRequest object itself.
     */
    public JoinCallRequest setRequestedModalities(List<CallModality> requestedModalities) {
        this.requestedModalities = requestedModalities;
        return this;
    }

    /**
     * Get the requestedCallEvents property: The requested call events to subscribe
     * to.
     *
     * @return the requestedCallEvents value.
     */
    public List<EventSubscriptionType> getRequestedCallEvents() {
        return this.requestedCallEvents;
    }

    /**
     * Set the requestedCallEvents property: The requested call events to subscribe
     * to.
     *
     * @param requestedCallEvents the requestedCallEvents value to set.
     * @return the JoinCallRequest object itself.
     */
    public JoinCallRequest setRequestedCallEvents(List<EventSubscriptionType> requestedCallEvents) {
        this.requestedCallEvents = requestedCallEvents;
        return this;
    }
}
