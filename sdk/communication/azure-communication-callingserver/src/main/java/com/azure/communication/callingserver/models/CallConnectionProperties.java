// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver.models;

import com.azure.communication.common.CommunicationIdentifier;
import com.azure.communication.common.PhoneNumberIdentifier;
import com.azure.core.annotation.Immutable;
import com.fasterxml.jackson.annotation.JsonProperty;

/** The CallConnectionProperties model. */
@Immutable
public final class CallConnectionProperties {
    /*
     * The call connection id.
     */
    @JsonProperty(value = "callLegId")
    private String callLegId;

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
     * The target of the call.
     */
    @JsonProperty(value = "target")
    private CommunicationIdentifier target;

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

    /**
     * Get the callLegId property: The call connection id.
     *
     * @return the callLegId value.
     */
    public String getCallLegId() {
        return this.callLegId;
    }

    /**
     * Set the callLegId property: The call connection id.
     *
     * @param callLegId the callLegId value to set.
     * @return the CallConnectionProperties object itself.
     */
    public CallConnectionProperties setCallLegId(String callLegId) {
        this.callLegId = callLegId;
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
     * Get the target property: The target of the call.
     *
     * @return the targets value.
     */
    public CommunicationIdentifier getTarget() {
        return this.target;
    }

    /**
     * Set the target property: The target of the call.
     *
     * @param target set the target value.
     * @return the CallConnectionProperties object itself.
     */
    public CallConnectionProperties setTarget(CommunicationIdentifier target) {
        this.target = this.target;
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
}
