// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver.models;

import com.azure.communication.common.CommunicationIdentifier;
import com.azure.communication.common.PhoneNumberIdentifier;
import com.azure.core.annotation.Fluent;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/** The invite participants request. */
@Fluent
public final class InviteParticipantsRequest {
    /*
     * The alternate identity of source participant.
     */
    @JsonProperty(value = "alternateCallerId")
    private PhoneNumberIdentifier alternateCallerId;

    /*
     * The list of participants to be added to the call.
     */
    @JsonProperty(value = "participants", required = true)
    private List<CommunicationIdentifier> participants;

    /*
     * The operation context.
     */
    @JsonProperty(value = "operationContext")
    private String operationContext;

    /*
     * The callback URI.
     */
    @JsonProperty(value = "callbackUri")
    private String callbackUri;

    /**
     * Get the alternateCallerId property: The alternate identity of source
     * participant.
     *
     * @return the alternateCallerId value.
     */
    public PhoneNumberIdentifier getAlternateCallerId() {
        return this.alternateCallerId;
    }

    /**
     * Set the alternateCallerId property: The alternate identity of source
     * participant.
     *
     * @param alternateCallerId the alternateCallerId value to set.
     * @return the InviteParticipantsRequest object itself.
     */
    public InviteParticipantsRequest setAlternateCallerId(PhoneNumberIdentifier alternateCallerId) {
        this.alternateCallerId = alternateCallerId;
        return this;
    }

    /**
     * Get the participants property: The list of participants to be added to the
     * call.
     *
     * @return the participants value.
     */
    public List<CommunicationIdentifier> getParticipants() {
        return this.participants;
    }

    /**
     * Set the participants property: The list of participants to be added to the
     * call.
     *
     * @param participants the participants value to set.
     * @return the InviteParticipantsRequest object itself.
     */
    public InviteParticipantsRequest setParticipants(List<CommunicationIdentifier> participants) {
        this.participants = participants;
        return this;
    }

    /**
     * Get the operationContext property: The operation context.
     *
     * @return the operationContext value.
     */
    public String getOperationContext() {
        return this.operationContext;
    }

    /**
     * Set the operationContext property: The operation context.
     *
     * @param operationContext the operationContext value to set.
     * @return the InviteParticipantsRequest object itself.
     */
    public InviteParticipantsRequest setOperationContext(String operationContext) {
        this.operationContext = operationContext;
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
     * @return the InviteParticipantsRequest object itself.
     */
    public InviteParticipantsRequest setCallbackUri(String callbackUri) {
        this.callbackUri = callbackUri;
        return this;
    }
}
