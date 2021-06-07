// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver.models;

import com.azure.core.annotation.Fluent;
import com.fasterxml.jackson.annotation.JsonProperty;

/** The options for join call. */
@Fluent
public final class JoinCallOptions {
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
    private CallModality[] requestedModalities;

    /*
     * The requested call events to subscribe to.
     */
    @JsonProperty(value = "requestedCallEvents", required = true)
    private EventSubscriptionType[] requestedCallEvents;

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
     * @return the JoinCallOptions object itself.
     */
    public JoinCallOptions setSubject(String subject) {
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
     * @return the JoinCallOptions object itself.
     */
    public JoinCallOptions setCallbackUri(String callbackUri) {
        this.callbackUri = callbackUri;
        return this;
    }

    /**
     * Get the requestedModalities property: The requested modalities.
     *
     * @return the requestedModalities value.
     */
    public CallModality[] getRequestedModalities() {
        if (this.requestedModalities == null) {
            return new CallModality[0];
        }
        CallModality[] requestedModalities = new CallModality[this.requestedModalities.length];
        System.arraycopy(this.requestedModalities, 0, requestedModalities, 0, this.requestedModalities.length);
        return requestedModalities;
    }

    /**
     * Set the requestedModalities property: The requested modalities.
     *
     * @param requestedModalities the requestedModalities value to set.
     * @return the JoinCallOptions object itself.
     */
    public JoinCallOptions setRequestedModalities(CallModality[] requestedModalities) {
        this.requestedModalities = new CallModality[requestedModalities.length];
        System.arraycopy(requestedModalities, 0, this.requestedModalities, 0, requestedModalities.length);
        return this;
    }

    /**
     * Get the requestedCallEvents property: The requested call events to subscribe
     * to.
     *
     * @return the requestedCallEvents value.
     */
    public EventSubscriptionType[] getRequestedCallEvents() {
        if (this.requestedCallEvents == null) {
            return new EventSubscriptionType[0];
        }
        EventSubscriptionType[] requestedCallEvents = new EventSubscriptionType[this.requestedCallEvents.length];
        System.arraycopy(this.requestedCallEvents, 0, requestedCallEvents, 0, this.requestedCallEvents.length);
        return requestedCallEvents;
    }

    /**
     * Set the requestedCallEvents property: The requested call events to subscribe
     * to.
     *
     * @param requestedCallEvents the requestedCallEvents value to set.
     * @return the JoinCallOptions object itself.
     */
    public JoinCallOptions setRequestedCallEvents(EventSubscriptionType[] requestedCallEvents) {
        this.requestedCallEvents = new EventSubscriptionType[requestedCallEvents.length];
        System.arraycopy(requestedCallEvents, 0, this.requestedCallEvents, 0, requestedCallEvents.length);
        return this;
    }
}
