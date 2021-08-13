// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver.implementation.converters;

import com.azure.communication.callingserver.implementation.models.CallConnectionPropertiesInternal;
import com.azure.communication.callingserver.implementation.models.CommunicationIdentifierModel;
import com.azure.communication.callingserver.implementation.models.PhoneNumberIdentifierModel;
import com.azure.communication.callingserver.models.CallConnectionProperties;

import com.azure.communication.common.CommunicationIdentifier;
import com.azure.communication.common.PhoneNumberIdentifier;

import java.util.stream.Collectors;

/**
 * A converter for {@link CallConnectionProperties}
 */
public final class CallConnectionPropertiesConverter {

    /**
     * Converts to {@link CallConnectionProperties}.
     */
    public static CallConnectionProperties convert(
        CallConnectionPropertiesInternal callConnectionPropertiesInternal) {
            
        if (callConnectionPropertiesInternal == null) {
            return null;
        }

        PhoneNumberIdentifier alternateCallerId = null;
        if (callConnectionPropertiesInternal.getAlternateCallerId() != null) {
            alternateCallerId = new PhoneNumberIdentifier(callConnectionPropertiesInternal.getAlternateCallerId().getValue());
        }

        return new CallConnectionProperties()
            .setCallConnectionId(callConnectionPropertiesInternal.getCallConnectionId())
            .setSource(CommunicationIdentifierConverter.convert(callConnectionPropertiesInternal.getSource()))
            .setAlternateCallerId(alternateCallerId)
            .setTargets(callConnectionPropertiesInternal.getTargets()
                        .stream()
                        .map(CommunicationIdentifierConverter::convert)
                        .collect(Collectors.toList()))
            .setCallConnectionState(callConnectionPropertiesInternal.getCallConnectionState())
            .setSubject(callConnectionPropertiesInternal.getSubject())
            .setCallbackUri(callConnectionPropertiesInternal.getCallbackUri())
            .setRequestedMediaTypes(callConnectionPropertiesInternal.getRequestedMediaTypes())
            .setRequestedCallEvents(callConnectionPropertiesInternal.getRequestedCallEvents())
            .setServerCallId(callConnectionPropertiesInternal.getServerCallId());
    }

    private CallConnectionPropertiesConverter() {
    }
}