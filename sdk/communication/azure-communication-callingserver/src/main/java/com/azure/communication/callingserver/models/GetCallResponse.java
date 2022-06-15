// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver.models;

import com.azure.communication.common.CommunicationIdentifier;
import com.azure.communication.common.PhoneNumberIdentifier;
import com.azure.core.annotation.Immutable;

import java.util.List;

/** The GetCallResponse model. */
@Immutable
public final class GetCallResponse {
    /*
     * The call connection id.
     */
    private final String callConnectionId;

    /*
     * The source of the call.
     */
    private final CommunicationIdentifier source;

    /*
     * The alternate identity of the source of the call if dialing out to a
     * pstn number
     */
    private final PhoneNumberIdentifier alternateCallerId;

    /*
     * The target of the call.
     */
    private final List<CommunicationIdentifier> targets;

    /*
     * The state of the call connection.
     */
    private final CallConnectionState callConnectionState;

    /*
     * The subject.
     */
    private final String subject;

    /*
     * The callback URI.
     */
    private final String callbackUri;

    /**
     * Constructor of the class
     *
     * @param callConnectionId The callConnectionId
     * @param source The source
     * @param alternateCallerId The alternateCallerId
     * @param targets The targets
     * @param callConnectionState The callConnectionState
     * @param subject The subject
     * @param callbackUri The callbackUri
     */
    public GetCallResponse(String callConnectionId, CommunicationIdentifier source,
                                         PhoneNumberIdentifier alternateCallerId, List<CommunicationIdentifier> targets,
                                         CallConnectionState callConnectionState, String subject, String callbackUri) {
        this.callConnectionId = callConnectionId;
        this.source = source;
        this.alternateCallerId = alternateCallerId;
        this.targets = targets;
        this.callConnectionState = callConnectionState;
        this.subject = subject;
        this.callbackUri = callbackUri;
    }

    /**
     * Get the callLegId property: The call connection id.
     *
     * @return the callLegId value.
     */
    public String getCallConnectionId() {
        return this.callConnectionId;
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
     * Get the alternateCallerId property: The alternate identity of the source of the call if dialing out to a pstn
     * number.
     *
     * @return the alternateCallerId value.
     */
    public PhoneNumberIdentifier getAlternateCallerId() {
        return this.alternateCallerId;
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
     * Get the callConnectionState property: The state of the call connection.
     *
     * @return the callConnectionState value.
     */
    public CallConnectionState getCallConnectionState() {
        return this.callConnectionState;
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
     * Get the callbackUri property: The callback URI.
     *
     * @return the callbackUri value.
     */
    public String getCallbackUri() {
        return this.callbackUri;
    }
}
