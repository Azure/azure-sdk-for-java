// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver.implementation.converters;

import com.azure.communication.callingserver.models.CallConnectionProperties;
import com.azure.communication.callingserver.implementation.models.CallConnectionPropertiesInternal;
import com.azure.communication.common.PhoneNumberIdentifier;

/**
 * A converter for {@link CallConnectionProperties}
 */
public final class CallConnectionPropertiesConverter {

    /**
     * Converts to {@link CallConnectionProperties}.
     */
    public static CallConnectionProperties convert(CallConnectionPropertiesInternal callConnectionPropertiesInternal) {

        if (callConnectionPropertiesInternal == null) {
            return null;
        }

        PhoneNumberIdentifier alternateCallerId = null;
        if (callConnectionPropertiesInternal.getAlternateCallerId() != null) {
            alternateCallerId = new PhoneNumberIdentifier(callConnectionPropertiesInternal.getAlternateCallerId().getValue());
        }

        return new CallConnectionProperties()
            .setCallLegId(callConnectionPropertiesInternal.getCallLegId())
            .setSource(CommunicationIdentifierConverter.convert(callConnectionPropertiesInternal.getSource()))
            .setAlternateCallerId(alternateCallerId)
            .setTarget(CommunicationIdentifierConverter.convert(callConnectionPropertiesInternal.getTarget()))
            .setCallConnectionState(callConnectionPropertiesInternal.getCallConnectionState())
            .setSubject(callConnectionPropertiesInternal.getSubject())
            .setCallbackUri(callConnectionPropertiesInternal.getCallbackUri());
    }

    private CallConnectionPropertiesConverter() {
    }
}
