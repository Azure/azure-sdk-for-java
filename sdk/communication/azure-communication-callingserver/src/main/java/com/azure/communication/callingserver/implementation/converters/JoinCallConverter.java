// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver.implementation.converters;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.azure.communication.callingserver.models.CallModality;
import com.azure.communication.callingserver.models.EventSubscriptionType;
import com.azure.communication.callingserver.implementation.models.JoinCallRequest;
import com.azure.communication.callingserver.models.JoinCallOptions;
import com.azure.communication.common.CommunicationIdentifier;

/**
 * A converter between {@link JoinCallOptions} and
 * {@link JoinCallRequest}.
 */
public final class JoinCallConverter {
    /**
     * Maps from JoinCallOptions to JoinCallRequest.
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


        List<CallModality> requestedModalities = new ArrayList<>();
        Collections.addAll(requestedModalities, joinCallOptions.getRequestedMediaTypes());
        joinCallRequest.setRequestedMediaTypes(requestedModalities);


        List<EventSubscriptionType> requestedCallEvents = new ArrayList<>();
        Collections.addAll(requestedCallEvents, joinCallOptions.getRequestedCallEvents());
        joinCallRequest.setRequestedCallEvents(requestedCallEvents);

        return joinCallRequest;
    }

    private JoinCallConverter() {
    }
}

