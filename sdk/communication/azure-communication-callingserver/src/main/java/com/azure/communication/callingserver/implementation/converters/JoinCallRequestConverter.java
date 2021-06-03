// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver.implementation.converters;

import java.util.ArrayList;
import java.util.List;

import com.azure.communication.callingserver.implementation.models.CallModality;
import com.azure.communication.callingserver.implementation.models.EventSubscriptionType;
import com.azure.communication.callingserver.implementation.models.JoinCallRequest;
import com.azure.communication.callingserver.models.JoinCallOptions;
import com.azure.communication.common.CommunicationIdentifier;

/**
 * A converter between {@link com.azure.communication.callingserver.models.JoinCallOptions} and
 * {@link JoinCallRequestInternal}.
 */
public final class JoinCallRequestConverter {
    /**
     * Maps from {com.azure.communication.callingserver.models.JoinCallRequest} to {@link JoinCallRequestInternal}.
     */
    public static JoinCallRequest convert(CommunicationIdentifier source, JoinCallOptions joinCallOptions) {
        if (source == null) {
            return null;
        }

        JoinCallRequest joinCallRequest = new JoinCallRequest()
            .setSource(CommunicationIdentifierConverter.convert(source));

        if (joinCallOptions == null) {
            return joinCallRequest;
        }

        joinCallRequest.setSubject(joinCallOptions.getSubject());
        joinCallRequest.setCallbackUri(joinCallOptions.getCallbackUri());

        if (joinCallOptions.getRequestedModalities() != null) {
            List<CallModality> requestedModalities = new ArrayList<>();
            for (CallModality callModality : joinCallOptions.getRequestedModalities()) {
                requestedModalities.add(callModality);
            }
            joinCallRequest.setRequestedModalities(requestedModalities);
        }

        if (joinCallOptions.getRequestedCallEvents() != null) {
            List<EventSubscriptionType> requestedCallEvents = new ArrayList<>();
            for (EventSubscriptionType eventSubscription : joinCallOptions.getRequestedCallEvents()) {
                requestedCallEvents.add(eventSubscription);
            }
            joinCallRequest.setRequestedCallEvents(requestedCallEvents);
        }
 
        return joinCallRequest;
    }

    private JoinCallRequestConverter() {
    }
}

