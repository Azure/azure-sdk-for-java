// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver.models;

import com.azure.core.annotation.Fluent;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.net.URI;
import java.util.List;

/** The options for answer call. */
@Fluent
public final class AnswerCallOptions {
    /*
     * The incoming call context.
     */
    @JsonProperty(value = "incomingCallContext")
    private String incomingCallContext;

    /*
     * The callback URI.
     */
    @JsonProperty(value = "callbackUri", required = true)
    private URI callbackUri;

    /*
     * The requested MediaTypes.
     */
    @JsonProperty(value = "requestedMediaTypes", required = true)
    private List<MediaType> requestedMediaTypes;

    /*
     * The requested call events to subscribe to.
     */
    @JsonProperty(value = "requestedCallEvents", required = true)
    private List<EventSubscriptionType> requestedCallEvents;

    /**
     * Get the incomingCallContext property: The incomingCallContext.
     *
     * @return the incomingCallContext value.
     */
    public String getIncomingCallContext() {
        return incomingCallContext;
    }

    /**
     * Set the incomingCallContext property: The incomingCallContext.
     *
     * @param incomingCallContext the incomingCallContext value to set.
     * @return the AnswerCallOptions object itself.
     */
    public AnswerCallOptions setIncomingCallContext(String incomingCallContext) {
        this.incomingCallContext = incomingCallContext;
        return this;
    }

    /**
     * Get the callbackUri property: The callback URI.
     *
     * @return the callbackUri value.
     */
    public URI getCallbackUri() {
        return callbackUri;
    }

    /**
     * Set the callbackUri property: The callback URI.
     *
     * @param callbackUri the callbackUri value to set.
     * @return the AnswerCallOptions object itself.
     */
    public AnswerCallOptions setCallbackUri(URI callbackUri) {
        this.callbackUri = callbackUri;
        return this;
    }

    /**
     * Get the requestedMediaTypes property: The requested MediaTypes.
     *
     * @return the requestedMediaTypes value.
     */
    public List<MediaType> getRequestedMediaTypes() {
        return requestedMediaTypes;
    }

    /**
     * Set the requestedMediaTypes property: The requested MediaTypes.
     *
     * @param requestedMediaTypes the requestedModalities value to set.
     * @return the AnswerCallOptions object itself.
     */
    public AnswerCallOptions setRequestedMediaTypes(List<MediaType> requestedMediaTypes) {
        this.requestedMediaTypes = requestedMediaTypes;
        return this;
    }

    /**
     * Get the requestedCallEvents property: The requested call events to subscribe
     * to.
     *
     * @return the requestedCallEvents value.
     */
    public List<EventSubscriptionType> getRequestedCallEvents() {
        return requestedCallEvents;
    }

    /**
     * Set the requestedCallEvents property: The requested call events to subscribe
     * to.
     *
     * @param requestedCallEvents the requestedCallEvents value to set.
     * @return the AnswerCallOptions object itself.
     */
    public AnswerCallOptions setRequestedCallEvents(List<EventSubscriptionType> requestedCallEvents) {
        this.requestedCallEvents = requestedCallEvents;
        return this;
    }

    /**
     * Initializes a new instance of AnswerCallOptions.
     *
     * @param callbackUri the callback URI.
     * @param requestedMediaTypes the requested media types.
     * @param requestedCallEvents the requested call events to subscribe to.
     * @throws IllegalArgumentException if any parameters are null.
     */
    public AnswerCallOptions(
        String incomingCallContext,
        URI callbackUri,
        List<MediaType> requestedMediaTypes,
        List<EventSubscriptionType> requestedCallEvents) {
        if (incomingCallContext == null) {
            throw new IllegalArgumentException("object incomingCallContext cannot be null");
        }
        if (callbackUri == null) {
        throw new IllegalArgumentException("object callbackUri cannot be null");
        }
        if (requestedMediaTypes == null) {
            throw new IllegalArgumentException("object requestedMediaTypes cannot be null");
        }
        if (requestedCallEvents == null) {
            throw new IllegalArgumentException("object requestedCallEvents cannot be null");
        }
        this.incomingCallContext = incomingCallContext;
        this.callbackUri = callbackUri;
        this.requestedMediaTypes = requestedMediaTypes;
        this.requestedCallEvents = requestedCallEvents;
    }
}
