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
     * The callback URI.
     */
    @JsonProperty(value = "callbackUri", required = true)
    private URI callbackUri;

    /*
     * The requested MediaTypes.
     */
    @JsonProperty(value = "requestedMediaTypes", required = true)
    private List<CallMediaType> requestedMediaTypes;

    /*
     * The requested call events to subscribe to.
     */
    @JsonProperty(value = "requestedCallEvents", required = true)
    private List<CallingEventSubscriptionType> requestedCallEvents;

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
    public List<CallMediaType> getRequestedMediaTypes() {
        return requestedMediaTypes;
    }

    /**
     * Set the requestedMediaTypes property: The requested MediaTypes.
     *
     * @param requestedMediaTypes the requestedModalities value to set.
     * @return the AnswerCallOptions object itself.
     */
    public AnswerCallOptions setRequestedMediaTypes(List<CallMediaType> requestedMediaTypes) {
        this.requestedMediaTypes = requestedMediaTypes;
        return this;
    }

    /**
     * Get the requestedCallEvents property: The requested call events to subscribe
     * to.
     *
     * @return the requestedCallEvents value.
     */
    public List<CallingEventSubscriptionType> getRequestedCallEvents() {
        return requestedCallEvents;
    }

    /**
     * Set the requestedCallEvents property: The requested call events to subscribe
     * to.
     *
     * @param requestedCallEvents the requestedCallEvents value to set.
     * @return the AnswerCallOptions object itself.
     */
    public AnswerCallOptions setRequestedCallEvents(List<CallingEventSubscriptionType> requestedCallEvents) {
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
        URI callbackUri,
        List<CallMediaType> requestedMediaTypes,
        List<CallingEventSubscriptionType> requestedCallEvents) {
        if (callbackUri == null) {
            throw new IllegalArgumentException("object callbackUri cannot be null");
        }
        if (requestedMediaTypes == null) {
            throw new IllegalArgumentException("object requestedMediaTypes cannot be null");
        }
        if (requestedCallEvents == null) {
            throw new IllegalArgumentException("object requestedCallEvents cannot be null");
        }
        this.callbackUri = callbackUri;
        this.requestedMediaTypes = requestedMediaTypes;
        this.requestedCallEvents = requestedCallEvents;
    }
}
