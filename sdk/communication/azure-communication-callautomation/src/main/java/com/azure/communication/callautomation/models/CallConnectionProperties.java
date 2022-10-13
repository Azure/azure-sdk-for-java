// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.models;

import com.azure.communication.callautomation.implementation.accesshelpers.CallConnectionPropertiesConstructorProxy;
import com.azure.communication.callautomation.implementation.converters.CallSourceConverter;
import com.azure.communication.callautomation.implementation.converters.CommunicationIdentifierConverter;
import com.azure.communication.callautomation.implementation.models.CallConnectionPropertiesInternal;
import com.azure.communication.common.CommunicationIdentifier;
import com.azure.core.annotation.Immutable;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Asynchronous client that supports call connection operations.
 */
@Immutable
public final class CallConnectionProperties {
    private final String callConnectionId;
    private final String serverCallId;
    private final CallSource source;
    private final List<CommunicationIdentifier> targets;
    private final CallConnectionState callConnectionState;
    private final String subject;
    private final URI callbackUri;
    private final String mediaSubscriptionId;

    static {
        CallConnectionPropertiesConstructorProxy.setAccessor(
            new CallConnectionPropertiesConstructorProxy.CallConnectionPropertiesConstructorAccessor() {
                @Override
                public CallConnectionProperties create(CallConnectionPropertiesInternal internalHeaders) throws URISyntaxException {
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
        this.serverCallId = null;
        this.targets = null;
        this.callConnectionState = null;
        this.subject = null;
        this.callbackUri = null;
        this.mediaSubscriptionId = null;
    }

    /**
     * Package-private constructor of the class, used internally.
     *
     * @param callConnectionPropertiesInternal The internal response of callConnectionProperties
     * @throws URISyntaxException exception of invalid URI.
     */
    CallConnectionProperties(CallConnectionPropertiesInternal callConnectionPropertiesInternal) throws URISyntaxException {
        this.callConnectionId = callConnectionPropertiesInternal.getCallConnectionId();
        this.source = CallSourceConverter.convert(callConnectionPropertiesInternal.getSource());
        this.serverCallId = callConnectionPropertiesInternal.getServerCallId();
        this.targets = callConnectionPropertiesInternal.getTargets().stream().map(CommunicationIdentifierConverter::convert).collect(Collectors.toList());
        this.callConnectionState = CallConnectionState.fromString(callConnectionPropertiesInternal.getCallConnectionState().toString());
        this.subject = callConnectionPropertiesInternal.getSubject();
        this.callbackUri = new URI(callConnectionPropertiesInternal.getCallbackUri());
        this.mediaSubscriptionId = callConnectionPropertiesInternal.getMediaSubscriptionId();
    }

    /**
     * Get the source property.
     *
     * @return source value.
     */
    public CallSource getSource() {
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
    public URI getCallbackUri() {
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

    /**
     * Get the mediaSubscriptionId property: SubscriptionId for media streaming.
     *
     * @return the mediaSubscriptionId value.
     */
    public String getMediaSubscriptionId() { 
        return mediaSubscriptionId;
    }
}
