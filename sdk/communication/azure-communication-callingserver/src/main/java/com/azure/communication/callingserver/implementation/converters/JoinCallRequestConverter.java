// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver.implementation.converters;

import java.util.ArrayList;
import java.util.List;

import com.azure.communication.callingserver.implementation.models.CallModality;
import com.azure.communication.callingserver.implementation.models.EventSubscriptionType;
import com.azure.communication.callingserver.implementation.models.JoinCallRequestInternal;

/**
 * A converter between {@link com.azure.communication.callingserver.models.JoinCallRequest} and
 * {@link JoinCallRequestInternal}.
 */
public final class JoinCallRequestConverter {
    /**
     * Maps from {com.azure.communication.callingserver.models.JoinCallRequest} to {@link JoinCallRequestInternal}.
     */
    public static JoinCallRequestInternal convert(com.azure.communication.callingserver.models.JoinCallRequest obj) {
        if (obj == null) {
            return null;
        }

        List<CallModality> requestedModalities = new ArrayList<>();
        for (CallModality callModality : obj.getRequestedModalities()) {
            requestedModalities.add(callModality);
        }

        List<EventSubscriptionType> requestedCallEvents = new ArrayList<>();
        for (EventSubscriptionType eventSubscription : obj.getRequestedCallEvents()){
            requestedCallEvents.add(eventSubscription);
        }

        JoinCallRequestInternal joinCallRequestInternal = new JoinCallRequestInternal()
            .setSource(CommunicationIdentifierConverter.convert(obj.getSource()))
            .setSubject(obj.getSubject())
            .setCallbackUri(obj.getCallbackUri())
            .setRequestedModalities(requestedModalities)
            .setRequestedCallEvents(requestedCallEvents);
            
        return joinCallRequestInternal;
    }

    private JoinCallRequestConverter() {
    }
}