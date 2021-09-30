// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver.models;

import com.azure.communication.common.CommunicationIdentifier;
import com.azure.communication.common.PhoneNumberIdentifier;

import com.azure.communication.callingserver.models.CallConnectionState;
import com.azure.communication.callingserver.models.EventSubscriptionType;
import com.azure.communication.callingserver.models.MediaType;
import com.azure.core.annotation.Immutable;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/** The CallConnectionProperties model. */
@Immutable
public final class CallConnectionProperties {
    /*
     * The call connection id.
     */
    @JsonProperty(value = "callConnectionId")
    private String callConnectionId;

    /*
     * The source of the call.
     */
    @JsonProperty(value = "source")
    private CommunicationIdentifier source;

    /*
     * The alternate identity of the source of the call if dialing out to a
     * pstn number
     */
    @JsonProperty(value = "alternateCallerId")
    private PhoneNumberIdentifier alternateCallerId;

    /*
     * The targets of the call.
     */
    @JsonProperty(value = "targets")
    private List<CommunicationIdentifier> targets;

    /*
     * The state of the call connection.
     */
    @JsonProperty(value = "callConnectionState")
    private CallConnectionState callConnectionState;

    /*
     * The subject.
     */
    @JsonProperty(value = "subject")
    private String subject;

    /*
     * The callback URI.
     */
    @JsonProperty(value = "callbackUri")
    private String callbackUri;

    /*
     * The requested modalities.
     */
    @JsonProperty(value = "requestedMediaTypes")
    private List<MediaType> requestedMediaTypes;

    /*
     * The requested call events to subscribe to.
     */
    @JsonProperty(value = "requestedCallEvents")
    private List<EventSubscriptionType> requestedCallEvents;

    /*
     * The call locator.
     */
    @JsonProperty(value = "callLocator")
    private CallLocator callLocator;

    /**
     * Get the callConnectionId property: The call connection id.
     *
     * @return the callConnectionId value.
     */
    public String getCallConnectionId() {
        return this.callConnectionId;
    }

    /**
     * Set the callConnectionId property: The call connection id.
     *
     * @param callConnectionId the callConnectionId value to set.
     * @return the CallConnectionProperties object itself.
     */
    public CallConnectionProperties setCallConnectionId(String callConnectionId) {
        this.callConnectionId = callConnectionId;
        return this;
    }

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
     * @return the CallConnectionProperties object itself.
     */
    public CallConnectionProperties setSource(CommunicationIdentifier source) {
        this.source = source;
        return this;
    }

    /**
     * Get the alternateCallerId property: The alternate identity of the source of the call if dialing out to a pstn
     * number.
     *
     * @return the alternateCallerId value.
     */
    public PhoneNumberIdentifier getAlternateCallerId() {
        return this.alternateCallerId;
    }

    /**
     * Set the alternateCallerId property: The alternate identity of the source of the call if dialing out to a pstn
     * number.
     *
     * @param alternateCallerId the alternateCallerId value to set.
     * @return the CallConnectionProperties object itself.
     */
    public CallConnectionProperties setAlternateCallerId(PhoneNumberIdentifier alternateCallerId) {
        this.alternateCallerId = alternateCallerId;
        return this;
    }

    /**
     * Get the targets property: The targets of the call.
     *
     * @return the targets value.
     */
    public List<CommunicationIdentifier> getTargets() {
        return this.targets;
    }

    /**
     * Set the targets property: The targets of the call.
     *
     * @param targets the targets value to set.
     * @return the CallConnectionProperties object itself.
     */
    public CallConnectionProperties setTargets(List<CommunicationIdentifier> targets) {
        this.targets = targets;
        return this;
    }

    /**
     * Get the callConnectionState property: The state of the call connection.
     *
     * @return the callConnectionState value.
     */
    public CallConnectionState getCallConnectionState() {
        return this.callConnectionState;
    }

    /**
     * Set the callConnectionState property: The state of the call connection.
     *
     * @param callConnectionState the callConnectionState value to set.
     * @return the CallConnectionProperties object itself.
     */
    public CallConnectionProperties setCallConnectionState(CallConnectionState callConnectionState) {
        this.callConnectionState = callConnectionState;
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
     * @return the CallConnectionProperties object itself.
     */
    public CallConnectionProperties setSubject(String subject) {
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
     * @return the CallConnectionProperties object itself.
     */
    public CallConnectionProperties setCallbackUri(String callbackUri) {
        this.callbackUri = callbackUri;
        return this;
    }

    /**
     * Get the requestedMediaTypes property: The requested modalities.
     *
     * @return the requestedMediaTypes value.
     */
    public List<MediaType> getRequestedMediaTypes() {
        return this.requestedMediaTypes;
    }

    /**
     * Set the requestedMediaTypes property: The requested modalities.
     *
     * @param requestedMediaTypes the requestedMediaTypes value to set.
     * @return the CallConnectionProperties object itself.
     */
    public CallConnectionProperties setRequestedMediaTypes(List<MediaType> requestedMediaTypes) {
        this.requestedMediaTypes = requestedMediaTypes;
        return this;
    }

    /**
     * Get the requestedCallEvents property: The requested call events to subscribe to.
     *
     * @return the requestedCallEvents value.
     */
    public List<EventSubscriptionType> getRequestedCallEvents() {
        return this.requestedCallEvents;
    }

    /**
     * Set the requestedCallEvents property: The requested call events to subscribe to.
     *
     * @param requestedCallEvents the requestedCallEvents value to set.
     * @return the CallConnectionProperties object itself.
     */
    public CallConnectionProperties setRequestedCallEvents(List<EventSubscriptionType> requestedCallEvents) {
        this.requestedCallEvents = requestedCallEvents;
        return this;
    }

    /**
     * Get the callLocator property: The call locator.
     *
     * @return the callLocator value.
     */
    public CallLocator getCallLocator() {
        return this.callLocator;
    }

    /**
     * Set the callLocator property: The call locator.
     *
     * @param callLocator the callLocator value to set.
     * @return the CallConnectionProperties object itself.
     */
    public CallConnectionProperties setCallLocator(CallLocator callLocator) {
        this.callLocator = callLocator;
        return this;
    }
}
