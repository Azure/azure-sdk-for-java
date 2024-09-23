// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.models;

import com.azure.communication.callautomation.implementation.accesshelpers.CallConnectionPropertiesConstructorProxy;
import com.azure.communication.callautomation.implementation.converters.CommunicationIdentifierConverter;
import com.azure.communication.callautomation.implementation.converters.CommunicationUserIdentifierConverter;
import com.azure.communication.callautomation.implementation.converters.PhoneNumberIdentifierConverter;
import com.azure.communication.callautomation.implementation.models.CallConnectionPropertiesInternal;
import com.azure.communication.common.CommunicationIdentifier;
import com.azure.communication.common.CommunicationUserIdentifier;
import com.azure.communication.common.PhoneNumberIdentifier;
import com.azure.core.annotation.Immutable;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Asynchronous client that supports call connection operations.
 */
@Immutable
public final class CallConnectionProperties {
    private final String callConnectionId;
    private final String serverCallId;
    private final CommunicationIdentifier source;
    private final PhoneNumberIdentifier sourceCallerIdNumber;
    private final String sourceDisplayName;
    private final List<CommunicationIdentifier> targetParticipants;
    private final CallConnectionState callConnectionState;
    private final String callbackUrl;
    private final CommunicationUserIdentifier answeredBy;
    private final String correlationId;

    static {
        CallConnectionPropertiesConstructorProxy.setAccessor(
            new CallConnectionPropertiesConstructorProxy.CallConnectionPropertiesConstructorAccessor() {
                @Override
                public CallConnectionProperties create(CallConnectionPropertiesInternal internalHeaders) {
                    return new CallConnectionProperties(internalHeaders);
                }
            });
    }

    /**
     * Public constructor.
     *
     */
    public CallConnectionProperties() {
        this.callConnectionId = null;
        this.source = null;
        this.sourceCallerIdNumber = null;
        this.sourceDisplayName = null;
        this.serverCallId = null;
        this.targetParticipants = null;
        this.callConnectionState = null;
        this.callbackUrl = null;
        this.answeredBy = null;
        this.correlationId = null;
    }

    /**
     * Package-private constructor of the class, used internally.
     *
     * @param callConnectionPropertiesInternal The internal response of callConnectionProperties
     */
    CallConnectionProperties(CallConnectionPropertiesInternal callConnectionPropertiesInternal) {
        this.callConnectionId = callConnectionPropertiesInternal.getCallConnectionId();
        this.source = CommunicationIdentifierConverter.convert(callConnectionPropertiesInternal.getSource());
        this.sourceCallerIdNumber = PhoneNumberIdentifierConverter.convert(callConnectionPropertiesInternal.getSourceCallerIdNumber());
        this.sourceDisplayName = callConnectionPropertiesInternal.getSourceDisplayName();
        this.serverCallId = callConnectionPropertiesInternal.getServerCallId();
        this.targetParticipants = callConnectionPropertiesInternal.getTargets().stream().map(CommunicationIdentifierConverter::convert).collect(Collectors.toList());
        this.callConnectionState = CallConnectionState.fromString(callConnectionPropertiesInternal.getCallConnectionState().toString());
        this.callbackUrl = callConnectionPropertiesInternal.getCallbackUri();
        this.answeredBy = CommunicationUserIdentifierConverter.convert(callConnectionPropertiesInternal.getAnsweredBy());
        this.correlationId = callConnectionPropertiesInternal.getCorrelationId();
    }

    /**
     * Get the targets property.
     *
     * @return list of targets
     */
    public List<CommunicationIdentifier> getTargetParticipants() {
        return targetParticipants;
    }

    /**
     * Get the source identity.
     *
     * @return sourceIdentity value.
     */
    public CommunicationIdentifier getSource() {
        return source;
    }

    /**
     * Get the source caller id number for PSTN.
     *
     * @return sourceCallerIdNumber value.
     */
    public PhoneNumberIdentifier getSourceCallerIdNumber() {
        return sourceCallerIdNumber;
    }

    /**
     * Get the display name.
     *
     * @return sourceDisplayName value.
     */
    public String getSourceDisplayName() {
        return sourceDisplayName;
    }

    /**
     * Get the server call Id.
     *
     * @return serverCallId value.
     */
    public String getServerCallId() {
        return serverCallId;
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
     * Get the callbackUri property.
     *
     * @return callbackUri value.
     */
    public String getCallbackUrl() {
        return callbackUrl;
    }

    /**
     * Get the callConnectionId property, which is the call connection id.
     *
     * @return callConnectionId value.
     */
    public String getCallConnectionId() {
        return callConnectionId;
    }

    /**
     *  Get identity that answered the call
     * @return identity that answered the call
     */
    public CommunicationUserIdentifier getAnsweredBy() {
        return answeredBy;
    }

    /**
     * Get correlationId for the call
     * @return correlationId for the call
     */
    public String getCorrelationId() {
        return correlationId;
    }
}
