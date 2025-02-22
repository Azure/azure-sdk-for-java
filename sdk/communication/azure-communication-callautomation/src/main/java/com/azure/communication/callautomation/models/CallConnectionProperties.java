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
    private final MediaStreamingSubscription mediaStreamingSubscription;
    private final TranscriptionSubscription transcriptionSubscription;
    private final CommunicationUserIdentifier answeredBy;
    private final String correlationId;
    private final PhoneNumberIdentifier answeredFor;

    static {
        CallConnectionPropertiesConstructorProxy
            .setAccessor(new CallConnectionPropertiesConstructorProxy.CallConnectionPropertiesConstructorAccessor() {
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
        this.mediaStreamingSubscription = null;
        this.transcriptionSubscription = null;
        this.answeredBy = null;
        this.correlationId = null;
        this.answeredFor = null;
    }

    /**
     * Package-private constructor of the class, used internally.
     *
     * @param callConnectionPropertiesInternal The internal response of callConnectionProperties
     */
    CallConnectionProperties(CallConnectionPropertiesInternal callConnectionPropertiesInternal) {
        this.callConnectionId = callConnectionPropertiesInternal.getCallConnectionId();
        this.source = CommunicationIdentifierConverter.convert(callConnectionPropertiesInternal.getSource());
        this.sourceCallerIdNumber
            = PhoneNumberIdentifierConverter.convert(callConnectionPropertiesInternal.getSourceCallerIdNumber());
        this.sourceDisplayName = callConnectionPropertiesInternal.getSourceDisplayName();
        this.serverCallId = callConnectionPropertiesInternal.getServerCallId();
        this.targetParticipants = callConnectionPropertiesInternal.getTargets()
            .stream()
            .map(CommunicationIdentifierConverter::convert)
            .collect(Collectors.toList());
        this.callConnectionState
            = CallConnectionState.fromString(callConnectionPropertiesInternal.getCallConnectionState().toString());
        this.callbackUrl = callConnectionPropertiesInternal.getCallbackUri();
        this.mediaStreamingSubscription = callConnectionPropertiesInternal.getMediaStreamingSubscription() != null
            ? new MediaStreamingSubscription(callConnectionPropertiesInternal.getMediaStreamingSubscription())
            : null;
        this.transcriptionSubscription = callConnectionPropertiesInternal.getTranscriptionSubscription() != null
            ? new TranscriptionSubscription(callConnectionPropertiesInternal.getTranscriptionSubscription())
            : null;
        this.answeredBy
            = CommunicationUserIdentifierConverter.convert(callConnectionPropertiesInternal.getAnsweredBy());
        this.correlationId = callConnectionPropertiesInternal.getCorrelationId();
        this.answeredFor = PhoneNumberIdentifierConverter.convert(callConnectionPropertiesInternal.getAnsweredFor());
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
     * @return source value.
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
     * Get the MediaStreamingSubscription property: SubscriptionId for media streaming.
     *
     * @return the MediaStreamingSubscription value.
     */
    public MediaStreamingSubscription getMediaStreamingSubscription() {
        return mediaStreamingSubscription;
    }

    /**
     * Get the TranscriptionSubscription property: SubscriptionId for transcription.
     *
     * @return the TranscriptionSubscription value.
     */
    public TranscriptionSubscription getTranscriptionSubscription() {
        return transcriptionSubscription;
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

    /**
     * Get the answeredFor property: Identity of the original Pstn target of an incoming Call. Only populated when the
     * original target is a Pstn number.
     * 
     * @return the answeredFor value.
     */
    public PhoneNumberIdentifier getAnsweredFor() {
        return answeredFor;
    }
}
