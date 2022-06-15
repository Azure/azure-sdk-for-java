// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver.implementation.converters;

import com.azure.communication.callingserver.implementation.models.CommunicationIdentifierModel;
import com.azure.communication.callingserver.models.GetCallResponse;
import com.azure.communication.callingserver.implementation.models.GetCallResponseInternal;
import com.azure.communication.common.CommunicationIdentifier;
import com.azure.communication.common.PhoneNumberIdentifier;

import java.util.List;

/**
 * A converter for {@link GetCallResponse}
 */
public final class GetCallResponseConverter {

    /**
     * Converts to {@link GetCallResponse}.
     */
    public static GetCallResponse convert(GetCallResponseInternal getCallResponse) {

        if (getCallResponse == null) {
            return null;
        }

        PhoneNumberIdentifier alternateCallerId = null;
        if (getCallResponse.getAlternateCallerId() != null) {
            alternateCallerId = new PhoneNumberIdentifier(getCallResponse.getAlternateCallerId().getValue());
        }

        List<CommunicationIdentifier> targets = null;
        if (getCallResponse.getTargets() != null) {

            for (CommunicationIdentifierModel target : getCallResponse.getTargets()) {
                targets.add(CommunicationIdentifierConverter.convert(target));
            }
        }

        return new GetCallResponse(getCallResponse.getCallConnectionId(),
            CommunicationIdentifierConverter.convert(getCallResponse.getSource()),
            alternateCallerId, targets, CallConnectionStateConverter.convert(getCallResponse.getCallConnectionState()),
            getCallResponse.getSubject(), getCallResponse.getCallbackUri());
    }

    private GetCallResponseConverter() {
    }
}
