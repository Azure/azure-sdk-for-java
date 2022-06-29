// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver;

import com.azure.communication.callingserver.implementation.converters.CommunicationIdentifierConverter;
import com.azure.communication.callingserver.implementation.converters.PhoneNumberIdentifierConverter;
import com.azure.communication.callingserver.implementation.models.CallConnectionPropertiesDto;
import com.azure.communication.callingserver.models.CallConnectionState;
import com.azure.communication.common.PhoneNumberIdentifier;
import com.azure.communication.common.CommunicationIdentifier;
import com.azure.core.annotation.Immutable;

import java.util.List;

/**
 * Asynchronous client that supports call connection operations.
 */
@Immutable
public final class CallConnection {
    private final String callConnectionId;
    private final CommunicationIdentifier source;
    private final PhoneNumberIdentifier alternateCallerId;
    private final List<CommunicationIdentifier> targets;
    private final CallConnectionState callConnectionState;
    private final String subject;
    private final String callbackUri;

    CallConnection(CallConnectionPropertiesDto callConnectionPropertiesDto) {
        this.callConnectionId = callConnectionPropertiesDto.getCallConnectionId();
        this.source = CommunicationIdentifierConverter.convert(callConnectionPropertiesDto.getSource());
        this.alternateCallerId = PhoneNumberIdentifierConverter.convert(callConnectionPropertiesDto.getAlternateCallerId());
        this.targets = null;
        callConnectionPropertiesDto.getTargets().forEach(target -> this.targets.add(CommunicationIdentifierConverter.convert(target)));
        this.callConnectionState = CallConnectionState.fromString(callConnectionPropertiesDto.getCallConnectionState().toString());
        this.subject = callConnectionPropertiesDto.getSubject();
        this.callbackUri = callConnectionPropertiesDto.getCallbackUri();
    }

    /**
     * Get the source property.
     *
     * @return source value.
     */
    public CommunicationIdentifier getSource() {
        return source;
    }

    /**
     * Get the targets property.
     *
     * @return list of targets
     */
    public List<CommunicationIdentifier> getTargets() {
        return targets;
    }

    /**
     * Get the alternateCallerId property.
     *
     * @return alternateCallerId value.
     */
    public PhoneNumberIdentifier getAlternateCallerId() {
        return alternateCallerId;
    }

    /**
     * Get the callConnectionState property.
     *
     * @return callConnectionState value.
     */
    public CallConnectionState getCallConnectionState() {
        return callConnectionState;
    }

    /**
     * Get the subject property.
     *
     * @return subject value.
     */
    public String getSubject() {
        return subject;
    }

    /**
     * Get the callbackUri property.
     *
     * @return callbackUri value.
     */
    public String getCallbackUri() {
        return callbackUri;
    }

    /**
     * Get the callConnectionId property, which is the call connection id.
     *
     * @return callConnectionId value.
     */
    public String getCallConnectionId() {
        return callConnectionId;
    }

}
